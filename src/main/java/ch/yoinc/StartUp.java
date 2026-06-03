package ch.yoinc;

import ch.yoinc.dekarios.Dekarios;
import ch.yoinc.listeners.YoincBotListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class StartUp {

    private static final Logger logger = LogManager.getLogger(StartUp.class);

    public static void main(String[] args) {
        try {

            logger.info("StartUp (main) - Application started at {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss")));

            InputStream inputStream = StartUp.class.getClassLoader().getResourceAsStream("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            JDA jda = JDABuilder.createDefault(properties.getProperty("discord.key"))
                    .addEventListeners(new YoincBotListener(properties))
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)
                    .build();

            jda.getPresence().setActivity(Activity.playing("YOINC.ch"));
            jda.updateCommands().addCommands(
                    Commands.context(Command.Type.MESSAGE, "Move message to another channel."),
                    Commands.context(Command.Type.USER, "Turn user into idyet."),
                    Commands.context(Command.Type.USER, "Turn user into Bag."),
                    Commands.context(Command.Type.USER, "Turn user into Tier 1 Bag."),
                    Commands.context(Command.Type.USER, "Turn user into npc.")
            ).queue();

            jda.awaitReady();

            Dekarios.registerListener(jda, properties.getProperty("dekarios.api"));

        } catch (InterruptedException | IOException ex) {
            logger.error(ex);
        }
    }
}
