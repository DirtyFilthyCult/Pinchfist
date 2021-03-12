package me.cronkhinator.pinchfist.util

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.cronkhinator.pinchfist.Pinchfist
import me.cronkhinator.pinchfist.objects.CultMember
import me.cronkhinator.pinchfist.objects.ReactionRole
import me.cronkhinator.pinchfist.objects.Settings
import java.io.File
import java.io.FileWriter
import java.io.IOException

object MembersUtil {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    @JvmStatic fun getOrCreate(discordID: Long): CultMember {
        var cultMember = Pinchfist.cultMembers.firstOrNull {
            it.discordID == discordID
        }
        if(cultMember == null) {
            Pinchfist.cultMembers.add(CultMember(discordID,
                    null,
                    null,
                    null,
                    mutableListOf(),
                    mutableListOf()).apply{cultMember = this})
        }
        return cultMember!!
    }

    @JvmStatic fun getBySteamID(steamID: String): CultMember? {
        return Pinchfist.cultMembers.firstOrNull {
            it.steamID == steamID
        }
    }


    @JvmStatic fun serializeData() {
        try {
            println("    Serializing cult members...")
            File("${Settings.filePath}/data/").mkdirs()
            File("${Settings.filePath}/data/cultMembers.json").createNewFile()
            FileWriter("${Settings.filePath}/data/cultMembers.json").apply {
                write(gson.toJson(Pinchfist.cultMembers))
                close()
            }
            println("    Done.")
            println("    Serializing reaction roles...")
            File("${Settings.filePath}/data/reactionRoles.json").createNewFile()
            FileWriter("${Settings.filePath}/data/reactionRoles.json").apply {
                write(gson.toJson(Pinchfist.reactionRoles))
                close()
            }
            println("    Done. Serialization complete.")
        } catch (e: IOException) {
            println("    Failed to serialize data:"); e.printStackTrace()
        }
    }

    @JvmStatic fun deserializeData() {
        try {
            println("Deserializing cult members...")
            if(!File("${Settings.filePath}/data/cultMembers.json").exists()) {
                println("cultMembers.json file does not exist. Initializing file for next restart.")
                serializeData(); return
            }

            // Using Type reflection allows us to ignore a cast for the MutableList type variable
            val mutableListCultMemberType = object : TypeToken<MutableList<CultMember>>(){}.type
            val deserializedCultMembers: MutableList<CultMember> =
                gson.fromJson(File("${Settings.filePath}/data/cultMembers.json").readText(), mutableListCultMemberType)
            Pinchfist.cultMembers = deserializedCultMembers
            println("Done.")

            println("Deserializing reaction roles...")
            if(!File("${Settings.filePath}/data/reactionRoles.json").exists()) {
                println("reactionRoles.json file does not exist. Initializing file for next restart.")
                serializeData(); return
            }

            val mutableListReactionRoleType = object : TypeToken<MutableList<ReactionRole>>(){}.type
            val deserializedReactionRoles: MutableList<ReactionRole> =
                gson.fromJson(File("${Settings.filePath}/data/reactionRoles.json").readText(), mutableListReactionRoleType)
            Pinchfist.reactionRoles = deserializedReactionRoles

            println("Done. Deserialization complete.")
        } catch(e: Exception) {
            println("Failed to deserialize data:"); e.printStackTrace()
        }
    }

    @JvmStatic fun applyRoles() {
        var processed = 0
        Pinchfist.cultMembers.forEach { cultMember ->
            val guild = Pinchfist.jda.getGuildById(Settings.guildID) ?: return

            val matched = Pinchfist.leaderboard.filter {it.steamID == cultMember.steamID}
            if(matched.isEmpty()) return@forEach

            val newRole = Pinchfist.jda.getRoleById(Settings.rankingRoles[matched[0].rank] ?: return@forEach) ?: return@forEach

            Settings.rankingRoles.values.forEach roles@ {
                if(guild.getMemberById(cultMember.discordID)?.roles?.contains(guild.getRoleById(it) ?: return@roles) == true) {
                    if(it == newRole.id) return@forEach
                    guild.removeRoleFromMember(cultMember.discordID, guild.getRoleById(it)!!).queue()
                }
            }

            guild.addRoleToMember(cultMember.discordID, newRole).queue()
            processed++
        }
        println("    Updated the ranks of $processed member(s).")
        Settings.rankingRoles
    }
}