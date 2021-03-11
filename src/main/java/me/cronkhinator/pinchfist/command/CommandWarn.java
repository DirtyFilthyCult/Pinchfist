package me.cronkhinator.pinchfist.command;

import kotlin.ranges.IntRange;
import me.cronkhinator.pinchfist.Pinchfist;
import me.cronkhinator.pinchfist.objects.CultMember;
import me.cronkhinator.pinchfist.objects.Infraction;
import me.cronkhinator.pinchfist.objects.Messages;
import me.cronkhinator.pinchfist.objects.Settings;
import me.cronkhinator.pinchfist.util.MembersUtil;
import me.cronkhinator.pinchfist.util.PinchfistUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommandWarn implements Command {
    @NotNull @Override public String getName() { return "warn"; }
    @Override public boolean getStaffOnly() { return true; }
    @NotNull @Override public IntRange getArgsCount() { return new IntRange(1, Integer.MAX_VALUE); }

    @Override public void execute(@NotNull GuildMessageReceivedEvent event, @NotNull List<String> args, @NotNull Member member) {
        Member target = PinchfistUtil.getMentionedMember(event);

        if (target == null) {
            event.getChannel().sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue(); return;
        }

        CultMember targetMember = MembersUtil.getOrCreate(target.getIdLong());

        // If first argument is "remove," execute the removal code
        // TODO: Should be converted to a modular SubCommand system later on for sake of maintainability
        if(args.get(0).equals("remove")) {
            if(args.size() != 3) {
                event.getChannel().sendMessage(Messages.INVALID_ARGUMENTS.replace("{name}", target.getUser().getAsMention())).queue(); return;
            }
            // Try block catches an invalid integer from Integer#parseInt()
            try {
                int id = Integer.parseInt(args.get(2));
                // Filter the user's infractions based on whether their ID matches the given ID
                // Would be much cleaner if this code was Kotlin-ified with Iterable#firstOrNull()
                List<Infraction> matched = targetMember.getInfractions()
                        .stream()
                        .filter(it -> it.getId() == id)
                        .collect(Collectors.toList());

                // If no infractions matched the predicate, assume the given ID was invalid and return
                if(matched.size() == 0) {
                    event.getChannel().sendMessage(Messages.INVALID_ID).queue(); return;
                }

                // Remove the first infraction that matched; IDs should be unique, so this is safe so long as the code works as expected
                targetMember.getInfractions().remove(matched.get(0));

                // Reformat the existing infractions to have their ID reset based on their new index
                targetMember.getInfractions().forEach(it -> it.setId(targetMember.getInfractions().indexOf(it) + 1));

                event.getChannel().sendMessage("Done :ok_hand:").queue();
            } catch(NumberFormatException ignored) {
                // We weren't given a valid integer for the ID
                event.getChannel().sendMessage(Messages.INVALID_ID).queue();
            }
        } else {
            // Assume that we are adding an infraction, not removing one
            if (!target.hasPermission(Permission.ADMINISTRATOR)
                    && !(member.getRoles().contains(member.getGuild().getRoleById(Settings.getInquisitorRole()))
                    && target.getRoles().contains(member.getGuild().getRoleById(Settings.getInquisitorRole())))) {

                // Create timestamp and reason
                String time = Pinchfist.dateFormatter.format(LocalDateTime.now());
                String reason = args.size() > 1 ? PinchfistUtil.concatArgs(1, args) : "";

                // Add new infraction to user's infractions list
                targetMember.getInfractions().add(new Infraction(targetMember.getInfractions().size() + 1, time, reason));
                event.getChannel().sendMessage("Done :ok_hand:").queue();
            } else {
                event.getChannel().sendMessage(Messages.CANNOT_WARN_MODERATOR).queue();
            }
        }
    }
}
