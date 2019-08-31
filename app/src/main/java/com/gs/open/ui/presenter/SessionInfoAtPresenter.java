package com.gs.open.ui.presenter;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.gs.factory.common.data.DataSource;
import com.gs.factory.data.helper.GroupHelper;
import com.gs.factory.model.api.group.GroupCreateModel;
import com.gs.factory.model.api.group.GroupMemberAddModel;
import com.gs.factory.model.api.group.GroupMemberDelModel;
import com.gs.factory.model.card.GroupCard;
import com.gs.factory.model.card.GroupMemberCard;
import com.gs.factory.model.card.GroupResponseCard;
import com.gs.factory.persistence.Account;
import com.gs.open.db.model.Friend;
import com.gs.open.temp.Conversation;
import com.gs.open.temp.UserInfo;
import com.gs.open.ui.activity.MainActivity;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.gs.open.R;
import com.gs.open.api.ApiRetrofit;
import com.gs.open.app.AppConst;
import com.gs.open.db.DBManager;
import com.gs.open.db.model.GroupMember;
import com.gs.open.db.model.Groups;
import com.gs.open.manager.BroadcastManager;
import com.gs.open.model.cache.UserCache;
import com.gs.open.model.response.QuitGroupResponse;
import com.gs.open.ui.activity.CreateGroupActivity;
import com.gs.open.ui.activity.RemoveGroupMemberActivity;
import com.gs.open.ui.activity.SessionInfoActivity;
import com.gs.open.ui.activity.UserInfoActivity;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.ISessionInfoAtView;
import com.gs.open.util.LogUtils;
import com.gs.open.util.PinyinUtils;
import com.gs.open.util.UIUtils;
import com.gs.open.widget.CustomDialog;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.gs.open.ui.activity.SessionActivity.SESSION_TYPE_GROUP;
import static com.gs.open.ui.activity.SessionActivity.SESSION_TYPE_PRIVATE;


public class SessionInfoAtPresenter extends BasePresenter<ISessionInfoAtView> {


    private Conversation.ConversationType mConversationType;
    private String mSessionId;
    private List<GroupMember> mData = new ArrayList<>();
    private LQRAdapterForRecyclerView<GroupMember> mAdapter;
    private boolean mIsManager = false;
    public boolean mIsCreateNewGroup = false;
    public String mDisplayName = "";
    private CustomDialog mSetDisplayNameDialog;
    private Groups mGroups;
    private Observable<QuitGroupResponse> quitGroupResponseObservable = null;

    public SessionInfoAtPresenter(BaseActivity context, String sessionId, Conversation.ConversationType conversationType) {
        super(context);
        mSessionId = sessionId;
        mConversationType = conversationType;
    }

    public void loadMembers() {
        loadData();
        setAdapter();
    }

