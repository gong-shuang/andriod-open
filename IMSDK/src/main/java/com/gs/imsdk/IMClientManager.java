package com.gs.imsdk;

import android.content.Context;

import com.gs.imsdk.business.LoginCallback;
import com.gs.imsdk.business.LoginPacket;
import com.gs.imsdk.event.GetPushIDEvent;

import net.openmob.mobileimsdk.android.ClientCoreSDK;
import net.openmob.mobileimsdk.android.conf.ConfigEntity;

public class IMClientManager {
    private static IMClientManager instance = null;
    private Context context = null;
    /** MobileIMSDK是否已被初始化. true表示已初化完成，否则未初始化. */
    private boolean init = false;
    private static GetPushIDEvent getPushIDEvent = null;

    public static IMClientManager getInstance(Context context)
    {
        if(instance == null)
            instance = new IMClientManager(context);
        return instance;
    }

    private IMClientManager(Context context)
    {
        this.context = context;
        initMobileIMSDK();
    }

    public void initMobileIMSDK()
    {
        if(!init)
        {
            // 设置服务器ip和服务器端口
            ConfigEntity.serverIP = "192.168.1.101";
            ConfigEntity.serverUDPPort = 7901;

            // 开启/关闭DEBUG信息输出
            ClientCoreSDK.DEBUG = true;

            // 【特别注意】请确保首先进行核心库的初始化（这是不同于iOS和Java端的地方)
            ClientCoreSDK.getInstance().init(this.context);

            init = true;
        }
    }

    public void getPushId(final GetPushIDEvent getPushIDEvent){

        this.getPushIDEvent = getPushIDEvent;

        //登录，这个登录时获取pushID的意思
        LoginPacket.getInstance(context, new LoginCallback(){
            @Override
            public void loginSucceed(String pushID) {
                ClientCoreSDK.getInstance().getInstance().setCurrentLoginUserId(pushID);
                if(getPushIDEvent != null){
                    getPushIDEvent.setPushID(pushID);
                }
            }

            @Override
            public void loginFailure() {
                //失败
            }
        }).sendLogin();
    }

    public Context getContext() {
        return context;
    }

    public void release()
    {
        ClientCoreSDK.getInstance().release();
        resetInitFlag();
    }

    /**
     * 重置init标识。
     * <p>
     * <b>重要说明：</b>不退出APP的情况下，重新登陆时记得调用一下本方法，不然再
     *
     */
    public void resetInitFlag()
    {
        init = false;
    }


}
