package com.gs.open.ui.activity;

import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.presenter.FriendCircleAtPresenter;
import com.gs.open.ui.view.IFriendCircleAtView;

/**
 * @创建者 CSDN_LQR
 * @描述 朋友圈
 */
public class FriendCircleActivity extends BaseActivity<IFriendCircleAtView, FriendCircleAtPresenter> implements IFriendCircleAtView {

    @Override
    protected FriendCircleAtPresenter createPresenter() {
        return new FriendCircleAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return 0;
    }
}