    private void loadData() {
        if (mConversationType == Conversation.ConversationType.PRIVATE) {
            UserInfo userInfo = DBManager.getInstance().getUserInfo(mSessionId);
            if (userInfo != null) {
                mData.clear();
                GroupMember newMember = new GroupMember(mSessionId,
                        userInfo.getUserId(),
                        userInfo.getName(),
                        userInfo.getPortraitUri().toString(),
                        userInfo.getName(),
                        PinyinUtils.getPinyin(userInfo.getName()),
                        PinyinUtils.getPinyin(userInfo.getName()),
                        "",
                        "",
                        "");
                mData.add(newMember);
                mData.add(new GroupMember("", "", ""));//+
            }
            mIsCreateNewGroup = true;
        } else {
            List<GroupMember> groupMembers = DBManager.getInstance().getGroupMembers(mSessionId);
            if (groupMembers != null && groupMembers.size() > 0) {
                Groups groupsById = DBManager.getInstance().getGroupsById(mSessionId);
                if (groupsById != null && groupsById.getRole().equals("0")) {
                    mIsManager = true;
                }
                mData.clear();
                mData.addAll(groupMembers);
                mData.add(new GroupMember("", "", ""));//+
                if (mIsManager) {
                    mData.add(new GroupMember("", "", ""));//-
                }
            }
            mIsCreateNewGroup = false;
        }
        setAdapter();
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<GroupMember>(mContext, mData, R.layout.item_member_info) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, GroupMember item, int position) {
                    ImageView ivHeader = helper.getView(R.id.ivHeader);
                    if (mIsManager && position >= mData.size() - 2) {//+和-
                        if (position == mData.size() - 2) {//+
                            ivHeader.setImageResource(R.mipmap.ic_add_team_member);
                        } else {//-
                            ivHeader.setImageResource(R.mipmap.ic_remove_team_member);
                        }
                        helper.setText(R.id.tvName, "");
                    } else if (!mIsManager && position >= mData.size() - 1) {//+
                        ivHeader.setImageResource(R.mipmap.ic_add_team_member);
                        helper.setText(R.id.tvName, "");
                    } else {
                        Glide.with(mContext).load(item.getPortraitUri()).centerCrop().into(ivHeader);
                        helper.setText(R.id.tvName, item.getName());
                    }
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
                if (mIsManager && position >= mData.size() - 2) {//+和-
                    //是群管理员
                    if (position == mData.size() - 2) {//+
                        addMember(mConversationType == Conversation.ConversationType.GROUP);
                    } else {//-
                        removeMember();
                    }
                } else if (!mIsManager && position >= mData.size() - 1) {//+
                    //不是群管理员
                    addMember(mConversationType == Conversation.ConversationType.GROUP);
                } else {
                    seeUserInfo(DBManager.getInstance().getUserInfo(mData.get(position).getUserId()));
                }
            });
            getView().getRvMember().setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }

    //点击“+”号键，添加新的成员
    private void addMember(boolean isAddMember) {

        Intent intent = new Intent(mContext, CreateGroupActivity.class);

        //如果是群组的话就把当前已经的群成员发过去
        if (isAddMember) {
            ArrayList<String> selectedTeamMemberAccounts = new ArrayList<>();
            String id;
            for (int i = 0; i < mData.size(); i++) {
                id = mData.get(i).getUserId();
                //最后两个是“+” “—”
                if(id != null){
                    selectedTeamMemberAccounts.add(id);
                }
            }
            intent.putExtra("selectedMember", selectedTeamMemberAccounts);
        }

        mContext.startActivityForResult(intent, SessionInfoActivity.REQ_ADD_MEMBERS);
    }

    //点击“-”号键，删除成员
    private void removeMember() {
        Intent intent = new Intent(mContext, RemoveGroupMemberActivity.class);
        intent.putExtra("sessionId", mSessionId);
        mContext.startActivityForResult(intent, SessionInfoActivity.REQ_REMOVE_MEMBERS);
    }

    private void seeUserInfo(UserInfo userInfo) {
        Intent intent = new Intent(mContext, UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        mContext.jumpToActivity(intent);
    }

    public void addGroupMember(ArrayList<String> selectedIds) {
        LogUtils.sf("addGroupMember : " + selectedIds);
//        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
//        ApiRetrofit.getInstance().addGroupMember(mSessionId, selectedIds)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(addGroupMemberResponse -> {
//                    if (addGroupMemberResponse != null && addGroupMemberResponse.getCode() == 200) {
//                        LogUtils.sf("网络请求成功，开始添加群成员：");
//                        Groups groups = DBManager.getInstance().getGroupsById(mSessionId);
//                        for (String groupMemberId : selectedIds) {
//                            UserInfo userInfo = DBManager.getInstance().getUserInfo(groupMemberId);
//                            if (userInfo != null) {
//                                GroupMember newMember = new GroupMember(mSessionId,
//                                        userInfo.getUserId(),
//                                        userInfo.getName(),
//                                        userInfo.getPortraitUri().toString(),
//                                        userInfo.getName(),
//                                        PinyinUtils.getPinyin(userInfo.getName()),
//                                        PinyinUtils.getPinyin(userInfo.getName()),
//                                        groups.getName(),
//                                        PinyinUtils.getPinyin(groups.getName()),
//                                        groups.getPortraitUri());
//                                DBManager.getInstance().saveOrUpdateGroupMember(newMember);
//                                LogUtils.sf("添加群成员成功");
//                            }
//                        }
//                        LogUtils.sf("添加群成员结束");
//                        mContext.hideWaitingDialog();
//                        loadData();
//                        LogUtils.sf("重新加载数据");
//                        UIUtils.showToast(UIUtils.getString(R.string.add_member_success));
//                    }
//                }, this::addMembersError);

        if(selectedIds.size() == 0 || mConversationType == Conversation.ConversationType.PRIVATE)
            return;

        Set<String> users = new HashSet<>();
        for (String s: selectedIds){
            users.add(s);
        }

        // 进行网络请求
        GroupMemberAddModel model = new GroupMemberAddModel(users);
        GroupHelper.addMembers(mSessionId, model, new DataSource.Callback<List<GroupMemberCard>>() {
            @Override
            public void onDataNotAvailable(int strRes) {
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        UIUtils.showToast(strRes);
                    }
                });
            }

            @Override
            public void onDataLoaded(List<GroupMemberCard> groupMemberCards) {
                //返回新添加的成员
                //更新本地保存的组信息
                for(GroupMemberCard memberCard: groupMemberCards){
                    //保存到数据库
                    DBManager.getInstance().saveOrUpdateGroupMember(toGroupMember(memberCard));
                }
                //更新UI
                loadData();
            }
        });
    }

    //转化为GroupMember格式
    private GroupMember toGroupMember(GroupMemberCard member){
        Groups groups =  DBManager.getInstance().getGroupsById(member.getGroupId());
        Friend friend =  DBManager.getInstance().getFriendById(member.getUserId());
        if(groups == null || friend == null){
            LogUtils.e("groups == null || friend == null");
            return null;
        }
        GroupMember groupMember = new GroupMember(friend.getUserId(), friend.getName(), friend.getPortraitUri());
        groupMember.setNameSpelling(PinyinUtils.getPinyin(friend.getName() == null ? "null" : friend.getName()));
        groupMember.setDisplayName(member.getAlias());
        groupMember.setDisplayNameSpelling(PinyinUtils.getPinyin(member.getAlias() == null ? "null" : member.getAlias()));
        groupMember.setGroupId(member.getGroupId());
        groupMember.setGroupName(groups.getName());
        groupMember.setGroupNameSpelling(PinyinUtils.getPinyin(groups.getName() == null ? "null" : groups.getName()));
        groupMember.setGroupPortrait(groups.getPortraitUri());
        return groupMember;
    }

    public void deleteGroupMembers(ArrayList<String> selectedIds) {
//        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
//        ApiRetrofit.getInstance().deleGroupMember(mSessionId, selectedIds)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(deleteGroupMemberResponse -> {
//                    if (deleteGroupMemberResponse != null && deleteGroupMemberResponse.getCode() == 200) {
//                        LogUtils.sf("网络请求成功，开始删除：");
//                        for (int i = 0; i < mData.size(); i++) {
//                            GroupMember member = mData.get(i);
//                            if (selectedIds.contains(member.getUserId())) {
//                                LogUtils.sf("删除用户：" + member.getUserId());
//                                member.delete();
//                                mData.remove(i);
//                                i--;
//                            }
//                        }
//                        LogUtils.sf("删除结束");
//                        mContext.hideWaitingDialog();
//                        setAdapter();
//                        UIUtils.showToast(UIUtils.getString(R.string.del_member_success));
//                    } else {
//                        LogUtils.sf("网络请求失败");
//                        mContext.hideWaitingDialog();
//                        UIUtils.showToast(UIUtils.getString(R.string.del_member_fail));
//                    }
//                }, this::delMembersError);

        if(selectedIds.size() == 0 || mConversationType == Conversation.ConversationType.PRIVATE)
            return;

        Set<String> users = new HashSet<>();
        for (String s: selectedIds){
            users.add(s);
        }

        // 进行网络请求
        GroupMemberDelModel model = new GroupMemberDelModel(users);
        GroupHelper.delMembers(mSessionId, model, new DataSource.Callback<List<GroupMemberCard>>() {
            @Override
            public void onDataNotAvailable(int strRes) {
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        UIUtils.showToast(strRes);
                    }
                });
            }

            @Override
            public void onDataLoaded(List<GroupMemberCard> groupMemberCards) {
                //返回新添加的成员
                //更新本地保存的组信息
                for(GroupMemberCard memberCard: groupMemberCards){
                    //保存到数据库
                    DBManager.getInstance().deleteGroupMembers(toGroupMember(memberCard));
                }
                //更新UI
                loadData();
            }
        });
    }

    private void addMembersError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        mContext.hideWaitingDialog();
        UIUtils.showToast(UIUtils.getString(R.string.add_member_fail));
    }

    private void delMembersError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        mContext.hideWaitingDialog();
        UIUtils.showToast(UIUtils.getString(R.string.del_member_fail));
    }

    public void loadOtherInfo(int sessionType, String sessionId) {
        setToTop();
        switch (sessionType) {
            case SESSION_TYPE_PRIVATE:

                break;
            case SESSION_TYPE_GROUP:
                Observable.just(DBManager.getInstance().getGroupsById(sessionId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(groups -> {
                            if (groups == null)
                                return;
                            mGroups = groups;
                            //设置群信息
                            getView().getOivGroupName().setRightText(groups.getName());
                            mDisplayName = TextUtils.isEmpty(groups.getDisplayName()) ?
                                    DBManager.getInstance().getUserInfo(UserCache.getId()).getName() :
                                    groups.getDisplayName();
                            getView().getOivNickNameInGroup().setRightText(mDisplayName);
                            getView().getBtnQuit().setText(groups.getRole().equals("0") ? UIUtils.getString(R.string.dismiss_this_group) :
                                    UIUtils.getString(R.string.delete_and_exit));
                        }, this::loadOtherError);
                break;
        }
    }

    private void setToTop() {
//        Observable.just(RongIMClient.getInstance().getConversation(mConversationType, mSessionId))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(conversation -> {
//                    if (conversation != null) {
//                        getView().getSbToTop().setChecked(conversation.isTop());
//                    }
//                });
        LogUtils.d("setToTop()");
    }

    private void loadOtherError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
    }

    //退出该群
    public void quit() {
        if (mGroups == null)
            return;
        String tip = "";
        if (mGroups.getRole().equalsIgnoreCase("0")) {
            tip = UIUtils.getString(R.string.are_you_sure_to_dismiss_this_group);
            quitGroupResponseObservable = ApiRetrofit.getInstance().dissmissGroup(mSessionId);
        } else {
            tip = UIUtils.getString(R.string.you_will_never_receive_any_msg_after_quit);
            quitGroupResponseObservable = ApiRetrofit.getInstance().quitGroup(mSessionId);
        }
        mContext.showMaterialDialog(null, tip, UIUtils.getString(R.string.sure), UIUtils.getString(R.string.cancel)
//                , v -> quitGroupResponseObservable
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(quitGroupResponse -> {
//                            mContext.hideMaterialDialog();
//                            if (quitGroupResponse != null && quitGroupResponse.getCode() == 200) {
////                                RongIMClient.getInstance().getConversation(mConversationType, mSessionId, new RongIMClient.ResultCallback<Conversation>() {
////                                    @Override
////                                    public void onSuccess(Conversation conversation) {
////                                        RongIMClient.getInstance().clearMessages(Conversation.ConversationType.GROUP, mSessionId, new RongIMClient.ResultCallback<Boolean>() {
////                                            @Override
////                                            public void onSuccess(Boolean aBoolean) {
////                                                RongIMClient.getInstance().removeConversation(mConversationType, mSessionId, null);
////                                            }
////
////                                            @Override
////                                            public void onError(RongIMClient.ErrorCode errorCode) {
////
////                                            }
////                                        });
////                                    }
////
////                                    @Override
////                                    public void onError(RongIMClient.ErrorCode errorCode) {
////
////                                    }
////                                });
//                                DBManager.getInstance().deleteGroupMembersByGroupId(mSessionId);
//                                DBManager.getInstance().deleteGroupsById(mSessionId);
//                                BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
//                                BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_GROUP);
//                                BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.CLOSE_CURRENT_SESSION);
//                                mContext.finish();
//                            } else {
//                                UIUtils.showToast(UIUtils.getString(R.string.exit_group_fail));
//                            }
//                        }, this::quitError)
                , v ->quitGroupSendRequest()
                , v -> mContext.hideMaterialDialog());
    }

    //发送网络请求
    void quitGroupSendRequest(){
        mContext.hideMaterialDialog();

        if(mConversationType == Conversation.ConversationType.PRIVATE)
            return;

        // 进行网络请求
        GroupHelper.quitGroup(mSessionId, new DataSource.Callback<GroupResponseCard>() {
                    @Override
                    public void onDataNotAvailable(int strRes) {
                        Run.onUiAsync(new Action() {
                            @Override
                            public void call() {
                                UIUtils.showToast(strRes);
                            }
                        });
                    }

                    @Override
                    public void onDataLoaded(GroupResponseCard groupResponseCard) {
                        //退出群的信息
                        //不用更新本地保存的组信息
                        //也不用更新UI
                        //直接退到主界面
                        mContext.jumpToActivityAndClearTask(MainActivity.class);
                        mContext.finish();
                    }
                }
//                new DataSource.Callback<List<GroupMemberCard>>() {
//            @Override
//            public void onDataNotAvailable(int strRes) {
//                Run.onUiAsync(new Action() {
//                    @Override
//                    public void call() {
//                        UIUtils.showToast(strRes);
//                    }
//                });
//            }
//
//            @Override
//            public void onDataLoaded(List<GroupMemberCard> groupMemberCards) {
//                //返回新添加的成员
//                //更新本地保存的组信息
//                for(GroupMemberCard memberCard: groupMemberCards){
//                    //保存到数据库
//                    DBManager.getInstance().deleteGroupMembers(toGroupMember(memberCard));
//                }
//                //更新UI
//                loadData();
//            }
//        }
        );
    }

    private void quitError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        UIUtils.showToast(UIUtils.getString(R.string.exit_group_fail));
    }

    public void clearConversationMsg() {
//        mContext.showMaterialDialog(null, UIUtils.getString(R.string.are_you_sure_to_clear_msg_record), UIUtils.getString(R.string.clear), UIUtils.getString(R.string.cancel)
//                , v1 -> RongIMClient.getInstance().clearMessages(mConversationType, mSessionId, new RongIMClient.ResultCallback<Boolean>() {
//                    @Override
//                    public void onSuccess(Boolean aBoolean) {
//                        mContext.hideMaterialDialog();
//                        BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
//                        BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.REFRESH_CURRENT_SESSION);
//                    }
//
//                    @Override
//                    public void onError(RongIMClient.ErrorCode errorCode) {
//                        mContext.hideMaterialDialog();
//                    }
//                }), v2 -> mContext.hideMaterialDialog());
    }

    public void setDisplayName() {
        View view = View.inflate(mContext, R.layout.dialog_group_display_name_change, null);
        mSetDisplayNameDialog = new CustomDialog(mContext, view, R.style.MyDialog);
        EditText etName = (EditText) view.findViewById(R.id.etName);
        etName.setText(mDisplayName);
        etName.setSelection(mDisplayName.length());
        view.findViewById(R.id.tvCancle).setOnClickListener(v -> mSetDisplayNameDialog.dismiss());
        view.findViewById(R.id.tvOk).setOnClickListener(v -> {
            String displayName = etName.getText().toString().trim();
            if (!TextUtils.isEmpty(displayName)) {
                ApiRetrofit.getInstance().setGroupDisplayName(mSessionId, displayName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(setGroupDisplayNameResponse -> {
                            if (setGroupDisplayNameResponse != null && setGroupDisplayNameResponse.getCode() == 200) {
                                Groups groups = DBManager.getInstance().getGroupsById(mSessionId);
                                if (groups != null) {
                                    groups.setDisplayName(displayName);
                                    groups.saveOrUpdate("groupid=?", groups.getGroupId());
                                    mDisplayName = displayName;
                                    getView().getOivNickNameInGroup().setRightText(mDisplayName);
                                }
                                UIUtils.showToast(UIUtils.getString(R.string.change_success));
                            } else {
                                UIUtils.showToast(UIUtils.getString(R.string.change_fail));
                            }
                            mSetDisplayNameDialog.dismiss();
                        }, this::setDisplayNameError);
            }
        });
        mSetDisplayNameDialog.show();
    }


    private void setDisplayNameError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        UIUtils.showToast(UIUtils.getString(R.string.change_fail));
        mSetDisplayNameDialog.dismiss();
    }
}
