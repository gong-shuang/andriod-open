package com.gs.open.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRHeaderAndFooterAdapter;
import com.lqr.recyclerview.LQRRecyclerView;
import com.gs.open.R;
import com.gs.open.app.AppConst;
//import com.gs.open.delete.db.model.Friend;
import com.gs.open.manager.BroadcastManager;
import com.gs.open.ui.activity.GroupListActivity;
import com.gs.open.ui.activity.MainActivity;
import com.gs.open.ui.activity.NewFriendActivity;
import com.gs.open.ui.base.BaseFragment;
import com.gs.open.ui.presenter.ContactsFgPresenter;
import com.gs.open.ui.view.IContactsFgView;
import com.gs.base.util.UIUtils;
import com.gs.open.widget.QuickIndexBar;

import java.util.List;

import butterknife.BindView;

/**
 * @创建者 CSDN_LQR
 * @描述 通讯录界面
 */
public class ContactsFragment extends BaseFragment<IContactsFgView, ContactsFgPresenter> implements IContactsFgView {

    @BindView(R.id.rvContacts)
    LQRRecyclerView mRvContacts;
    @BindView(R.id.qib)
    QuickIndexBar mQib;
    @BindView(R.id.tvLetter)
    TextView mTvLetter;

    private View mHeaderView;
    private TextView mFooterView;
    private TextView mTvNewFriendUnread;

    @Override
    public void initView(View rootView) {
        mHeaderView = View.inflate(getActivity(), R.layout.header_rv_contacts, null);
        mTvNewFriendUnread = (TextView) mHeaderView.findViewById(R.id.tvNewFriendUnread);
        mFooterView = new TextView(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.dip2Px(50));
        mFooterView.setLayoutParams(params);
        mFooterView.setGravity(Gravity.CENTER);

//        registerBR();
    }

    @Override
    public void initData() {
        mPresenter.loadContacts();
    }

    @Override
    public void initListener() {
        //新朋友的监听
        mHeaderView.findViewById(R.id.llNewFriend).setOnClickListener(v -> {
            ((MainActivity) getActivity()).jumpToActivity(NewFriendActivity.class);
            ((MainActivity) getActivity()).mTvContactRedDot.setVisibility(View.GONE);
            mTvNewFriendUnread.setVisibility(View.GONE);
        });
        //群聊的监听
        mHeaderView.findViewById(R.id.llGroup).setOnClickListener(v -> ((MainActivity) getActivity()).jumpToActivity(GroupListActivity.class));
        //字母
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
//                    List<Friend> data = ((LQRAdapterForRecyclerView) ((LQRHeaderAndFooterAdapter) mRvContacts.getAdapter()).getInnerAdapter()).getData();
//                    for (int i = 0; i < data.size(); i++) {
//                        Friend friend = data.get(i);
//                        String c = friend.getDisplayNameSpelling().charAt(0) + "";
//                        if (c.equalsIgnoreCase(letter)) {
//                            mRvContacts.moveToPosition(i);
//                            break;
//                        }
//                    }
                }
            }

            @Override
            public void onLetterCancel() {
                //隐藏对话框
                hideLetter();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterBR();
    }

    private void registerBR() {
        BroadcastManager.getInstance(getActivity()).register(AppConst.UPDATE_RED_DOT, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((MainActivity) getActivity()).mTvContactRedDot.setVisibility(View.VISIBLE);
                mTvNewFriendUnread.setVisibility(View.VISIBLE);
            }
        });
        BroadcastManager.getInstance(getActivity()).register(AppConst.UPDATE_FRIEND, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPresenter.loadContacts();
            }
        });
    }

    private void unregisterBR() {
        BroadcastManager.getInstance(getActivity()).unregister(AppConst.UPDATE_RED_DOT);
        BroadcastManager.getInstance(getActivity()).unregister(AppConst.UPDATE_FRIEND);
    }

    private void showLetter(String letter) {
        mTvLetter.setVisibility(View.VISIBLE);
        mTvLetter.setText(letter);
    }

    private void hideLetter() {
        mTvLetter.setVisibility(View.GONE);
    }

    /**
     * 是否显示快速导航条
     *
     * @param show
     */
    public void showQuickIndexBar(boolean show) {
        if (mQib != null) {
            mQib.setVisibility(show ? View.VISIBLE : View.GONE);
            mQib.invalidate();
        }
    }

    @Override
    protected ContactsFgPresenter createPresenter() {
        return new ContactsFgPresenter((MainActivity) getActivity());
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_contacts;
    }

    @Override
    public View getHeaderView() {
        return mHeaderView;
    }

    @Override
    public LQRRecyclerView getRvContacts() {
        return mRvContacts;
    }

    @Override
    public TextView getFooterView() {
        return mFooterView;
    }
}
