package me.cronkhinator.pinchfist.objects

import java.io.Serializable

data class Infraction(var id: Int, val timestamp: String, val reason: String) : Serializable