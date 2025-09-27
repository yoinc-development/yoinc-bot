package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

public class ResponseProfile {

    @SerializedName("characterEquipment")
    public CharacterEquipmentDataResponse characterEquipment;

    @SerializedName("characters")
    public CharactersDataResponse characters;

    @SerializedName("profile")
    public ProfileDataResponse profile;

    @SerializedName("responseMintedTimestamp")
    public String responseMintedTimestamp;

    @SerializedName("secondaryComponentsMintedTimestamp")
    public String secondaryComponentsMintedTimestamp;

    public ResponseProfile() {
    }
}
