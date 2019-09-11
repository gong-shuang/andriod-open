package com.gs.im.data.group;

import com.gs.im.data.helper.DbHelper;
import com.gs.im.data.helper.GroupHelper;
import com.gs.im.data.helper.UserHelper;
import com.gs.im.model.card.GroupMemberCard;
import com.gs.im.model.card.GroupCard;
import com.gs.im.model.db.Group;
import com.gs.im.model.db.GroupMember;
import com.gs.im.model.db.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.gs.im.data.helper.GroupHelper.getMemberFromGroup;

/**
 * 群／群成员卡片中心的实现类
 * @author qiujuer Email:qiujuer@live.cn
 * @version 1.0.0
 */
public class GroupDispatcher implements GroupCenter {
    private static GroupCenter instance;
    private Executor executor = Executors.newSingleThreadExecutor();

    public static GroupCenter instance() {
        if (instance == null) {
            synchronized (GroupDispatcher.class) {
                if (instance == null)
                    instance = new GroupDispatcher();
            }
        }
        return instance;
    }

    @Override
    public void dispatch(GroupCard... cards) {
        if (cards == null || cards.length == 0)
            return;
        executor.execute(new GroupHandler(cards));
    }

    @Override
    public void dispatch(GroupMemberCard... cards) {
        if (cards == null || cards.length == 0)
            return;
        executor.execute(new GroupMemberRspHandler(cards));
    }

    @Override
    public void dispatchDel(GroupMemberCard... cards) {
        if (cards == null || cards.length == 0)
            return;
        executor.execute(new GroupMemberRspDelHandler(cards));
    }


    @Override
    public void dispatchQuitGroup(String groupId) {
        executor.execute(new GroupQuitRspHandler(groupId));
    }

    private class GroupMemberRspHandler implements Runnable {
        private final GroupMemberCard[] cards;

        GroupMemberRspHandler(GroupMemberCard[] cards) {
            this.cards = cards;
        }

        @Override
        public void run() {
            List<GroupMember> members = new ArrayList<>();
            for (GroupMemberCard model : cards) {
                // 成员对应的人的信息
                User user = UserHelper.search(model.getUserId());
                // 成员对应的群的信息
                Group group = GroupHelper.find(model.getGroupId());
                if (user != null && group != null) {
                    GroupMember member = model.build(group, user);
                    members.add(member);
                }
            }
            if (members.size() > 0)
                DbHelper.save(GroupMember.class, members.toArray(new GroupMember[0]));
        }
    }

    private class GroupMemberRspDelHandler implements Runnable {
        private final GroupMemberCard[] cards;

        GroupMemberRspDelHandler(GroupMemberCard[] cards) {
            this.cards = cards;
        }

        @Override
        public void run() {
            List<GroupMember> members = new ArrayList<>();
            for (GroupMemberCard model : cards) {
                // 成员对应的人的信息
                User user = UserHelper.search(model.getUserId());
                // 成员对应的群的信息
                Group group = GroupHelper.find(model.getGroupId());
                if (user != null && group != null) {
                    GroupMember member = model.build(group, user);
                    members.add(member);
                }
            }
            if (members.size() > 0)
                DbHelper.delete(GroupMember.class, members.toArray(new GroupMember[0]));
        }
    }

    private class GroupQuitRspHandler implements Runnable {
        private final String groupId;

        GroupQuitRspHandler(String groupId) {
            this.groupId = groupId;
        }

        @Override
        public void run() {
            List<GroupMember> members = getMemberFromGroup(groupId);
            if (members.size() > 0) {
                DbHelper.delete(GroupMember.class, members.toArray(new GroupMember[0]));
            }

            //同时删除组
            List<Group> groups = new ArrayList<>();
            Group group = GroupHelper.find(groupId);
            groups.add(group);
            if (groups.size() > 0)
                DbHelper.delete(Group.class, groups.toArray(new Group[0]));
        }
    }

    /**
     * 把群Card处理为群DB类
     */
    private class GroupHandler implements Runnable {
        private final GroupCard[] cards;

        GroupHandler(GroupCard[] cards) {
            this.cards = cards;
        }

        @Override
        public void run() {
            List<Group> groups = new ArrayList<>();
            for (GroupCard card : cards) {
                // 搜索管理员
                User owner = UserHelper.search(card.getOwnerId());
                if (owner != null) {
                    Group group = card.build(owner);
                    groups.add(group);
                }
            }
            if (groups.size() > 0)
                DbHelper.save(Group.class, groups.toArray(new Group[0]));
        }
    }
}
