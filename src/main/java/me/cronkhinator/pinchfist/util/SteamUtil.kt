package me.cronkhinator.pinchfist.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import me.cronkhinator.pinchfist.Pinchfist
import me.cronkhinator.pinchfist.objects.CultMember
import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.objects.steam.LeaderboardEntry
import me.cronkhinator.pinchfist.objects.steam.NamedLeaderboardEntry
import me.cronkhinator.pinchfist.objects.steam.SteamPlayerSummary
import net.dv8tion.jda.api.EmbedBuilder
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.time.LocalDateTime
import java.util.*

object SteamUtil {
    private val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

    fun downloadLeaderboard(leaderboardID: String, updateChannel: Boolean = true): List<LeaderboardEntry> {
        return runBlocking {
            val response = Pinchfist.client.submitForm<HttpResponse>(
                url = "https://dirtyfilthycu.lt/steam/leaderboard",
                encodeInQuery = true,
                formParameters = Parameters.build {
                    append("id", leaderboardID)
                }
            )

            val listLeaderboardEntryType = object : TypeToken<List<LeaderboardEntry>>(){}.type
            val list: List<LeaderboardEntry> = gson.fromJson(response.readText(), listLeaderboardEntryType)

            println("    Downloaded leaderboard from Steam.")
            if(updateChannel) updateLeaderboardChannel(list)
            return@runBlocking list
        }
    }

    private fun updateLeaderboardChannel(leaderboardEntries: List<LeaderboardEntry>) {
        val builder = EmbedBuilder()
                .setAuthor("Leaderboard Entries", null, Settings.steamImage)
                .setColor(Colors.CYAN.hex)
                .setTitle("Forts Ranked 1v1 | Top 20")
                .setImage("https://i.stack.imgur.com/Fzh0w.png")
                .setFooter("Last updated: ${Pinchfist.dateFormatter.format(LocalDateTime.now())}")

        val namedEntries = getPlayerSummaries(leaderboardEntries.map {it.steamID}).map { player ->
            NamedLeaderboardEntry(
                    leaderboardEntries.first { entry -> entry.steamID == player.steamid},
                    player.personaname)
        }.sortedBy{it.leaderboardEntry.rank}

        namedEntries.forEach {
            val name = if(MembersUtil.getBySteamID(it.leaderboardEntry.steamID) == null) it.name
            else Pinchfist.jda.getUserById(MembersUtil.getBySteamID(it.leaderboardEntry.steamID)!!.discordID)!!.asMention
            builder.addField("Rank ${it.leaderboardEntry.rank}", "$name | Elo: ${it.leaderboardEntry.elo}", false)
        }

        val embed = builder.build()
        val channel = Pinchfist.jda.getGuildById(Settings.guildID)!!.getTextChannelById(Settings.leaderboardChannel)
        val messages = channel!!.history.retrievePast(1).complete()

        if(messages.isEmpty()) channel.sendMessage(embed).queue()
            else messages[0].editMessage(embed).queue()

        println("    Updated leaderboard channel. New embed created: ${messages.isEmpty()}.")
    }

    fun updateSteamData() {
        val cultMembers = Pinchfist.cultMembers.filter{it.steamID != null}.map{it.steamID!!}
        if(cultMembers.isEmpty()) {
            println("    No CultMembers found with a Steam ID. Returning.")
            return
        }

        getPlayerSummaries(cultMembers).forEach {
            MembersUtil.getBySteamID(it.steamid)!!.apply {
                this.steamAvatar = it.avatarmedium
                this.steamName = it.personaname
            }
        }
        println("    Successfully processed ${cultMembers.size} members.")
    }

    fun updateSteamData(steamID: String) {
        getPlayerSummaries(listOf(steamID)).forEach {
            MembersUtil.getBySteamID(it.steamid)!!.apply {
                this.steamAvatar = it.avatarmedium
                this.steamName = it.personaname
            }
        }
    }

    private fun getPlayerSummaries(ids: List<String>): List<SteamPlayerSummary> {
        val idString = StringBuilder().apply {
            ids.forEach {this.append(if(it == ids.last()) it else "$it,")}
        }.toString()

        return runBlocking {
            val response = Pinchfist.client.submitForm<HttpResponse>(
                url = "https://dirtyfilthycu.lt/steam/summaries",
                encodeInQuery = true,
                formParameters = Parameters.build {
                    append("ids", idString)
                }
            )
            val listPlayerSummaryType = object : TypeToken<List<SteamPlayerSummary>>(){}.type
            return@runBlocking gson.fromJson(response.readText(), listPlayerSummaryType)
        }
    }

    @JvmStatic fun userAuth(discordID: Long): String {
        val uuid = UUID.randomUUID()
        Pinchfist.steamAuthMap[uuid] = discordID
        return "https://dirtyfilthycu.lt/steam/login?token=$uuid"
    }
}