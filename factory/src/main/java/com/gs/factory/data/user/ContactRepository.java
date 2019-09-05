package com.gs.factory.data.user;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import com.gs.factory.data.BaseDbRepository;
import com.gs.factory.common.data.DataSource;
import com.gs.factory.model.db.User;
import com.gs.factory.model.db.User_Table;
import com.gs.factory.persistence.Account;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.List;

/**
 * 联系人仓库
 *
 * @author qiujuer Email:qiujuer@live.cn
 * @version 1.0.0
 */
public class ContactRepository extends BaseDbRepository<User> implements ContactDataSource {
    @Override
    public void load(DataSource.SucceedCallback<List<User>> callback) {
        super.load(callback);

        // 加载本地数据库数据
        SQLite.select()
                .from(User.class)
                .where(User_Table.isFollow.eq(true))
                .and(User_Table.id.notEq(Account.getUserId()))
                .orderBy(User_Table.name, true)
                .limit(100)
                .async()
                .queryListResultCallback(this)
                .execute();
    }

    public void getDataByDB(final DataSource.SucceedCallback<List<User>> callback) {

        // 加载本地数据库数据
        SQLite.select()
                .from(User.class)
                .where(User_Table.isFollow.eq(true))
                .and(User_Table.id.notEq(Account.getUserId()))
                .orderBy(User_Table.name, true)
                .limit(100)
                .async()
                .queryListResultCallback(new QueryTransaction.QueryResultListCallback<User>() {
                    @Override
                    public void onListQueryResult(QueryTransaction transaction, @NonNull List<User> tResult) {
                        if(callback != null){
                            callback.onDataLoaded(tResult);
                        }
                    }
                })
                .execute();
    }

    @Override
    protected boolean isRequired(User user) {
        return user.isFollow() && !user.getId().equals(Account.getUserId());
    }
}
