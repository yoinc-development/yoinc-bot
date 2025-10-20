package ch.yoinc.models;

import com.google.gson.annotations.SerializedName;

public class InternalUser {

    @SerializedName("userID")
    public String userID;

    @SerializedName("bungieID")
    public String bungieID;

    public InternalUser() {
    }
}
