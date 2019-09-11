package com.gs.open.ui.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gs.im.data.helper.UserHelper;
import com.gs.im.model.db.User;
import com.gs.im.persistence.Account;
//import com.gs.open.temp.UserInfo;
import com.lqr.optionitemview.OptionItemView;
import com.gs.open.R;
import com.gs.open.app.AppConst;
import com.gs.open.manager.BroadcastManager;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.base.util.LogUtils;
import com.gs.base.util.UIUtils;

import butterknife.BindView;

/**
 * @创建者 CSDN_LQR
 * @描述 用户信息界面
 */
public class UserInfoActivity extends BaseActivity {

    User mUserInfo;
    String userID;

    @BindView(R.id.ibToolbarMore)
    ImageButton mIbToolbarMore;

    @BindView(R.id.ivHeader)
    ImageView mIvHeader;
    @BindView(R.id.tvName)
    TextView mTvName;
    @BindView(R.id.ivGender)
    ImageView mIvGender;
    @BindView(R.id.tvAccount)
    TextView mTvAccount;
    @BindView(R.id.tvNickName)
    TextView mTvNickName;
    @BindView(R.id.tvArea)
    TextView mTvArea;
    @BindView(R.id.tvSignature)
    TextView mTvSignature;

    @BindView(R.id.oivAliasAndTag)
    OptionItemView mOivAliasAndTag;
    @BindView(R.id.llArea)
    LinearLayout mLlArea;
    @BindView(R.id.llSignature)
    LinearLayout mLlSignature;

    @BindView(R.id.btnCheat)
    Button mBtnCheat;
    @BindView(R.id.btnAddToContact)
    Button mBtnAddToContact;

    @BindView(R.id.rlMenu)
    RelativeLayout mRlMenu;
    @BindView(R.id.svMenu)
    ScrollView mSvMenu;
    @BindView(R.id.oivAlias)
    OptionItemView mOivAlias;
    @BindView(R.id.oivDelete)
    OptionItemView mOivDelete;
//    private Friend mFriend;

    @Override
    public void init() {
        Intent intent = getIntent();
        userID = intent.getStringExtra("userInfo");
        mUserInfo = UserHelper.findFromLocal(userID);
 //       registerBR();
    }

    @Override
    public void initView() {
        if (mUserInfo == null) {
            LogUtils.e("mUserInfo id:" + mUserInfo + " not exist!");
            finish();
            return;
        }

        mIbToolbarMore.setVisibility(View.VISIBLE);
    }

    @Override
    public void initData() {
 //       mFriend = DBManager.getInstance().getFriendById(mUserInfo.getId());
        Glide.with(this).load(UserHelper.getLocalFileAsyncUpdateDB(mUserInfo)).centerCrop().into(mIvHeader);
        mTvAccount.setText(UIUtils.getString(R.string.my_chat_account, Account.getUser().getPhone()));
        mTvName.setText(mUserInfo.getName());

        if(mUserInfo.getRole() == User.ROLE_FRIEND){
            String nickName = mUserInfo.getName();
            mTvName.setText(nickName);
            if (TextUtils.isEmpty(nickName)) {
                mTvNickName.setVisibility(View.INVISIBLE);
            } else {
                mTvNickName.setText(UIUtils.getString(R.string.nickname_colon, mUserInfo.getName()));
            }
        }else if(mUserInfo.getRole() == User.ROLE_STRANGER){
            mBtnCheat.setVisibility(View.GONE);
            mBtnAddToContact.setVisibility(View.VISIBLE);
            mTvNickName.setVisibility(View.INVISIBLE);
        }else if(mUserInfo.getRole() == User.ROLE_SELF){
            mTvNickName.setVisibility(View.INVISIBLE);
            mOivAliasAndTag.setVisibility(View.GONE);
            mLlArea.setVisibility(View.GONE);
            mLlSignature.setVisibility(View.GONE);
            mBtnCheat.setVisibility(View.GONE);  // 是我的消息的时候，这个按钮也屏蔽，不允许自己给自己发消息。
        }else{
            //错误
        }
    }

