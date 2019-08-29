package com.gs.open.ui.base;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class BasePresenter<V> {

    /*================== 以下是网络请求接口 ==================*/

    public BaseActivity mContext;  // activity，Fragment 都是其子类。

    public BasePresenter(BaseActivity context) {
        mContext = context;
    }

    protected Reference<V> mViewRef;  // view 的软连接

    public void attachView(V view) {
        mViewRef = new WeakReference<V>(view);
    }

    public boolean isViewAttached() {
        return mViewRef != null && mViewRef.get() != null;
    }

    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
        }
    }

    public V getView() {
        return mViewRef != null ? mViewRef.get() : null;
    }

}
