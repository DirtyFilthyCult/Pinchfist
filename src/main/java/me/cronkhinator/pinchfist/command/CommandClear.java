package me.cronkhinator.pinchfist.command;

import kotlin.ranges.IntRange;
import me.cronkhinator.pinchfist.objects.Messages;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandClear implements Command {
    @NotNull @Override public String getName() { return "clear"; }
    @Override public boolean getStaffOnly() { return true; }
    @NotNull @Override public IntRange getArgsCount() { return new IntRange(1, 1); }

    @Override public void execute(@NotNull GuildMessageReceivedEvent event, @NotNull List<String> args, @NotNull Member member) {
        try {
            event.getChannel().deleteMessages(event.getChannel().getHistory().retrievePast(Integer.parseInt(args.get(0)) + 1).complete()).queue();
            event.getChannel().sendMessage("Successfully deleted " + args.get(0) + " messages.").queue(message -> message.delete().queueAfter(2, TimeUnit.SECONDS));
        } catch(NumberFormatException ignored) {
            event.getChannel().sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue();
        }
    }
}