    @Override
    public void initListener() {
        mIbToolbarMore.setOnClickListener(v -> showMenu());
        mOivAliasAndTag.setOnClickListener(v -> jumpToSetAlias());

        mBtnCheat.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, SessionActivity.class);   // 跳转到聊天界面。
            intent.putExtra("sessionId", mUserInfo.getId());
            intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
            jumpToActivity(intent);
            finish();
        });

        mBtnAddToContact.setOnClickListener(v -> {
            //跳转到写附言界面
            Intent intent = new Intent(UserInfoActivity.this, PostScriptActivity.class);
            intent.putExtra("userId", mUserInfo.getId());
            jumpToActivity(intent);
        });

        mRlMenu.setOnClickListener(v -> hideMenu());

        mOivAlias.setOnClickListener(v -> {
            jumpToSetAlias();
            hideMenu();
        });
        mOivDelete.setOnClickListener(v -> {
            hideMenu();
//            showMaterialDialog(UIUtils.getString(R.string.delete_contact),
//                    UIUtils.getString(R.string.delete_contact_content, mUserInfo.getName()),
//                    UIUtils.getString(R.string.delete),
//                    UIUtils.getString(R.string.cancel),
//                    v1 -> ApiRetrofit.getInstance()
//                            .deleteFriend(mUserInfo.getUserId())
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(deleteFriendResponse -> {
//                                hideMaterialDialog();
//                                if (deleteFriendResponse.getCode() == 200) {
////                                    RongIMClient.getInstance().getConversation(Conversation.ConversationType.PRIVATE, mUserInfo.getUserId(), new RongIMClient.ResultCallback<Conversation>() {
////                                        @Override
////                                        public void onSuccess(Conversation conversation) {
////                                            RongIMClient.getInstance().clearMessages(Conversation.ConversationType.PRIVATE, mUserInfo.getUserId(), new RongIMClient.ResultCallback<Boolean>() {
////                                                @Override
////                                                public void onSuccess(Boolean aBoolean) {
////                                                    RongIMClient.getInstance().removeConversation(Conversation.ConversationType.PRIVATE, mUserInfo.getUserId(), null);
////                                                }
////
////                                                @Override
////                                                public void onError(RongIMClient.ErrorCode errorCode) {
////
////                                                }
////                                            });
////                                        }
////
////                                        @Override
////                                        public void onError(RongIMClient.ErrorCode errorCode) {
////
////                                        }
////                                    });
//                                    //通知对方被删除(把我的id发给对方)
////                                    DeleteContactMessage deleteContactMessage = DeleteContactMessage.obtain(UserCache.getId());
////                                    RongIMClient.getInstance().sendMessage(Message.obtain(mUserInfo.getUserId(), Conversation.ConversationType.PRIVATE, deleteContactMessage), "", "", null, null);
//                                    DBManager.getInstance().deleteFriendById(mUserInfo.getUserId());
//                                    UIUtils.showToast(UIUtils.getString(R.string.delete_success));
//                                    BroadcastManager.getInstance(UserInfoActivity.this).sendBroadcast(AppConst.UPDATE_FRIEND);
//                                    finish();
//                                } else {
//                                    UIUtils.showToast(UIUtils.getString(R.string.delete_fail));
//                                }
//                            }, this::loadError)
//                    , v2 -> hideMaterialDialog());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBR();
    }

    private void loadError(Throwable throwable) {
        hideMaterialDialog();
        LogUtils.sf(throwable.getLocalizedMessage());
    }

    private void jumpToSetAlias() {
        Intent intent = new Intent(this, SetAliasActivity.class);
        intent.putExtra("userId", mUserInfo.getId());
        jumpToActivity(intent);
    }

    private void showMenu() {
        mRlMenu.setVisibility(View.VISIBLE);
        TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        ta.setDuration(200);
        mSvMenu.startAnimation(ta);
    }

    private void hideMenu() {
        TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRlMenu.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ta.setDuration(200);
        mSvMenu.startAnimation(ta);
    }

    private void registerBR() {
//        BroadcastManager.getInstance(this).register(AppConst.CHANGE_INFO_FOR_USER_INFO, new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                mUserInfo = DBManager.getInstance().getUserInfo(mUserInfo.getUserId());
//                initData();
//            }
//        });
    }

    private void unRegisterBR() {
        BroadcastManager.getInstance(this).unregister(AppConst.CHANGE_INFO_FOR_USER_INFO);
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_user_info;
    }
}
