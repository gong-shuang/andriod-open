package com.gs.open.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gs.factory.common.data.DataSource;
import com.gs.factory.data.group.GroupsRepository;
import com.gs.factory.data.helper.GroupHelper;
import com.gs.factory.model.db.Group;
import com.gs.factory.model.db.GroupMember;
import com.gs.factory.persistence.Account;
import com.gs.open.db.model.Friend;
import com.gs.base.util.LogUtils;
import com.gs.base.util.PinyinUtils;
import com.gs.base.util.UIUtils;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.ninegridimageview.LQRNineGridImageView;
import com.lqr.ninegridimageview.LQRNineGridImageViewAdapter;
import com.gs.open.R;
import com.gs.open.db.DBManager;
//import com.gs.open.db.model.GroupMember;
//import com.gs.open.db.model.Groups;
import com.gs.open.ui.activity.SessionActivity;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.IGroupListAtView;

import java.util.ArrayList;
import java.util.List;


public class GroupListAtPresenter extends BasePresenter<IGroupListAtView> {

    private List<Group> mData = new ArrayList<>();  // 群聊
    private LQRAdapterForRecyclerView<Group> mAdapter;
    private LQRNineGridImageViewAdapter mNgivAdapter = new LQRNineGridImageViewAdapter<GroupMember>() {
        @Override
        protected void onDisplayImage(Context context, ImageView imageView, GroupMember groupMember) {
            Glide.with(context).load(groupMember.getUser().getPortrait()).centerCrop().into(imageView);
        }
    };
    GroupsRepository groupsRepository ;  // gs-add

    public GroupListAtPresenter(BaseActivity context) {
        super(context);
        groupsRepository =  new GroupsRepository();
    }

//    private Groups toGroupUI(Group group){
//        Groups groups = new Groups(group.getId(), group.getName(), group.getPicture());
//        groups.setDisplayName(group.getName());
//        groups.setRole(group.getOwner().getId().equals(Account.getUserId()) ? "0" : "1");
//        groups.setBulletin(group.getJoinAt().toString());
//        groups.setTimestamp(group.getModifyAt().toString());
//        groups.setNameSpelling(PinyinUtils.getPinyin(group.getName()));
//
//        return groups;
//    }

//    private GroupMember toGroupMember(com.gs.factory.model.db.GroupMember member){
//        Groups groups =  DBManager.getInstance().getGroupsById(member.getGroup().getId());
//        Friend friend =  DBManager.getInstance().getFriendById(member.getUser().getId());
//        if(groups == null || friend == null){
//            LogUtils.e("groups == null || friend == null");
//            return null;
//        }
//        GroupMember groupMember = new GroupMember(friend.getUserId(), friend.getName(), friend.getPortraitUri());
//        groupMember.setNameSpelling(PinyinUtils.getPinyin(friend.getName() == null ? "null" : friend.getName()));
//        groupMember.setDisplayName(member.getAlias());
//        groupMember.setDisplayNameSpelling(PinyinUtils.getPinyin(member.getAlias() == null ? "null" : member.getAlias()));
//        groupMember.setGroupId(member.getGroup().getId());
//        groupMember.setGroupName(groups.getName());
//        groupMember.setGroupNameSpelling(PinyinUtils.getPinyin(groups.getName() == null ? "null" : groups.getName()));
//        groupMember.setGroupPortrait(groups.getPortraitUri());
//        return groupMember;
//    }

    public void loadGroups() {

        //设置对数据库的监听
        groupsRepository.getDataByDB(new DataSource.SucceedCallback<List<Group>>() {
            @Override
            public void onDataLoaded(List<Group> groups) {
                if(groups!=null && groups.size()!=0){
//                    List<Groups> groupsUI =  new ArrayList<>();
//                    for(Group group :groups){
//                        groupsUI.add(toGroupUI(group));
//                    }
                    //保存到lqr_wechat 数据库中。
//                    DBManager.getInstance().deleteGroups(); //先清除
//                    DBManager.getInstance().saveGroupsAll(groupsUI); //再保存。

                    mData.clear();
                    mData.addAll(groups);

                    LogUtils.e("ddddd: " + this);

                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Stuff that updates the UI
                            setAdapter();
                            getView().getLlGroups().setVisibility(View.VISIBLE);

                        }
                    });

                    //更新群成员的信息
                    List<com.gs.factory.model.db.GroupMember> models = GroupHelper.getMemberUsersAll();
                    LogUtils.e("All group member,count= " + models.size());
                    for(com.gs.factory.model.db.GroupMember member : models){
                        if(member.getGroup() != null || member.getUser() != null){
                            //保存到数据库
//                            DBManager.getInstance().saveOrUpdateGroupMember(toGroupMember(member));
                        }
                    }
                }
            }
        });
        loadData();
        setAdapter();
    }

    private void loadData() {
//        List<Groups> groups = DBManager.getInstance().getGroups();
//        if (groups != null && groups.size() > 0) {
//            mData.clear();
//            mData.addAll(groups);
//            setAdapter();
//            getView().getLlGroups().setVisibility(View.VISIBLE);
//        } else {
//            getView().getLlGroups().setVisibility(View.GONE);
//        }

        // 加载网络数据, 以后可以优化到下拉刷新中
        // 只有用户下拉进行网络请求刷新
        GroupHelper.refreshGroups();
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<Group>(mContext, mData, R.layout.item_contact) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Group item, int position) {
                    LQRNineGridImageView ngvi = helper.setViewVisibility(R.id.ngiv, View.VISIBLE)
                            .setViewVisibility(R.id.ivHeader, View.GONE)
                            .setText(R.id.tvName, item.getName())
                            .getView(R.id.ngiv);
                    ngvi.setAdapter(mNgivAdapter);
//                    List<GroupMember> groupMembers = DBManager.getInstance().getGroupMembers(item.getGroupId());
                    List<GroupMember> groupMembers = GroupHelper.getMemberFromGroup(item.getId());
                    ngvi.setImagesData(groupMembers);
                }
            };
            mAdapter.setOnItemClickListener((lqrViewHolder, viewGroup, view, i) -> {
                Intent intent = new Intent(mContext, SessionActivity.class);
                intent.putExtra("sessionId", mData.get(i).getId());
                intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_GROUP);
                mContext.jumpToActivity(intent);
                mContext.finish();
            });
            getView().getRvGroupList().setAdapter(mAdapter);
        }
        mAdapter.notifyDataSetChangedWrapper();
    }

    public void dispose(){
        //取消监听
        groupsRepository.dispose();
    }
}
