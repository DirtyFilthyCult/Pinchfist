package me.cronkhinator.pinchfist.objects.steam

import java.io.Serializable

data class SteamSummaryResponse(val players: ArrayList<SteamPlayerSummary>): Serializable