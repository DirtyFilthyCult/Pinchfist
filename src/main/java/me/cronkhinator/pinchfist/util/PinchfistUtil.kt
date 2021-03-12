package me.cronkhinator.pinchfist.util

import me.cronkhinator.pinchfist.Pinchfist
import me.cronkhinator.pinchfist.objects.Settings
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

object PinchfistUtil {
    private var messages: List<Message> = listOf()
    private val chHall = Pinchfist.jda.getGuildById(Settings.guildID)!!
            .getTextChannelsByName(Settings.hallOfNamesChannel, false)[0]

    @JvmStatic fun getTitles(member: Member): Array<String>? {
        for (m in chHall.history.retrievePast(100).complete()) {
            if (m.embeds.isEmpty()) continue
            val embed = m.embeds[0]
            if (embed.footer!!.text == member.id) {
                var desc = embed.description ?: return null
                desc = desc.replace("- ", "")
                return desc.split("\n").toTypedArray()
            }
        }
        return null
    }

    @JvmStatic fun isRole(m: Member, r: Role): Boolean {
        return m.roles.contains(r)
    }

    @JvmStatic fun isAdmin(m: Member): Boolean {
        return m.hasPermission(Permission.ADMINISTRATOR)
    }

    @JvmStatic fun incSynMsg(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage("Incorrect Syntax. Type !help for a list of commands.").queue()
    }

    @JvmStatic fun noPermMsg(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage("I'm sorry, but you do not have permission to use that command.").queue()
    }

    @JvmStatic fun findTitle(chHall: TextChannel, args: List<String>, event: GuildMessageReceivedEvent): Boolean {
        var embed: MessageEmbed
        val ml: List<Message> = chHall.history.retrievePast(100).complete()
        var found = false
        val title = concatArgs(0, args)

        if(title.toLowerCase().length < 3) {
            event.channel.sendMessage("Please enter a keyword with more than 3 characters.").queue()
            return true
        } else {
            for (me in ml) {
                if (me.embeds.size > 0) {
                    embed = me.embeds[0]
                    if (embed.description!!.toLowerCase().contains(title.toLowerCase())) {
                        event.channel.sendMessage("This title is already claimed by {}.\nLink: ${me.jumpUrl}").queue {
                            it.editMessage(it.contentRaw.replace("{}", event.guild.getMemberById(embed.footer?.text!!)!!.asMention)).queue()
                        }
                        found = true
                        break
                    }
                }
            }
        }
        return found
    }

    @JvmStatic fun findTitle(m: Member, event: GuildMessageReceivedEvent): Boolean {
        var embed: MessageEmbed
        messages = chHall.history.retrievePast(100).complete()
        var found = false
        for (me in messages) {
            if (me.embeds.size > 0) {
                embed = me.embeds[0]
                if (embed.footer!!.text == m.id) {
                    event.channel.sendMessage("{}'s titles: ${me.jumpUrl}").queue {
                        it.editMessage(it.contentRaw.replace("{}", event.guild.getMemberById(embed.footer?.text!!)!!.asMention)).queue()
                    }
                    found = true
                    break
                }
            }
        }
        return found
    }

    @JvmStatic fun feedbackMsg(event: GuildMessageReceivedEvent) {
        event.channel.sendMessage("Done :ok_hand:").queue()
    }

    @JvmStatic fun concatArgs(start: Int, args: List<String>): String {
        return args.subList(start, args.size).toList().joinToString(separator = " ")
    }

    @JvmStatic fun getWinner(i: Int, j: Int, p1: String, p2: String): String {
        return if (i > j) p1 else p2
    }

    @JvmStatic fun updateHallEntry(member: Member, first: Boolean) {
        if(first) messages = chHall.history.retrievePast(100).complete()
        for (me in messages) {
            if (me.embeds.size > 0 && member.id == me.embeds[0].footer!!.text) {
                val embed: MessageEmbed = me.embeds[0]
                val embedBuilder = EmbedBuilder()
                    .setTitle(member.user.name)
                    .setColor(embed.color)
                    .setDescription(embed.description)
                    .setThumbnail(member.user.avatarUrl ?: Settings.thumbnailImage)
                    .setFooter(member.id)

                me.editMessage(embedBuilder.build()).queue()
            }
        }
    }

    @JvmStatic fun getTitle(start: Int, args: List<String>): String {
        val sb = StringBuilder()
        for (i in start until args.size) {
            sb.append(args[i])
            sb.append(" ")
        }
        return sb.toString().substring(0, sb.length - 1)
    }

    @JvmStatic fun getDFTitle(start: Int, args: List<String>): String {
        var newStart = start
        val sb = StringBuilder()
        if (args[start].toLowerCase() == "dirty" && args[start + 1].toLowerCase() == "filthy" && args.size > 2) newStart += 2
        sb.append("Dirty Filthy ").append(getTitle(newStart, args))
        return sb.toString()
    }

    @JvmStatic fun getMentionedMember(event: GuildMessageReceivedEvent): Member? {
        //gets the member mentioned in third argument using the ID found in a mention
        return if (event.message.mentionedMembers.isEmpty()) {
            null
        } else event.message.mentionedMembers[0]

        /*String id = args[i];

        id = id.replaceAll("<", "");
        id = id.replaceAll(">", "");
        id = id.replaceAll("!", "");
        id = id.replaceAll("@", "");
        return event.getGuild().retrieveMemberById(id).complete();*/
    }

    @JvmStatic fun getMemberFromFooter(event: GuildMessageReceivedEvent, embed: MessageEmbed): Member {
        // uses the ID in the Embed's footer to get the corresponding member
        return event.guild.retrieveMemberById(embed.footer!!.text!!).complete()
    }

    @JvmStatic fun getUserFromFooter(embed: MessageEmbed): User? {
        return Pinchfist.jda.getUserById(embed.footer!!.text!!)
    }
}