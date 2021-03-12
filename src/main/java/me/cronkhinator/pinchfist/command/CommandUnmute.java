package me.cronkhinator.pinchfist.command;

import kotlin.ranges.IntRange;
import me.cronkhinator.pinchfist.objects.Messages;
import me.cronkhinator.pinchfist.util.Logs;
import me.cronkhinator.pinchfist.util.PinchfistUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandUnmute implements Command {
    @NotNull @Override public String getName() { return "unmute"; }
    @Override public boolean getStaffOnly() { return true; }
    @NotNull @Override public IntRange getArgsCount() { return new IntRange(1, 1); }

    @Override public void execute(@NotNull GuildMessageReceivedEvent event, @NotNull List<String> args, @NotNull Member member) {
        Member target = PinchfistUtil.getMentionedMember(event);
        Role mutedRole = event.getGuild().getRolesByName("Muted", false).get(0);

        if (target == null) {
            event.getChannel().sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue(); return;
        } else if(target.equals(member)) {
            Logs.logMute(target.getUser(), "self-mute", event.getAuthor());
            event.getGuild().addRoleToMember(target, mutedRole).queue();
            event.getChannel().sendMessage("haha retard").queue();
            return;
        }

        Logs.logUnmute(target.getUser(), event.getAuthor());
        event.getGuild().removeRoleFromMember(target, mutedRole).queue();
        event.getChannel().sendMessage("Done :ok_hand:").queue();
    }
}
