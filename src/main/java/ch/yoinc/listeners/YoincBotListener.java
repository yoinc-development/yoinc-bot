package ch.yoinc.listeners;

import ch.yoinc.DiscordService;
import ch.yoinc.commands.ChameleonCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class YoincBotListener extends ListenerAdapter {

    //commands
    private ChameleonCommand chameleonCommand;

    //services
    public DiscordService discordService;

    public YoincBotListener() {
        discordService = new DiscordService();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannel().getType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannel().getType().equals(ChannelType.GUILD_PRIVATE_THREAD)) {
            if (chameleonCommand != null && chameleonCommand.running && event.getChannel().getId().equals(chameleonCommand.global_voteMessageID)) {
                chameleonCommand.messageReceived(event);
            }
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            if (chameleonCommand != null && chameleonCommand.running && event.getMessageId().equals(chameleonCommand.global_voteMessageID)) {
                chameleonCommand.vote(event);
            }
        }
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Channel eventChannel = event.getChannel();
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (eventChannel.getType().equals(ChannelType.PRIVATE)) {
            //no slash commands allowed in private channels
        } else if (eventChannel.getType().equals(ChannelType.TEXT)) {
            if ("chameleon".equals(event.getName())) {
                if (chameleonCommand == null) {
                    if (discordService.isUserInVoiceChannel(member, guild.getVoiceChannels())) {
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
            }
        }
    }
}
