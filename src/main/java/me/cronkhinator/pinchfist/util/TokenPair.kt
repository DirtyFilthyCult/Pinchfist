package me.cronkhinator.pinchfist.util

import java.io.Serializable

data class TokenPair(val authToken: String, val steamID: String) : Serializable
