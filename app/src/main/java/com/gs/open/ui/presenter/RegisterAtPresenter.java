package com.gs.open.ui.presenter;

import android.text.TextUtils;

import com.gs.im.common.data.DataSource;
import com.gs.im.data.helper.AccountHelper;
import com.gs.im.model.api.account.RegisterModel;
import com.gs.im.model.db.User;
import com.gs.im.persistence.Account;
import com.gs.open.R;
//import com.gs.open.delete.model.cache.UserCache;
import com.gs.open.ui.activity.LoginActivity;
import com.gs.open.ui.activity.MainActivity;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.open.ui.view.IRegisterAtView;
import com.gs.base.util.LogUtils;
import com.gs.base.util.RegularUtils;
import com.gs.base.util.UIUtils;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.util.Timer;
import java.util.TimerTask;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RegisterAtPresenter extends BasePresenter<IRegisterAtView> {

    int time = 0;
    private Timer mTimer;
    private Subscription mSubscription;

    public RegisterAtPresenter(BaseActivity context) {
        super(context);
    }

    public void sendCode() {
        String phone = getView().getEtPhone().getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            UIUtils.showToast(UIUtils.getString(R.string.phone_not_empty));
            return;
        }

        if (!RegularUtils.isMobile(phone)) {
            UIUtils.showToast(UIUtils.getString(R.string.phone_format_error));
            return;
        }

        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
//        ApiRetrofit.getInstance().checkPhoneAvailable(AppConst.REGION, phone)
//                .subscribeOn(Schedulers.io())
//                .flatMap(new Func1<CheckPhoneResponse, Observable<SendCodeResponse>>() {
//                    @Override
//                    public Observable<SendCodeResponse> call(CheckPhoneResponse checkPhoneResponse) {
//                        int code = checkPhoneResponse.getCode();
//                        if (code == 200) {
//                            return ApiRetrofit.getInstance().sendCode(AppConst.REGION, phone);
//                        } else {
//                            return Observable.error(new ServerException(UIUtils.getString(R.string.phone_not_available)));
//                        }
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(sendCodeResponse -> {
//                    mContext.hideWaitingDialog();
//                    int code = sendCodeResponse.getCode();
//                    if (code == 200) {
//                        changeSendCodeBtn();
//                    } else {
//                        sendCodeError(new ServerException(UIUtils.getString(R.string.send_code_error)));
//                    }
//                }, this::sendCodeError);
    }

    private void sendCodeError(Throwable throwable) {
        mContext.hideWaitingDialog();
        LogUtils.e(throwable.getLocalizedMessage());
        UIUtils.showToast(throwable.getLocalizedMessage());
    }

    private void changeSendCodeBtn() {
        //开始1分钟倒计时
        //每一秒执行一次Task
        mSubscription = Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            time = 60;
            TimerTask mTask = new TimerTask() {
                @Override
                public void run() {
                    subscriber.onNext(--time);
                }
            };
            mTimer = new Timer();
            mTimer.schedule(mTask, 0, 1000);//每一秒执行一次Task
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    if (getView().getBtnSendCode() != null) {
                        if (time >= 0) {
                            getView().getBtnSendCode().setEnabled(false);
                            getView().getBtnSendCode().setText(time + "");
                        } else {
                            getView().getBtnSendCode().setEnabled(true);
                            getView().getBtnSendCode().setText(UIUtils.getString(R.string.send_code_btn_normal_tip));
                        }
                    } else {
                        mTimer.cancel();
                    }
                }, throwable -> LogUtils.sf(throwable.getLocalizedMessage()));
    }

    public void register() {
        String phone = getView().getEtPhone().getText().toString().trim();
        String password = getView().getEtPwd().getText().toString().trim();
        String nickName = getView().getEtNickName().getText().toString().trim();
        String code = getView().getEtVerifyCode().getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            UIUtils.showToast(UIUtils.getString(R.string.phone_not_empty));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            UIUtils.showToast(UIUtils.getString(R.string.password_not_empty));
            return;
        }
        if (TextUtils.isEmpty(nickName)) {
            UIUtils.showToast(UIUtils.getString(R.string.nickname_not_empty));
            return;
        }

        // 进行网络请求
        // 构造Model，进行请求调用
        RegisterModel model = new RegisterModel(phone, password, nickName, Account.getPushId());
        // 进行网络请求，并设置回送接口为自己
        AccountHelper.register(model, new DataSource.Callback<User>() {
            @Override
            public void onDataLoaded(User user) {
                // 当网络请求成功，注册好了，回送一个用户信息回来
                //此时不需要保存，因为保存数据在网络请求阶段完成

                // 此时是从网络回送回来的，并不保证处于主现场状态
                // 强制执行在主线程中
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        // 调用主界面注册成功
                        mContext.jumpToActivityAndClearTask(MainActivity.class);
                        mContext.finish();
//                        MyApp.exit();
                    }
                });
            }

            @Override
            public void onDataNotAvailable(final int strRes) {
                // 网络请求告知注册失败
                IRegisterAtView view = getView();
                if (view == null)
                    return;
                // 此时是从网络回送回来的，并不保证处于主现场状态
                // 强制执行在主线程中
                Run.onUiAsync(new Action() {
                    @Override
                    public void call() {
                        // 调用主界面注册失败显示错误
                        UIUtils.showToast(UIUtils.getString(R.string.login_error));
                        mContext.jumpToActivity(LoginActivity.class);
                    }
                });
            }
        });
    }

    private void registerError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        UIUtils.showToast(throwable.getLocalizedMessage());
    }

    public void unsubscribe() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

}
