package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.objects.Messages
import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.util.Colors
import me.cronkhinator.pinchfist.util.Logs.logTitle
import me.cronkhinator.pinchfist.util.PinchfistUtil
import me.cronkhinator.pinchfist.util.PinchfistUtil.getMemberFromFooter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object CommandTitle : Command {
    override val name = "title"
    override val staffOnly = true
    override val argsCount = 3..Int.MAX_VALUE

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        if(!member.hasPermission(Permission.ADMINISTRATOR)) {
            event.channel.sendMessage(Messages.PERMISSION_DENIED).queue(); return
        }

        val chHall = event.guild.getTextChannelsByName(Settings.hallOfNamesChannel, false)[0]
        val title: String = PinchfistUtil.getDFTitle(2, args)
        val ml = chHall.history.retrievePast(100).complete()
        val mentionedMember = PinchfistUtil.getMentionedMember(event)

        if(mentionedMember == null) {
            event.channel.sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue(); return
        }

        when (args[0]) {
            "add" -> {
                if (title.length > 32) {
                    event.channel.sendMessage("Title must not be longer than 32 characters.").queue()
                    return
                }
                var found = false
                for (me in ml) {
                    if (me.embeds.size > 0 && mentionedMember.id == me.embeds[0].footer!!.text) {
                        val message = me
                        val embed = me.embeds[0]
                        val eb = EmbedBuilder()
                            .setTitle(getMemberFromFooter(event, embed).user.name)
                            .setColor(embed.color)
                            .setDescription("${embed.description ?: ""}\n- $title")
                            .setThumbnail(getMemberFromFooter(event, embed).user.avatarUrl ?: Settings.thumbnailImage)
                            .setFooter(embed.footer!!.text)
                        message.editMessage(eb.build()).queue()
                        PinchfistUtil.feedbackMsg(event)
                        logTitle(title, "added", mentionedMember.user, message.jumpUrl, event.author)
                        found = true
                        break
                    }
                }
                if (!found) {
                    val embed = EmbedBuilder()
                        .setTitle(mentionedMember.user.name)
                        .setColor(Colors.CYAN.hex)
                        .setDescription("- $title")
                        .setThumbnail(mentionedMember.user.avatarUrl ?: Settings.thumbnailImage)
                        .setFooter(mentionedMember.id)
                        .build()
                    chHall.sendMessage(embed).queue { message1 ->
                        logTitle(
                            title,
                            "added",
                            mentionedMember.user,
                            message1.jumpUrl,
                            event.author
                        )
                    }
                    event.channel.sendMessage("Done :ok_hand:").queue()
                }
            }
            "remove" -> {
                for (me in ml) {
                    if (me.embeds.size > 0 && mentionedMember.id == me.embeds[0].footer!!.text) {
                        val message = me
                        var embed = me.embeds[0]
                        val eb = EmbedBuilder()
                        eb.setTitle(getMemberFromFooter(event, embed).user.name)
                        eb.setColor(embed.color)
                        var desc: String?
                        desc = if (embed.description != null) embed.description else ""
                        if (desc!!.contains(title)) {
                            desc = desc.replace("- $title", "")
                            val adjusted = desc.replace("(?m)^[ \t]*\r?\n".toRegex(), "") //removes empty lines
                            eb.setDescription(adjusted)
                            eb.setThumbnail(getMemberFromFooter(event, embed).user.avatarUrl)
                            eb.setFooter(embed.footer!!.text)
                            embed = eb.build()
                            message.editMessage(embed).queue()
                            PinchfistUtil.feedbackMsg(event)
                            logTitle(title, "deleted", mentionedMember.user, message.jumpUrl, event.author)
                            if (PinchfistUtil.getTitles(mentionedMember) == null) {
                                me.delete().queue()
                            }
                        } else event.channel.sendMessage("Title not found.").queue()
                    }
                }
            }
        }

    }
}