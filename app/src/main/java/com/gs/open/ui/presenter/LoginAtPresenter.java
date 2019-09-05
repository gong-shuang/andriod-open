package com.gs.open.ui.presenter;

import android.text.TextUtils;

import com.gs.factory.common.data.DataSource;
import com.gs.factory.data.helper.AccountHelper;
import com.gs.factory.model.api.account.LoginModel;
import com.gs.factory.model.db.User;
import com.gs.factory.persistence.Account;
import com.gs.open.R;
import com.gs.open.ui.activity.MainActivity;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.ILoginAtView;
import com.gs.base.util.LogUtils;
import com.gs.base.util.UIUtils;

public class LoginAtPresenter extends BasePresenter<ILoginAtView> {

    public LoginAtPresenter(BaseActivity context) {
        super(context);
    }

    public void login() {
        String phone = getView().getEtPhone().getText().toString().trim();
        String pwd = getView().getEtPwd().getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            UIUtils.showToast(UIUtils.getString(R.string.phone_not_empty));
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            UIUtils.showToast(UIUtils.getString(R.string.password_not_empty));
            return;
        }

        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));

        // 尝试传递PushId
        LoginModel model = new LoginModel(phone, pwd, Account.getPushId());
        AccountHelper.login(model, new DataSource.Callback<User> (){
            /**
             * 回调函数，数据加载成功
             * @param user
             */
            @Override
            public void onDataLoaded(User user) {
                //在 AccountRspCallback() 中已经做了sp的保持了。
                LogUtils.d("onDataLoaded()");
                mContext.hideWaitingDialog();
                mContext.jumpToActivityAndClearTask(MainActivity.class);
                mContext.finish();
            }

            /**
             * 回调函数，数据加载失败
             * @param strRes
             */
            @Override
            public void onDataNotAvailable(final int strRes) {
                LogUtils.d("onDataNotAvailable()， strRes = "+ strRes);
                UIUtils.showToast(UIUtils.getString(R.string.login_error));
                mContext.hideWaitingDialog();
            }
        });

//        ApiRetrofit.getInstance().login(AppConst.REGION, phone, pwd)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(loginResponse -> {
//                    int code = loginResponse.getCode();
//                    mContext.hideWaitingDialog();
//                    if (code == 200) {
//                        UserCache.save(loginResponse.getResult().getId(), phone, loginResponse.getResult().getToken());
//                        mContext.jumpToActivityAndClearTask(MainActivity.class);
//                        mContext.finish();
//                    } else {
//                        loginError(new ServerException(UIUtils.getString(R.string.login_error) + code));
//                    }
//                }, this::loginError);
    }

//    private void loginError(Throwable throwable) {
//        LogUtils.e(throwable.getLocalizedMessage());
//        UIUtils.showToast(throwable.getLocalizedMessage());
//        mContext.hideWaitingDialog();
//    }
}
