package ch.yoinc.listeners;

import ch.yoinc.services.DiscordService;
import ch.yoinc.tasks.TaskScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class YoincBotListener extends ListenerAdapter {

    public TaskScheduler taskScheduler;
    private final DiscordService discordService;
    private final Properties properties;

    public YoincBotListener(Properties properties) {
        this.properties = properties;
        discordService = new DiscordService();
        taskScheduler = new TaskScheduler();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        discordService.cleanMessage(event);
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if ("Move message to another channel.".equals(event.getName())) {
            if (discordService.isAllowedUserGroup(event)) {
                event.replyModal(discordService.createMoveMessageModal(event)).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().startsWith("move-message-modal")) {
            discordService.moveMessage(event);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        CompletableFuture.runAsync(() -> taskScheduler.startAllTasks(jda, properties));
    }
}
