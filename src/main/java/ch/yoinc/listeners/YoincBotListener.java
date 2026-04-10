package ch.yoinc.listeners;

import ch.yoinc.services.DiscordService;
import ch.yoinc.tasks.TaskScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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
            if (discordService.hasAllowedRole(event)) {
                event.replyModal(discordService.createMoveMessageModal(event)).queue();
            }
        }
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        if (discordService.hasAllowedRole(event)) {
            if ("Turn user into idyet.".equals(event.getName())) {
                discordService.giveRoleToUser(Objects.requireNonNull(event.getTargetMember()), properties.getProperty("discord.role.idyet"), false);
            }
            if ("Turn user into Bag.".equals(event.getName())) {
                discordService.giveRoleToUser(Objects.requireNonNull(event.getTargetMember()), properties.getProperty("discord.role.bag"), true);
            }
            if ("Turn user into Tier 1 Bag.".equals(event.getName())) {
                discordService.giveRoleToUser(Objects.requireNonNull(event.getTargetMember()), properties.getProperty("discord.role.tieronebag"), true);
            }
            if ("Turn user into npc.".equals(event.getName())) {
                discordService.giveRoleToUser(Objects.requireNonNull(event.getTargetMember()), properties.getProperty("discord.role.npc"), false);
            }
            event.reply(Emoji.fromUnicode("U+1F44D").getAsReactionCode()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().startsWith("move-message-modal")) {
            discordService.moveMessage(event);
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Member member = event.getMember();
        if (!member.getUser().isBot()) {
            discordService.giveRoleToUser(member, properties.getProperty("discord.role.npc"), false);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        CompletableFuture.runAsync(() -> taskScheduler.startAllTasks(jda, properties));
    }
}
