package com.gs.open.ui.presenter;

import com.gs.im.common.data.DataSource;
import com.gs.im.data.helper.UserHelper;
import com.gs.im.model.card.UserCard;
import com.gs.im.model.db.User;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.IPostScriptAtView;
import com.gs.base.util.LogUtils;
import com.gs.base.util.UIUtils;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;


public class PostScriptAtPresenter extends BasePresenter<IPostScriptAtView> {

    public PostScriptAtPresenter(BaseActivity context) {
        super(context);
    }

    public void addFriend(String userId) {
        String msg = getView().getEtMsg().getText().toString().trim();
//        ApiRetrofit.getInstance().sendFriendInvitation(userId, msg)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(friendInvitationResponse -> {
//                    if (friendInvitationResponse.getCode() == 200) {
//                        UIUtils.showToast(UIUtils.getString(R.string.rquest_sent_success));
//                        mContext.finish();
//                    } else {
//                        UIUtils.showToast(UIUtils.getString(R.string.rquest_sent_fail));
//                    }
//                }, this::loadError);

        //关注就是：添加好友。
        UserHelper.follow(userId, new DataSource.Callback<UserCard>() {
            @Override
            public void onDataNotAvailable(int strRes) {
                final IPostScriptAtView view = getView();
                if (view != null) {
                    Run.onUiAsync(new Action() {
                        @Override
                        public void call() {
 //                           view.showError();
                            UIUtils.showToast(strRes);
                        }
                    });
                }
            }

            @Override
            public void onDataLoaded(UserCard userCard) {
                //更新数据库
                UserHelper.updateUserRole(userCard.getId(), User.ROLE_FRIEND);
                // 成功
                final IPostScriptAtView view = getView();
                if (view != null) {
                    Run.onUiAsync(new Action() {
                        @Override
                        public void call() {
 //                           view.onFollowSucceed(userCard);
                            UIUtils.showToast("请求消息已经发送成功，等待好友答复");
                            //请求消息已经发送成功，等待好友答复。
                        }
                    });
                }
            }
        });
    }

    private void loadError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        UIUtils.showToast(throwable.getLocalizedMessage());
    }
}
