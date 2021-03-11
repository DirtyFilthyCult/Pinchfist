package me.cronkhinator.pinchfist.listener;

import me.cronkhinator.pinchfist.objects.Settings;
import me.cronkhinator.pinchfist.util.Colors;
import me.cronkhinator.pinchfist.util.Logs;
import me.cronkhinator.pinchfist.Pinchfist;
import me.cronkhinator.pinchfist.util.SteamUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Commands extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.isWebhookMessage()) return;

        String[] args = event.getMessage().getContentRaw().split("\\s+");
        String prefix = Pinchfist.prefix;
        TextChannel chHall = event.getGuild().getTextChannelsByName(Pinchfist.chHName, false).get(0);
        TextChannel chName = event.getGuild().getTextChannelsByName(Pinchfist.chNName, false).get(0);
        Member eMember = event.getMember();
        Role inquisitor = event.getGuild().getRoleById("769969002125590558");

        if (!eMember.getUser().isBot() && args[0].startsWith(prefix)) {
            switch (args[0].substring(1).toLowerCase()) {
                case "title": {
                    if (isAdmin(eMember)) {
                        if (args.length > 4 && getMentionedMember(event) != null) {
                            String title = getDFTitle(args, 3);
                            assert chHall != null;
                            List<Message> ml;
                            Message message;
                            MessageEmbed embed;
                            ml = chHall.getHistory().retrievePast(100).complete();
                            Member mentionedMember = getMentionedMember(event);
                            Member footerMember;
                            switch (args[1]) {
                                case "add": {
                                    if (title.length() > 32) {
                                        event.getChannel().sendMessage("Title must not be longer than 32 characters.").queue();
                                        return;
                                    }
                                    boolean found = false;
                                    for (Message me : ml) {
                                        if (me.getEmbeds().size() > 0 && mentionedMember.getId().equals(me.getEmbeds().get(0).getFooter().getText())) {
                                            message = me;
                                            embed = me.getEmbeds().get(0);
                                            EmbedBuilder eb = new EmbedBuilder();
                                            footerMember = getMemberFromFooter(event, embed);
                                            eb.setTitle(footerMember.getUser().getName());
                                            String desc;
                                            if (embed.getDescription() != null)
                                                desc = embed.getDescription();
                                            else desc = "";
                                            eb.setColor(embed.getColor());
                                            eb.setDescription(desc + "\n- " + title);
                                            eb.setThumbnail(footerMember.getUser().getAvatarUrl());
                                            eb.setFooter(embed.getFooter().getText());
                                            embed = eb.build();
                                            message.editMessage(embed).queue();
                                            feedbackMsg(event);
                                            Logs.logTitle(title, "added", mentionedMember.getUser(), message.getJumpUrl(), event.getAuthor());
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        EmbedBuilder eb = new EmbedBuilder();
                                        eb.setTitle(mentionedMember.getUser().getName());
                                        eb.setColor(Colors.CYAN.getHex());
                                        eb.setDescription("- " + title);
                                        eb.setThumbnail(mentionedMember.getUser().getAvatarUrl());
                                        eb.setFooter(mentionedMember.getId());
                                        embed = eb.build();
                                        chHall.sendMessage(embed).queue(message1 -> {
                                            Logs.logTitle(title, "added", mentionedMember.getUser(), message1.getJumpUrl(), event.getAuthor());
                                        });
                                        feedbackMsg(event);
                                    }


                                    break;
                                }
                                case "remove": {
                                    for (Message me : ml) {
                                        if (me.getEmbeds().size() > 0 && mentionedMember.getId().equals(me.getEmbeds().get(0).getFooter().getText())) {
                                            message = me;
                                            embed = me.getEmbeds().get(0);
                                            EmbedBuilder eb = new EmbedBuilder();
                                            footerMember = getMemberFromFooter(event, embed);
                                            eb.setTitle(footerMember.getUser().getName());
                                            eb.setColor(embed.getColor());
                                            String desc;
                                            if (embed.getDescription() != null)
                                                desc = embed.getDescription();
                                            else desc = "";
                                            if (desc.contains(title)) {
                                                desc = desc.replace("- " + title, "");
                                                String adjusted = desc.replaceAll("(?m)^[ \t]*\r?\n", ""); //removes empty lines
                                                eb.setDescription(adjusted);
                                                eb.setThumbnail(footerMember.getUser().getAvatarUrl());
                                                eb.setFooter(embed.getFooter().getText());
                                                embed = eb.build();
                                                message.editMessage(embed).queue();
                                                feedbackMsg(event);
                                                Logs.logTitle(title, "deleted", mentionedMember.getUser(), message.getJumpUrl(), event.getAuthor());
                                                if (getTitles(mentionedMember) == null) {
                                                    me.delete().queue();
                                                }
                                            } else event.getChannel().sendMessage("Title not found.").queue();
                                        }
                                    }
                                    break;
                                }
                                /*case "delete": {
                                    for (Message me : ml) {
                                        if (me.getEmbeds().size() > 0 && mentionedMember.getId().equals(me.getEmbeds().get(0).getFooter().getText())) {
                                            message = me;
                                            message.delete().queue();
                                            feedbackMsg(event);
                                        }
                                    }
                                }*/
                            }
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                    break;
                }
                case "findtitle": {
                    if (args.length > 1) {
                        Member m = getMentionedMember(event);
                        if (m != null) {
                            if (!findTitle(m, event))
                                event.getChannel().sendMessage("This person owns no titles.").queue();
                            break;
                        }

                        if (!findTitle(chHall, args, event))
                            event.getChannel().sendMessage("Nobody claimed this title yet!").queue();
                    } else incSynMsg(event);
                    break;
                }
                case "refresh": {
                    if (isAdmin(eMember)) {
                        event.getMessage().delete().queue();
                        assert chHall != null;
                        List<Message> ml;
                        MessageEmbed embed;
                        ml = chHall.getHistory().retrievePast(100).complete();
                        for (Message me : ml) {
                            if (me.getEmbeds().size() > 0) {
                                embed = me.getEmbeds().get(0);
                                if (event.getGuild().isMember(getUserFromFooter(embed))) {
                                    Member m = getMemberFromFooter(event, embed);

                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle(m.getUser().getName());
                                    eb.setColor(embed.getColor());
                                    eb.setDescription(embed.getDescription());
                                    eb.setThumbnail(m.getUser().getAvatarUrl());
                                    eb.setFooter(embed.getFooter().getText());

                                    me.editMessage(eb.build()).queue();
                                } else me.delete().queue();
                            }
                        }
                    }

                    break;
                }
                case "update": {
                    update(event);
                    break;
                }
                case "help": {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Commands");
                    eb.setThumbnail(event.getGuild().retrieveMemberById("765227840655982593").complete().getUser().getAvatarUrl());
                    eb.setColor(Colors.CYAN.getHex());
                    eb.setDescription("List of all available commands");
                    eb.addField("findTitle", "Usage: !findTitle <keyword/full title/mention>\nDisplays whether and by whom a title has been claimed/the titles someone claimed.", false);
                    eb.addField("update", "Usage: !update\nLets you update your pfp/name in hall of names.", false);
                    eb.addField("setName", "Usage: !setName <title>\nLets you change your nickname to a title you own.\n'!setName' for no title.", false);
                    eb.addField("requestTitle", "Usage: !requestTitle <title>\nInforms staff about your desired title.", false);
                    eb.addField("rules", "Usage: !rules\nPosts the title rules.", false);
                    eb.setFooter(eMember.getId(), eMember.getUser().getAvatarUrl());
                    event.getChannel().sendMessage(eb.build()).queue();
                    break;
                }
                case "modhelp": {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("Mod Commands");
                    eb.setThumbnail(event.getGuild().retrieveMemberById("765227840655982593").complete().getUser().getAvatarUrl());
                    eb.setColor(Colors.CYAN.getHex());
                    eb.setDescription("List of all available mod commands");

                    if (isAdmin(eMember) || isRole(eMember, inquisitor)) {
                        eb.addField("Name Battle Add Result", "Admin only. Usage: !nb.ar <name1> <name2> <score1> <score2> <title>", false);
                        eb.addField("Set Welcome Message", "Admin only. Usage: !set.wm <message>\nUser {user} for ping, \\n for linebreak.", false);
                        eb.addField("Add Reaction Role", "Admin only. Usage: !arr <role mention> <text>", false);

                        eb.addField("Kick", "Mod only. Usage: !kick <mention> <reason>", false);
                        eb.addField("Ban", "Mod only. Usage: !ban <mention> <reason>", false);

                        eb.addField("Mute Commands:", "", false);

                        eb.addField("Mute", "Mod only. Usage: !mute <mention> <reason>", true);
                        eb.addField("Tempmute", "Mod only. Usage: !tmute <mention> <duration> <time format>(s,m,h,d) <reason>\nDefault is minutes", true);
                        eb.addField("Unmute", "Mod only. Usage: !unmute <mention>", true);

                        eb.addField("Warn Commands:", "", false);

                        eb.addField("Warn", "Mod only. Usage: !warn <mention> <reason>", true);
                        eb.addField("Remove Warn", "Mod only. Usage: !warn remove <mention> <index>", true);
                        eb.addField("Infractions", "Mod only. Usage: !infractions <mention>", true);

                        eb.addField("Title Commands:", "", false);

                        eb.addField("Add Title", "Admin only. Usage: !title add <mention> <title>", true);
                        eb.addField("Remove Title", "Admin only. Usage: !title remove <mention> <title>", true);
                        eb.addField("Delete All Titles", "Admin only. Usage: !title delete <mention>", true);
                    }
                    eb.setFooter(eMember.getId(), eMember.getUser().getAvatarUrl());
                    event.getChannel().sendMessage(eb.build()).queue();
                    break;
                }
                /*case "transfer": {
                    if (isAdmin(eMember)) {
                        if (args.length > 1) {
                            event.getMessage().delete().queue();
                            List<Message> messages;
                            String channelID = args[1];
                            TextChannel ch = Main.jda.getGuildById("755820069015126078").getTextChannelById(channelID);
                            messages = ch.getHistory().retrievePast(100).complete();
                            for (int i = messages.size() - 1; i >= 0; i--) {
                                event.getChannel().sendMessage(messages.get(i)).queue();
                            }
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                    break;
                }*/
                case "nb.ar": {
                    if (isAdmin(eMember)) {
                        if (args.length > 5) {
                            event.getMessage().delete().queue();
                            String p1 = args[1];
                            String p2 = args[2];
                            int scoreP1 = Integer.valueOf(args[3]);
                            int scoreP2 = Integer.valueOf(args[4]);
                            String winner = getWinner(scoreP1, scoreP2, p1, p2);
                            String title = getTitle(5, args);

                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setTitle(p1 + " vs " + p2);
                            eb.setColor(Colors.CYAN.getHex());
                            eb.setDescription("The battle for the title: " + title);
                            eb.addField(p1, String.valueOf(scoreP1), true);
                            eb.addField(p2, String.valueOf(scoreP2), true);
                            eb.addField("Congratulations " + winner, "You won the title " + title + "!", false);
                            eb.setThumbnail("https://cdn.discordapp.com/attachments/756901728049299518/757284107494490252/PinchfistSuperior.png");
                            chName.sendMessage(eb.build()).queue();
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                    break;
                }
                /*case "save": {
                    try {
                        save(event);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }*/
                case "setname": {
                    if (args.length == 1) {
                        eMember.modifyNickname("[DFC] " + event.getAuthor().getName()).queue();
                        feedbackMsg(event);
                        break;
                    } else if (args.length > 2) {
                        String id = eMember.getId(); //ID of the person who sent the message
                        for (Message m : chHall.getHistory().retrievePast(100).complete()) { //browses through all messages in hall of names
                            if (m.getEmbeds().isEmpty()) continue; //if no embed, go on
                            MessageEmbed embed = m.getEmbeds().get(0); // obviously
                            if (embed.getFooter() != null && embed.getFooter().getText().equals(id)) {
                                String title = getDFTitle(args, 1);
                                String[] titles = getTitles(eMember);
                                boolean found = false;
                                if (titles != null) {
                                    for (String s : titles) {
                                        if (s.toLowerCase().equals(title.toLowerCase())) {
                                            eMember.modifyNickname(s).queue();
                                            feedbackMsg(event);
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                                if (!found)
                                    event.getChannel().sendMessage("Sorry, you don't own this title.").queue();
                            }
                        }
                    } else incSynMsg(event);
                    break;
                }
                case "requesttitle": {
                    if (args.length > 1) {
                        String title = getDFTitle(args, 1);
                        //if (!findTitle(chHall, args, event)) {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle(eMember.getUser().getName());
                        eb.setThumbnail(eMember.getUser().getAvatarUrl());
                        eb.setColor(Colors.CYAN.getHex());
                        eb.setDescription(title);
                        eb.setFooter(eMember.getId());
                        event.getGuild().getTextChannelById("771783872072646666").sendMessage(eb.build()).queue();
                        feedbackMsg(event);
                        //}
                    } else incSynMsg(event);
                    break;
                }
                case "set.wm": {
                    if (isAdmin(eMember)) {
                        if (args.length > 1) {
                            try {
                                File welcomeMessage = new File(Settings.getFilePath() + "src/res/WelcomeMessage.txt");
                                FileWriter fw = new FileWriter(welcomeMessage);
                                fw.write(getTitle(1, args));
                                fw.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                }
                case "ban": {
                    if (isRole(eMember, inquisitor) || isAdmin(eMember)) {
                        Member m = getMentionedMember(event);
                        if (args.length > 1 && m != null) {
                            if (!isAdmin(m) && !(isRole(eMember, inquisitor) && isRole(m, inquisitor))) {
                                String reason = "";
                                if (args.length > 2) {
                                    reason = getTitle(2, args);
                                }
                                Logs.logBan(m.getUser(), reason, event.getAuthor());
                                m.ban(0).queue();
                                feedbackMsg(event);
                            } else modBan(event, "ban");
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                    break;
                }
                case "kick": {
                    if (isRole(eMember, inquisitor) || isAdmin(eMember)) {
                        Member m = getMentionedMember(event);
                        if (args.length > 1 && m != null) {
                            if (!isAdmin(m) && !(isRole(eMember, inquisitor) && isRole(m, inquisitor))) {
                                String reason = "";
                                if (args.length > 2) {
                                    reason = getTitle(2, args);
                                }
                                Logs.logKick(m.getUser(), reason, event.getAuthor());
                                m.kick().queue();
                                feedbackMsg(event);
                            } else modBan(event, "kick");
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                    break;
                }
                case "mute": {
                    Member m = getMentionedMember(event);
                    Role mutedRole = event.getGuild().getRolesByName("Muted", false).get(0);
                    if (isRole(eMember, inquisitor) || isAdmin(eMember)) {
                        if (args.length > 1 && m != null) {
                            if (!isAdmin(m) && !(isRole(eMember, inquisitor) && isRole(m, inquisitor))) {
                                String reason = "";
                                if (args.length > 2) {
                                    reason = getTitle(2, args);
                                }
                                Logs.logMute(m.getUser(), reason, event.getAuthor());
                                event.getGuild().addRoleToMember(m, mutedRole).queue();
                                feedbackMsg(event);
                            } else modBan(event, "mute");
                        } else incSynMsg(event);
                    } else if (m != null && m.equals(event.getMember())) {
                        Logs.logMute(m.getUser(), "self-mute", event.getAuthor());
                        event.getGuild().addRoleToMember(m, mutedRole).queue();
                        event.getChannel().sendMessage("haha retard").queue();
                    } else noPermMsg(event);
                    break;
                }
                case "tmute": {
                    Member m = getMentionedMember(event);
                    Role mutedRole = event.getGuild().getRolesByName("Muted", false).get(0);
                    if (isRole(eMember, inquisitor) || isAdmin(eMember)) {
                        if (args.length > 3 && m != null) {
                            if (!isAdmin(m) && !(isRole(eMember, inquisitor) && isRole(m, inquisitor))) {
                                String reason = "";
                                if (args.length > 4) {
                                    reason = getTitle(4, args);
                                }
                                int duration = 1;
                                try {
                                    duration = Integer.valueOf(args[2]);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    incSynMsg(event);
                                }
                                String timeFormat = args[3];
                                TimeUnit u;
                                switch (timeFormat) {
                                    case "s": {
                                        u = TimeUnit.SECONDS;
                                        break;
                                    }
                                    case "m": {
                                        u = TimeUnit.MINUTES;
                                        break;
                                    }
                                    case "h": {
                                        u = TimeUnit.HOURS;
                                        break;
                                    }
                                    case "d": {
                                        u = TimeUnit.DAYS;
                                        break;
                                    }
                                    default: {
                                        u = TimeUnit.MINUTES;
                                        reason = getTitle(3, args);
                                        break;
                                    }
                                }
                                Logs.logTempMute(m.getUser(), event.getAuthor(), reason, duration, u.toString());
                                int finalDuration = duration;
                                event.getGuild().addRoleToMember(m, mutedRole).queue(unm ->
                                        event.getGuild().removeRoleFromMember(m, mutedRole)
                                                .queueAfter(finalDuration, u, logs ->
                                                        Logs.logTempUnmute(m.getUser())));
                                feedbackMsg(event);
                            } else modBan(event, "mute");
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                    break;
                }
                case "unmute": {
                    if (isRole(eMember, inquisitor) || isAdmin(eMember)) {
                        Member m = getMentionedMember(event);
                        if (args.length > 1 && m != null) {
                            Role mutedRole = event.getGuild().getRolesByName("Muted", false).get(0);
                            Logs.logUnmute(m.getUser(), event.getAuthor());
                            event.getGuild().removeRoleFromMember(m, mutedRole).queue();
                            feedbackMsg(event);
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                    break;
                }
                case "clear": {
                    if (isRole(eMember, inquisitor) || isAdmin(eMember)) {
                        if (args.length > 1) {
                            event.getChannel().deleteMessages(event.getChannel().getHistory().retrievePast(Integer.valueOf(args[1]) + 1).complete()).queue();
                            event.getChannel().sendMessage("Successfully deleted " + args[1] + " messages!").queue(message -> {
                                message.delete().queueAfter(2, TimeUnit.SECONDS);
                            });
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                    break;
                }
                case "warn": {
                    if (isRole(eMember, inquisitor) || isAdmin(eMember)) {
                        Member m = getMentionedMember(event);
                        String reason = " ";
                        Date d = new Date();
                        if (m != null) {
                            if (args[1].equals("remove")) {
                                if (args.length > 3) {
                                    try {
                                        File warnsl = new File(Settings.getFilePath() + "src/res/Warns.txt");
                                        Scanner sc = new Scanner(warnsl);
                                        int i = 0;
                                        StringBuilder fileContent = new StringBuilder();
                                        while (sc.hasNextLine()) {
                                            fileContent.append(sc.nextLine());
                                            fileContent.append("\n");
                                        }
                                        String[] msgs = fileContent.toString().split("\n");
                                        for (String str : msgs) {
                                            if (str.isEmpty()) continue;
                                            String[] ids = str.split(";");
                                            if (ids[0].equals(m.getId())) {
                                                i++;
                                                try {
                                                    if (i == Integer.valueOf(args[3])) {
                                                        FileWriter fw = new FileWriter(warnsl);
                                                        String adjusted = fileContent.toString().replace(str, "").replaceAll("(?m)^[ \t]*\r?\n", "");
                                                        fw.write(adjusted);
                                                        fw.close();
                                                        feedbackMsg(event);
                                                        return;
                                                    }
                                                } catch (NumberFormatException e) {
                                                    incSynMsg(event);
                                                }
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        event.getChannel().sendMessage("Something went wrong... Blame Joe").queue();
                                    }
                                } else incSynMsg(event);
                            } else {
                                if (!isAdmin(m) && !(isRole(eMember, inquisitor) && isRole(m, inquisitor))) {
                                    if (args.length > 2) {
                                        reason = getTitle(2, args);
                                    }
                                    try {
                                        File warns = new File(Settings.getFilePath() + "src/res/Warns.txt");
                                        Scanner s = new Scanner(warns);
                                        StringBuilder stringBuilder = new StringBuilder();
                                        while (s.hasNextLine()) {
                                            stringBuilder.append(s.nextLine());
                                            stringBuilder.append("\n");
                                        }
                                        FileWriter fw = new FileWriter(warns);
                                        fw.write(stringBuilder.toString() + m.getId() + ";" + reason + ";" + d.toString() + "\n");
                                        fw.close();
                                        Logs.logWarn(m.getUser(), event.getAuthor(), reason);
                                        feedbackMsg(event);
                                        return;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else modBan(event, "warn");
                            }
                        } else incSynMsg(event);
                    } else noPermMsg(event);
                    break;
                }
                case "infractions": {
                    if (isRole(eMember, inquisitor) || isAdmin(eMember)) {
                        Member m = getMentionedMember(event);
                        if (m != null) {
                            try {
                                File warnsl = new File(Settings.getFilePath() + "src/res/Warns.txt");
                                Scanner sc = new Scanner(warnsl);
                                int i = 0;
                                HashMap<String, String> infractions = new HashMap<>();
                                StringBuilder fileContent = new StringBuilder();
                                while (sc.hasNextLine()) {
                                    fileContent.append(sc.nextLine());
                                    fileContent.append("\n");
                                }
                                String[] msgs = fileContent.toString().split("\n");
                                for (String str : msgs) {
                                    if (str.isEmpty()) continue;
                                    String[] ids = str.split(";");
                                    if (ids[0].equals(m.getId())) {
                                        i++;
                                        //infractions.put(ids[2], ids[1]);
                                        infractions.put(String.valueOf(i), ids[2] + ";" + ids[1]);
                                    }
                                }
                                EmbedBuilder eb = new EmbedBuilder();
                                eb.setThumbnail("https://cdn.discordapp.com/attachments/769965448388542497/772878088119517235/PINCHFIST.png");
                                eb.setAuthor("Infractions " + m.getUser().getName(), null, m.getUser().getAvatarUrl());
                                eb.setDescription("This user has received a total of " + i + " warns.");
                                for (String st : infractions.keySet()) {
                                    String s;
                                    s = infractions.get(st);
                                    String[] arg = s.split(";");
                                    String reason = arg[1];
                                    if (reason.equals(" ")) reason = "No reason provided.";
                                    eb.addField(st + " " + arg[0], reason, false);
                                }
                                event.getChannel().sendMessage(eb.build()).queue();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    } else noPermMsg(event);
                    break;
                }
                case "steam": {
                    event.getChannel().sendMessage("Please check your DMs.").queue();
                    event.getAuthor().openPrivateChannel().queue(channel ->
                            channel.sendMessage("Please click on this link and log in with Steam to verify your account:\n"
                                    + SteamUtil.userAuth(event.getAuthor().getIdLong())).queue());
                }
            }
        }

    }

    private void modBan(GuildMessageReceivedEvent event, String action) {
        event.getChannel().sendMessage("You cannot " + action + " a moderator!").queue();
    }

    private String[] getTitles(Member member) {
        TextChannel ch = Pinchfist.jda.getGuildById("769959815958102036").getTextChannelsByName(Pinchfist.chHName, false).get(0);
        for (Message m : ch.getHistory().retrievePast(100).complete()) {
            if (m.getEmbeds().isEmpty()) continue;
            MessageEmbed embed = m.getEmbeds().get(0);
            if (embed.getFooter().getText().equals(member.getId())) {
                String desc = embed.getDescription();
                if (desc == null) return null;
                desc = desc.replace("- ", "");
                return desc.split("\n");
            }
        }
        return null;
    }

    private boolean isRole(Member m, Role r) {
        return m.getRoles().contains(r);
    }

    private boolean isAdmin(Member m) {
        return m.hasPermission(Permission.ADMINISTRATOR);
    }

    private void incSynMsg(GuildMessageReceivedEvent event) {
        event.getChannel().sendMessage("Incorrect Syntax. Type !help for a list of commands.").queue();
    }

    private void noPermMsg(GuildMessageReceivedEvent event) {
        event.getChannel().sendMessage("I'm sorry, but you do not have permission to use that command.").queue();
    }

    private boolean findTitle(TextChannel chHall, String[] args, GuildMessageReceivedEvent event) {
        List<Message> ml;
        MessageEmbed embed;
        ml = chHall.getHistory().retrievePast(100).complete();
        boolean found = false;

        String title = getTitle(1, args);

        for (Message me : ml) {
            if (me.getEmbeds().size() > 0) {
                embed = me.getEmbeds().get(0);
                if (embed.getDescription().toLowerCase().contains(title.toLowerCase())) {
                    event.getChannel().sendMessage("This title is already claimed by " + embed.getTitle() + ". " + me.getJumpUrl()).queue();
                    found = true;
                }
            }
        }
        return found;
    }

    private boolean findTitle(Member m, GuildMessageReceivedEvent event) {
        TextChannel chHall = event.getGuild().getTextChannelsByName(Pinchfist.chHName, false).get(0);
        List<Message> ml;
        MessageEmbed embed;
        ml = chHall.getHistory().retrievePast(100).complete();
        boolean found = false;

        for (Message me : ml) {
            if (me.getEmbeds().size() > 0) {
                embed = me.getEmbeds().get(0);
                if (embed.getFooter().getText().equals(m.getId())) {
                    event.getChannel().sendMessage(me.getJumpUrl()).queue();
                    found = true;
                }
            }
        }
        return found;
    }

    private void feedbackMsg(GuildMessageReceivedEvent event) {
        event.getChannel().sendMessage("Done :ok_hand:").queue();
    }

    private String getTitle(int start, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(" ");
        }
        return sb.toString().substring(0, sb.length() - 1);
    }

    private String getDFTitle(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        if (args[start].equals("Dirty") && args[start + 1].equals("Filthy") && args.length > 3)
            start += 2;
        sb.append("Dirty Filthy ").append(getTitle(start, args));
        return sb.toString();
    }

    private String getWinner(int i, int j, String p1, String p2) {
        if (i > j) return p1;
        else return p2;
    }

    private void update(GuildMessageReceivedEvent event) {
        // lets a user update his own Thumbnail and Name in an embed
        TextChannel ch = event.getGuild().getTextChannelsByName(Pinchfist.chHName, false).get(0);
        List<Message> ml = ch.getHistory().retrievePast(100).complete();
        Member member = event.getMember();
        for (Message me : ml) {
            if (me.getEmbeds().size() > 0 && member.getId().equals(me.getEmbeds().get(0).getFooter().getText())) {
                EmbedBuilder eb = new EmbedBuilder();
                MessageEmbed embed;
                embed = me.getEmbeds().get(0);
                eb.setTitle(member.getUser().getName());
                eb.setColor(embed.getColor());
                eb.setDescription(embed.getDescription());
                eb.setThumbnail(member.getUser().getAvatarUrl());
                eb.setFooter(member.getId());
                me.editMessage(eb.build()).queue();
                feedbackMsg(event);
            }
        }
    }

    private Member getMentionedMember(GuildMessageReceivedEvent event) {
        //gets the member mentioned in third argument using the ID found in a mention
        if (event.getMessage().getMentionedMembers().isEmpty()) {
            return null;
        }

        return event.getMessage().getMentionedMembers().get(0);

        /*String id = args[i];

        id = id.replaceAll("<", "");
        id = id.replaceAll(">", "");
        id = id.replaceAll("!", "");
        id = id.replaceAll("@", "");
        return event.getGuild().retrieveMemberById(id).complete();*/
    }

    private Member getMemberFromFooter(GuildMessageReceivedEvent event, MessageEmbed embed) {
        //uses the ID in the Embed's footer to get the corresponding member
        return event.getGuild().retrieveMemberById(embed.getFooter().getText()).complete();
    }

    private User getUserFromFooter(MessageEmbed embed) {
        return Pinchfist.jda.getUserById(embed.getFooter().getText());
    }

    /*private void save(GuildMessageReceivedEvent event) throws IOException {
        TextChannel channel = event.getGuild().getTextChannelsByName(chHName, false).get(0);
        FileWriter writer = new FileWriter("src\\res\\titles.txt");
        for (Message m : channel.getHistory().retrievePast(100).complete()) {
            writer.write("\n");
            String id;
            String[] titles;
            if (m.getEmbeds().isEmpty()) {
                continue;
            }
            MessageEmbed embed = m.getEmbeds().get(0);
            id = embed.getFooter().getText();
            titles = embed.getDescription().split("\n");
            writer.write(id + "{");
            for (int i = 0; i < titles.length; i++) {
                writer.write(titles[i]);
                if (i != titles.length - 1) {
                    writer.write(",");
                }
            }
            writer.write("}");
        }
        writer.close();
    }*/

}
