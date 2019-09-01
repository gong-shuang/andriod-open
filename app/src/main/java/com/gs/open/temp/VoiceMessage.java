package com.gs.open.temp;

import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class VoiceMessage extends MessageContent {
    private Uri mUri;
    private int mDuration;
    private String mBase64;
    protected String extra;
    public static final Creator<VoiceMessage> CREATOR = new Creator<VoiceMessage>() {
        public VoiceMessage createFromParcel(Parcel source) {
            return new VoiceMessage(source);
        }

        public VoiceMessage[] newArray(int size) {
            return new VoiceMessage[size];
        }
    };

    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public VoiceMessage(Parcel in) {
        this.setExtra(ParcelUtils.readFromParcel(in));
        this.mUri = (Uri)ParcelUtils.readFromParcel(in, Uri.class);
        this.mDuration = ParcelUtils.readIntFromParcel(in);
        this.setUserInfo((UserInfo)ParcelUtils.readFromParcel(in, UserInfo.class));
    }

    public VoiceMessage(byte[] data) {
        String jsonStr = new String(data);

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            if (jsonObj.has("duration")) {
                this.setDuration(jsonObj.optInt("duration"));
            }

            if (jsonObj.has("content")) {
                this.setBase64(jsonObj.optString("content"));
            }

            if (jsonObj.has("extra")) {
                this.setExtra(jsonObj.optString("extra"));
            }

            if (jsonObj.has("user")) {
                this.setUserInfo(this.parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }
        } catch (JSONException var4) {
            Log.e("JSONException", var4.getMessage());
        }

    }

    private VoiceMessage(Uri uri, int duration) {
        this.mUri = uri;
        this.mDuration = duration;
    }

    public static VoiceMessage obtain(Uri uri, int duration) {
        return new VoiceMessage(uri, duration);
    }

    public Uri getUri() {
        return this.mUri;
    }

    public void setUri(Uri uri) {
        this.mUri = uri;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public String getBase64() {
        return this.mBase64;
    }

    public void setBase64(String base64) {
        this.mBase64 = base64;
    }

    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("content", this.mBase64);
            jsonObj.put("duration", this.mDuration);
            if (!TextUtils.isEmpty(this.getExtra())) {
                jsonObj.put("extra", this.extra);
            }

            if (this.getJSONUserInfo() != null) {
                jsonObj.putOpt("user", this.getJSONUserInfo());
            }
        } catch (JSONException var3) {
            Log.e("JSONException", var3.getMessage());
        }

        this.mBase64 = null;
        return jsonObj.toString().getBytes();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.extra);
        ParcelUtils.writeToParcel(dest, this.mUri);
        ParcelUtils.writeToParcel(dest, this.mDuration);
        ParcelUtils.writeToParcel(dest, this.getUserInfo());
    }
}

