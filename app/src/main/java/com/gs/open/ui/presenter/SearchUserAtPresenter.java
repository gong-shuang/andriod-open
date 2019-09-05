package com.gs.open.ui.presenter;

import android.text.TextUtils;

import com.gs.factory.common.data.DataSource;
import com.gs.factory.data.helper.UserHelper;
import com.gs.factory.model.card.UserCard;
import com.gs.open.R;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.ISearchUserAtView;
import com.gs.base.util.LogUtils;
import com.gs.base.util.RegularUtils;
import com.gs.base.util.UIUtils;


import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.util.List;

public class SearchUserAtPresenter extends BasePresenter<ISearchUserAtView> {

    public SearchUserAtPresenter(BaseActivity context) {
        super(context);
    }

    public void searchUser() {
        String content = getView().getEtSearchContent().getText().toString().trim();

        // content 手机号或者用户名。
        if (TextUtils.isEmpty(content)) {
            UIUtils.showToast(UIUtils.getString(R.string.content_no_empty));
            return;
        }

//        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
//        if (RegularUtils.isMobile(content)) {
//            ApiRetrofit.getInstance().getUserInfoFromPhone(AppConst.REGION, content)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(getUserInfoByPhoneResponse -> {
//                        mContext.hideWaitingDialog();
//                        if (getUserInfoByPhoneResponse.getCode() == 200) {
//                            GetUserInfoByPhoneResponse.ResultEntity result = getUserInfoByPhoneResponse.getResult();
//                            UserInfo userInfo = new UserInfo(result.getId(), result.getNickname(), Uri.parse(result.getPortraitUri()));
//                            Intent intent = new Intent(mContext, UserInfoActivity.class);
//                            intent.putExtra("userInfo", userInfo);
//                            mContext.jumpToActivity(intent);
//                        } else {
//                            getView().getRlNoResultTip().setVisibility(View.VISIBLE);
//                            getView().getLlSearch().setVisibility(View.GONE);
//                        }
//                    }, this::loadError);
//        } else {
//            ApiRetrofit.getInstance().getUserInfoById(content)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(getUserInfoByIdResponse -> {
//                        mContext.hideWaitingDialog();
//                        if (getUserInfoByIdResponse.getCode() == 200) {
//                            GetUserInfoByIdResponse.ResultEntity result = getUserInfoByIdResponse.getResult();
//                            UserInfo userInfo = new UserInfo(result.getId(), result.getNickname(), Uri.parse(result.getPortraitUri()));
//                            Intent intent = new Intent(mContext, UserInfoActivity.class);
//                            intent.putExtra("userInfo", userInfo);
//                            mContext.jumpToActivity(intent);
//                        } else {
//                            getView().getRlNoResultTip().setVisibility(View.VISIBLE);
//                            getView().getLlSearch().setVisibility(View.GONE);
//                        }
//                    }, this::loadError);
//        }

        UserHelper.search(content, new DataSource.Callback<List<UserCard>>() {
            @Override
            public void onDataNotAvailable(int strRes) {
                // 搜索失败
                final ISearchUserAtView view = getView();
                if(view!=null){
                    Run.onUiAsync(new Action() {
                        @Override
                        public void call() {
                            view.showError(strRes);
                        }
                    });
                }
            }

            @Override
            public void onDataLoaded(List<UserCard> userCards) {
                // 搜索成功
                final ISearchUserAtView view = getView();
                if(view!=null){
                    Run.onUiAsync(new Action() {
                        @Override
                        public void call() {
                            view.onSearchDone(userCards);
                        }
                    });
                }
            }
        });
    }

    private void loadError(Throwable throwable) {
        mContext.hideWaitingDialog();
        LogUtils.sf(throwable.getLocalizedMessage());
    }
}
