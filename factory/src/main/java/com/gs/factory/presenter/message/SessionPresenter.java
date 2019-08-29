package com.gs.factory.presenter.message;

import android.support.v7.util.DiffUtil;

import com.gs.factory.data.message.SessionDataSource;
import com.gs.factory.data.message.SessionRepository;
import com.gs.factory.model.db.Session;
import com.gs.factory.presenter.BaseSourcePresenter;
import com.gs.factory.utils.DiffUiDataCallback;

import java.util.List;

/**
 * 最近聊天列表的Presenter
 * 聊天列表，就是主界面的第一个TAB。
 * @author qiujuer Email:qiujuer@live.cn
 * @version 1.0.0
 */
public class SessionPresenter extends BaseSourcePresenter<Session, Session,
        SessionDataSource, SessionContract.View> implements SessionContract.Presenter {

    public SessionPresenter(SessionContract.View view) {
        super(new SessionRepository(), view);
    }

    /**
     * 在 SessionRepository.load() 中设置数据加载成功的回调。
     * 这个函数是由 BaseDbRepository.notifyDataChange() 方法调用的。
     * @param sessions， 这个是刚从数据库获取到的 sessions，
     */
    @Override
    public void onDataLoaded(List<Session> sessions) {
        SessionContract.View view = getView();
        if (view == null)
            return;

        // 差异对比
        List<Session> old = view.getRecyclerAdapter().getItems();   // 获取当前显示在RecyclerView 中的数据。
        DiffUiDataCallback<Session> callback = new DiffUiDataCallback<>(old, sessions);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        // 刷新界面
        refreshData(result, sessions);
    }
}
