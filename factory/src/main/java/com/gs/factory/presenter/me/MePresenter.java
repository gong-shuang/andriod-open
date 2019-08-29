package com.gs.factory.presenter.me;

import com.gs.factory.common.app.Application;
import com.gs.factory.Factory;
import com.gs.factory.persistence.Account;
import com.gs.factory.common.presenter.BasePresenter;

public class MePresenter extends BasePresenter<MeContract.View>
        implements MeContract.Presenter {

    public MePresenter(MeContract.View view) {
        super(view);
    }

    @Override
    public void quit() {
        // 清除 xml 文件
        Account.clear(Factory.app());
    }
}
