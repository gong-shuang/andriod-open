package com.gs.open.temp;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.gs.open.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FileMessage extends MediaMessageContent {
    private static final String TAG = "FileMessage";
    private String mName;
    private long mSize;
    private String mType;
    public static final Parcelable.Creator<FileMessage> CREATOR = new Parcelable.Creator<FileMessage>() {
        public FileMessage createFromParcel(Parcel source) {
            return new FileMessage(source);
        }

        public FileMessage[] newArray(int size) {
            return new FileMessage[size];
        }
    };

    public String getName() {
        return this.mName;
    }

    public void setName(String Name) {
        this.mName = Name;
    }

    public long getSize() {
        return this.mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    public String getType() {
        return this.mType;
    }

    public void setType(String type) {
        if (!TextUtils.isEmpty(type)) {
            this.mType = type;
        } else {
            this.mType = "bin";
        }

    }

    public Uri getFileUrl() {
        return this.getMediaUrl();
    }

    public void setFileUrl(Uri uri) {
        this.setMediaUrl(uri);
    }

    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            if (!TextUtils.isEmpty(this.mName)) {
                jsonObj.put("name", this.mName);
            }

            jsonObj.put("size", this.mSize);
            if (!TextUtils.isEmpty(this.mType)) {
                jsonObj.put("type", this.mType);
            }

            if (this.getLocalPath() != null) {
                jsonObj.put("localPath", this.getLocalPath().toString());
            }

            if (this.getMediaUrl() != null) {
                jsonObj.put("fileUrl", this.getMediaUrl().toString());
            }

            if (!TextUtils.isEmpty(this.getExtra())) {
                jsonObj.put("extra", this.getExtra());
            }

            if (this.getJSONUserInfo() != null) {
                jsonObj.putOpt("user", this.getJSONUserInfo());
            }
        } catch (JSONException var4) {
            LogUtils.e("FileMessage", "JSONException " + var4.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public FileMessage(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException var5) {
            var5.printStackTrace();
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            if (jsonObj.has("name")) {
                this.setName(jsonObj.optString("name"));
            }

            if (jsonObj.has("size")) {
                this.setSize(jsonObj.getLong("size"));
            }

            if (jsonObj.has("type")) {
                this.setType(jsonObj.optString("type"));
            }

            if (jsonObj.has("localPath")) {
                this.setLocalPath(Uri.parse(jsonObj.optString("localPath")));
            }

            if (jsonObj.has("fileUrl")) {
                this.setFileUrl(Uri.parse(jsonObj.optString("fileUrl")));
            }

            if (jsonObj.has("extra")) {
                this.setExtra(jsonObj.optString("extra"));
            }

            if (jsonObj.has("user")) {
                this.setUserInfo(this.parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }
        } catch (JSONException var4) {
            LogUtils.e("FileMessage", "JSONException " + var4.getMessage());
        }

    }

    private FileMessage() {
    }

    private FileMessage(File file, Uri localUrl) {
        this.setLocalPath(localUrl);
        this.mName = file.getName();
        this.mSize = file.length();
    }

    public static FileMessage obtain(Uri localUrl) {
        if (localUrl != null && localUrl.toString().startsWith("file")) {
            File file = new File(localUrl.toString().substring(7));
            return file.exists() && file.isFile() ? new FileMessage(file, localUrl) : null;
        } else {
            return null;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.getExtra());
        ParcelUtils.writeToParcel(dest, this.getName());
        ParcelUtils.writeToParcel(dest, this.getSize());
        ParcelUtils.writeToParcel(dest, this.getType());
        ParcelUtils.writeToParcel(dest, this.getLocalPath());
        ParcelUtils.writeToParcel(dest, this.getFileUrl());
        ParcelUtils.writeToParcel(dest, this.getUserInfo());
    }

    public FileMessage(Parcel in) {
        this.setExtra(ParcelUtils.readFromParcel(in));
        this.setName(ParcelUtils.readFromParcel(in));
        this.setSize(ParcelUtils.readLongFromParcel(in));
        this.setType(ParcelUtils.readFromParcel(in));
        this.setLocalPath((Uri)ParcelUtils.readFromParcel(in, Uri.class));
        this.setFileUrl((Uri)ParcelUtils.readFromParcel(in, Uri.class));
        this.setUserInfo((UserInfo)ParcelUtils.readFromParcel(in, UserInfo.class));
    }

    public List<String> getSearchableWord() {
        List<String> words = new ArrayList();
        if (this.mName != null) {
            words.add(this.mName);
        }

        return words;
    }
}

