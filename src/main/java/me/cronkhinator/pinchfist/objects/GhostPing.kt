package me.cronkhinator.pinchfist.objects

import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User

data class GhostPing(val sender: User, val target: User, val channel: TextChannel, val message: String)