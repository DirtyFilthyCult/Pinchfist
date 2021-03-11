package me.cronkhinator.pinchfist.listener

import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.command.CommandRegistry
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object CommandHandler : ListenerAdapter() {

    @Override
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.isWebhookMessage) return

        // Split message into arguments
        val args = event.message.contentRaw.split(" ")

        // Delegate to CommandHandler if root command starts with Settings#PREFIX
        if(args[0].startsWith(Settings.prefix)) {
            CommandRegistry.handleCommand(event, args)
        }
    }
}