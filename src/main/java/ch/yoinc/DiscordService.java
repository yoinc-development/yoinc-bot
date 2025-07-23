package ch.yoinc;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Properties;

public class DiscordService {

    Properties properties;

    public DiscordService(Properties properties) {
        this.properties = properties;
    }

    public boolean isUserInVoiceChannel(Member member, List<VoiceChannel> voiceChannels) {
        VoiceChannel voiceChannel = voiceChannels.stream()
                .filter(vc -> vc.getMembers().contains(member))
                .findFirst()
                .orElse(null);

        if (voiceChannel == null) {
            return false;
        }
        return true;
    }

    public VoiceChannel getCurrentVoiceChannel(Member member, List<VoiceChannel> voiceChannels) {
        VoiceChannel voiceChannel = voiceChannels.stream()
                .filter(vc -> vc.getMembers().contains(member))
                .findFirst()
                .orElse(null);
        return voiceChannel;
    }

    public List<Member> getAllActiveVoiceChannelMembers(VoiceChannel voiceChannel) {
        return voiceChannel.getMembers().stream()
                .filter(m -> m.getVoiceState() != null && !m.getVoiceState().isDeafened() && !m.getVoiceState().isSelfDeafened() && !m.getVoiceState().isMuted() && !m.getVoiceState().isSelfMuted())
                .toList();
    }

    public boolean isThreadActive(MessageReceivedEvent event, Member member, Guild guild) {
        //channel type is a thread
        if (event.getChannel().getType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannel().getType().equals(ChannelType.GUILD_PRIVATE_THREAD)) {
            //thread is not locked or closed
            if (!event.getChannel().asThreadChannel().isLocked() && !event.getChannel().asThreadChannel().isArchived()) {
                //the thread owner is the bot
                if (event.getChannel().asThreadChannel().getOwner().getId().equals(properties.getProperty("discord.bot"))) {
                    //the author of the message is not the bot and is currently in a voice channel
                    if (!event.getAuthor().isBot() && isUserInVoiceChannel(member, guild.getVoiceChannels())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
