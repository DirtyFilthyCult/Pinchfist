package me.cronkhinator.pinchfist.objects

object Messages {
    const val INVALID_COMMAND: String = "Invalid command. Type \"!help\" for a list of commands."
    const val PERMISSION_DENIED: String = "I'm sorry, but you don't have permission to use that command."
    const val INVALID_ARGUMENTS: String = "Invalid arguments. Type \"!help\" for the valid syntax."
    const val INVALID_ARGUMENT_TYPE: String = "Invalid argument type(s). Type \"!help\" for the valid syntax."
    const val INVALID_ID: String = "Invalid infraction ID. Please type `!infractions {name}` to view this user's list of infractions."

    const val CANNOT_BAN_MODERATOR: String = "You cannot ban a moderator!"
    const val CANNOT_KICK_MODERATOR: String = "You cannot kick a moderator!"
    const val CANNOT_MUTE_MODERATOR: String = "You cannot mute a moderator!"
    const val CANNOT_WARN_MODERATOR: String = "You cannot warn a moderator!"
}