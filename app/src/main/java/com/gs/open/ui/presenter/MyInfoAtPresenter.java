package com.gs.open.ui.presenter;

import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.gs.factory.Factory;
import com.gs.factory.common.data.DataSource;
import com.gs.factory.data.helper.UserHelper;
import com.gs.factory.model.api.user.UserUpdateModel;
import com.gs.factory.model.card.UserCard;
import com.gs.factory.model.db.User;
import com.gs.factory.net.UploadHelper;
import com.gs.factory.persistence.Account;
//import com.gs.open.temp.UserInfo;
import com.lqr.imagepicker.bean.ImageItem;
import com.gs.open.R;
import com.gs.open.api.ApiRetrofit;
import com.gs.open.app.AppConst;
import com.gs.open.db.DBManager;
import com.gs.open.db.model.Friend;
import com.gs.open.manager.BroadcastManager;
import com.gs.open.model.response.QiNiuTokenResponse;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.IMyInfoAtView;
import com.gs.base.util.LogUtils;
import com.gs.base.util.UIUtils;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.io.File;


import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MyInfoAtPresenter extends BasePresenter<IMyInfoAtView> {

    public User mUserInfo;
//    private UploadManager mUploadManager;

    public MyInfoAtPresenter(BaseActivity context) {
        super(context);
    }

    public void loadUserInfo() {
//        mUserInfo = DBManager.getInstance().getUserInfo(UserCache.getId());
        mUserInfo = Account.getUser();
        if (mUserInfo != null) {
            Glide.with(mContext).load(mUserInfo.getPortrait()).centerCrop().into(getView().getIvHeader());
            getView().getOivName().setRightText(mUserInfo.getName());
            getView().getOivAccount().setRightText(Account.getUser().getPhone());
            getView().getOivSignature().setRightText(Account.getUser().getDesc());
        }
    }

    public void update(final String name,final String photoFilePath, final String desc, final boolean isMan) {

        final IMyInfoAtView view = getView();
        if(view == null)
            return;

        // 上传头像
        Factory.runOnAsync(new Runnable() {
            @Override
            public void run() {
                String url = null;
                if(photoFilePath != null){
                    //先上次到阿里oos。
                    url = UploadHelper.uploadPortrait(photoFilePath);
                    if (TextUtils.isEmpty(url)) {
                        Run.onUiAsync(new Action() {
                            @Override
                            public void call() {
                                // 上传失败
                                view.showError(R.string.data_upload_error);
                                return;
                            }
                        });
                    }
                }

                // 构建Model
                UserUpdateModel model = new UserUpdateModel(name, url, desc,
                        isMan ? User.SEX_MAN : User.SEX_WOMAN);
                // 进行网络请求，上传
                UserHelper.update(model, new DataSource.Callback<UserCard>() {

                    @Override
                    public void onDataLoaded(UserCard userCard) {
                        if(userCard.getId() != null) {
                            DBManager.getInstance().saveOrUpdateFriend(new Friend(userCard.getId(), userCard.getName(),
                                    userCard.getPortrait() == null ? null : userCard.getPortrait()));
                            //发送广播，通知user信息改变了。
                            BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.CHANGE_INFO_FOR_ME);
                        }
                    }

                    @Override
                    public void onDataNotAvailable(final int strRes) {
                        // 强制执行在主线程中
                        Run.onUiAsync(new Action() {
                            @Override
                            public void call() {
                                view.showError(strRes);
                            }
                        });
                    }
                });

            }
        });

    }

    //废弃
    public void setPortrait(ImageItem imageItem) {
        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
        //上传头像
        ApiRetrofit.getInstance().getQiNiuToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiNiuTokenResponse -> {
                    if (qiNiuTokenResponse != null && qiNiuTokenResponse.getCode() == 200) {
//                        if (mUploadManager == null)
//                            mUploadManager = new UploadManager();
                        File imageFile = new File(imageItem.path);
                        QiNiuTokenResponse.ResultEntity result = qiNiuTokenResponse.getResult();
                        String domain = result.getDomain();
                        String token = result.getToken();
                        //上传到七牛
//                        mUploadManager.put(imageFile, null, token, (s, responseInfo, jsonObject) -> {
//                            if (responseInfo.isOK()) {
//                                String key = jsonObject.optString("key");
//                                String imageUrl = "http://" + domain + "/" + key;
//                                //修改自己服务器头像数据
//                                ApiRetrofit.getInstance().setPortrait(imageUrl)
//                                        .subscribeOn(Schedulers.io())
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .subscribe(setPortraitResponse -> {
//                                            if (setPortraitResponse != null && setPortraitResponse.getCode() == 200) {
//                                                Friend friend = DBManager.getInstance().getFriendById(UserCache.getId());
//                                                if (friend != null) {
//                                                    friend.setPortraitUri(imageUrl);
//                                                    DBManager.getInstance().saveOrUpdateFriend(friend);
//                                                    DBManager.getInstance().updateGroupMemberPortraitUri(UserCache.getId(), imageUrl);
//                                                    Glide.with(mContext).load(friend.getPortraitUri()).centerCrop().into(getView().getIvHeader());
//                                                    BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.CHANGE_INFO_FOR_ME);
//                                                    BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
//                                                    BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_GROUP);
//                                                    UIUtils.showToast(UIUtils.getString(R.string.set_success));
//                                                }
//                                                mContext.hideWaitingDialog();
//                                            } else {
//                                                uploadError(null);
//                                            }
//                                        }, this::uploadError);
//                            } else {
//                                uploadError(null);
//                            }
//                        }, null);
                    } else {
                        uploadError(null);
                    }
                }, this::uploadError);
    }

    private void uploadError(Throwable throwable) {
        if (throwable != null)
            LogUtils.sf(throwable.getLocalizedMessage());
        mContext.hideWaitingDialog();
        UIUtils.showToast(UIUtils.getString(R.string.set_fail));
    }
}
