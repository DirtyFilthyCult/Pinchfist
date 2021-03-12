package me.cronkhinator.pinchfist

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.cronkhinator.pinchfist.listener.GhostPingHandler
import me.cronkhinator.pinchfist.listener.MemberHandler
import me.cronkhinator.pinchfist.listener.CommandHandler
import me.cronkhinator.pinchfist.listener.ReactionRoleHandler
import me.cronkhinator.pinchfist.objects.CultMember
import me.cronkhinator.pinchfist.objects.ReactionRole
import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.objects.steam.LeaderboardEntry
import me.cronkhinator.pinchfist.util.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import java.io.File
import java.lang.IllegalStateException
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.fixedRateTimer

object Pinchfist {

    @JvmStatic lateinit var jda: JDA
    @JvmField var prefix = "!"
    @JvmField var chHName = "\uD83C\uDFC6hall-of-names\uD83C\uDFC6"
    @JvmField var chNName = "name-battle-results"

    lateinit var client: HttpClient
    lateinit var leaderboard: List<LeaderboardEntry> // lateinit should be safe here; exception window is 1 second on startup

    @JvmField var steamAuthMap = mutableMapOf<UUID, Long>()
    @JvmField val dateFormatter = DateTimeFormatter.ofPattern("hh:mm a 'UTC' | dd MMMM yyyy")!!
    var cultMembers = mutableListOf<CultMember>()
    var reactionRoles = mutableListOf<ReactionRole>()

    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting bot.")
        MembersUtil.deserializeData()
        jda = JDABuilder.createDefault(File("${Settings.filePath}/token.txt").readText())
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(CommandHandler, MemberHandler, GhostPingHandler, ReactionRoleHandler)
                .build()
        jda.presence.setStatus(OnlineStatus.ONLINE)
        jda.presence.activity = Activity.playing("Forts")

        client = HttpClient(Apache)

        fixedRateTimer(initialDelay = 1000L, period = 3600000L, daemon = true) {
            executeHourlyUpdate()
        }

        embeddedServer(Netty, applicationEngineEnvironment {
            module {
                module()
            }
            connector {
                port = 5555
                host = "127.0.0.1"
            }
        }) {
        }.start(true)
    }

    private fun Application.module() {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            post("/authPassthrough") {
                val tokenPair = call.receive<TokenPair>()
                call.respond(HttpStatusCode.Accepted)

                println("TokenPair received from Synergy, processing...")
                val id = steamAuthMap[UUID.fromString(tokenPair.authToken)]
                if(id == null) {
                    println("Token is invalid (probably expired). Ignoring.")
                    return@post
                }
                println("Processing Steam data for CultMember.")

                val cultMember = MembersUtil.getOrCreate(id)

                cultMember.steamID = tokenPair.steamID
                SteamUtil.updateSteamData(tokenPair.steamID)

                jda.getUserById(id)!!.openPrivateChannel().queue {
                    val embed = EmbedBuilder()
                            .setAuthor("Steam Account Linked", null, Settings.steamImage)
                            .setTitle("Hello, ${cultMember.steamName}!")
                            .setDescription("You've successfully linked your Steam account to the DFC!")
                            .setFooter("Your Steam ID: ${tokenPair.steamID}")
                            .setThumbnail(cultMember.steamAvatar)
                            .build()
                    it.sendMessage(embed).queue()
                }

                steamAuthMap.remove(UUID.fromString(tokenPair.authToken))
                Logs.logSteamLink(cultMember, jda.getUserById(cultMember.discordID)!!)
                println("Validated.")
            }
        }
    }

    @JvmStatic fun executeHourlyUpdate() {
        println("Running hourly task.")

        println("[1/5] Executing leaderboard update...")
        leaderboard = SteamUtil.downloadLeaderboard(Settings.leaderboardID)
        println("[1/5] Done.")

        println("[2/5] Updating Steam data for applicable CultMembers...")
        SteamUtil.updateSteamData()
        println("[2/5] Done.")

        println("[3/5] Serializing data to file...")
        MembersUtil.serializeData()
        println("[3/5] Done.")

        println("[4/5] Updating Steam leaderboard roles...")
        MembersUtil.applyRoles()
        println("[4/5] Done.")

        println("[5/5] Updating member information for Hall of Names...")
        cultMembers.forEach {
            try {
                jda.getGuildById(Settings.guildID)?.getMemberById(it.discordID)?.apply {
                    PinchfistUtil.updateHallEntry(this, it == cultMembers.first())
                }
            } catch(e: IllegalStateException) {
                println("    ERROR: This message was created by another Pinchfist instance and must be updated.")
                println("    Member name: ${jda.getGuildById(Settings.guildID)?.getMemberById(it.discordID)?.user?.name}")
            }
        }
        println("[5/5] Done.")
    }

    @JvmStatic fun executeLeaderboardUpdate() {
        println("Forcing leaderboard update.")

        println("[1/2] Executing leaderboard update...")
        leaderboard = SteamUtil.downloadLeaderboard(Settings.leaderboardID, false)
        println("[1/2] Done.")

        println("[2/2] Updating Steam leaderboard roles...")
        MembersUtil.applyRoles()
        println("[2/2] Done.")
    }
}