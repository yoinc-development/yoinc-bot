package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

public class ProfileDataResponse {

    @SerializedName("data")
    public ProfileResponse data;

    public ProfileDataResponse() {
    }
}
