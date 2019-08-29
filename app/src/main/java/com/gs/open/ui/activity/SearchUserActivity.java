package com.gs.open.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gs.factory.model.card.UserCard;
import com.gs.factory.model.db.User;
import com.gs.open.R;
import com.gs.open.db.DBManager;
import com.gs.open.db.model.Friend;
import com.gs.open.temp.UserInfo;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.presenter.SearchUserAtPresenter;
import com.gs.open.ui.view.ISearchUserAtView;
import com.gs.open.util.LogUtils;
import com.gs.open.util.PinyinUtils;
import com.gs.open.util.SortUtils;
import com.gs.open.util.UIUtils;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.recyclerview.LQRRecyclerView;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @创建者 CSDN_LQR
 * @描述 搜索用户界面
 */
public class SearchUserActivity extends BaseActivity<ISearchUserAtView, SearchUserAtPresenter> implements ISearchUserAtView {

    @BindView(R.id.llToolbarSearch)
    LinearLayout mLlToolbarSearch;
    @BindView(R.id.etSearchContent)
    EditText mEtSearchContent;

    @BindView(R.id.rlNoResultTip)
    RelativeLayout mRlNoResultTip;
    @BindView(R.id.llSearch)
    LinearLayout mLlSearch;
    @BindView(R.id.tvMsg)
    TextView mTvMsg;

    //查询到的好友
    @BindView(R.id.rvContacts)
    LQRRecyclerView mRvContacts;
    private List<Friend> mData = new ArrayList<>();
    private LQRAdapterForRecyclerView<Friend> mAdapter;


    @Override
    public void initView() {
        mToolbarTitle.setVisibility(View.GONE);
        mLlToolbarSearch.setVisibility(View.VISIBLE);
        setAdapter();
    }

    @Override
    public void initListener() {
        mEtSearchContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String content = mEtSearchContent.getText().toString().trim();
                mRlNoResultTip.setVisibility(View.GONE);
                if (content.length() > 0) {
                    mLlSearch.setVisibility(View.VISIBLE);
                    mTvMsg.setText(content);
                } else {
                    mLlSearch.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mLlSearch.setOnClickListener(v -> {
            mPresenter.searchUser();
        });
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<Friend>(this, mData, R.layout.item_contact) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Friend item, int position) {
                    helper.setText(R.id.tvName, item.getDisplayName());  //设置名字
                    ImageView ivHeader = helper.getView(R.id.ivHeader);   // 设置头像
                    Glide.with(SearchUserActivity.this).load(item.getPortraitUri()).centerCrop().into(ivHeader);

                }
            };
        }
        mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
            Friend item = mData.get(position);
            UserInfo userInfo = new UserInfo(item.getUserId(), item.getName(), Uri.parse(item.getPortraitUri()));
            Intent intent = new Intent(this, UserInfoActivity.class);
            intent.putExtra("userInfo", userInfo);
            jumpToActivity(intent);
        });
        mRvContacts.setAdapter(mAdapter);
    }

    @Override
    protected SearchUserAtPresenter createPresenter() {
        return new SearchUserAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_search_user;
    }

    @Override
    public EditText getEtSearchContent() {
        return mEtSearchContent;
    }

    @Override
    public RelativeLayout getRlNoResultTip() {
        return mRlNoResultTip;
    }

    @Override
    public LinearLayout getLlSearch() {
        return mLlSearch;
    }

    @Override
    public void showError(int str){
        UIUtils.showToast(str);
    }

    @Override
    public void onSearchDone(List<UserCard> userCards){
        if (userCards != null && userCards.size() > 0) {
            List<Friend> friends = new ArrayList<>();
            for(UserCard user: userCards){
                Friend friend = new Friend(
                        user.getId(),
                        user.getName(),
                        user.getPortrait(),
                        user.getName() ,
                        null, user.getPhone(), null, null,
                        PinyinUtils.getPinyin(user.getName()),
                        PinyinUtils.getPinyin(user.getName())
                );
                if (TextUtils.isEmpty(friend.getPortraitUri())) {
                    friend.setPortraitUri(DBManager.getInstance().getPortrait(friend));
                }
                friends.add(friend);
            }
            mData.clear();
            mData.addAll(friends);
            //整理排序
            SortUtils.sortContacts(friends);

            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    // 这里是主线程运行时
                    if (mAdapter != null)
                        mAdapter.notifyDataSetChanged();
                }
            });
            mRlNoResultTip.setVisibility(View.GONE);  // 该用户不存在，消失
            mLlSearch.setVisibility(View.GONE);
        }else {
            mRlNoResultTip.setVisibility(View.VISIBLE);
            mLlSearch.setVisibility(View.GONE);
        }

    }
}
