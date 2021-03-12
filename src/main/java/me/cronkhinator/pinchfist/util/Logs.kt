package me.cronkhinator.pinchfist.util

import me.cronkhinator.pinchfist.Pinchfist
import me.cronkhinator.pinchfist.objects.CultMember
import me.cronkhinator.pinchfist.objects.Settings
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

object Logs {
    private fun log(embed: MessageEmbed) {
        Pinchfist.jda
            .getGuildById(Settings.guildID)
            ?.getTextChannelById(Settings.loggingChannelID)
            ?.sendMessage(embed)
            ?.queue()
    }

    @JvmStatic fun logBan(user: User, reason: String, mod: User) {
        val embed = EmbedBuilder()
            .setAuthor("Ban", null, user.avatarUrl)
            .setColor(Colors.RED.hex)
            .setDescription("**${user.asMention} was banned**\n${user.name}#${user.discriminator}")
            .setThumbnail("https://cdn.discordapp.com/attachments/769965448388542497/772878088119517235/PINCHFIST.png")
            .setFooter("Moderator: " + mod.name).apply {
                if(reason != "") addField("Reason:", reason, false)
            }.build()
        log(embed)
    }

    @JvmStatic fun logSteamLink(cultMember: CultMember, user: User) {
        val embed = EmbedBuilder()
            .setAuthor("Synergy Request Received", null, user.avatarUrl)
            .setColor(Colors.CYAN.hex)
            .setDescription("**${user.asMention} linked their Steam account**\nSteam ID: ${cultMember.steamID}")
            .setThumbnail(cultMember.steamAvatar ?: Settings.steamImage)
            .build()
        log(embed)
    }

    @JvmStatic fun logKick(user: User, reason: String, mod: User) {
        val embed = EmbedBuilder()
            .setAuthor("Kick", null, user.avatarUrl)
            .setColor(Colors.RED.hex)
            .setDescription("**${user.asMention} was kicked**\n${user.name}#${user.discriminator}")
            .setThumbnail("https://cdn.discordapp.com/attachments/769965448388542497/772878088119517235/PINCHFIST.png")
            .setFooter("Moderator: " + mod.name).apply {
                if(reason != "") addField("Reason:", reason, false)
            }.build()
        log(embed)
    }

    @JvmStatic fun logTitle(title: String?, action: String, u: User, msgUrl: String?, mod: User) {
        val embed = EmbedBuilder()
            .setTitle("Title $action")
            .setAuthor(u.name, msgUrl, u.avatarUrl)
            .setColor(Colors.CYAN.hex)
            .setDescription(title)
            .setFooter("Moderator: " + mod.name)
            .build()
        log(embed)
    }

    @JvmStatic fun logMute(user: User, reason: String, mod: User) {
        val embed = EmbedBuilder()
            .setAuthor("Mute", null, user.avatarUrl)
            .setColor(Colors.RED.hex)
            .setDescription("**${user.asMention} was muted**\n${user.name}#${user.discriminator}")
            .setThumbnail("https://cdn.discordapp.com/attachments/769965448388542497/772878088119517235/PINCHFIST.png")
            .setFooter("Moderator: " + mod.name).apply {
                if(reason != "") addField("Reason:", reason, false)
            }.build()
        log(embed)
    }

    @JvmStatic fun logTempMute(user: User, mod: User, reason: String, duration: Int, tf: String) {
        val embed = EmbedBuilder()
            .setAuthor("Mute", null, user.avatarUrl)
            .setColor(Colors.RED.hex)
            .setDescription("**${user.asMention} was muted for $duration$tf**\n${user.name}#${user.discriminator}")
            .setThumbnail("https://cdn.discordapp.com/attachments/769965448388542497/772878088119517235/PINCHFIST.png")
            .setFooter("Moderator: " + mod.name).apply {
                if(reason != "") addField("Reason:", reason, false)
            }.build()
        log(embed)
    }

    @JvmStatic fun logUnmute(user: User, mod: User) {
        val embed = EmbedBuilder()
            .setAuthor("Unmute", null, user.avatarUrl)
            .setColor(Colors.GREEN.hex)
            .setDescription("**${user.asMention} was unmuted**\n${user.name}#${user.discriminator}")
            .setThumbnail("https://cdn.discordapp.com/attachments/769965448388542497/772878088119517235/PINCHFIST.png")
            .setFooter("Moderator: " + mod.name)
            .build()
        log(embed)
    }

    @JvmStatic fun logTempUnmute(user: User) {
        val embed = EmbedBuilder()
            .setAuthor("Unmute", null, user.avatarUrl)
            .setColor(Colors.GREEN.hex)
            .setDescription("**${user.asMention} was unmuted**\n${user.name}#${user.discriminator}")
            .setThumbnail("https://cdn.discordapp.com/attachments/769965448388542497/772878088119517235/PINCHFIST.png")
            .setFooter("Temp-Mute Ended")
            .build()
        log(embed)
    }

    @JvmStatic fun logWarn(user: User, mod: User, reason: String) {
        val embed = EmbedBuilder()
            .setAuthor("Warn", null, user.avatarUrl)
            .setColor(Colors.RED.hex)
            .setDescription("**${user.asMention} was warned**\n${user.name}#${user.discriminator}")
            .setThumbnail("https://cdn.discordapp.com/attachments/769965448388542497/772878088119517235/PINCHFIST.png")
            .setFooter("Moderator: " + mod.name).apply {
                if(reason != "") addField("Reason:", reason, false)
            }.build()
        log(embed)
    }

    @JvmStatic fun logMemberLeave(user: User) {
        val embed = EmbedBuilder()
            .setAuthor("Member Left", null, user.avatarUrl)
            .setColor(Colors.RED.hex)
            .setDescription("**${user.asMention} left the server**\n${user.name}#${user.discriminator}")
            .build()
        log(embed)
    }

    @JvmStatic fun logMemberJoin(user: User) {
        val embed = EmbedBuilder()
            .setAuthor("Member Joined", null, user.avatarUrl)
            .setColor(Colors.GREEN.hex)
            .setDescription("**${user.asMention} joined the server**\n${user.name}#${user.discriminator}")
            .addField("Account Age:", user.timeCreated.toString(), false)
            .build()
        log(embed)
    }

    @JvmStatic fun logGhostPing(sender: User, target: User, channel: TextChannel, message: String) {
        val embed = EmbedBuilder()
            .setAuthor("Ghost Ping", null, sender.avatarUrl)
            .setColor(Colors.WHITE.hex)
            .setDescription("**${sender.asMention} ghost pinged ${target.asMention}** in channel ${channel.asMention}")
            .addField("Message:", message, false)
            .setThumbnail("https://cdn.discordapp.com/attachments/769965448388542497/780796149388148766/PinchfistPing.png")
            .build()
        log(embed)

        /*user2.openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage("Hey, just letting you know that " + user.getName() + " ghost pinged you.").queue()
        );*/
    }
}