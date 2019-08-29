package com.gs.open.ui.activity;

import com.gs.open.R;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;

/**
 * @创建者 CSDN_LQR
 * @描述 关于界面
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_about;
    }
}
