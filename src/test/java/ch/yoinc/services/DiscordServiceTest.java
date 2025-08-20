package ch.yoinc.services;

import ch.yoinc.StartUp;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscordServiceTest {

    DiscordService discordService;

    Properties properties;

    @BeforeEach
    public void setup() {
        properties = new Properties();
        try {
            InputStream inputStream = StartUp.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(inputStream);
        } catch (Exception e) {
            System.out.println("Properties could not be loaded: " + e.getMessage());
        }
        discordService = new DiscordService(properties);
    }

    @Test
    public void testCleanMessageSuccess() {

        MessageReceivedEvent event1 = createMockEvent("https://x.com/test", false, ChannelType.TEXT);
        discordService.cleanMessage(event1);
        verify(event1.getMessage()).delete();
        verify(event1.getMessage()).reply("https://www.xcancel.com/test");

        MessageReceivedEvent event2 = createMockEvent("https://www.x.com/example", false, ChannelType.TEXT);
        discordService.cleanMessage(event2);
        verify(event2.getMessage()).delete();
        verify(event2.getMessage()).reply("https://www.xcancel.com/example");

        MessageReceivedEvent event3 = createMockEvent("https://twitter.com/user/status", false, ChannelType.TEXT);
        discordService.cleanMessage(event3);
        verify(event3.getMessage()).delete();
        verify(event3.getMessage()).reply("https://www.xcancel.com/user/status");

        MessageReceivedEvent event4 = createMockEvent("https://www.twitter.com/post", false, ChannelType.TEXT);
        discordService.cleanMessage(event4);
        verify(event4.getMessage()).delete();
        verify(event4.getMessage()).reply("https://www.xcancel.com/post");
    }

    @Test
    public void testCleanMessageNoAction() {
        MessageReceivedEvent event1 = createMockEvent("Just a regular message", false, ChannelType.TEXT);
        discordService.cleanMessage(event1);
        verify(event1.getMessage(), never()).delete();
        verify(event1.getMessage(), never()).reply(anyString());

        MessageReceivedEvent event2 = createMockEvent("https://x.com/test", true, ChannelType.TEXT);
        discordService.cleanMessage(event2);
        verify(event2.getMessage(), never()).delete();
        verify(event2.getMessage(), never()).reply(anyString());

        MessageReceivedEvent event3 = createMockEvent("https://x.com/test", false, ChannelType.VOICE);
        discordService.cleanMessage(event3);
        verify(event3.getMessage(), never()).delete();
        verify(event3.getMessage(), never()).reply(anyString());

        MessageReceivedEvent event4 = createMockEvent("https://github.com/user/repo", false, ChannelType.TEXT);
        discordService.cleanMessage(event4);
        verify(event4.getMessage(), never()).delete();
        verify(event4.getMessage(), never()).reply(anyString());
    }

    @Test
    public void testCleanMessageMultipleUrls() {
        String originalMessage = "Check these: https://x.com/first and https://twitter.com/second";
        String expectedMessage = "Check these: https://www.xcancel.com/first and https://www.xcancel.com/second";
        
        MessageReceivedEvent event = createMockEvent(originalMessage, false, ChannelType.TEXT);
        discordService.cleanMessage(event);
        verify(event.getMessage()).delete();
        verify(event.getMessage()).reply(expectedMessage);
    }

    @Test
    public void testCleanMessageInThreadChannels() {
        MessageReceivedEvent event1 = createMockEvent("https://x.com/test", false, ChannelType.GUILD_PUBLIC_THREAD);
        discordService.cleanMessage(event1);
        verify(event1.getMessage()).delete();
        verify(event1.getMessage()).reply("https://www.xcancel.com/test");

        MessageReceivedEvent event2 = createMockEvent("https://twitter.com/test", false, ChannelType.GUILD_PRIVATE_THREAD);
        discordService.cleanMessage(event2);
        verify(event2.getMessage()).delete();
        verify(event2.getMessage()).reply("https://www.xcancel.com/test");
    }

    @Test
    public void testIsThreadActiveSuccess() {
        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        when(event.getChannel()).thenReturn(mock(MessageChannelUnion.class));
        when(event.getChannel().getType()).thenReturn(ChannelType.GUILD_PUBLIC_THREAD);
        when(event.getChannel().asThreadChannel()).thenReturn(mock(ThreadChannel.class));
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().getOwner()).thenReturn(mock(Member.class));
        when(event.getChannel().asThreadChannel().getOwner().getId()).thenReturn(properties.getProperty("discord.bot"));
        when(event.getAuthor()).thenReturn(mock(User.class));
        when(event.getAuthor().isBot()).thenReturn(false);

        Member member = mock(Member.class);

        //helper
        VoiceChannel voiceChannel = mock(VoiceChannel.class);
        when(voiceChannel.getMembers()).thenReturn(List.of(member));

        Guild guild = mock(Guild.class);
        when(guild.getVoiceChannels()).thenReturn(List.of(voiceChannel));

        assertTrue(discordService.isThreadActive(event, member, guild));
    }

    @Test
    public void testIsThreadActiveNotInVC() {
        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        when(event.getChannel()).thenReturn(mock(MessageChannelUnion.class));
        when(event.getChannel().getType()).thenReturn(ChannelType.GUILD_PUBLIC_THREAD);
        when(event.getChannel().asThreadChannel()).thenReturn(mock(ThreadChannel.class));
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().getOwner()).thenReturn(mock(Member.class));
        when(event.getChannel().asThreadChannel().getOwner().getId()).thenReturn(properties.getProperty("discord.bot"));
        when(event.getAuthor()).thenReturn(mock(User.class));
        when(event.getAuthor().isBot()).thenReturn(false);

        Member member = mock(Member.class);

        //helper
        VoiceChannel voiceChannel = mock(VoiceChannel.class);

        Guild guild = mock(Guild.class);
        when(guild.getVoiceChannels()).thenReturn(List.of(voiceChannel));

        assertFalse(discordService.isThreadActive(event, member, guild));
    }

    @Test
    public void testIsThreadActiveByBot() {
        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        when(event.getChannel()).thenReturn(mock(MessageChannelUnion.class));
        when(event.getChannel().getType()).thenReturn(ChannelType.GUILD_PUBLIC_THREAD);
        when(event.getChannel().asThreadChannel()).thenReturn(mock(ThreadChannel.class));
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().getOwner()).thenReturn(mock(Member.class));
        when(event.getChannel().asThreadChannel().getOwner().getId()).thenReturn(properties.getProperty("discord.bot"));
        when(event.getAuthor()).thenReturn(mock(User.class));
        when(event.getAuthor().isBot()).thenReturn(true);

        Member member = mock(Member.class);

        //helper
        VoiceChannel voiceChannel = mock(VoiceChannel.class);
        when(voiceChannel.getMembers()).thenReturn(List.of(member));

        Guild guild = mock(Guild.class);
        when(guild.getVoiceChannels()).thenReturn(List.of(voiceChannel));

        assertFalse(discordService.isThreadActive(event, member, guild));
    }

    @Test
    public void testAllActiveVoiceChannelMembersFilterOne() {
        Member memberActive = mock(Member.class);
        when(memberActive.getVoiceState()).thenReturn(mock(GuildVoiceState.class));
        when(memberActive.getVoiceState().isDeafened()).thenReturn(false);

        Member memberInactive = mock(Member.class);
        when(memberInactive.getVoiceState()).thenReturn(mock(GuildVoiceState.class));
        when(memberInactive.getVoiceState().isDeafened()).thenReturn(true);

        VoiceChannel voiceChannel = mock(VoiceChannel.class);
        when(voiceChannel.getMembers()).thenReturn(List.of(memberActive, memberInactive));

        assertEquals(List.of(memberActive), discordService.getAllActiveVoiceChannelMembers(voiceChannel));
    }

    @Test
    public void testAllActiveVoiceChannelMembersFilterAll() {
        Member memberActive = mock(Member.class);
        when(memberActive.getVoiceState()).thenReturn(mock(GuildVoiceState.class));
        when(memberActive.getVoiceState().isDeafened()).thenReturn(true);

        Member memberInactive = mock(Member.class);
        when(memberInactive.getVoiceState()).thenReturn(mock(GuildVoiceState.class));
        when(memberInactive.getVoiceState().isDeafened()).thenReturn(true);

        VoiceChannel voiceChannel = mock(VoiceChannel.class);
        when(voiceChannel.getMembers()).thenReturn(List.of(memberActive, memberInactive));

        assertEquals(0, discordService.getAllActiveVoiceChannelMembers(voiceChannel).size());
    }

    @Test
    public void testGetCurrentVoiceChannel() {
        Member member = mock(Member.class);

        VoiceChannel voiceChannelOne = mock(VoiceChannel.class);
        when(voiceChannelOne.getMembers()).thenReturn(List.of(member));

        VoiceChannel voiceChannelTwo = mock(VoiceChannel.class);
        when(voiceChannelTwo.getMembers()).thenReturn(new ArrayList<Member>());

        VoiceChannel voiceChannelThree = mock(VoiceChannel.class);
        when(voiceChannelThree.getMembers()).thenReturn(new ArrayList<Member>());

        List<VoiceChannel> voiceChannels = List.of(voiceChannelOne, voiceChannelTwo, voiceChannelThree);

        assertEquals(voiceChannelOne, discordService.getCurrentVoiceChannel(member, voiceChannels));
    }


    private MessageReceivedEvent createMockEvent(String messageContent, boolean isBot, ChannelType channelType) {
        MessageReceivedEvent event = mock(MessageReceivedEvent.class);
        Message message = mock(Message.class);
        User author = mock(User.class);
        MessageChannelUnion channel = mock(MessageChannelUnion.class);

        when(message.getContentRaw()).thenReturn(messageContent);
        when(event.getMessage()).thenReturn(message);
        when(author.isBot()).thenReturn(isBot);
        when(event.getAuthor()).thenReturn(author);
        when(channel.getType()).thenReturn(channelType);
        when(event.getChannel()).thenReturn(channel);

        // Setup message operations (delete and reply return mocked RestActions)
        when(message.delete()).thenReturn(mock(AuditableRestAction.class));
        when(message.reply(anyString())).thenReturn(mock(MessageCreateAction.class));

        return event;
    }
}
