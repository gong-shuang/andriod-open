package com.gs.open.ui.activity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gs.im.data.helper.UserHelper;
import com.gs.im.model.db.User;
import com.gs.im.persistence.Account;
import com.gs.open.R;
//import com.gs.open.delete.ApiRetrofit;
//import com.gs.open.delete.db.DBManager;
//import com.gs.open.delete.db.model.Friend;
//import com.gs.open.delete.model.cache.UserCache;
//import com.gs.open.temp.UserInfo;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.base.util.LogUtils;
import com.gs.base.util.UIUtils;

import butterknife.BindView;

/**
 * @创建者 CSDN_LQR
 * @描述 更改名字界面
 */
public class ChangeMyNameActivity extends BaseActivity {

    @BindView(R.id.btnToolbarSend)
    Button mBtnToolbarSend;
    @BindView(R.id.etName)
    EditText mEtName;

    @Override
    public void initView() {
        mBtnToolbarSend.setText(UIUtils.getString(R.string.save));
        mBtnToolbarSend.setVisibility(View.VISIBLE);
//        UserInfo userInfo = DBManager.getInstance().getUserInfo(UserCache.getId());
        User userInfo = UserHelper.findFromLocal(Account.getUserId());
        if (userInfo != null)
            mEtName.setText(userInfo.getName());
        mEtName.setSelection(mEtName.getText().toString().trim().length());
    }

    @Override
    public void initListener() {
        mBtnToolbarSend.setOnClickListener(v -> changeMyName());
        mEtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEtName.getText().toString().trim().length() > 0) {
                    mBtnToolbarSend.setEnabled(true);
                } else {
                    mBtnToolbarSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void changeMyName() {
        showWaitingDialog(UIUtils.getString(R.string.please_wait));
        String nickName = mEtName.getText().toString().trim();
//        ApiRetrofit.getInstance().setName(nickName)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(setNameResponse -> {
//                    hideWaitingDialog();
//                    if (setNameResponse.getCode() == 200) {
//                        Friend friend = DBManager.getInstance().getFriendById(UserCache.getId());
//                        if (friend != null) {
//                            friend.setName(nickName);
//                            friend.setDisplayName(nickName);
//                            DBManager.getInstance().saveOrUpdateFriend(friend);
//                            BroadcastManager.getInstance(ChangeMyNameActivity.this).sendBroadcast(AppConst.CHANGE_INFO_FOR_ME);
//                            BroadcastManager.getInstance(ChangeMyNameActivity.this).sendBroadcast(AppConst.CHANGE_INFO_FOR_CHANGE_NAME);
//                        }
//                        finish();
//                    }
//                }, this::loadError);
    }

    private void loadError(Throwable throwable) {
        hideWaitingDialog();
        LogUtils.sf(throwable.getLocalizedMessage());
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_change_name;
    }
}
