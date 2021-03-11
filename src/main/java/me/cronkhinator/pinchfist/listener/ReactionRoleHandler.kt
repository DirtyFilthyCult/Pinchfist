package me.cronkhinator.pinchfist.listener

import me.cronkhinator.pinchfist.Pinchfist
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object ReactionRoleHandler : ListenerAdapter() {
    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        super.onGuildMessageReactionAdd(event)
        if(event.user.isBot) return
        if(event.reactionEmote.name != "Pinchfist") return

        val matched = Pinchfist.reactionRoles.filter {it.messageID == event.messageId}
        if(matched.isEmpty()) return

        val role = event.guild.getRoleById(matched[0].roleID)
        role?.let { event.guild.addRoleToMember(event.member, it).queue() }
    }

    override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
        super.onGuildMessageReactionRemove(event)
        if(event.user!!.isBot) return
        if(event.reactionEmote.name != "Pinchfist") return

        val matched = Pinchfist.reactionRoles.filter {it.messageID == event.messageId}
        if(matched.isEmpty()) return

        val role = event.guild.getRoleById(matched[0].roleID)
        role?.let { it ->
            event.member?.let { member ->
                event.guild.removeRoleFromMember(member, it).queue()
            }
        }
    }
}