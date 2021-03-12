package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.Pinchfist
import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.util.MembersUtil
import me.cronkhinator.pinchfist.util.SteamUtil
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object CommandLeaderboard : Command {
    override val name: String = "leaderboard"
    override val argsCount: IntRange = 0..0
    override val staffOnly: Boolean = false

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        val cultMember = MembersUtil.getOrCreate(member.idLong)
        val steamID = cultMember.steamID
        if(steamID == null) {
             event.channel.sendMessage("You must link your Steam account to DFC with `${Settings.prefix}steam` to use this command!").queue(); return
        }

        Pinchfist.executeLeaderboardUpdate()

        val matched = Pinchfist.leaderboard.filter {it.steamID == steamID}

        val embed = EmbedBuilder()
            .setThumbnail(cultMember.steamAvatar)
            .setAuthor("Leaderboard Info for: ${cultMember.steamName}", null, Settings.steamImage)
            .setDescription(if (matched.isEmpty())
                "You are not in the top 20 on the Forts Ranked leaderboard."
                else "Your rank on the Forts Ranked leaderboard is: **Rank ${matched[0].rank}** with Elo **${matched[0].elo}**.")
            .setFooter("Rankings are synchronized upon execution of this command.")
            .build()

        event.channel.sendMessage(embed).queue()
    }
}