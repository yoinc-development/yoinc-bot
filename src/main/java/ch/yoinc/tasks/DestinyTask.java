package ch.yoinc.tasks;

import ch.yoinc.models.InternalUser;
import ch.yoinc.models.bungie.CharacterResponse;
import ch.yoinc.models.bungie.ResponseData;
import ch.yoinc.services.BungieService;
import ch.yoinc.services.DataService;
import ch.yoinc.services.DiscordService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class DestinyTask implements ScheduledTask {

    private final String BUNGIE_URL = "https://www.bungie.net";

    @Override
    public void execute(JDA jda, Properties properties) {
        DataService dataService = new DataService(properties);
        BungieService bungieService = new BungieService(properties);
        DiscordService discordService = new DiscordService(properties);

        List<InternalUser> destinyInternalUsers = dataService.getAllDestinyUsers();
        for (InternalUser user : destinyInternalUsers) {
            ResponseData responseData = bungieService.getProfile(user.bungieID);
            if (responseData != null) {
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
                        Objects.requireNonNull(jda.getTextChannelById(properties.getProperty("discord.destinychannel"))).sendMessageEmbeds(embedBuilder.build()).queue();
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
