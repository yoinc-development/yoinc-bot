package ch.yoinc.tasks;

import ch.yoinc.models.InternalUser;
import ch.yoinc.models.bungie.CharacterResponse;
import ch.yoinc.models.bungie.ResponseData;
import ch.yoinc.services.BungieService;
import ch.yoinc.services.DataService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class DestinyTask implements ScheduledTask {

    @Override
    public void execute(JDA jda, Properties properties) {
        DataService dataService = new DataService(properties);
        BungieService bungieService = new BungieService(properties);

        List<InternalUser> destinyInternalUsers = dataService.getAllDestinyUsers();
        for (InternalUser user : destinyInternalUsers) {
            ResponseData responseData = bungieService.getProfile(user.bungieID);
            if (responseData != null) {
                Map<String, CharacterResponse> characterResponseList = responseData.Response.characters.data;
                for (Map.Entry<String, CharacterResponse> characterResponse : characterResponseList.entrySet()) {
                    boolean isLightHigher = bungieService.isLightHigher(user.userID, characterResponse.getValue());
                    if (isLightHigher) {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("New light level for " + responseData.Response.profile.data.userInfo.displayName);
                        embedBuilder.setAuthor("Powered by YOINC.", "https://www.yoinc.ch");
                        embedBuilder.setImage(properties.getProperty("bungie.url") + responseData.Response.characters.data.get(characterResponse.getKey()).emblemPath);
                        embedBuilder.setDescription("Light level: " + responseData.Response.characters.data.get(characterResponse.getKey()).light);
                        embedBuilder.setFooter("Updated at " + responseData.Response.profile.data.dateLastPlayed);
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
