package ch.yoinc.models.bungie;

import com.google.gson.annotations.SerializedName;

public class ResponseData {

    @SerializedName("ErrorCode")
    public int ErrorCode;

    @SerializedName("ErrorStatus")
    public String ErrorStatus;

    @SerializedName("Message")
    public String Message;

    @SerializedName("Response")
    public ResponseProfile Response;

    @SerializedName("ThrottleSeconds")
    public int ThrottleSeconds;

    @SerializedName("MessageData")
    public MessageData MessageData;

    public ResponseData() {
    }
}
