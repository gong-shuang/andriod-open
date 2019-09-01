package com.gs.open.temp;

import android.net.Uri;

public abstract class MediaMessageContent extends MessageContent {
    private Uri mLocalPath;
    private Uri mMediaUrl;
    private String mExtra;

    public MediaMessageContent() {
    }

    public Uri getLocalPath() {
        return this.mLocalPath;
    }

    public Uri getMediaUrl() {
        return this.mMediaUrl;
    }

    public void setMediaUrl(Uri mMediaUrl) {
        this.mMediaUrl = mMediaUrl;
    }

    public void setLocalPath(Uri mLocalPath) {
        this.mLocalPath = mLocalPath;
    }

    public String getExtra() {
        return this.mExtra;
    }

    public void setExtra(String mExtra) {
        this.mExtra = mExtra;
    }
}