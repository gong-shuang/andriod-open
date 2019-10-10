package com.gs.imsdk.business;

import android.util.Log;

public class ErrorHandle {
    private static final String TAG = ErrorHandle.class.getSimpleName();
    private static ErrorHandle errorHandle = null;

    private ErrorHandle(){
    }

    public static ErrorHandle getInstance(){
        if(errorHandle == null){
            errorHandle = new ErrorHandle();
        }
        return errorHandle;
    }

    public void userUnExist(String userId){
        Log.e(TAG, "userUnExist(), userId:" + userId);
    }

    public void userUnLogin(String userId){
        Log.e(TAG, "userUnLogin(), userId:" + userId);
    }


}
