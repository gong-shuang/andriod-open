package com.gs.factory.presenter.me;

import com.gs.factory.model.db.User;
import com.gs.factory.common.presenter.BaseContract;

public interface MeContract {

    interface View extends BaseContract.View<Presenter> {

    }

    interface Presenter extends BaseContract.Presenter {

        // 退出
        void quit();
    }
}
