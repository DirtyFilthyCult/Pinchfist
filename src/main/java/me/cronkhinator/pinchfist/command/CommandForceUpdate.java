package me.cronkhinator.pinchfist.command;

import kotlin.ranges.IntRange;
import me.cronkhinator.pinchfist.Pinchfist;
import me.cronkhinator.pinchfist.objects.Messages;
import me.cronkhinator.pinchfist.objects.Settings;
import me.cronkhinator.pinchfist.util.Logs;
import me.cronkhinator.pinchfist.util.MembersUtil;
import me.cronkhinator.pinchfist.util.PinchfistUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandForceUpdate implements Command {
    @NotNull @Override public String getName() { return "forceupdate"; }
    @Override public boolean getStaffOnly() { return true; }
    @NotNull @Override public IntRange getArgsCount() { return new IntRange(0, 0); }

    @Override public void execute(@NotNull GuildMessageReceivedEvent event, @NotNull List<String> args, @NotNull Member member) {
        event.getChannel().sendMessage("Forcing hourly update...").queue();
        System.out.println("Hourly update being forced by command.");
        Pinchfist.executeHourlyUpdate();
        event.getChannel().sendMessage("Done :ok_hand:").queue();
    }
}
