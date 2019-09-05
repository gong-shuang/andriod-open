package com.gs.base.init;

import android.content.Context;

import com.gs.base.util.LogUtils;

// 初始化
public class BaseInit {

    public static Context context;   //后面再优化。
    public BaseInit(Context context) {
        this.context = context;
    }

    public static Context getContext(){
        return context;
    }

}
