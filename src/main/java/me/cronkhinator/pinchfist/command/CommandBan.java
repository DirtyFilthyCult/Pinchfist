package me.cronkhinator.pinchfist.command;

import kotlin.ranges.IntRange;
import me.cronkhinator.pinchfist.objects.Messages;
import me.cronkhinator.pinchfist.objects.Settings;
import me.cronkhinator.pinchfist.util.Logs;
import me.cronkhinator.pinchfist.util.PinchfistUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandBan implements Command {
    @NotNull @Override public String getName() { return "ban"; }
    @Override public boolean getStaffOnly() { return true; }
    @NotNull @Override public IntRange getArgsCount() { return new IntRange(1, Integer.MAX_VALUE); }

    @Override public void execute(@NotNull GuildMessageReceivedEvent event, @NotNull List<String> args, @NotNull Member member) {
        Member target = PinchfistUtil.getMentionedMember(event);
        if (target == null) {
            event.getChannel().sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue(); return;
        }

        if (!target.hasPermission(Permission.ADMINISTRATOR)
                && !(member.getRoles().contains(member.getGuild().getRoleById(Settings.getInquisitorRole()))
                && target.getRoles().contains(member.getGuild().getRoleById(Settings.getInquisitorRole())))) {
            String reason = "";
            if (args.size() > 1)
                reason = PinchfistUtil.concatArgs(1, args);

            Logs.logBan(target.getUser(), reason, event.getAuthor());
            target.ban(0).queue();
            event.getChannel().sendMessage("Done :ok_hand:").queue();
        } else {
            event.getChannel().sendMessage(Messages.CANNOT_BAN_MODERATOR).queue();
        }
    }
}
