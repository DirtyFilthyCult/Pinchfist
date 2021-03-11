package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.objects.Messages
import me.cronkhinator.pinchfist.util.MembersUtil
import me.cronkhinator.pinchfist.util.SteamUtil.userAuth
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object CommandSteam : Command {
    override val name: String = "steam"
    override val argsCount: IntRange = 0..1
    override val staffOnly: Boolean = false

    override fun execute(event: GuildMessageReceivedEvent, args: List<String>, member: Member) {
        if(args.isEmpty()) {
            if(MembersUtil.getOrCreate(member.idLong).steamID != null) {
                event.channel.sendMessage("You've already linked your Steam account!").queue(); return
            }

            event.channel.sendMessage("Please check your DMs.").queue()
            event.author.openPrivateChannel().queue { channel ->
                channel.sendMessage("Please click on this link to log in with Steam to verify your account:\n${userAuth(event.author.idLong)}\n\n" +
                        "**This does not give the DFC access to your Steam credentials.** For more information, please see https://dirtyfilthycu.lt/pinchfist/privacy.").queue()
            }
        } else if(args[0] == "unlink") {
            if(MembersUtil.getOrCreate(member.idLong).steamID == null) {
                event.channel.sendMessage("There is no account to unlink!").queue()
                return
            }
            MembersUtil.getOrCreate(member.idLong).steamID = null
            event.channel.sendMessage("Successfully unlinked your Steam account. You may link your account again with the `!steam` command.").queue()
            return
        } else {
            event.channel.sendMessage(Messages.INVALID_ARGUMENTS).queue()
        }
    }
}