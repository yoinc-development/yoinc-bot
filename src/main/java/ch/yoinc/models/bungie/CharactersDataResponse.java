package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class CharactersDataResponse {

    @SerializedName("data")
    public Map<String, CharacterResponse> data;

    public CharactersDataResponse() {
    }
}
