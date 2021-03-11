package me.cronkhinator.pinchfist.command;

import kotlin.ranges.IntRange;
import me.cronkhinator.pinchfist.objects.CultMember;
import me.cronkhinator.pinchfist.objects.Infraction;
import me.cronkhinator.pinchfist.objects.Messages;
import me.cronkhinator.pinchfist.objects.Settings;
import me.cronkhinator.pinchfist.util.MembersUtil;
import me.cronkhinator.pinchfist.util.PinchfistUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandInfractions implements Command {
    @NotNull @Override public String getName() { return "infractions"; }
    @Override public boolean getStaffOnly() { return true; }
    @NotNull @Override public IntRange getArgsCount() { return new IntRange(1, 1); }

    @Override public void execute(@NotNull GuildMessageReceivedEvent event, @NotNull List<String> args, @NotNull Member member) {
        Member discordTarget = PinchfistUtil.getMentionedMember(event);
        if (discordTarget == null) {
            event.getChannel().sendMessage(Messages.INVALID_ARGUMENT_TYPE).queue();
            return;
        }

        CultMember target = MembersUtil.getOrCreate(discordTarget.getIdLong());
        List<Infraction> infractions = target.getInfractions();

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setThumbnail(Settings.getThumbnailImage())
                .setAuthor("Infractions for: " + discordTarget.getUser().getName(), null, discordTarget.getUser().getAvatarUrl())
                .setDescription("This user has received a total of " + infractions.size() + " infraction" + (infractions.size() == 1 ? "" : "s") + ".");

        for (Infraction infraction : infractions) {
            String reason = infraction.getReason().isEmpty() ? "No reason provided." : infraction.getReason();
            embedBuilder.addField("#" + infraction.getId() + " | " + infraction.getTimestamp(), infraction.getReason(), false);
        }

        event.getChannel().sendMessage(embedBuilder.build()).queue();
    }
}