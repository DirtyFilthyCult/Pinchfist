package me.cronkhinator.pinchfist.util

import com.google.gson.GsonBuilder
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import me.cronkhinator.pinchfist.Pinchfist
import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.objects.steam.LeaderboardEntry
import me.cronkhinator.pinchfist.objects.steam.NamedLeaderboardEntry
import me.cronkhinator.pinchfist.objects.steam.SteamSummaryWrapper
import net.dv8tion.jda.api.EmbedBuilder
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import java.time.LocalDateTime
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

object SteamUtil {
    val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

    fun downloadLeaderboard(leaderboardID: String, updateChannel: Boolean = true): List<LeaderboardEntry> {
        var list: List<LeaderboardEntry> = emptyList()
        runBlocking {
            val entryList: MutableList<LeaderboardEntry> = mutableListOf()
            val request: HttpResponse = Pinchfist.client.get {
                url("https://steamcommunity.com/stats/410900/leaderboards/${leaderboardID}?xml=1&start=1&end=20")
            }

            val xml: Document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(InputSource(StringReader(request.readText())))

            val entries = xml.getElementsByTagName("entry")
            iterable(entries).map{(it as Element)}.map {
                LeaderboardEntry(
                        it.getElementsByTagName("steamid").item(0).textContent,
                        it.getElementsByTagName("score").item(0).textContent.toInt(),
                        it.getElementsByTagName("rank").item(0).textContent.toInt()
                )
            }.forEach(entryList::add)
            list = entryList.toList()
        }
        println("    Downloaded leaderboard from Steam.")
        if(updateChannel) updateLeaderboardChannel(list)
        return list
    }

    private fun updateLeaderboardChannel(leaderboardEntries: List<LeaderboardEntry>) {
        val builder = EmbedBuilder()
                .setAuthor("Leaderboard Entries", null, Settings.steamImage)
                .setColor(Colors.CYAN.hex)
                .setTitle("Forts Ranked 1v1 | Top 20")
                .setImage("https://i.stack.imgur.com/Fzh0w.png")
                .setFooter("Last updated: ${Pinchfist.dateFormatter.format(LocalDateTime.now())}")

        val namedEntries = getPlayerSummaries(leaderboardEntries.map {it.steamID}).response.players.map { player ->
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

        getPlayerSummaries(cultMembers).forEachPlayer {
            MembersUtil.getBySteamID(it.steamid)!!.apply {
                this.steamAvatar = it.avatarmedium
                this.steamName = it.personaname
            }
        }
        println("    Successfully processed ${cultMembers.size} members.")
    }

    fun updateSteamData(steamID: String) {
        getPlayerSummaries(listOf(steamID)).forEachPlayer {
            MembersUtil.getBySteamID(it.steamid)!!.apply {
                this.steamAvatar = it.avatarmedium
                this.steamName = it.personaname
            }
        }
    }

    private fun getPlayerSummaries(ids: List<String>): SteamSummaryWrapper {
        val idString = StringBuilder().apply {
            ids.forEach {this.append(if(it == ids.last()) it else "$it,")}
        }.toString()

        return runBlocking {
            val request = Pinchfist.client.submitForm<HttpResponse>(
                    url = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/",
                    encodeInQuery = true,
                    formParameters = Parameters.build {
                        append("key", Settings.steamUserKey)
                        append("steamids", idString)
                    }
            )
            return@runBlocking gson.fromJson(request.readText(), SteamSummaryWrapper::class.java)
        }
    }


    private fun iterable(nodeList: NodeList): Iterable<Node> {
        return Iterable {
            object : MutableIterator<Node> {
                private var index = 0
                override fun hasNext(): Boolean {
                    return index < nodeList.length
                }

                override fun next(): Node {
                    if (!hasNext()) throw NoSuchElementException()
                    return nodeList.item(index++)
                }
                override fun remove() {
                    throw UnsupportedOperationException("Elements cannot be removed from a NodeList.")
                }
            }
        }
    }

    @JvmStatic fun userAuth(discordID: Long): String {
        val uuid = UUID.randomUUID()
        Pinchfist.steamAuthMap[uuid] = discordID
        return "https://dirtyfilthycu.lt/pinchfist/login?token=$uuid"
    }
}