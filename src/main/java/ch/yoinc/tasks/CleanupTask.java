package ch.yoinc.tasks;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class CleanupTask implements ScheduledTask {
    @Override
    public void execute(JDA jda, Properties properties) {
        for (Guild guild : jda.getGuilds()) {
            List<ThreadChannel> threadChannels = guild.getThreadChannels().stream()
                    .filter(ThreadChannel::isLocked)
                    .filter(threadChannel -> Objects.requireNonNull(threadChannel.getOwner()).getId().equals(jda.getSelfUser().getId()))
                    .toList();

            threadChannels.forEach(threadChannel -> threadChannel.delete().queue());
        }
    }

    @Override
    public String getTaskName() {
        return "CleanupTask";
    }
}
