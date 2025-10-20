package ch.yoinc.http;

import ch.yoinc.models.InternalUser;
import ch.yoinc.models.bungie.ResponseData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Connection {

    private final Logger logger = LogManager.getLogger(Connection.class);

    public HttpClient client;
    public String bungieUrl;
    public String bungieKey;
    public String internalUrl;

    public Connection() {
        client = HttpClient.newHttpClient();
    }

    /**
     * Returns all Destiny users from the internal database.
     *
     * @return List of InternalUser objects
     */
    public List<InternalUser> getAllDestinyUsers() {
        HttpRequest request;
        request = HttpRequest.newBuilder()
                .uri(URI.create(internalUrl + "/users/discord/destiny"))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), new TypeToken<List<InternalUser>>() {
            }.getType());
        } catch (IOException | InterruptedException ex) {
            logger.error(ex);
        }
        return new ArrayList<>();
    }

    /**
     * Gets a user profile from Bungie.
     *
     * @param bungieID the user's Bungie ID
     * @return ResponseData object containing the user's profile
     */
    public ResponseData getProfile(String bungieID) {
        HttpRequest request;
        request = HttpRequest.newBuilder()
                .uri(URI.create(bungieUrl + "/Destiny2/3/Profile/" + bungieID + "/?components=100,200,205"))
                .header("X-API-Key", bungieKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), ResponseData.class);
        } catch (IOException | InterruptedException ex) {
            logger.error(ex);
        }
        return null;
    }

    /**
     * Checks if a user's light level is higher than the current light level.
     *
     * @param userID the user's internal ID
     * @param light the user's light level
     * @param charType the charType of the user
     * @return true if the user's light level is higher than the current light level, false otherwise
     */
    public boolean isLightHigher(String userID, int light, int charType) {
        HttpRequest request;
        request = HttpRequest.newBuilder()
                .uri(URI.create(internalUrl + "/bungie/discord/isLightHigher"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"userID\":" + userID + ",\"light\":" + light + ",\"charType\":" + charType + "}"))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Boolean.parseBoolean(response.body());
        } catch (IOException | InterruptedException ex) {
            logger.error(ex);
        }
        return false;
    }
}
