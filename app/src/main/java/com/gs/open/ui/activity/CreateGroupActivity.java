package com.gs.open.ui.activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRHeaderAndFooterAdapter;
import com.lqr.recyclerview.LQRRecyclerView;
import com.gs.open.R;
import com.gs.open.db.model.Friend;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.presenter.CreateGroupAtPresenter;
import com.gs.open.ui.view.ICreateGroupAtView;
import com.gs.base.util.UIUtils;
import com.gs.open.widget.QuickIndexBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @创建者 CSDN_LQR
 * @描述 发起群聊
 */
public class CreateGroupActivity extends BaseActivity<ICreateGroupAtView, CreateGroupAtPresenter> implements ICreateGroupAtView {

    public ArrayList<String> mSelectedTeamMemberAccounts;  //已经是群成员的人的id ，由上个activity传过来的。

    @BindView(R.id.btnToolbarSend)
    Button mBtnToolbarSend;

    @BindView(R.id.rvSelectedContacts)
    LQRRecyclerView mRvSelectedContacts;
    @BindView(R.id.etKey)
    EditText mEtKey;

    private View mHeaderView;
    @BindView(R.id.rvContacts)
    LQRRecyclerView mRvContacts;
    @BindView(R.id.qib)
    QuickIndexBar mQib;
    @BindView(R.id.tvLetter)
    TextView mTvLetter;

    @Override
    public void init() {
        mSelectedTeamMemberAccounts = getIntent().getStringArrayListExtra("selectedMember");
    }

    @Override
    public void initView() {
        mBtnToolbarSend.setVisibility(View.VISIBLE);
        mBtnToolbarSend.setText(UIUtils.getString(R.string.sure));
        mBtnToolbarSend.setEnabled(false);
        mHeaderView = View.inflate(this, R.layout.header_group_cheat, null);
    }

    @Override
    public void initData() {
        mPresenter.loadContacts(mSelectedTeamMemberAccounts);
    }

    @Override
    public void initListener() {
        //创建群的“确定”按钮
        mBtnToolbarSend.setOnClickListener(v -> {
            if (mSelectedTeamMemberAccounts == null) {
                mPresenter.createGroup();
            } else {
                //添加群成员
                mPresenter.addGroupMembers();
            }
        });
        //选择一个群还没实现
        mHeaderView.findViewById(R.id.tvSelectOneGroup).setOnClickListener(v -> UIUtils.showToast("选择一个群"));
        //右侧的字母进度条
        mQib.setOnLetterUpdateListener(new QuickIndexBar.OnLetterUpdateListener() {
            @Override
            public void onLetterUpdate(String letter) {
                //显示对话框
                showLetter(letter);
                //滑动到第一个对应字母开头的联系人
                if ("↑".equalsIgnoreCase(letter)) {
                    mRvContacts.moveToPosition(0);
                } else if ("☆".equalsIgnoreCase(letter)) {
                    mRvContacts.moveToPosition(0);
                } else {
                    List<Friend> data = ((LQRAdapterForRecyclerView) ((LQRHeaderAndFooterAdapter) mRvContacts.getAdapter()).getInnerAdapter()).getData();
                    for (int i = 0; i < data.size(); i++) {
                        Friend friend = data.get(i);
                        String c = friend.getDisplayNameSpelling().charAt(0) + "";
                        if (c.equalsIgnoreCase(letter)) {
                            mRvContacts.moveToPosition(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onLetterCancel() {
                //隐藏对话框
                hideLetter();
            }
        });
    }

    private void showLetter(String letter) {
        mTvLetter.setVisibility(View.VISIBLE);
        mTvLetter.setText(letter);
    }

    private void hideLetter() {
        mTvLetter.setVisibility(View.GONE);
    }

    @Override
    protected CreateGroupAtPresenter createPresenter() {
        return new CreateGroupAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_create_group;
    }

    @Override
    public Button getBtnToolbarSend() {
        return mBtnToolbarSend;
    }

    @Override
    public LQRRecyclerView getRvContacts() {
        return mRvContacts;
    }

    @Override
    public LQRRecyclerView getRvSelectedContacts() {
        return mRvSelectedContacts;
    }

    @Override
    public EditText getEtKey() {
        return mEtKey;
    }

    @Override
    public View getHeaderView() {
        return mHeaderView;
    }
}
