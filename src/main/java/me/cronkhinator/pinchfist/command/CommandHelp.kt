package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.util.Colors
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object CommandHelp : Command {
    override val name: String = "help"
    override val argsCount: IntRange = 0..0
    override val staffOnly: Boolean = false

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        val embed = EmbedBuilder()
            .setTitle("Commands")
            .setThumbnail(event.guild.retrieveMemberById("765227840655982593").complete().user.avatarUrl)
            .setColor(Colors.CYAN.hex)
            .setDescription("List of all available commands")
            .addField(
            "findTitle",
            "Usage: !findTitle <keyword/full title/mention>\nText input: Displays whether a title has been claimed.\nMention: Displays the titles owned by a cult member.",
            false)
            .addField(
            "setName",
            "Usage: !setName <title>\nChanges your nickname to a title you own.\nUse `!setName` to reset your nickname.",
            false)
            .addField("requestTitle", "Usage: !requestTitle <title>\nInforms staff about your desired title.", false)
            .addField("steam", "Usage: !steam [unlink]\nLinks your Steam account to the DFC Discord.\nUse `!steam unlink` to unlink your account", false)
            .addField("leaderboard", "Usage: !leaderboard\nGrabs your current position on the Forts Ranked 1v1 leaderboard.", false)
            .setFooter(member.id, member.user.avatarUrl)
            .build()
        event.channel.sendMessage(embed).queue()
    }
}