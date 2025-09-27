package ch.yoinc.listeners;

import ch.yoinc.commands.ChameleonCommand;
import ch.yoinc.commands.StreetsCommand;
import ch.yoinc.services.DiscordService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Properties;

public class GameListener extends ListenerAdapter {

    private ChameleonCommand chameleonCommand;
    private StreetsCommand streetsCommand;
    private final DiscordService discordService;

    public GameListener(Properties properties) {
        discordService = new DiscordService(properties);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannel().getType().equals(ChannelType.GUILD_PRIVATE_THREAD)) {
            if (chameleonCommand != null && chameleonCommand.running && event.getChannel().getId().equals(chameleonCommand.global_voteMessageID)) {
                chameleonCommand.messageReceived(event);
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() != null && !event.getUser().isBot()) {
            if (chameleonCommand != null && chameleonCommand.running && event.getMessageId().equals(chameleonCommand.global_voteMessageID)) {
                chameleonCommand.vote(event);
            } else if (streetsCommand != null && streetsCommand.isReactingCorrectly(event.getUser().getId(), event.getChannel().getId(), event.getReaction().getEmoji().asUnicode().getAsCodepoints().toLowerCase())) {
                streetsCommand.gamble(event, event.getChannel().getId());
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Channel eventChannel = event.getChannel();
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (eventChannel.getType().equals(ChannelType.PRIVATE)) {
            //no slash commands allowed in private channels
        } else if (eventChannel.getType().equals(ChannelType.TEXT)) {
            if ("chameleon".equals(event.getName())) {
                if (chameleonCommand == null) {
                    if (discordService.isUserInVoiceChannel(member, Objects.requireNonNull(guild).getVoiceChannels())) {
                        chameleonCommand = new ChameleonCommand(discordService);
                        chameleonCommand.startCommand(event);
                    } else {
                        event.reply("You need to be in a voice channel to use this command").setEphemeral(true).queue();
                    }
                } else {
                    if (!chameleonCommand.running) {
                        chameleonCommand.startCommand(event);
                    } else {
                        event.reply("A game of Chameleon is already in progress in a channel").setEphemeral(true).queue();
                    }
                }
            } else if ("streets".equals(event.getName())) {
                if (streetsCommand == null) {
                    streetsCommand = new StreetsCommand();
                    streetsCommand.startCommand(event);
                } else {
                    if (streetsCommand.isUserInSession(Objects.requireNonNull(member).getUser().getId(), null)) {
                        event.reply("A game of Streets is already in progress").setEphemeral(true).queue();
                    } else {
                        streetsCommand.startCommand(event);
                    }
                }
            }
        }
    }
}
