package me.cronkhinator.pinchfist.command

import me.cronkhinator.pinchfist.objects.Messages
import me.cronkhinator.pinchfist.objects.Settings
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

/**
 * A registry to validate and execute commands
 *
 * This singleton is intended to be called by a [ListenerAdapter][net.dv8tion.jda.api.hooks.ListenerAdapter]
 * to validate and execute a command once the message has been identified as one by the Settings prefix
 *
 * @author Joseph Pereira
 */
object CommandRegistry {
    // Map of commands by name; Kotlin-based commands do not need an () initializer so long as they are objects (singletons)
    private val commands: Map<String, Command> = registerCommands(
        CommandBan(),
        CommandKick(),
        CommandMute(),
        CommandUnmute(),
        CommandTempMute(),
        CommandClear(),
        CommandRequestTitle(),
        CommandInfractions(),
        CommandWarn(),
        CommandForceUpdate(),
        CommandSteam,
        CommandLeaderboard,
        CommandAddReactionRole,
        CommandTitle,
        CommandFindTitle,
        CommandHelp,
        CommandModHelp,
        CommandNameBattleAddResult,
        CommandSetName
    )

    /**
     * A function to handle an identified command from a [GuildMessageReceivedEvent] and pass it to its proper [Command] executor.
     *
     * @param event The [GuildMessageReceivedEvent] to be passed from a [ListenerAdapter][net.dv8tion.jda.api.hooks.ListenerAdapter]
     * @param args The [List] of arguments to be passed to the command executor
     */
    @JvmStatic fun handleCommand(event: GuildMessageReceivedEvent, args: List<String>) {
        val name = args[0].substring(1).toLowerCase()

        val argsTrimmed = args.toMutableList().drop(1)

        val command = if(commands[name] != null) commands[name] else {
            event.channel.sendMessage(Messages.INVALID_COMMAND).queue(); return
        }!!

        if(command.staffOnly) {
            if(!(event.member!!.hasPermission(Permission.ADMINISTRATOR)
                        || event.member!!.roles.contains(event.guild.getRoleById(Settings.inquisitorRole)))) {
                event.channel.sendMessage(Messages.PERMISSION_DENIED).queue(); return
            }
        }

        if(command.argsCount != 0..0) {
            if(!command.argsCount.contains(argsTrimmed.size)) {
                event.channel.sendMessage(Messages.INVALID_ARGUMENTS).queue(); return
            }
        }

        // All should be good; pass execution to Command#execute()
        command.execute(event, argsTrimmed, event.member!!)
    }

    /**
     * A function to register a list of commands by returning a new registry [Map] indexed by [Command.name]
     *
     * @param commands List of commands to add to the registry map
     * @return New map indexed in the form of <[Command.name], [Command]>
     */
    private fun registerCommands(vararg commands: Command): Map<String, Command> {
        val commandMap = mutableMapOf<String, Command>()
        commands.forEach {commandMap[it.name] = it}
        return commandMap.toMap()
    }
}