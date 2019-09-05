package com.gs.open.model.exception;

import com.gs.open.R;
import com.gs.base.util.UIUtils;

/**
 * @创建者 CSDN_LQR
 * @描述 服务器异常
 */
public class ServerException extends Exception {

    public ServerException(int errorCode) {
        this(UIUtils.getString(R.string.error_code) + errorCode);
    }

    public ServerException(String message) {
        super(message);
    }

}
