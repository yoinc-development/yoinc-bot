package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

public class ResponseProfile {

    @SerializedName("characterEquipment")
    public CharacterEquipmentDataResponse characterEquipment;

    @SerializedName("characters")
    public CharactersDataResponse characters;

    @SerializedName("characterActivities")
    public CharacterActivitiesDataResponse characterActivities;

    @SerializedName("profile")
    public ProfileDataResponse profile;

    @SerializedName("profileTransitoryData")
    public ProfileTransitoryDataResponse profileTransitoryData;

    @SerializedName("responseMintedTimestamp")
    public String responseMintedTimestamp;

    @SerializedName("secondaryComponentsMintedTimestamp")
    public String secondaryComponentsMintedTimestamp;

    public ResponseProfile() {
    }
}
