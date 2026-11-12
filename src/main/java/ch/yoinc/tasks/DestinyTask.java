package ch.yoinc.tasks;

import ch.yoinc.models.InternalUser;
import ch.yoinc.models.bungie.CharacterResponse;
import ch.yoinc.models.bungie.ResponseData;
import ch.yoinc.services.BungieService;
import ch.yoinc.services.DataService;
import ch.yoinc.services.DiscordService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class DestinyTask implements ScheduledTask {

    private final Logger logger = LogManager.getLogger(DestinyTask.class);

    private final String BUNGIE_URL = "https://www.bungie.net";

    @Override
    public void execute(JDA jda, Properties properties) {
        DataService dataService = new DataService();
        BungieService bungieService = new BungieService();
        DiscordService discordService = new DiscordService();

        List<InternalUser> destinyInternalUsers = dataService.getAllDestinyUsers();
        for (InternalUser user : destinyInternalUsers) {
            ResponseData responseData = bungieService.getProfile(user.bungieID);
            if (responseData != null) {
                // prevent light levels to be read when the player is in an activity that increases their light, like a dungeon/raid etc
                if (!bungieService.isSafeToCheckLight(responseData)) {
                    logger.info("Skipping light check for user {}: player is in an activity", user.userID);
                    continue;
                }
                Map<String, CharacterResponse> characterResponseList = responseData.Response.characters.data;
                for (Map.Entry<String, CharacterResponse> characterResponse : characterResponseList.entrySet()) {
                    boolean isLightHigher = bungieService.isLightHigher(user.userID, characterResponse.getValue());
                    if (isLightHigher) {
                        String description = switch (responseData.Response.characters.data.get(characterResponse.getKey()).classType) {
                            case 0 ->
                                    "Their Titan has a new power level: " + responseData.Response.characters.data.get(characterResponse.getKey()).light;
                            case 1 ->
                                    "Their Hunter has a new power level: " + responseData.Response.characters.data.get(characterResponse.getKey()).light;
                            case 2 ->
                                    "Their Warlock has a new power level: " + responseData.Response.characters.data.get(characterResponse.getKey()).light;
                            default ->
                                    "Their character has a new power level: " + responseData.Response.characters.data.get(characterResponse.getKey()).light;
                        };
                        EmbedBuilder embedBuilder = discordService.createEmbedBuilder(
                                "New light level for " + responseData.Response.profile.data.userInfo.displayName,
                                description,
                                BUNGIE_URL + responseData.Response.characters.data.get(characterResponse.getKey()).emblemPath,
                                "Updated at " + responseData.Response.profile.data.dateLastPlayed
                        );
                        Objects.requireNonNull(jda.getTextChannelById(properties.getProperty("discord.channel.destiny"))).sendMessageEmbeds(embedBuilder.build()).queue();
                    }
                }
            }
        }
    }

    @Override
    public String getTaskName() {
        return "DestinyTask";
    }
}
