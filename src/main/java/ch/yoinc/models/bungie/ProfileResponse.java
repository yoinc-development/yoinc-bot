package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    @SerializedName("userInfo")
    public UserInfo userInfo;

    @SerializedName("dateLastPlayed")
    public String dateLastPlayed;

    @SerializedName("versionsOwned")
    public int versionsOwned;

    @SerializedName("currentSeasonHash")
    public long currentSeasonHash;

    @SerializedName("currentSeasonPassHash")
    public long currentSeasonPassHash;

    @SerializedName("currentSeasonRewardPowerCap")
    public int currentSeasonRewardPowerCap;

    @SerializedName("currentGuardianRank")
    public int currentGuardianRank;

    @SerializedName("lifetimeHighestGuardianRank")
    public int lifetimeHighestGuardianRank;

    @SerializedName("renewedGuardianRank")
    public int renewedGuardianRank;

    public ProfileResponse() {
    }
}
