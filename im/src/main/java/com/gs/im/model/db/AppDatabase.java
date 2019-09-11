package com.gs.im.model.db;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * 数据库的基本信息
 *
 * @author qiujuer Email:qiujuer@live.cn
 * @version 1.0.0
 */
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {
    public static final String NAME = "AppDatabase";
    public static final int VERSION = 3;
}
//备注：这个数据库设计的有问题，没有区分user 和 friend，因为通过群，可以查看到其他不是好友的user，而这些user也是应该存储的。
// 也没有图片对应的缩略图，缩略图的设计没想好，如果说是，为了减少服务器的存储控件，服务器不用保存缩略图，服务器指保存原始图片，
// 每次收到消息后，后台