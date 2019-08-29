package com.gs.open.temp;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.gs.open.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class MessageContent implements Parcelable {
    private static final String TAG = "MessageContent";
    private UserInfo userInfo;
    private MentionedInfo mentionedInfo;

    protected MessageContent() {
    }

    public MessageContent(byte[] data) {
    }

    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    public void setUserInfo(UserInfo info) {
        this.userInfo = info;
    }

    public MentionedInfo getMentionedInfo() {
        return this.mentionedInfo;
    }

    public void setMentionedInfo(MentionedInfo info) {
        this.mentionedInfo = info;
    }

    public JSONObject getJSONUserInfo() {
        if (this.getUserInfo() != null && this.getUserInfo().getUserId() != null) {
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("id", this.getUserInfo().getUserId());
                if (!TextUtils.isEmpty(this.getUserInfo().getName())) {
                    jsonObject.put("name", this.getUserInfo().getName());
                }

                if (this.getUserInfo().getPortraitUri() != null) {
                    jsonObject.put("portrait", this.getUserInfo().getPortraitUri());
                }
            } catch (JSONException var3) {
                LogUtils.e("MessageContent", "JSONException " + var3.getMessage());
            }

            return jsonObject;
        } else {
            return null;
        }
    }

    public UserInfo parseJsonToUserInfo(JSONObject jsonObj) {
        UserInfo info = null;
        String id = jsonObj.optString("id");
        String name = jsonObj.optString("name");
        String icon = jsonObj.optString("portrait");
        if (TextUtils.isEmpty(icon)) {
            icon = jsonObj.optString("icon");
        }

        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(name)) {
            Uri portrait = icon != null ? Uri.parse(icon) : null;
            info = new UserInfo(id, name, portrait);
        }

        return info;
    }

    public JSONObject getJsonMentionInfo() {
        if (this.getMentionedInfo() == null) {
            return null;
        } else {
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("type", this.getMentionedInfo().getType().getValue());
                if (this.getMentionedInfo().getMentionedUserIdList() == null) {
                    jsonObject.put("userIdList", (Object)null);
                } else {
                    JSONArray jsonArray = new JSONArray();
                    Iterator i$ = this.getMentionedInfo().getMentionedUserIdList().iterator();

                    while(i$.hasNext()) {
                        String userId = (String)i$.next();
                        jsonArray.put(userId);
                    }

                    jsonObject.put("userIdList", jsonArray);
                }

                jsonObject.put("mentionedContent", this.getMentionedInfo().getMentionedContent());
            } catch (JSONException var5) {
                LogUtils.e("MessageContent", "JSONException " + var5.getMessage());
            }

            return jsonObject;
        }
    }

    public MentionedInfo parseJsonToMentionInfo(JSONObject jsonObject) {
        MentionedInfo.MentionedType type = MentionedInfo.MentionedType.valueOf(jsonObject.optInt("type"));
        JSONArray userList = jsonObject.optJSONArray("userIdList");
        String mentionContent = jsonObject.optString("mentionedContent");
        MentionedInfo mentionedInfo;
        if (type.equals(MentionedInfo.MentionedType.ALL)) {
            mentionedInfo = new MentionedInfo(type, (List)null, mentionContent);
        } else {
            ArrayList list = new ArrayList();

            try {
                for(int i = 0; i < userList.length(); ++i) {
                    list.add((String)userList.get(i));
                }
            } catch (JSONException var8) {
                var8.printStackTrace();
            }

            mentionedInfo = new MentionedInfo(type, list, mentionContent);
        }

        return mentionedInfo;
    }

    public List<String> getSearchableWord() {
        return null;
    }

    public abstract byte[] encode();

}

