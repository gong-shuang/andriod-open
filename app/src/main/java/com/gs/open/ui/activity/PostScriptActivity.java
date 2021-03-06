package com.gs.open.ui.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.gs.open.R;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.presenter.PostScriptAtPresenter;
import com.gs.open.ui.view.IPostScriptAtView;

import butterknife.BindView;

/**
 * @创建者 CSDN_LQR
 * @描述 添加朋友时， 发送请求消息的界面
 */
public class PostScriptActivity extends BaseActivity<IPostScriptAtView, PostScriptAtPresenter> implements IPostScriptAtView {

    @BindView(R.id.btnToolbarSend)
    Button mBtnToolbarSend;

    @BindView(R.id.etMsg)
    EditText mEtMsg;
    @BindView(R.id.ibClear)
    ImageButton mIbClear;
    private String mUserId;

    @Override
    public void init() {
        mUserId = getIntent().getStringExtra("userId");
    }

    @Override
    public void initView() {
        mBtnToolbarSend.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(mUserId)) {
            finish();
        }
    }

    @Override
    public void initListener() {
        mIbClear.setOnClickListener(v -> mEtMsg.setText(""));
        mBtnToolbarSend.setOnClickListener(v -> mPresenter.addFriend(mUserId));
    }

    @Override
    protected PostScriptAtPresenter createPresenter() {
        return new PostScriptAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_postscript;
    }

    @Override
    public EditText getEtMsg() {
        return mEtMsg;
    }
}
