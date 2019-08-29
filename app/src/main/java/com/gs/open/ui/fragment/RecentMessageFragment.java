package com.gs.open.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gs.open.util.LogUtils;
import com.gs.open.util.UIUtils;
import com.lqr.recyclerview.LQRRecyclerView;
import com.gs.open.R;
import com.gs.open.app.AppConst;
import com.gs.open.manager.BroadcastManager;
import com.gs.open.ui.activity.MainActivity;
import com.gs.open.ui.base.BaseFragment;
import com.gs.open.ui.presenter.RecentMessageFgPresenter;
import com.gs.open.ui.view.IRecentMessageFgView;

import butterknife.BindView;

/**
 * 最近会话列表界面，
 * 会话信息，服务器没有保持，因此，不需要从服务器同步信息，只需要从当前的数据库中获取，如果有就显示。
 */
public class RecentMessageFragment extends BaseFragment<IRecentMessageFgView, RecentMessageFgPresenter> implements IRecentMessageFgView {

    private boolean isFirst = true;
    @BindView(R.id.rvRecentMessage)
    LQRRecyclerView mRvRecentMessage;  // 显示会话的Recycler

    @Override
    public void init() {
//        registerBR();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d("RecentMessageFragment.onResume()");
//        if (!isFirst) {
//            mPresenter.getConversations();
//        }
        mPresenter.getConversations(); // 界面可见的时候，进行一次数据加载
    }

    @Override
    public void initData() {
//        UIUtils.postTaskDelay(() -> {
//        mPresenter.getConversations();
//        }, 1000);
        if(isFirst ){
            mPresenter.getConversations();  // 第一次显示。
            isFirst = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unRegisterBR();
    }

    /**
     * 注册广播，
     * 从中可以看到，更新会话是用的广播。
     */
//    private void registerBR() {
//        BroadcastManager.getInstance(getActivity()).register(AppConst.UPDATE_CONVERSATIONS, new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                mPresenter.getConversations();
//                isFirst = false;
//            }
//        });
//    }

    //注销广播，
//    private void unRegisterBR() {
//        BroadcastManager.getInstance(getActivity()).unregister(AppConst.UPDATE_CONVERSATIONS);
//    }

    @Override
    protected RecentMessageFgPresenter createPresenter() {
        return new RecentMessageFgPresenter((MainActivity) getActivity());
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_recent_message;
    }

    @Override
    public LQRRecyclerView getRvRecentMessage() {
        return mRvRecentMessage;
    }
}
