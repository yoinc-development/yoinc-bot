package ch.yoinc.services;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

public class DiscordService extends BaseService {

    public DiscordService(Properties properties) {
        super(properties);
    }

    /**
     * Cleans a message if it contains a link to X.com or Twitter.com.
     *
     * @param event the message received event
     */
    public void cleanMessage(MessageReceivedEvent event) {
        if (isHumanInValidPermissionChannel(event)) {
            String messageContent = event.getMessage().getContentRaw();
            String newMessageContent;
            String regex = "(https://)(www\\.)?(x\\.com|twitter\\.com)";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(messageContent).find()) {
                newMessageContent = messageContent.replaceAll(regex, properties.getProperty("xcancel.url"));
                event.getMessage().delete().queue();
                event.getMessage().reply(newMessageContent).queue();
            }
        }
    }

    /**
     * Checks if a user is in a voice channel.
     *
     * @param member        the member to check
     * @param voiceChannels the possible voice channels
     * @return true if the member is in a voice channel, false otherwise
     */
    public boolean isUserInVoiceChannel(Member member, List<VoiceChannel> voiceChannels) {
        VoiceChannel voiceChannel = voiceChannels.stream()
                .filter(vc -> vc.getMembers().contains(member))
                .findFirst()
                .orElse(null);

        return voiceChannel != null;
    }

    /**
     * Gets the current voice channel of a member.
     *
     * @param member        the member to check
     * @param voiceChannels the possible voice channels
     * @return the current voice channel of the member, null if the member is not in a voice channel
     */
    public VoiceChannel getCurrentVoiceChannel(Member member, List<VoiceChannel> voiceChannels) {
        return voiceChannels.stream()
                .filter(vc -> vc.getMembers().contains(member))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all active members in a voice channel.
     *
     * @param voiceChannel the current voice channel
     * @return a list of active members in the voice channel
     */
    public List<Member> getAllActiveVoiceChannelMembers(VoiceChannel voiceChannel) {
        return voiceChannel.getMembers().stream()
                .filter(m -> m.getVoiceState() != null && !m.getVoiceState().isDeafened() && !m.getVoiceState().isSelfDeafened() && !m.getVoiceState().isMuted() && !m.getVoiceState().isSelfMuted())
                .toList();
    }

    /**
     * Checks if a thread is active.
     *
     * @param event  the message received event
     * @param member the member to check
     * @param guild  the guild containing the thread
     * @return true if the thread is active, false otherwise
     */
    public boolean isThreadActive(MessageReceivedEvent event, Member member, Guild guild) {
        //channel type is a thread
        if (event.getChannel().getType().equals(ChannelType.GUILD_PUBLIC_THREAD) || event.getChannel().getType().equals(ChannelType.GUILD_PRIVATE_THREAD)) {
            //thread is not locked or closed
            if (!event.getChannel().asThreadChannel().isLocked() && !event.getChannel().asThreadChannel().isArchived()) {
                //the thread owner is the bot
                if (Objects.requireNonNull(event.getChannel().asThreadChannel().getOwner()).getId().equals(event.getJDA().getSelfUser().getId())) {
                    //the author of the message is not the bot and is currently in a voice channel
                    return !event.getAuthor().isBot() && isUserInVoiceChannel(member, guild.getVoiceChannels());
                }
            }
        }
        return false;
    }

    /**
     * Creates an embed builder with the given title, description, image URL, and footer.
     *
     * @param title       the title of the embed
     * @param description the description of the embed
     * @param imageUrl    the image URL of the embed
     * @param footer      the footer of the embed
     * @return the created embed builder
     */
    public EmbedBuilder createEmbedBuilder(String title, String description, String imageUrl, String footer) {
        return new YoincEmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setImage(imageUrl)
                .setFooter(footer);
    }

    /**
     * Checks if the event is from a human and from a valid channel.
     *
     * @param event the message received event
     * @return true if the event is from a human and from a valid channel, false otherwise
     */
    private boolean isHumanInValidPermissionChannel(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            return event.getChannel().getType().equals(ChannelType.GUILD_PUBLIC_THREAD) ||
                    event.getChannel().getType().equals(ChannelType.GUILD_PRIVATE_THREAD) ||
                    event.getChannel().getType().equals(ChannelType.TEXT);
        }
        return false;
    }

    public static class YoincEmbedBuilder extends EmbedBuilder {
        public YoincEmbedBuilder() {
            super();
            this.setAuthor("Powered by YOINC.", "https://www.yoinc.ch");
        }
    }
}
