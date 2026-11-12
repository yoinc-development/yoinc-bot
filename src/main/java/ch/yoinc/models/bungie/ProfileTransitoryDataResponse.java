package ch.yoinc.models.bungie;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class ProfileTransitoryDataResponse {

    @SerializedName("privacy")
    public int privacy;

    @SerializedName("data")
    public JsonObject data;

    public ProfileTransitoryDataResponse() {
    }
}
