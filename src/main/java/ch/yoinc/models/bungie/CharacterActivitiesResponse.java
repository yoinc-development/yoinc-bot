package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

public class CharacterActivitiesResponse {

    @SerializedName("dateActivityStarted")
    public String dateActivityStarted;

    @SerializedName("currentActivityHash")
    public long currentActivityHash;

    public CharacterActivitiesResponse() {
    }
}
