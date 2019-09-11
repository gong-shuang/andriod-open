package com.gs.open.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Looper;
import android.os.MessageQueue;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.gs.im.persistence.Account;
import com.gs.base.util.LogUtils;
import com.jaeger.library.StatusBarUtil;
import com.gs.open.R;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.base.util.UIUtils;

import butterknife.BindView;
import kr.co.namee.permissiongen.PermissionGen;

/**
 * @创建者 CSDN_LQR
 * @描述 微信闪屏页
 */
public class SplashActivity extends BaseActivity {

    // 是否已经得到PushId
    private boolean mAlreadyGotPushReceiverId = false;

    @BindView(R.id.rlButton)
    RelativeLayout mRlButton;
    @BindView(R.id.btnLogin)
    Button mBtnLogin;
    @BindView(R.id.btnRegister)
    Button mBtnRegister;

    @Override
    public void init() {
        PermissionGen.with(this)
                .addRequestCode(100)
                .permissions(
                        //电话通讯录
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.READ_PHONE_STATE,
                        //位置
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        //相机、麦克风
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.CAMERA,
                        //存储空间
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_SETTINGS,
                        //网络
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_WIFI_STATE
                )
                .request();

        // 检查等待状态
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                waitPushReceiverId();
                return false;
            }
        });
    }

    @Override
    public void initView() {

        StatusBarUtil.setColor(this, UIUtils.getColor(R.color.black));

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(1000);
        mRlButton.startAnimation(alphaAnimation);

        //如果已经登陆，就不显示登陆和注册按钮
        if (Account.isLogin()) {
            mBtnLogin.setVisibility(View.GONE);
            mBtnRegister.setVisibility(View.GONE);
        }

    }

    @Override
    public void initListener() {
        mBtnLogin.setOnClickListener(v -> {
            jumpToActivity(LoginActivity.class);
            finish();
        });
        mBtnRegister.setOnClickListener(v -> {
            jumpToActivity(RegisterActivity.class);
            finish();
        });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_splash;
    }

    /**
     * 等待个推框架对我们的PushId设置好值
     */
    private void waitPushReceiverId() {
        LogUtils.d("waitPushReceiverId()");
        if (Account.isLogin()) {
            // 已经登录情况下，判断是否绑定
            // 如果没有绑定则等待广播接收器进行绑定
            if (Account.isBind()) {
                waitPushReceiverIdDone();
                return;
            }
        } else {
            // 没有登录
            // 如果拿到了PushId, 没有登录是不能绑定PushId的
            if (!TextUtils.isEmpty(Account.getPushId())) {
                // 跳转
                waitPushReceiverIdDone();
                return;
            }
        }

        // 循环等待
        getWindow().getDecorView()
                .postDelayed(this::waitPushReceiverId, 500);
    }

    /**
     * 在跳转之前需要把剩下的50%进行完成
     */
    private void waitPushReceiverIdDone() {
        // 标志已经得到PushId
        mAlreadyGotPushReceiverId = true;
        reallySkip();
    }

    /**
     * 真实的跳转
     */
    private void reallySkip() {
        // 检查跳转到主页还是登录
        if (Account.isLogin()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            jumpToActivity(intent);
            finish();
        }
    }
}
