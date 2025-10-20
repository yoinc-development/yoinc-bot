package ch.yoinc.services;

import ch.yoinc.models.bungie.CharacterResponse;
import ch.yoinc.models.bungie.ResponseData;

import java.util.Properties;

public class BungieService extends BaseService {
    public BungieService(Properties properties) {
        super(properties);
    }

    /**
     * Checks if a user's light level is higher than the current light level.
     *
     * @param userID the user's internal ID
     * @param characterResponse the user's characterResponse object
     * @return true if the user's light level is higher than the current light level, false otherwise
     */
    public boolean isLightHigher(String userID, CharacterResponse characterResponse) {
        return connection.isLightHigher(userID, characterResponse.light, characterResponse.classType);
    }

    /**
     * Gets the user's profile from Bungie.
     *
     * @param bungieID the user's Bungie ID
     * @return ResponseData object containing the user's profile
     */
    public ResponseData getProfile(String bungieID) {
        ResponseData responseData = connection.getProfile(bungieID);
        if (responseData != null) {
            if ("Success".equals(responseData.ErrorStatus)) {
                if (responseData.Response != null) {
                    if (responseData.Response.characters != null) {
                        return responseData;
                    }
                }
            }
        }
        return null;
    }
}
