package ch.yoinc;


import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
    public void testIsThreadActiveSuccess() {
        MessageReceivedEvent event = Mockito.mock(MessageReceivedEvent.class);
        when(event.getChannel()).thenReturn(Mockito.mock(MessageChannelUnion.class));
        when(event.getChannel().getType()).thenReturn(ChannelType.GUILD_PUBLIC_THREAD);
        when(event.getChannel().asThreadChannel()).thenReturn(Mockito.mock(ThreadChannel.class));
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().getOwner()).thenReturn(Mockito.mock(Member.class));
        when(event.getChannel().asThreadChannel().getOwner().getId()).thenReturn(properties.getProperty("discord.bot"));
        when(event.getAuthor()).thenReturn(Mockito.mock(User.class));
        when(event.getAuthor().isBot()).thenReturn(false);

        Member member = Mockito.mock(Member.class);

        //helper
        VoiceChannel voiceChannel = Mockito.mock(VoiceChannel.class);
        when(voiceChannel.getMembers()).thenReturn(List.of(member));

        Guild guild = Mockito.mock(Guild.class);
        when(guild.getVoiceChannels()).thenReturn(List.of(voiceChannel));

        assertTrue(discordService.isThreadActive(event, member, guild));
    }

    @Test
    public void testIsThreadActiveNotInVC() {
        MessageReceivedEvent event = Mockito.mock(MessageReceivedEvent.class);
        when(event.getChannel()).thenReturn(Mockito.mock(MessageChannelUnion.class));
        when(event.getChannel().getType()).thenReturn(ChannelType.GUILD_PUBLIC_THREAD);
        when(event.getChannel().asThreadChannel()).thenReturn(Mockito.mock(ThreadChannel.class));
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().getOwner()).thenReturn(Mockito.mock(Member.class));
        when(event.getChannel().asThreadChannel().getOwner().getId()).thenReturn(properties.getProperty("discord.bot"));
        when(event.getAuthor()).thenReturn(Mockito.mock(User.class));
        when(event.getAuthor().isBot()).thenReturn(false);

        Member member = Mockito.mock(Member.class);

        //helper
        VoiceChannel voiceChannel = Mockito.mock(VoiceChannel.class);

        Guild guild = Mockito.mock(Guild.class);
        when(guild.getVoiceChannels()).thenReturn(List.of(voiceChannel));

        assertFalse(discordService.isThreadActive(event, member, guild));
    }

    @Test
    public void testIsThreadActiveByBot() {
        MessageReceivedEvent event = Mockito.mock(MessageReceivedEvent.class);
        when(event.getChannel()).thenReturn(Mockito.mock(MessageChannelUnion.class));
        when(event.getChannel().getType()).thenReturn(ChannelType.GUILD_PUBLIC_THREAD);
        when(event.getChannel().asThreadChannel()).thenReturn(Mockito.mock(ThreadChannel.class));
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().isLocked()).thenReturn(false);
        when(event.getChannel().asThreadChannel().getOwner()).thenReturn(Mockito.mock(Member.class));
        when(event.getChannel().asThreadChannel().getOwner().getId()).thenReturn(properties.getProperty("discord.bot"));
        when(event.getAuthor()).thenReturn(Mockito.mock(User.class));
        when(event.getAuthor().isBot()).thenReturn(true);

        Member member = Mockito.mock(Member.class);

        //helper
        VoiceChannel voiceChannel = Mockito.mock(VoiceChannel.class);
        when(voiceChannel.getMembers()).thenReturn(List.of(member));

        Guild guild = Mockito.mock(Guild.class);
        when(guild.getVoiceChannels()).thenReturn(List.of(voiceChannel));

        assertFalse(discordService.isThreadActive(event, member, guild));
    }

    @Test
    public void testAllActiveVoiceChannelMembersFilterOne() {
        Member memberActive = Mockito.mock(Member.class);
        when(memberActive.getVoiceState()).thenReturn(Mockito.mock(GuildVoiceState.class));
        when(memberActive.getVoiceState().isDeafened()).thenReturn(false);

        Member memberInactive = Mockito.mock(Member.class);
        when(memberInactive.getVoiceState()).thenReturn(Mockito.mock(GuildVoiceState.class));
        when(memberInactive.getVoiceState().isDeafened()).thenReturn(true);

        VoiceChannel voiceChannel = Mockito.mock(VoiceChannel.class);
        when(voiceChannel.getMembers()).thenReturn(List.of(memberActive, memberInactive));

        assertEquals(List.of(memberActive), discordService.getAllActiveVoiceChannelMembers(voiceChannel));
    }

    @Test
    public void testAllActiveVoiceChannelMembersFilterAll() {
        Member memberActive = Mockito.mock(Member.class);
        when(memberActive.getVoiceState()).thenReturn(Mockito.mock(GuildVoiceState.class));
        when(memberActive.getVoiceState().isDeafened()).thenReturn(true);

        Member memberInactive = Mockito.mock(Member.class);
        when(memberInactive.getVoiceState()).thenReturn(Mockito.mock(GuildVoiceState.class));
        when(memberInactive.getVoiceState().isDeafened()).thenReturn(true);

        VoiceChannel voiceChannel = Mockito.mock(VoiceChannel.class);
        when(voiceChannel.getMembers()).thenReturn(List.of(memberActive, memberInactive));

        assertEquals(0, discordService.getAllActiveVoiceChannelMembers(voiceChannel).size());
    }
}
