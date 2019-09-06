package com.gs.open.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gs.factory.common.data.DataSource;
import com.gs.factory.data.helper.DbHelper;
import com.gs.factory.data.helper.GroupHelper;
import com.gs.factory.data.helper.MessageHelper;
import com.gs.factory.data.helper.UserHelper;
import com.gs.factory.data.message.SessionRepository;
import com.gs.factory.manager.MyMessageHandler;
import com.gs.factory.model.db.GroupMember;
import com.gs.factory.model.db.Message;
import com.gs.factory.model.db.Session;
import com.gs.factory.model.db.User;
import com.gs.factory.persistence.Account;
import com.gs.open.db.model.Friend;
//import com.gs.open.temp.Conversation;
//import com.gs.open.temp.FileMessage;
//import com.gs.open.temp.ImageMessage;
//import com.gs.open.temp.LocationMessage;
//import com.gs.open.temp.TextMessage;
//import com.gs.open.temp.UserInfo;
//import com.gs.open.temp.VoiceMessage;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.emoji.MoonUtils;
import com.lqr.ninegridimageview.LQRNineGridImageView;
import com.lqr.ninegridimageview.LQRNineGridImageViewAdapter;
import com.gs.open.R;
import com.gs.open.db.DBManager;
//import com.gs.open.db.model.GroupMember;
//import com.gs.open.db.model.Groups;
import com.gs.open.ui.activity.MainActivity;
import com.gs.open.ui.activity.SessionActivity;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.IRecentMessageFgView;
import com.gs.base.util.LogUtils;
import com.gs.base.util.MediaFileUtils;
import com.gs.base.util.TimeUtils;
import com.gs.base.util.UIUtils;
import com.gs.open.widget.CustomDialog;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RecentMessageFgPresenter extends BasePresenter<IRecentMessageFgView> {

//    private List<Conversation> mData = new ArrayList<>();   //这个是会话的数据。
    private List<Session> mData = new ArrayList<>();
    private LQRAdapterForRecyclerView<Session> mAdapter;
    private int mUnreadCountTotal = 0;
    private LQRNineGridImageViewAdapter mNgivAdapter = new LQRNineGridImageViewAdapter<GroupMember>() {
        @Override
        protected void onDisplayImage(Context context, ImageView imageView, GroupMember groupMember) {
            Glide.with(context).load(groupMember.getUser().getPortrait()).centerCrop().into(imageView);
        }
    };
    private CustomDialog mConversationMenuDialog;
    SessionRepository sessionRepository;

    public RecentMessageFgPresenter(BaseActivity context) {
        super(context);
        sessionRepository = new SessionRepository();
        DbHelper.setSessionCallback(new DataSource.SucceedCallback<Session>() {
            @Override
            public void onDataLoaded(Session session) {
                int index = -1;
                for (int i = 0; i < mData.size(); i++) {
                    if (session.getId().equals(mData.get(i).getId())) {
                        index = i;
                        break;
                    }
                }

                if (index == -1) {
                    //新增
                    mData.add(0, session);
                } else {
                    //更新
                    mData.remove(index);
                    mData.add(0, session);
                }

                UIUtils.postTaskSafely(new Runnable() {
                    @Override
                    public void run() {
                        filterData(mData);
                    }
                });
            }

        });
    }


    // 获取会话
    public void getConversations() {
        loadData();  // 从本地数据库中获取
        setAdapter();  // 更新view。
    }

//    Conversation toConversation(Session session){
//        Conversation conversation = new Conversation();
//        conversation.setTargetId(session.getId());
//        conversation.setConversationTitle(session.getTitle());
//        conversation.setPortraitUrl(session.getPicture());
//        conversation.setUnreadMessageCount(session.getUnReadCount());
//        //消息
//        Message message = MessageHelper.findFromLocal(session.getMessage().getId());
//        if(message.getSender().getId().equals(Account.getUserId())){
//            //发送
//            conversation.setSentStatus(message.getStatus() == Message.STATUS_DONE ?
//                    com.gs.open.temp.Message.SentStatus.SENT :
//                    com.gs.open.temp.Message.SentStatus.SENDING);
//            conversation.setSentTime(session.getModifyAt().getTime());
//            conversation.setSenderUserId(Account.getUserId());
//            conversation.setSenderUserName(Account.getUser().getName());
//        }else{
//            //接受
//            conversation.setReceivedStatus(message.getStatus() == Message.STATUS_DONE ?
//                     new com.gs.open.temp.Message.ReceivedStatus(1) :
//                    new com.gs.open.temp.Message.ReceivedStatus(2) );
//            conversation.setReceivedTime(session.getModifyAt().getTime());
//        }
//        int type = message.getType();
//        if(type == Message.TYPE_STR){
//            conversation.setLatestMessage(TextMessage.obtain(message.getContent()));
//        }else if(type == Message.TYPE_PIC){
//            conversation.setLatestMessage(ImageMessage.obtain(Uri.parse(message.getContent()), Uri.parse(message.getContent())));
//        }else if(type == Message.TYPE_FILE){
//            FileMessage fileMessage = FileMessage.obtainByOSS(message.getContent());
//            if(fileMessage == null)
//                return null;
//            conversation.setLatestMessage(fileMessage);
//        }else if(type == Message.TYPE_AUDIO){
//            int  value = Integer.parseInt(message.getAttach());
//            if(value == 0)
//                return null;
//            conversation.setLatestMessage(VoiceMessage.obtain(Uri.parse(message.getContent()),value /1000));
//        }else if(type == Message.TYPE_LOCATION){
////            conversation.setLatestMessage(new LocationMessage(message.getContent()));
//        }else{
//            return null;
//        }
//
//        if(session.getReceiverType() == Message.RECEIVER_TYPE_NONE) {
//            //个人
//            conversation.setConversationType(Conversation.ConversationType.PRIVATE);
//            Friend friend =  DBManager.getInstance().getFriendById(session.getId());
//            if(friend == null)
//                return null;
//            conversation.setSenderUserId(friend.getUserId());
//            conversation.setSenderUserName(friend.getName());
//
//        }else{
//            //组
//            conversation.setConversationType(Conversation.ConversationType.GROUP);
//            Groups groups =  DBManager.getInstance().getGroupsById(session.getId());
//            if(groups == null)
//                return null;
//            conversation.setSenderUserId(groups.getGroupId());
//            conversation.setSenderUserName(groups.getName());
//        }
//
//        return conversation;
//    }

    /**
     * 从本地数据库中获取。
     */
    private void loadData() {
//        RongIMClient.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
//            @Override
//            public void onSuccess(List<Conversation> conversations) {
//                if (conversations != null && conversations.size() > 0) {
//                    mData.clear();
//                    mData.addAll(conversations);
//                    filterData(mData);
//                }
//            }
//
//            @Override
//            public void onError(RongIMClient.ErrorCode errorCode) {
//                LogUtils.e("加载最近会话失败：" + errorCode);
//            }
//        });
        sessionRepository.getDataByDB(new DataSource.SucceedCallback<List<Session>>() {
            @Override
            public void onDataLoaded(List<Session> sessions) {
                if (sessions != null && sessions.size() != 0) {
//                    List<Conversation> conversations = new ArrayList<>();
//                    for (Session session : sessions) {
//                        Conversation conversation = toConversation(session);
//                        if (conversation != null) {
//                            conversations.add(conversation);
//                        }
//                    }
//                    if (conversations.size() == 0)
//                        return;
                    mData.clear();
                    mData.addAll(sessions);
                    UIUtils.postTaskSafely(new Runnable() {
                        @Override
                        public void run() {
                            filterData(mData);
                        }
                    });
                }
            }
        });
    }

    private void filterData(List<Session> conversations) {
        for (int i = 0; i < conversations.size(); i++) {
            Session item = conversations.get(i);
            //其他消息会话不显示（比如：系统消息）
//            if (!(item.getConversationType() == Conversation.ConversationType.PRIVATE || item.getConversationType() == Conversation.ConversationType.GROUP)) {
//                conversations.remove(i);
//                i--;
//                continue;
//            }
            if (item.getReceiverType() == Message.RECEIVER_TYPE_GROUP) {
 //               List<GroupMember> groupMembers = DBManager.getInstance().getGroupMembers(item.getTargetId());
                long count = GroupHelper.getMemberCount(item.getId());
//                List<GroupMember> groupMembers = DBManager.getInstance().getGroupMembers(item.getTargetId());
//                if (groupMembers == null || groupMembers.size() == 0) {
//                    DBManager.getInstance().deleteGroupsById(item.getTargetId());//删除没有群成员的群
                if(count == 0 ){
                    conversations.remove(i);
                    i--;
                }
            } else if (item.getReceiverType() == Message.RECEIVER_TYPE_NONE) {
                if (UserHelper.findFromLocal(item.getId()).getRole() != User.ROLE_FRIEND) {
                    conversations.remove(i);
                    i--;
                }
            }
        }
        mUnreadCountTotal = 0;
        for (Session conversation : conversations) {
            mUnreadCountTotal += conversation.getUnReadCount();
        }
        updateTotalUnreadView();
        if (mAdapter != null)
            mAdapter.notifyDataSetChangedWrapper();
    }

    // 更新未读消息。
    private void updateTotalUnreadView() {
        if (mUnreadCountTotal > 0) {
            ((MainActivity) mContext).getTvMessageCount().setText(mUnreadCountTotal + "");
            ((MainActivity) mContext).getTvMessageCount().setVisibility(View.VISIBLE);
            ((MainActivity) mContext).setToolbarTitle(UIUtils.getString(R.string.app_name) + "(" + mUnreadCountTotal + ")");
        } else {
            ((MainActivity) mContext).getTvMessageCount().setVisibility(View.GONE);
            ((MainActivity) mContext).setToolbarTitle(UIUtils.getString(R.string.app_name));
        }
    }

    // 这个写的不好很好。
    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<Session>(mContext, mData, R.layout.item_recent_message) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Session item, int position) {
                    if (item.getReceiverType() == Message.RECEIVER_TYPE_NONE) {
                        ImageView ivHeader = helper.getView(R.id.ivHeader);

                        Glide.with(mContext).load(item.getPicture()).centerCrop().into(ivHeader);
                        helper.setText(R.id.tvDisplayName, item.getTitle())
                                .setText(R.id.tvTime, TimeUtils.getMsgFormatTime(item.getModifyAt().getTime()))
                                .setViewVisibility(R.id.ngiv, View.GONE)
                                .setViewVisibility(R.id.ivHeader, View.VISIBLE);

                    } else {
                        //九宫格头像
                        LQRNineGridImageView ngiv = helper.getView(R.id.ngiv);
                        ngiv.setAdapter(mNgivAdapter);
                        List<com.gs.factory.model.db.GroupMember> groupMembers = GroupHelper.getMemberFromGroup(item.getId());
                        ngiv.setImagesData(groupMembers);
                        //群昵称
                        helper.setText(R.id.tvDisplayName, item.getTitle())
                                .setText(R.id.tvTime, TimeUtils.getMsgFormatTime(item.getModifyAt().getTime()))
                                .setViewVisibility(R.id.ngiv, View.VISIBLE)
                                .setViewVisibility(R.id.ivHeader, View.GONE);
                    }

//                    helper.setBackgroundColor(R.id.flRoot, item.isTop() ? UIUtils.getColor(R.color.gray7) : UIUtils.getColor(android.R.color.white))
                    helper.setBackgroundColor(R.id.flRoot, item.isTop() ? R.color.gray8 : android.R.color.white)
                            .setText(R.id.tvCount, item.getUnReadCount() + "")
                            .setViewVisibility(R.id.tvCount, item.getUnReadCount() > 0 ? View.VISIBLE : View.GONE);
                    TextView tvContent = helper.getView(R.id.tvContent);
                    if (!TextUtils.isEmpty(item.getDraft())) {
                        MoonUtils.identifyFaceExpression(mContext, tvContent, item.getDraft(), ImageSpan.ALIGN_BOTTOM);
                        helper.setViewVisibility(R.id.tvDraft, View.VISIBLE);
                        return;
                    } else {
                        helper.setViewVisibility(R.id.tvDraft, View.GONE);
                    }

                    if (item.getMessage().getType() == Message.TYPE_STR) {
                        MoonUtils.identifyFaceExpression(mContext, tvContent, item.getContent(), ImageSpan.ALIGN_BOTTOM);
                    }
                    else if (item.getMessage().getType() == Message.TYPE_PIC) {
                        tvContent.setText("[" + UIUtils.getString(R.string.picture) + "]");
                    } else if (item.getMessage().getType() == Message.TYPE_AUDIO) {
                        tvContent.setText("[" + UIUtils.getString(R.string.voice) + "]");
                    } else if (item.getMessage().getType() == Message.TYPE_VIDEO) {
                        tvContent.setText("[" + UIUtils.getString(R.string.video) + "]");
                    } else if (item.getMessage().getType() == Message.TYPE_FILE) {
                        if (MediaFileUtils.isImageFileType(item.getContent())) {
                            tvContent.setText("[" + UIUtils.getString(R.string.sticker) + "]");
                        } else if (MediaFileUtils.isVideoFileType(item.getContent())) {
                            tvContent.setText("[" + UIUtils.getString(R.string.video) + "]");
                        }
                    } else if (item.getMessage().getType() == Message.TYPE_LOCATION) {
                        tvContent.setText("[" + UIUtils.getString(R.string.location) + "]");
                    }
//                    else if (item.getLatestMessage() instanceof GroupNotificationMessage) {
//                        GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) item.getLatestMessage();
//                        try {
//                            UserInfo curUserInfo = DBManager.getInstance().getUserInfo(UserCache.getId());
//                            GroupNotificationMessageData data = JsonMananger.jsonToBean(groupNotificationMessage.getData(), GroupNotificationMessageData.class);
//                            String operation = groupNotificationMessage.getOperation();
//                            String notification = "";
//                            String operatorName = data.getOperatorNickname().equals(curUserInfo.getName()) ? UIUtils.getString(R.string.you) : data.getOperatorNickname();
//                            String targetUserDisplayNames = "";
//                            List<String> targetUserDisplayNameList = data.getTargetUserDisplayNames();
//                            for (String name : targetUserDisplayNameList) {
//                                targetUserDisplayNames += name.equals(curUserInfo.getName()) ? UIUtils.getString(R.string.you) : name;
//                            }
//                            if (operation.equalsIgnoreCase(GroupNotificationMessage.GROUP_OPERATION_CREATE)) {
//                                notification = UIUtils.getString(R.string.created_group, operatorName);
//                            } else if (operation.equalsIgnoreCase(GroupNotificationMessage.GROUP_OPERATION_DISMISS)) {
//                                notification = operatorName + UIUtils.getString(R.string.dismiss_groups);
//                            } else if (operation.equalsIgnoreCase(GroupNotificationMessage.GROUP_OPERATION_KICKED)) {
//                                if (operatorName.contains(UIUtils.getString(R.string.you))) {
//                                    notification = UIUtils.getString(R.string.remove_group_member, operatorName, targetUserDisplayNames);
//                                } else {
//                                    notification = UIUtils.getString(R.string.remove_self, targetUserDisplayNames, operatorName);
//                                }
//                            } else if (operation.equalsIgnoreCase(GroupNotificationMessage.GROUP_OPERATION_ADD)) {
//                                notification = UIUtils.getString(R.string.invitation, operatorName, targetUserDisplayNames);
//                            } else if (operation.equalsIgnoreCase(GroupNotificationMessage.GROUP_OPERATION_QUIT)) {
//                                notification = operatorName + UIUtils.getString(R.string.quit_groups);
//                            } else if (operation.equalsIgnoreCase(GroupNotificationMessage.GROUP_OPERATION_RENAME)) {
//                                notification = UIUtils.getString(R.string.change_group_name, operatorName, data.getTargetGroupName());
//                            }
//                            tvContent.setText(notification);
//                        } catch (HttpException e) {
//                            e.printStackTrace();
//                        }
//                    } else if (item.getLatestMessage() instanceof RedPacketMessage) {
//                        RedPacketMessage redPacketMessage = (RedPacketMessage) item.getLatestMessage();
//                        tvContent.setText("[" + UIUtils.getString(R.string.wx_red_pack) + "]" + redPacketMessage.getContent());
//                    }
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
                Intent intent = new Intent(mContext, SessionActivity.class);
                Session item = mData.get(position);
                intent.putExtra("sessionId", item.getId());
                if (item.getReceiverType() == Message.RECEIVER_TYPE_NONE) {
                    intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
                } else {
                    intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_GROUP);
                }
                mContext.jumpToActivity(intent);
            });
            mAdapter.setOnItemLongClickListener((helper, parent, itemView, position) -> {
                Session item = mData.get(position);
                View conversationMenuView = View.inflate(mContext, R.layout.dialog_conversation_menu, null);
                mConversationMenuDialog = new CustomDialog(mContext, conversationMenuView, R.style.MyDialog);
                TextView tvSetConversationToTop = (TextView) conversationMenuView.findViewById(R.id.tvSetConversationToTop);
                tvSetConversationToTop.setText(item.isTop() ? UIUtils.getString(R.string.cancel_conversation_to_top) : UIUtils.getString(R.string.set_conversation_to_top));
                conversationMenuView.findViewById(R.id.tvSetConversationToTop).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtils.d("ToTop: position" + position);
                    }
                });
