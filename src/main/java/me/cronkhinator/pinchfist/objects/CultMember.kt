package me.cronkhinator.pinchfist.objects

import java.io.Serializable

data class CultMember(val discordID: Long,
                      var steamID: String?,
                      var steamAvatar: String?,
                      var steamName: String?,
                      val titles: List<Title>,
                      val infractions: List<Infraction>) : Serializable {

    init {
        titles.forEach {
            it.owner = this
        }
    }
}