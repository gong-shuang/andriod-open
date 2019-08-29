package com.gs.open.temp;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class UserInfo implements Parcelable {

    private String id;
    private String name;
    private Uri portraitUri;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.getUserId());
        dest.writeString(this.getName());
        dest.writeParcelable(this.getPortraitUri(), 0);

    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public UserInfo(Parcel in) {
        this.setUserId(in.readString());
        this.setName(in.readString());
        this.setPortraitUri(in.readParcelable((Uri.class).getClassLoader()));
    }

    public UserInfo(String id, String name, Uri portraitUri) {
        if (TextUtils.isEmpty(id)) {
            throw new NullPointerException("userId is null");
        } else {
            this.id = id;
            this.name = name;
            this.portraitUri = portraitUri;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(Uri portraitUri) {
        this.portraitUri = portraitUri;
    }

    public String getUserId() {
        if (TextUtils.isEmpty(this.id)) {
            throw new NullPointerException("userId  is null");
        } else {
            return this.id;
        }
    }

    public void setUserId(String userId) {
        this.id = userId;
    }
}
