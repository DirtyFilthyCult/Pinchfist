package me.cronkhinator.pinchfist.listener

import me.cronkhinator.pinchfist.Pinchfist
import me.cronkhinator.pinchfist.objects.Settings
import me.cronkhinator.pinchfist.util.Logs
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

object MemberHandler : ListenerAdapter() {
    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        super.onGuildMemberRemove(event)
        Logs.logMemberLeave(event.user)
        /*val titleChannel = event.guild.getTextChannelsByName(Pinchfist.chHName, false)[0]
        val id = event.user.id
        for (m in titleChannel.history.retrievePast(100).complete()) {
            if (m.embeds.isEmpty()) continue
            if (m.embeds[0].footer!!.text == id) {
                m.delete().queue()
            }
        }*/
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        super.onGuildMemberJoin(event)
        Logs.logMemberJoin(event.user)
        try {
            event.guild
                .getTextChannelsByName("general", false)[0]
                .sendMessage(Settings.welcomeMessage.replace("{user}", event.member.user.asMention))
                .queue()

            event.guild.getRoleById(Settings.defaultRole)?.let { event.guild.addRoleToMember(event.member, it).queueAfter(2, TimeUnit.SECONDS) }
            event.guild.getRoleById(Settings.oldRole)?.let { event.guild.removeRoleFromMember(event.member, it).queueAfter(2, TimeUnit.SECONDS) }
            event.member.modifyNickname("[DFC] " + event.member.user.name).queue()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onUserUpdateName(event: UserUpdateNameEvent) {
        super.onUserUpdateName(event)
        /*val newName = event.newName
        val ch = Pinchfist.jda.getGuildById("769959815958102036")!!
            .getTextChannelsByName(Pinchfist.chHName, false)[0]
        for (m in ch.history.retrievePast(100).complete()) {
            if (m.embeds.isEmpty()) continue
            val embed = m.embeds[0]
            if (embed.footer!!.text == event.user.id) {
                val eb = EmbedBuilder()
                eb.setTitle(newName)
                eb.setColor(embed.color)
                eb.setDescription(embed.description)
                eb.setFooter(embed.footer!!.text)
                eb.setThumbnail(embed.thumbnail!!.url)
                m.editMessage(eb.build()).queue()
            }
        }*/
    }

    override fun onUserUpdateAvatar(event: UserUpdateAvatarEvent) {
        super.onUserUpdateAvatar(event)
        /*val newURL = event.newAvatarUrl
        val ch = Pinchfist.jda.getGuildById("769959815958102036")!!
            .getTextChannelsByName(Pinchfist.chHName, false)[0]
        for (m in ch.history.retrievePast(100).complete()) {
            if (m.embeds.isEmpty()) continue
            val embed = m.embeds[0]
            if (embed.footer!!.text == event.user.id) {
                val eb = EmbedBuilder()
                eb.setTitle(embed.title)
                eb.setColor(embed.color)
                eb.setDescription(embed.description)
                eb.setFooter(embed.footer!!.text)
                eb.setThumbnail(newURL)
                m.editMessage(eb.build()).queue()
            }
        }*/
    }
}