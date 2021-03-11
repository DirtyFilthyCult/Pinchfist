package me.cronkhinator.pinchfist.objects

import java.io.Serializable

class Title(val name: String) : Serializable {
    @Transient var owner: CultMember? = null
}