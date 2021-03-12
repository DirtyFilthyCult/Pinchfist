package me.cronkhinator.pinchfist.listener

import me.cronkhinator.pinchfist.objects.GhostPing
import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.util.Logs
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*

object GhostPingHandler : ListenerAdapter() {
    val ghostPings = mutableMapOf<String, GhostPing>()

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        super.onGuildMessageReceived(event)
        if(event.member?.hasPermission(Permission.ADMINISTRATOR) == true
            || event.member?.roles?.contains(event.guild.getRoleById(Settings.inquisitorRole)) == true)
                return

        val message = event.message
        if (message.mentionedMembers.isEmpty() || message.isWebhookMessage || message.author.isBot) return

        val sender = event.member!!.user
        val target = message.mentionedMembers[0].user
        ghostPings[event.messageId] = GhostPing(sender, target, event.channel, message.contentRaw)

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                ghostPings.remove(event.messageId)
            }
        }, 300000L)
    }

    override fun onGuildMessageDelete(event: GuildMessageDeleteEvent) {
        val ghostPing = ghostPings[event.messageId] ?: return
        Logs.logGhostPing(ghostPing.sender, ghostPing.target, ghostPing.channel, ghostPing.message)
    }
}