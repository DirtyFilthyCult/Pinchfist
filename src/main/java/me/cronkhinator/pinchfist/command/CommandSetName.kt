package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.objects.Settings.inquisitorRole
import me.cronkhinator.pinchfist.util.PinchfistUtil
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object CommandSetName : Command {
    override val name = "setname"
    override val staffOnly = false
    override val argsCount = 0..Int.MAX_VALUE

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        if(member.roles.contains(member.guild.getRoleById(inquisitorRole))
            || member.hasPermission(Permission.ADMINISTRATOR)) {
            event.channel.sendMessage("Staff members may not use this command for technical reasons.").queue()
            return
        }

        val chHall = event.guild.getTextChannelsByName(Settings.hallOfNamesChannel, false)[0]

        if (args.isEmpty()) {
            member.modifyNickname("[DFC] ${event.author.name}").queue()
            event.channel.sendMessage("Done :ok_hand:").queue()
            return
        }

        for (m in chHall.history.retrievePast(100)
            .complete()) {
            if (m.embeds.isEmpty()) continue
            val embed = m.embeds[0]
            if (embed.footer != null && embed.footer!!.text == member.id) {
                val title: String = PinchfistUtil.getDFTitle(0, args)
                val titles = PinchfistUtil.getTitles(member)
                var found = false
                if (titles != null) {
                    for (s in titles) {
                        if (s.equals(title, ignoreCase = true)) {
                            member.modifyNickname(s).queue()
                            event.channel.sendMessage("Done :ok_hand:").queue()
                            found = true
                            break
                        }
                    }
                }
                if (!found) event.channel.sendMessage("Sorry, you do not own this title.").queue()
            }
        }
    }
}