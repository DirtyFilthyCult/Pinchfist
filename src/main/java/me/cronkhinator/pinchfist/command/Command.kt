package me.cronkhinator.pinchfist.command

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

interface Command {
    val name: String
    val argsCount: IntRange
    val staffOnly: Boolean
    fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member)
}