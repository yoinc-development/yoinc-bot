package ch.yoinc.tasks;

import net.dv8tion.jda.api.JDA;

import java.util.Calendar;
import java.util.Properties;

public interface ScheduledTask {

    /**
     * Execute the scheduled task.
     *
     * @param jda        The JDA instance for Discord operations
     * @param properties Application properties
     */
    void execute(JDA jda, Properties properties);

    /**
     * Get the name of this task for logging purposes.
     *
     * @return The task name
     */
    String getTaskName();

    /**
     * Get the interval in milliseconds for this task.
     * Default is 1 hour (3600000ms).
     *
     * @return The interval in milliseconds
     */
    default long getIntervalMs() {
        return 60 * 60 * 1000L; // 1 hour
    }

    /**
     * Get the initial delay in milliseconds before first execution.
     * Default waits until next full hour.
     *
     * @return The initial delay in milliseconds
     */
    default long getInitialDelayMs() {
        Calendar now = Calendar.getInstance();
        Calendar todayNextHour = Calendar.getInstance();

        todayNextHour.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) + 1);
        todayNextHour.set(Calendar.MINUTE, 0);
        todayNextHour.set(Calendar.SECOND, 0);
        todayNextHour.set(Calendar.MILLISECOND, 0);

        return todayNextHour.getTimeInMillis() - now.getTimeInMillis();
    }
}