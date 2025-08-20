package ch.yoinc.services;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SchedulerService {

    JDA jda;

    Properties properties;

    public void scheduleAllTasks(JDA jda, Properties properties) {
        this.jda = jda;
        this.properties = properties;

        long taskDelay = getTaskDelay();
        System.out.println("[YoincBot - SchedulerService (scheduleAllTasks) - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] Delay until the next task: " + (taskDelay / 1000 / 60 / 60) + " hours.");

        Timer timer = new Timer("Discord Service Tasks");
        timer.schedule(cleanupTask, 0, (60 * 60 * 1000L));
    }

    /**
     * Calculates the amount of time for the task to wait until first execution.
     * This task will wait until the next full hour.
     *
     * @return the calculated time in ms
     */
    private static long getTaskDelay() {
        Calendar now = Calendar.getInstance();
        Calendar todayNextHour = Calendar.getInstance();

        todayNextHour.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) + 1);
        todayNextHour.set(Calendar.MINUTE, 0);
        todayNextHour.set(Calendar.SECOND, 0);
        todayNextHour.set(Calendar.MILLISECOND, 0);

        return todayNextHour.getTimeInMillis() - now.getTimeInMillis();
    }

    // -------------------- Task Definitions --------------------
    private final TimerTask cleanupTask = new TimerTask() {
        @Override
        public void run() {
            runCleanupTask();
        }
    };

    // -------------------- Run Tasks --------------------
    protected void runCleanupTask() {
        for (Guild guild : jda.getGuilds()) {
            List<ThreadChannel> threadChannels = guild.getThreadChannels().stream()
                    .filter(ThreadChannel::isLocked)
                    .filter(threadChannel -> threadChannel.getOwner().getId().equals(properties.getProperty("discord.bot")))
                    .toList();

            threadChannels.forEach(threadChannel -> {
                threadChannel.delete().queue();
            });
        }
    }
}
