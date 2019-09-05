package com.gs.open.ui.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gs.open.R;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.base.util.LogUtils;
import com.gs.base.util.UIUtils;

import butterknife.BindView;

public class ChangeMyInfoActivity extends BaseActivity {

    @BindView(R.id.btnToolbarSend)
    Button mBtnToolbarSend;
    @BindView(R.id.etName)
    EditText mEtName;
    @BindView(R.id.tvDescription)
    TextView mTvDescription;

    String data;
    String description;

    @Override
    public void init(){
        Intent intent = getIntent();
        data = intent.getStringExtra("data");
        description = intent.getStringExtra("description");
    }

    @Override
    public void initView() {
        mBtnToolbarSend.setText(UIUtils.getString(R.string.save));
        mBtnToolbarSend.setVisibility(View.VISIBLE);
//        UserInfo userInfo = DBManager.getInstance().getUserInfo(UserCache.getId());
//        if (userInfo != null)
        mEtName.setText(data);
        mEtName.setSelection(mEtName.getText().toString().trim().length());
        mTvDescription.setText(description);
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
        Intent intent = new Intent();
        intent.putExtra("result",mEtName.getText().toString());
        setResult(RESULT_OK,intent);
        finish();
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
