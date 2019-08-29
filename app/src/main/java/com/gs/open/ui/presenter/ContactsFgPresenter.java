package com.gs.open.ui.presenter;

import android.content.Intent;
import android.support.v7.util.DiffUtil;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gs.factory.Factory;
import com.gs.factory.common.data.DataSource;
import com.gs.factory.data.helper.UserHelper;
import com.gs.factory.data.user.ContactDataSource;
import com.gs.factory.data.user.ContactRepository;
import com.gs.factory.model.db.User;
import com.gs.open.util.PinyinUtils;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRHeaderAndFooterAdapter;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.gs.open.R;
import com.gs.open.db.DBManager;
import com.gs.open.db.model.Friend;
import com.gs.open.ui.activity.UserInfoActivity;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.IContactsFgView;
import com.gs.open.util.LogUtils;
import com.gs.open.util.SortUtils;
import com.gs.open.util.UIUtils;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ContactsFgPresenter extends BasePresenter<IContactsFgView> {

    private List<Friend> mData = new ArrayList<>();
    private List<User> mDataByUser = new ArrayList<>();
    private LQRHeaderAndFooterAdapter mAdapter;
    private ContactRepository contactRepository; //gs-add

    /**
     * 构造函数，
     * 在构造函数里，设置对数据库的监听操作。
     * @param context
     */
    public ContactsFgPresenter(BaseActivity context) {
        super(context);
//        LogUtils.e("只能初始化一次，超过一次就报错。");
        //数据库的监听操作。
        contactRepository = new ContactRepository();
    }

    public void loadContacts() {
        contactRepository.load(new DataSource.SucceedCallback<List<User>>(){
            // 运行到这里的时候是子线程
            @Override
            public void onDataLoaded(List<User> users) {
                LogUtils.d("loadContacts(), thread: " + Thread.currentThread().getName());
                if (users != null && users.size() > 0) {
                    List<Friend> friends = new ArrayList<>();
                    for(User user: users){
                        Friend friend = new Friend(
                                user.getId(),
                                user.getName(),
                                user.getPortrait(),
                                TextUtils.isEmpty(user.getAlias()) ? user.getName() : user.getAlias(),
                                null, user.getPhone(), null, null,
                                PinyinUtils.getPinyin(user.getName()),
                                PinyinUtils.getPinyin(TextUtils.isEmpty(user.getAlias()) ? user.getName() : user.getAlias())
                        );
                        if (TextUtils.isEmpty(friend.getPortraitUri())) {
                            friend.setPortraitUri(DBManager.getInstance().getPortrait(friend));
                        }
                        friends.add(friend);
                    }
                    mData.clear();
                    mData.addAll(friends);
                    if(getView() == null)
                        return;
                    getView().getFooterView().setText(UIUtils.getString(R.string.count_of_contacts, mData.size()));
                    //整理排序
                    SortUtils.sortContacts(mData);

                    Run.onUiAsync(new Action() {
                        @Override
                        public void call() {
                            // 这里是主线程运行时
                            if (mAdapter != null)
                                mAdapter.notifyDataSetChanged();
                        }
                    });

                    //数据库操作，这里不能全部删除，因为还存储了本身的信息。
                    DBManager.getInstance().saveOrUpdateFriendsByFriends(mData);

                }
            }
        });
        setAdapter();
        loadData();
    }

    private void loadData() {
//        Observable.just(DBManager.getInstance().getFriends())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(friends -> {
//                    if (friends != null && friends.size() > 0) {
//                        mData.clear();
//                        mData.addAll(friends);
//                        getView().getFooterView().setText(UIUtils.getString(R.string.count_of_contacts, mData.size()));
//                        //整理排序
//                        SortUtils.sortContacts(mData);
//                        if (mAdapter != null)
//                            mAdapter.notifyDataSetChanged();
//                    }
//                }, this::loadError);

        // 加载网络数据
        UserHelper.refreshContacts();
    }

    private void setAdapter() {
        if (mAdapter == null) {
            LQRAdapterForRecyclerView adapter = new LQRAdapterForRecyclerView<Friend>(mContext, mData, R.layout.item_contact) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Friend item, int position) {
                    helper.setText(R.id.tvName, item.getDisplayName());
                    ImageView ivHeader = helper.getView(R.id.ivHeader);
                    Glide.with(mContext).load(item.getPortraitUri()).centerCrop().into(ivHeader);

                    String str = "";
                    //得到当前字母
                    String currentLetter = item.getDisplayNameSpelling().charAt(0) + "";
                    if (position == 0) {
                        str = currentLetter;
                    } else {
                        //得到上一个字母
                        String preLetter = mData.get(position - 1).getDisplayNameSpelling().charAt(0) + "";
                        //如果和上一个字母的首字母不同则显示字母栏
                        if (!preLetter.equalsIgnoreCase(currentLetter)) {
                            str = currentLetter;
                        }
                    }
                    int nextIndex = position + 1;
                    if (nextIndex < mData.size() - 1) {
                        //得到下一个字母
                        String nextLetter = mData.get(nextIndex).getDisplayNameSpelling().charAt(0) + "";
                        //如果和下一个字母的首字母不同则隐藏下划线
                        if (!nextLetter.equalsIgnoreCase(currentLetter)) {
                            helper.setViewVisibility(R.id.vLine, View.INVISIBLE);
                        } else {
                            helper.setViewVisibility(R.id.vLine, View.VISIBLE);
                        }
                    } else {
                        helper.setViewVisibility(R.id.vLine, View.INVISIBLE);
                    }
                    if (position == mData.size() - 1) {
                        helper.setViewVisibility(R.id.vLine, View.GONE);
                    }

                    //根据str是否为空决定字母栏是否显示
                    if (TextUtils.isEmpty(str)) {
                        helper.setViewVisibility(R.id.tvIndex, View.GONE);
                    } else {
                        helper.setViewVisibility(R.id.tvIndex, View.VISIBLE);
                        helper.setText(R.id.tvIndex, str);
                    }
                }
            };
            adapter.addHeaderView(getView().getHeaderView());
            adapter.addFooterView(getView().getFooterView());
            mAdapter = adapter.getHeaderAndFooterAdapter();
            getView().getRvContacts().setAdapter(mAdapter);
        }
        ((LQRAdapterForRecyclerView) mAdapter.getInnerAdapter()).setOnItemClickListener((lqrViewHolder, viewGroup, view, i) -> {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            intent.putExtra("userInfo", DBManager.getInstance().getUserInfo(mData.get(i - 1).getUserId()));//-1是因为有头部
            mContext.jumpToActivity(intent);
        });
    }

    private void loadError(Throwable throwable) {
        LogUtils.e(throwable.getLocalizedMessage());
        UIUtils.showToast(UIUtils.getString(R.string.load_contacts_error));
    }
}
