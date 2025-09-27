package ch.yoinc.tasks;

import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Manages and schedules all registered tasks.
 */
public class TaskScheduler {

    private final Logger logger = LogManager.getLogger(TaskScheduler.class);

    private final List<ScheduledTask> tasks;
    private JDA jda;
    private Properties properties;

    public TaskScheduler() {
        this.tasks = new ArrayList<>();

        registerTask(new CleanupTask());
        registerTask(new DestinyTask());
    }

    /**
     * Register a new task to be scheduled.
     *
     * @param task The task to register
     */
    public void registerTask(ScheduledTask task) {
        tasks.add(task);
        //add log message
    }

    /**
     * Start all registered tasks.
     *
     * @param jda        The JDA instance
     * @param properties Application properties
     */
    public void startAllTasks(JDA jda, Properties properties) {
        this.jda = jda;
        this.properties = properties;

        logger.info("Starting {} scheduled tasks.", tasks.size());

        for (ScheduledTask task : tasks) {
            startTask(task);
        }
    }

    /**
     * Start a specific task.
     *
     * @param task The task to start
     */
    private void startTask(ScheduledTask task) {
        Timer timer = new Timer("Task-" + task.getTaskName());

        long initialDelay = task.getInitialDelayMs();
        long interval = task.getIntervalMs();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    task.execute(jda, properties);
                } catch (Exception e) {
                    //error
                }
            }
        };

        timer.schedule(timerTask, initialDelay, interval);
    }
}