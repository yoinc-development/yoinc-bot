package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("crossSaveOverride")
    public int crossSaveOverride;

    @SerializedName("isPublic")
    public boolean isPublic;

    @SerializedName("membershipType")
    public int membershipType;

    @SerializedName("membershipId")
    public String membershipId;

    @SerializedName("displayName")
    public String displayName;

    @SerializedName("bungieGlobalDisplayName")
    public String bungieGlobalDisplayName;

    public UserInfo() {
    }
}
