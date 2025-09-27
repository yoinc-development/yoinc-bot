package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CharactersDataResponse {

    @SerializedName("data")
    public List<CharacterResponse> data;

    public CharactersDataResponse() {
    }
}