//                conversationMenuView.findViewById(R.id.tvSetConversationToTop).setOnClickListener(v ->
//                        RongIMClient.getInstance().setConversationToTop(item.getConversationType(), item.getTargetId(), !item.isTop(), new RongIMClient.ResultCallback<Boolean>() {
//                            @Override
//                            public void onSuccess(Boolean aBoolean) {
//                                loadData();
//                                mConversationMenuDialog.dismiss();
//                                mConversationMenuDialog = null;
//                            }
//
//                            @Override
//                            public void onError(RongIMClient.ErrorCode errorCode) {
//
//                            }
//                        }));
                conversationMenuView.findViewById(R.id.tvDeleteConversation).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtils.d("Delete: position" + position);
                    }
                });
//                conversationMenuView.findViewById(R.id.tvDeleteConversation).setOnClickListener(v -> {
//                    RongIMClient.getInstance().removeConversation(item.getConversationType(), item.getTargetId(), new RongIMClient.ResultCallback<Boolean>() {
//                        @Override
//                        public void onSuccess(Boolean aBoolean) {
//                            loadData();
//                            mConversationMenuDialog.dismiss();
//                            mConversationMenuDialog = null;
//                        }
//
//                        @Override
//                        public void onError(RongIMClient.ErrorCode errorCode) {
//
//                        }
//                    });
//                });
                mConversationMenuDialog.show();
                return true;
            });
            getView().getRvRecentMessage().setAdapter(mAdapter);
        }
    }
}
