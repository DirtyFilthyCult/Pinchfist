package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.util.Colors
import me.cronkhinator.pinchfist.util.PinchfistUtil.getDFTitle
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class CommandRequestTitle : Command {
    override val name: String = "requesttitle"
    override val staffOnly: Boolean = false
    override val argsCount: IntRange = IntRange(1, Int.MAX_VALUE)

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        val title = getDFTitle(0, args)
        val embed = EmbedBuilder()
            .setTitle(member.user.name)
            .setThumbnail(member.user.avatarUrl ?: Settings.thumbnailImage)
            .setColor(Colors.CYAN.hex)
            .setDescription(title)
            .setFooter(member.id)
            .build()
        event.guild.getTextChannelById("771783872072646666")!!.sendMessage(embed).queue()
        event.channel.sendMessage("Done :ok_hand:").queue()
    }
}