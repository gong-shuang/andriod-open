package com.gs.open.ui.presenter;

import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.IMainAtView;
import com.gs.base.util.LogUtils;
import com.gs.base.util.UIUtils;


public class MainAtPresenter extends BasePresenter<IMainAtView> {

    public MainAtPresenter(BaseActivity context) {
        super(context);

 //       connect(UserCache.getToken());  // 建立与融云服务器的连接，代码已经删除了。
        //同步所有用户信息

        //这里不同步用户信息，等打开具体的Fragment的时候在同步。
//        DBManager.getInstance().getAllUserInfo();
    }


}
