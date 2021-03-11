package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.util.Colors
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object CommandModHelp : Command {
    override val name: String = "modhelp"
    override val argsCount: IntRange = 0..0
    override val staffOnly: Boolean = true

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        val eb = EmbedBuilder()
            .setTitle("Mod Commands")
            .setThumbnail(event.guild.retrieveMemberById("765227840655982593").complete().user.avatarUrl)
            .setColor(Colors.CYAN.hex)
            .setDescription("List of all available mod commands")
            .addField(
            "Name Battle Add Result",
            "Admin only. Usage: !nbar <name1> <name2> <score1> <score2> <title>",
            false)
            .addField("Add Reaction Role", "Admin only. Usage: !arr <role mention> <text>", false)
            .addField("Kick", "Mod only. Usage: !kick <mention> <reason>", false)
            .addField("Ban", "Mod only. Usage: !ban <mention> <reason>", false)
            .addField("Mute Commands:", "", false)
            .addField("Mute", "Mod only. Usage: !mute <mention> <reason>", true)
            .addField(
            "Temp Mute",
            "Mod only. Usage: !tmute <mention> <duration> <time format>(s,m,h,d) <reason>\nDefault is minutes",
            true)
            .addField("Unmute", "Mod only. Usage: !unmute <mention>", true)
            .addField("Warn Commands:", "", false)
            .addField("Warn", "Mod only. Usage: !warn <mention> <reason>", true)
            .addField("Remove Warn", "Mod only. Usage: !warn remove <mention> <index>", true)
            .addField("Infractions", "Mod only. Usage: !infractions <mention>", true)
            .addField("Title Commands:", "", false)
            .addField("Add Title", "Admin only. Usage: !title add <mention> <title>", true)
            .addField("Remove Title", "Admin only. Usage: !title remove <mention> <title>", true)
            .setFooter(member.id, member.user.avatarUrl)
        event.channel.sendMessage(eb.build()).queue()
    }
}