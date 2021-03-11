package me.cronkhinator.pinchfist.objects.steam

import java.io.Serializable

data class SteamSummaryWrapper(val response: SteamSummaryResponse): Serializable {
    inline fun forEachPlayer(action: (SteamPlayerSummary) -> Unit) {
        for (element in response.players) action(element)
    }
}