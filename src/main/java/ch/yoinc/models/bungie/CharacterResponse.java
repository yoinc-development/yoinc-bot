package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

public class CharacterResponse {

    @SerializedName("baseCharacterLevel")
    public int baseCharacterLevel;

    @SerializedName("characterId")
    public long characterId;

    @SerializedName("classHash")
    public long classHash;

    @SerializedName("classType")
    public int classType;

    @SerializedName("light")
    public int light;

    @SerializedName("emblemPath")
    public String emblemPath;

    public CharacterResponse() {
    }
}
