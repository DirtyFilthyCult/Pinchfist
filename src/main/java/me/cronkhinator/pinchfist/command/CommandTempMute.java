package me.cronkhinator.pinchfist.command;

import kotlin.ranges.IntRange;
import me.cronkhinator.pinchfist.objects.Messages;
import me.cronkhinator.pinchfist.objects.Settings;
import me.cronkhinator.pinchfist.util.Logs;
import me.cronkhinator.pinchfist.util.PinchfistUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandTempMute implements Command {
    @NotNull @Override public String getName() { return "tmute"; }
    @Override public boolean getStaffOnly() { return true; }
    @NotNull @Override public IntRange getArgsCount() { return new IntRange(3, Integer.MAX_VALUE); }

    @Override public void execute(@NotNull GuildMessageReceivedEvent event, @NotNull List<String> args, @NotNull Member member) {
        Member target = PinchfistUtil.getMentionedMember(event);
        Role mutedRole = event.getGuild().getRolesByName("Muted", false).get(0);

        if (target == null) {
            event.getChannel().sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue(); return;
        } else if (target.equals(member)) {
            Logs.logMute(target.getUser(), "self-mute", event.getAuthor());
            event.getGuild().addRoleToMember(target, mutedRole).queue();
            event.getChannel().sendMessage("haha retard").queue();
            return;
        }

        if (!target.hasPermission(Permission.ADMINISTRATOR)
                && !(member.getRoles().contains(member.getGuild().getRoleById(Settings.getInquisitorRole()))
                && target.getRoles().contains(member.getGuild().getRoleById(Settings.getInquisitorRole())))) {
            String reason = "";
            if (args.size() > 3)
                reason = PinchfistUtil.concatArgs(3, args);

            int duration;
            try {
                duration = Integer.parseInt(args.get(1));
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue(); return;
            }

            String timeFormat = args.get(2);
            TimeUnit unit;
            switch (timeFormat) {
                case "s": {
                    unit = TimeUnit.SECONDS;
                    break;
                }
                case "m": {
                    unit = TimeUnit.MINUTES;
                    break;
                }
                case "h": {
                    unit = TimeUnit.HOURS;
                    break;
                }
                case "d": {
                    unit = TimeUnit.DAYS;
                    break;
                }
                default: {
                    unit = TimeUnit.MINUTES;
                    reason = PinchfistUtil.concatArgs(2, args);
                    break;
                }
            }

            Logs.logTempMute(target.getUser(), event.getAuthor(), reason, duration, unit.toString());
            event.getGuild().addRoleToMember(target, mutedRole).queue(__ ->
                    event.getGuild().removeRoleFromMember(target, mutedRole)
                            .queueAfter(duration, unit, ___ ->
                                    Logs.logTempUnmute(target.getUser())));
            event.getChannel().sendMessage("Done :ok_hand:").queue();
        } else {
            event.getChannel().sendMessage(Messages.CANNOT_MUTE_MODERATOR).queue();
        }
    }
}
