package com.gs.open.ui.activity;

import android.view.View;

import com.gs.factory.Factory;
import com.gs.factory.persistence.Account;
import com.lqr.optionitemview.OptionItemView;
import com.gs.open.R;
import com.gs.open.app.AppConst;
import com.gs.open.app.MyApp;
import com.gs.open.model.cache.UserCache;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.widget.CustomDialog;

import butterknife.BindView;

/**
 * @创建者 CSDN_LQR
 * @描述 设置界面
 */
public class SettingActivity extends BaseActivity {

    private View mExitView;

    @BindView(R.id.oivAbout)
    OptionItemView mOivAbout;
    @BindView(R.id.oivHelpFeedback)
    OptionItemView mOivHelpFeedback;
    @BindView(R.id.oivExit)
    OptionItemView mOivExit;
    private CustomDialog mExitDialog;

    @Override
    public void initListener() {
        mOivAbout.setOnClickListener(v -> jumpToActivity(AboutActivity.class));
        mOivHelpFeedback.setOnClickListener(v1 -> jumpToWebViewActivity(AppConst.WeChatUrl.HELP_FEED_BACK));
        mOivExit.setOnClickListener(v -> {
            if (mExitView == null) {
                mExitView = View.inflate(this, R.layout.dialog_exit, null);
                mExitDialog = new CustomDialog(this, mExitView, R.style.MyDialog);
                mExitView.findViewById(R.id.tvExitAccount).setOnClickListener(v1 -> {
 //                   RongIMClient.getInstance().logout();
                    UserCache.clear();
                    Account.clear(Factory.app());  // 清除 xml 文件
                    mExitDialog.dismiss();
                    MyApp.exit();
                    jumpToActivityAndClearTask(LoginActivity.class);
                });
                mExitView.findViewById(R.id.tvExitApp).setOnClickListener(v1 -> {
//                    RongIMClient.getInstance().disconnect();
                    mExitDialog.dismiss();
                    MyApp.exit();
                });
            }
            mExitDialog.show();
        });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_setting;
    }
}
