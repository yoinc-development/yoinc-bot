package ch.yoinc;

import ch.yoinc.listeners.YoincBotListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class StartUp {
    public static void main(String[] args) {
        try {

            System.out.println("[YoincBot - StartUp (main) - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] - Application Started");

            InputStream inputStream = StartUp.class.getClassLoader().getResourceAsStream("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            JDA jda = JDABuilder.createDefault(properties.getProperty("discord.api"))
                    .addEventListeners(new YoincBotListener())
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)
                    .build();

            jda.getPresence().setActivity(Activity.playing("YOINC.ch"));
            jda.updateCommands().addCommands(
                    Commands.slash("chameleon", "Start a game of Chameleon.")
            ).queue();

            jda.awaitReady();
        } catch (InterruptedException ex) {
            System.out.println("[YoincBot - StartUp (main) - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] InterruptedException thrown: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("[YoincBot - StartUp (main) - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")) + "] IOException thrown: " + ex.getMessage());
        }
    }
}