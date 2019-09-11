package com.gs.im.data.helper;

import com.gs.im.model.api.group.GroupMemberDelModel;
import com.gs.im.model.card.GroupResponseCard;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import com.gs.im.Factory;
import com.gs.im.R;
import com.gs.im.common.data.DataSource;
import com.gs.im.model.api.RspModel;
import com.gs.im.model.api.group.GroupCreateModel;
import com.gs.im.model.api.group.GroupMemberAddModel;
import com.gs.im.model.card.GroupCard;
import com.gs.im.model.card.GroupMemberCard;
import com.gs.im.model.db.Group;
import com.gs.im.model.db.GroupMember;
import com.gs.im.model.db.GroupMember_Table;
import com.gs.im.model.db.Group_Table;
import com.gs.im.model.db.User;
import com.gs.im.model.db.User_Table;
import com.gs.im.model.db.view.MemberUserModel;
import com.gs.im.net.Network;
import com.gs.im.net.RemoteService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 对群的一个简单的辅助工具类
 *
 * @author qiujuer Email:qiujuer@live.cn
 * @version 1.0.0
 */
public class GroupHelper {
    public static Group find(String groupId) {
        Group group = findFromLocal(groupId);
        if (group == null)
            group = findFormNet(groupId);
        return group;
    }

    // 从本地找Group
    public static Group findFromLocal(String groupId) {
        return SQLite.select()
                .from(Group.class)
                .where(Group_Table.id.eq(groupId))
                .querySingle();
    }

