package me.cronkhinator.pinchfist.objects

import me.cronkhinator.pinchfist.Pinchfist
import net.dv8tion.jda.api.entities.Role
import java.io.File
import java.io.Serializable

object Settings : Serializable {
    @JvmStatic var leaderboardID: String = "6099778"
    @JvmStatic var prefix: String = "!"
    @JvmStatic var filePath: String = File(File(this.javaClass.protectionDomain.codeSource.location.toURI()).parent).path
    @JvmStatic var hallOfNamesChannel = "\uD83C\uDFC6hall-of-names\uD83C\uDFC6"
    @JvmField var nameBattleChannel = "name-battle-results"
    @JvmField var leaderboardChannel = "815216383406505984"

    @JvmStatic var steamUserKey = File("$filePath/userkey.txt").readText()
    @JvmStatic var inquisitorRole: String = "769969002125590558"
    @JvmStatic var defaultRole: String = "774731818146332702"
    @JvmStatic var oldRole: String = "769969179200847923"
    @JvmStatic var guildID: String = "769959815958102036"
    @JvmStatic var loggingChannelID: String = "769965522136596490"
    @JvmStatic var thumbnailImage: String = "https://cdn.discordapp.com/attachments/769965448388542497/772878088119517235/PINCHFIST.png"
    @JvmStatic var steamImage: String = "https://cdn.discordapp.com/attachments/769965448388542497/814293092492247060/steamicon.png"
    @JvmStatic var rankingRoles = mutableMapOf(
            1 to "814300310829793310",
            2 to "814300426236723255",
            3 to "814300468691337236",
            4 to "814300508881813566",
            5 to "814300541450059817",
            6 to "814300587700650044",
            7 to "814300622559641620",
            8 to "814300659578437692",
            9 to "814300699898544148",
            10 to "814300737064009739",
            11 to "814300978831556720",
            12 to "814301019722481705",
            13 to "814301049791447071",
            14 to "814301086222909451",
            15 to "814301123174596648",
            16 to "814301158633111634",
            17 to "814301188936957993",
            18 to "814301215763595284",
            19 to "814301246264705045",
            20 to "814301278087151647"
    )

    @JvmStatic var welcomeMessage: String = "Welcome to the Dirty Filthy Cult, {user}!\n" +
            " Be sure to read the <#769961572369432648> and look for a title you would like to claim using `!findtitle`." +
            " Once you have found an unclaimed title, you can type `!requesttitle <title>` and a staff" +
            " member will give you the title ASAP. You can change your name to any of your titles whenever you want using `!setname <title>`."
}