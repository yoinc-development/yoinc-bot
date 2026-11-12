package ch.yoinc.services;

import ch.yoinc.models.bungie.CharacterActivitiesResponse;
import ch.yoinc.models.bungie.CharacterResponse;
import ch.yoinc.models.bungie.ResponseData;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Comparator;

public class BungieService extends BaseService {

    /**
     * This is the Orbit.
     */
    private static final long ORBIT_ACTIVITY_HASH = 82913930L;

    public BungieService() {}

    /**
     * Checks if the light levels in a profile can be trusted.
     * While a player is inside an activity, Bungie reports inflated light levels for all characters.
     * Offline players and players in orbit are safe to check.
     *
     * @param responseData the user's profile response (needs components 204 and 1000)
     * @return true if the player is offline or in orbit, false if in an activity or if it cannot be determined
     */
    public boolean isSafeToCheckLight(ResponseData responseData) {
        if (responseData == null || responseData.Response == null) {
            return false;
        }

        if (responseData.Response.profileTransitoryData == null
                || responseData.Response.profileTransitoryData.data == null) {
            return true;
        }

        if (responseData.Response.characterActivities == null
                || responseData.Response.characterActivities.data == null) {
            return false;
        }

        return responseData.Response.characterActivities.data.values().stream()
                .filter(activity -> parseStart(activity) != null)
                .max(Comparator.comparing(BungieService::parseStart))
                .map(activity -> activity.currentActivityHash == ORBIT_ACTIVITY_HASH)
                .orElse(false);
    }

    private static Instant parseStart(CharacterActivitiesResponse activity) {
        if (activity == null || activity.dateActivityStarted == null) {
            return null;
        }
        try {
            return Instant.parse(activity.dateActivityStarted);
        } catch (DateTimeParseException ex) {
            return null;
        }
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
