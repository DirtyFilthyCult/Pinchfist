package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.util.PinchfistUtil
import me.cronkhinator.pinchfist.util.PinchfistUtil.findTitle
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object CommandFindTitle : Command {
    override val name = "findtitle"
    override val staffOnly = false
    override val argsCount = 1..Int.MAX_VALUE

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        val chHall = event.guild.getTextChannelsByName(Settings.hallOfNamesChannel, false)[0]
        val m = PinchfistUtil.getMentionedMember(event)
        if (m != null) {
            if (!findTitle(m, event)) event.channel.sendMessage("This member does not own any titles.").queue()
            return
        }

        if (!findTitle(chHall, args, event)) event.channel.sendMessage("Nobody has claimed this title yet.").queue()
    }
}