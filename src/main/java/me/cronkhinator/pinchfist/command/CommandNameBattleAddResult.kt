package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.objects.Messages
import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.util.Colors
import me.cronkhinator.pinchfist.util.PinchfistUtil
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object CommandNameBattleAddResult : Command {
    override val name: String = "nbar"
    override val argsCount: IntRange = 5..Int.MAX_VALUE
    override val staffOnly: Boolean = true

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        val nameBattleChannel = event.guild.getTextChannelsByName(Settings.nameBattleChannel, false)[0]
        event.message.delete().queue()

        val mentionedMembers = event.message.mentionedMembers
        if(mentionedMembers[0] == null || mentionedMembers[1] == null) {
            event.channel.sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue(); return
        }

        val p1 = mentionedMembers[0].user.name
        val p2 = mentionedMembers[1].user.name
        val scoreP1 = Integer.valueOf(args[2])
        val scoreP2 = Integer.valueOf(args[3])
        val winner: String = PinchfistUtil.getWinner(scoreP1, scoreP2, p1, p2)
        val title: String = PinchfistUtil.getTitle(4, args)

        val eb = EmbedBuilder()
            .setTitle("$p1 vs $p2")
            .setColor(Colors.CYAN.hex)
            .setDescription("The battle for the title: $title")
            .addField(p1, scoreP1.toString(), true)
            .addField(p2, scoreP2.toString(), true)
            .addField("Congratulations, $winner!", "You won the title $title!", false)
            .setThumbnail("https://cdn.discordapp.com/attachments/756901728049299518/757284107494490252/PinchfistSuperior.png")
        nameBattleChannel.sendMessage(eb.build()).queue()
    }
}