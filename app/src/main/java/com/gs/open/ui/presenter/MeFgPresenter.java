package com.gs.open.ui.presenter;

import android.net.Uri;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.gs.factory.model.db.User;
import com.gs.factory.persistence.Account;
import com.gs.open.R;
import com.gs.open.api.ApiRetrofit;
import com.gs.open.db.DBManager;
import com.gs.open.db.model.Friend;
import com.gs.open.model.cache.UserCache;
import com.gs.open.model.response.GetUserInfoByIdResponse;
import com.gs.open.temp.UserInfo;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.IMeFgView;
import com.gs.open.util.LogUtils;
import com.gs.open.util.PinyinUtils;
import com.gs.open.util.RongGenerate;
import com.gs.open.util.UIUtils;


import java.net.URI;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MeFgPresenter extends BasePresenter<IMeFgView> {

    private UserInfo mUserInfo;
    private boolean isFirst = true;

    public MeFgPresenter(BaseActivity context) {
        super(context);
    }

    //初始过程，导入当前用户的信息。
    public void loadUserInfo() {

        //从本地数据库中获取当前user的信息。
        mUserInfo = DBManager.getInstance().getUserInfo(Account.getUserId());
        if(mUserInfo == null ){
            User user = Account.getUser();
            String portrait = user.getPortrait();
            if (TextUtils.isEmpty(portrait)) {
                portrait = RongGenerate.generateDefaultAvatar(user.getName(), user.getId());
                user.setPortrait(portrait);
            }
            mUserInfo = new UserInfo(user.getId(),user.getName(),user.getPortrait()==null ? null: Uri.parse(user.getPortrait()));
            Friend friend = new Friend(
                    user.getId(),
                    user.getName(),
                    user.getPortrait(),
                    TextUtils.isEmpty(user.getAlias()) ? user.getName() : user.getAlias(),
                    null, user.getPhone(), null, null,
                    PinyinUtils.getPinyin(user.getName()),
                    PinyinUtils.getPinyin(TextUtils.isEmpty(user.getAlias()) ? user.getName() : user.getAlias()));
            DBManager.getInstance().saveOrUpdateFriend(friend);
        }
        fillView();


//        //本地数据库里没有，则从网络获取，获取后，存储到本地数据库中。
//        if (mUserInfo == null || isFirst) {
//            isFirst = false;
//            ApiRetrofit.getInstance().getUserInfoById(UserCache.getId())
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(getUserInfoByIdResponse -> {
//                        if (getUserInfoByIdResponse != null && getUserInfoByIdResponse.getCode() == 200) {
//                            GetUserInfoByIdResponse.ResultEntity result = getUserInfoByIdResponse.getResult();
//
//                            mUserInfo = new UserInfo(UserCache.getId(), result.getNickname(), Uri.parse(result.getPortraitUri()));
//                            if (TextUtils.isEmpty(mUserInfo.getPortraitUri().toString())) {
//                                mUserInfo.setPortraitUri(Uri.parse(DBManager.getInstance().getPortraitUri(mUserInfo)));
//                            }
//
//                            DBManager.getInstance().saveOrUpdateFriend(new Friend(mUserInfo.getUserId(), mUserInfo.getName(), mUserInfo.getPortraitUri().toString()));
//                            fillView();
//                        }
//                    }, this::loadError);
//        } else {
//            fillView();
//        }
    }

    public void refreshUserInfo() {
        UserInfo userInfo = DBManager.getInstance().getUserInfo(UserCache.getId());
        if (userInfo == null) {
            loadUserInfo();
        } else {
            mUserInfo = userInfo;
        }
    }

    public void fillView() {
        if (mUserInfo != null) {
            Glide.with(mContext).load(mUserInfo.getPortraitUri()).centerCrop().into(getView().getIvHeader());
            getView().getTvAccount().setText(UIUtils.getString(R.string.my_chat_account, mUserInfo.getUserId()));
            getView().getTvName().setText(mUserInfo.getName());
        }
    }

    private void loadError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        UIUtils.showToast(throwable.getLocalizedMessage());
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }
}
