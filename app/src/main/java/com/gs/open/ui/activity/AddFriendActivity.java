package com.gs.open.ui.activity;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.gs.factory.persistence.Account;
import com.gs.open.R;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.presenter.AddFriendAtPresenter;
import com.gs.open.ui.view.IAddFriendAtView;
import com.gs.base.util.UIUtils;

import butterknife.BindView;

/**
 * @创建者 CSDN_LQR
 * @描述 添加朋友界面
 */

public class AddFriendActivity extends BaseActivity<IAddFriendAtView, AddFriendAtPresenter> implements IAddFriendAtView {

    @BindView(R.id.llSearchUser)
    LinearLayout mLlSearchUser;  // 搜索框
    @BindView(R.id.tvAccount)
    TextView mTvAccount;   // 当前账号

    @Override
    public void initView() {
        setToolbarTitle(UIUtils.getString(R.string.add_friend));
        mTvAccount.setText(Account.getUser().getPhone() + "");
    }

    @Override
    public void initListener() {
        mLlSearchUser.setOnClickListener(v -> jumpToActivity(SearchUserActivity.class));
    }

    @Override
    protected AddFriendAtPresenter createPresenter() {
        return new AddFriendAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_add_friend;
    }
}
