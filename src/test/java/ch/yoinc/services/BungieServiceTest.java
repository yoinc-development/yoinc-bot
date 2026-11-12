package ch.yoinc.services;

import ch.yoinc.models.bungie.ResponseData;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BungieServiceTest {

    private static final long ORBIT = 82913930L;
    private static final long DUNGEON = 1077850348L;
    private static final long PATROL = 359399753L;

    BungieService bungieService;

    @BeforeEach
    public void setup() {
        bungieService = new BungieService();
    }

    @Test
    public void testOfflineWithStaleActivityIsSafe() {
        ResponseData data = parse(transitoryOffline(), activities(
                character("1", "2026-11-12T20:44:27Z", PATROL),
                character("2", "2023-12-05T12:51:05Z", 0)));
        assertTrue(bungieService.isSafeToCheckLight(data));
    }

    @Test
    public void testOnlineInOrbitIsSafe() {
        ResponseData data = parse(transitoryOnline(), activities(
                character("1", "2026-11-12T20:44:27Z", ORBIT),
                character("2", "2023-12-05T12:51:05Z", 0)));
        assertTrue(bungieService.isSafeToCheckLight(data));
    }

    @Test
    public void testOnlineInActivityIsNotSafe() {
        ResponseData data = parse(transitoryOnline(), activities(
                character("1", "2026-11-12T19:56:42Z", DUNGEON),
                character("2", "2023-12-05T12:51:05Z", 0)));
        assertFalse(bungieService.isSafeToCheckLight(data));
    }

    @Test
    public void testUsesLatestStartedCharacter() {
        // the older character still shows orbit, but the newest one is inside a dungeon
        ResponseData data = parse(transitoryOnline(), activities(
                character("1", "2026-11-12T19:56:42Z", DUNGEON),
                character("2", "2026-11-11T10:00:00Z", ORBIT)));
        assertFalse(bungieService.isSafeToCheckLight(data));
    }

    @Test
    public void testOnlineWithoutActivitiesIsNotSafe() {
        ResponseData data = parse(transitoryOnline(), "");
        assertFalse(bungieService.isSafeToCheckLight(data));
    }

    @Test
    public void testMissingResponseIsNotSafe() {
        assertFalse(bungieService.isSafeToCheckLight(null));
        assertFalse(bungieService.isSafeToCheckLight(new ResponseData()));
    }

    private ResponseData parse(String transitory, String activities) {
        String json = "{\"ErrorStatus\":\"Success\",\"Response\":{" + transitory
                + (activities.isEmpty() ? "" : "," + activities) + "}}";
        return new Gson().fromJson(json, ResponseData.class);
    }

    private String transitoryOffline() {
        return "\"profileTransitoryData\":{\"privacy\":1}";
    }

    private String transitoryOnline() {
        return "\"profileTransitoryData\":{\"data\":{\"partyMembers\":[],\"currentActivity\":{}},\"privacy\":1}";
    }

    private String activities(String... characters) {
        return "\"characterActivities\":{\"data\":{" + String.join(",", characters) + "}}";
    }

    private String character(String id, String started, long hash) {
        return "\"" + id + "\":{\"dateActivityStarted\":\"" + started + "\",\"currentActivityHash\":" + hash
                + ",\"availableActivities\":[{\"activityHash\":1}]}";
    }
}
