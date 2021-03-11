package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.Pinchfist
import me.cronkhinator.pinchfist.objects.Messages
import me.cronkhinator.pinchfist.objects.ReactionRole
import me.cronkhinator.pinchfist.util.Colors
import me.cronkhinator.pinchfist.util.PinchfistUtil
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent


object CommandAddReactionRole : Command {
    override val name: String = "arr"
    override val argsCount: IntRange = 2..Int.MAX_VALUE
    override val staffOnly: Boolean = true

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        val mentionedRole = event.message.mentionedRoles[0]
        if (mentionedRole == null) {
            event.channel.sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue(); return
        }

        val embed = EmbedBuilder()
            .setTitle(mentionedRole.name)
            .setDescription(PinchfistUtil.concatArgs(1, args))
            .setColor(Colors.GREEN.hex)
            .build()

        event.channel.sendMessage(embed).queue {
            Pinchfist.reactionRoles.add(ReactionRole(it.id, mentionedRole.id))
            it.addReaction(event.guild.getEmotesByName("Pinchfist", false)[0]).queue()
        }
        event.message.delete().queue()
    }
}