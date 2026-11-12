package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class CharacterActivitiesDataResponse {

    @SerializedName("data")
    public Map<String, CharacterActivitiesResponse> data;

    public CharacterActivitiesDataResponse() {
    }
}
