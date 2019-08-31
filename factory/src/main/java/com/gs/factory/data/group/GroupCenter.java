package com.gs.factory.data.group;

import com.gs.factory.model.card.GroupCard;
import com.gs.factory.model.card.GroupMemberCard;

/**
 * 群中心的接口定义
 *
 * @author qiujuer Email:qiujuer@live.cn
 * @version 1.0.0
 */
public interface GroupCenter {
    // 群卡片的处理
    void dispatch(GroupCard... cards);

    // 群成员的处理
    void dispatch(GroupMemberCard... cards);

    // 群成员的处理
    void dispatchDel(GroupMemberCard... cards);
}