    // 从网络找Group
    public static Group findFormNet(String id) {
        RemoteService remoteService = Network.remote();
        try {
            Response<RspModel<GroupCard>> response = remoteService.groupFind(id).execute();
            GroupCard card = response.body().getResult();
            if (card != null) {
                // 数据库的存储并通知
                Factory.getGroupCenter().dispatch(card);

                User user = UserHelper.search(card.getOwnerId());
                if (user != null) {
                    return card.build(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // 群的创建
    public static void create(GroupCreateModel model, final DataSource.Callback<GroupCard> callback) {
        RemoteService service = Network.remote();
        service.groupCreate(model)
                .enqueue(new Callback<RspModel<GroupCard>>() {
                    @Override
                    public void onResponse(Call<RspModel<GroupCard>> call, Response<RspModel<GroupCard>> response) {
                        RspModel<GroupCard> rspModel = response.body();
                        if (rspModel.success()) {
                            GroupCard groupCard = rspModel.getResult();
                            // 唤起进行保存的操作
                            Factory.getGroupCenter().dispatch(groupCard);
                            // 返回数据
                            callback.onDataLoaded(groupCard);
                        } else {
                            Factory.decodeRspCode(rspModel, callback);
                        }
                    }

                    @Override
                    public void onFailure(Call<RspModel<GroupCard>> call, Throwable t) {
                        callback.onDataNotAvailable(R.string.data_network_error);
                    }
                });
    }

    // 搜索的方法
    public static Call search(String name, final DataSource.Callback<List<GroupCard>> callback) {
        RemoteService service = Network.remote();
        Call<RspModel<List<GroupCard>>> call = service.groupSearch(name);

        call.enqueue(new Callback<RspModel<List<GroupCard>>>() {
            @Override
            public void onResponse(Call<RspModel<List<GroupCard>>> call, Response<RspModel<List<GroupCard>>> response) {
                RspModel<List<GroupCard>> rspModel = response.body();
                if (rspModel.success()) {
                    // 返回数据
                    callback.onDataLoaded(rspModel.getResult());
                } else {
                    Factory.decodeRspCode(rspModel, callback);
                }
            }

            @Override
            public void onFailure(Call<RspModel<List<GroupCard>>> call, Throwable t) {
                callback.onDataNotAvailable(R.string.data_network_error);
            }
        });

        // 把当前的调度者返回
        return call;
    }

    // 刷新我的群组列表
    public static void refreshGroups() {
        RemoteService service = Network.remote();
        service.groups("").enqueue(new Callback<RspModel<List<GroupCard>>>() {
            @Override
            public void onResponse(Call<RspModel<List<GroupCard>>> call, Response<RspModel<List<GroupCard>>> response) {
                RspModel<List<GroupCard>> rspModel = response.body();
                if (rspModel.success()) {
                    List<GroupCard> groupCards = rspModel.getResult();
                    if (groupCards != null && groupCards.size() > 0) {
                        // 进行调度显示
                        Factory.getGroupCenter().dispatch(groupCards.toArray(new GroupCard[0]));
                    }
                } else {
                    Factory.decodeRspCode(rspModel, null);
                }
            }

            @Override
            public void onFailure(Call<RspModel<List<GroupCard>>> call, Throwable t) {
                // 不做任何事情
            }
        });
    }

    // 获取一个群的成员数量
    public static long getMemberCount(String id) {
        return SQLite.selectCountOf()
                .from(GroupMember.class)
                .where(GroupMember_Table.group_id.eq(id))
                .count();
    }

    // 从本地获取一个群的成员
    public static List<GroupMember> getMemberFromGroup(String id) {
        return SQLite.select()
                .from(GroupMember.class)
                .where(GroupMember_Table.group_id.eq(id))
                .queryList();
    }

    // 从网络去刷新一个群的成员信息
    public static void refreshGroupMember(Group group) {
        RemoteService service = Network.remote();
        service.groupMembers(group.getId()).enqueue(new Callback<RspModel<List<GroupMemberCard>>>() {
            @Override
            public void onResponse(Call<RspModel<List<GroupMemberCard>>> call, Response<RspModel<List<GroupMemberCard>>> response) {
                RspModel<List<GroupMemberCard>> rspModel = response.body();
                if (rspModel.success()) {
                    List<GroupMemberCard> memberCards = rspModel.getResult();
                    if (memberCards != null && memberCards.size() > 0) {
                        // 进行调度显示
                        Factory.getGroupCenter().dispatch(memberCards.toArray(new GroupMemberCard[0]));
                    }
                } else {
                    Factory.decodeRspCode(rspModel, null);
                }
            }

            @Override
            public void onFailure(Call<RspModel<List<GroupMemberCard>>> call, Throwable t) {
                // 不做任何事情
            }
        });
    }

    //获取所有的组的成员
    public static List<GroupMember> getMemberUsersAll(){
        return SQLite.select()
                .from(GroupMember.class)
                .queryList();
    }

    // 关联查询一个用户和群成员的表，返回一个MemberUserModel表的集合
    public static List<MemberUserModel> getMemberUsers(String groupId, int size) {
        return SQLite.select(GroupMember_Table.alias.withTable().as("alias"),
                User_Table.id.withTable().as("userId"),
                User_Table.name.withTable().as("name"),
                User_Table.portrait.withTable().as("portrait"))
                .from(GroupMember.class)
                .join(User.class, Join.JoinType.INNER)
                .on(GroupMember_Table.user_id.withTable().eq(User_Table.id.withTable()))
                .where(GroupMember_Table.group_id.withTable().eq(groupId))
                .orderBy(GroupMember_Table.user_id, true)
                .limit(size)
                .queryCustomList(MemberUserModel.class);
    }

    // 网络请求进行成员添加
    public static void addMembers(String groupId, GroupMemberAddModel model, final DataSource.Callback<List<GroupMemberCard>> callback) {
        RemoteService service = Network.remote();
        service.groupMemberAdd(groupId, model)
                .enqueue(new Callback<RspModel<List<GroupMemberCard>>>() {
                    @Override
                    public void onResponse(Call<RspModel<List<GroupMemberCard>>> call, Response<RspModel<List<GroupMemberCard>>> response) {
                        RspModel<List<GroupMemberCard>> rspModel = response.body();
                        if (rspModel.success()) {
                            List<GroupMemberCard> memberCards = rspModel.getResult();
                            if (memberCards != null && memberCards.size() > 0) {
                                // 进行调度显示
                                Factory.getGroupCenter().dispatch(memberCards.toArray(new GroupMemberCard[0]));
                                callback.onDataLoaded(memberCards);
                            }
                        } else {
                            Factory.decodeRspCode(rspModel, null);
                        }
                    }

                    @Override
                    public void onFailure(Call<RspModel<List<GroupMemberCard>>> call, Throwable t) {
                        callback.onDataNotAvailable(R.string.data_network_error);
                    }
                });
    }

    // 网络请求进行成员删除
    public static void delMembers(String groupId, GroupMemberDelModel model, final DataSource.Callback<List<GroupMemberCard>> callback) {
        RemoteService service = Network.remote();
        service.groupMemberDel(groupId, model)
                .enqueue(new Callback<RspModel<List<GroupMemberCard>>>() {
                    @Override
                    public void onResponse(Call<RspModel<List<GroupMemberCard>>> call, Response<RspModel<List<GroupMemberCard>>> response) {
                        RspModel<List<GroupMemberCard>> rspModel = response.body();
                        if (rspModel != null && rspModel.success()) {
                            List<GroupMemberCard> memberCards = rspModel.getResult();
                            if (memberCards != null && memberCards.size() > 0) {
                                // 进行调度显示
                                Factory.getGroupCenter().dispatchDel(memberCards.toArray(new GroupMemberCard[0]));
                                callback.onDataLoaded(memberCards);
                            }
                        } else {
                            Factory.decodeRspCode(rspModel, null);
                        }
                    }

                    @Override
                    public void onFailure(Call<RspModel<List<GroupMemberCard>>> call, Throwable t) {
                        callback.onDataNotAvailable(R.string.data_network_error);
                    }
                });
    }

    // 网络请求进行退出群
    public static void quitGroup(String groupId, final DataSource.Callback<GroupResponseCard> callback) {
        RemoteService service = Network.remote();
        service.groupQuit(groupId)
                .enqueue(new Callback<RspModel<GroupResponseCard>>() {
                    @Override
                    public void onResponse(Call<RspModel<GroupResponseCard>> call, Response<RspModel<GroupResponseCard>> response) {
                        RspModel<GroupResponseCard> rspModel = response.body();
                        if (rspModel != null && rspModel.success()) {
                            GroupResponseCard memberCards = rspModel.getResult();
                            if (memberCards != null && memberCards.getGroupId() != null) {
                                // 进行调度显示
                                Factory.getGroupCenter().dispatchQuitGroup(memberCards.getGroupId());
                                callback.onDataLoaded(memberCards);
                            }
                        } else {
                            Factory.decodeRspCode(rspModel, null);
                        }
                    }

                    @Override
                    public void onFailure(Call<RspModel<GroupResponseCard>> call, Throwable t) {
                        callback.onDataNotAvailable(R.string.data_network_error);
                    }
                });
    }
}
