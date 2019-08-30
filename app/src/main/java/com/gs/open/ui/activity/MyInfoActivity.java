package com.gs.open.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gs.factory.common.app.Application;
import com.gs.factory.common.ui.media.GalleryFragment;
import com.gs.factory.model.db.User;
import com.gs.open.app.MyApp;
import com.gs.open.util.UIUtils;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.bean.ImageItem;
import com.lqr.imagepicker.ui.ImageGridActivity;
import com.lqr.optionitemview.OptionItemView;
import com.gs.open.R;
import com.gs.open.app.AppConst;
import com.gs.open.manager.BroadcastManager;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.presenter.MyInfoAtPresenter;
import com.gs.open.ui.view.IMyInfoAtView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;


/**
 * @创建者 CSDN_LQR
 * @描述 我的个人信息
 */
public class MyInfoActivity extends BaseActivity<IMyInfoAtView, MyInfoAtPresenter> implements IMyInfoAtView {

    public static final int REQUEST_IMAGE_PICKER = 1000;
    public static final int REQUEST_NAME = 2000;
    public static final int REQUEST_SIGNATURE = 3000;

    @BindView(R.id.llHeader)
    LinearLayout mLlHeader;   //整个头像的一行
    @BindView(R.id.ivHeader)
    ImageView mIvHeader;   //头像
    @BindView(R.id.oivName)
    OptionItemView mOivName;
    @BindView(R.id.oivAccount)
    OptionItemView mOivAccount;
    @BindView(R.id.oivQRCodeCard)
    OptionItemView mOivQRCodeCard;
    @BindView(R.id.oivSignature)
    OptionItemView mOivSignature;  //个性签名

    // 头像的本地路径
    private String mPortraitPath;
    AppCompatActivity mActivity;

    @Override
    public void init() {
        super.init();
//        registerBR();
        mActivity = this;
    }

    @Override
    public void initData() {
        mPresenter.loadUserInfo();
    }

    @Override
    public void initListener() {
        mIvHeader.setOnClickListener(v -> {
            Intent intent = new Intent(MyInfoActivity.this, ShowBigImageActivity.class);
            intent.putExtra("url", mPresenter.mUserInfo.getPortraitUri().toString());
            jumpToActivity(intent);
        });
        //更换头像，以前的方法，废弃
//        mLlHeader.setOnClickListener(v -> {
//            Intent intent = new Intent(this, ImageGridActivity.class);
//            startActivityForResult(intent, REQUEST_IMAGE_PICKER);
//        });
        mLlHeader.setOnClickListener(v -> {
            new GalleryFragment()
                    .setListener(new GalleryFragment.OnSelectedListener() {
                        @Override
                        public void onSelectedImage(String path) {
                            UCrop.Options options = new UCrop.Options();
                            // 设置图片处理的格式JPEG
                            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                            // 设置压缩后的图片精度
                            options.setCompressionQuality(96);

                            // 得到头像的缓存地址
                            File dPath = Application.getPortraitTmpFile();

                            // 发起剪切
                            UCrop.of(Uri.fromFile(new File(path)), Uri.fromFile(dPath))
                                    .withAspectRatio(1, 1) // 1比1比例
                                    .withMaxResultSize(520, 520) // 返回最大的尺寸
                                    .withOptions(options) // 相关参数
                                    .start(mActivity);
                        }
                    })
                    // show 的时候建议使用getChildFragmentManager，
                    // tag GalleryFragment class 名
                    .show(mActivity.getSupportFragmentManager(), GalleryFragment.class.getName());
        });  //更新头像
        //二维码
        mOivQRCodeCard.setOnClickListener(v -> jumpToActivity(QRCodeCardActivity.class));
        //名字
//        mOivName.setOnClickListener(v -> jumpToActivity(ChangeMyNameActivity.class));
        mOivName.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangeMyInfoActivity.class);
            intent.putExtra("data", "haha");
            intent.putExtra("description", "请填写名字");
            startActivityForResult(intent, REQUEST_NAME);
        });
        //个性签名
        mOivSignature.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangeMyInfoActivity.class);
            intent.putExtra("data", "haha");
            intent.putExtra("description", "请填写个性签名");
            startActivityForResult(intent, REQUEST_SIGNATURE);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterBR();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case REQUEST_IMAGE_PICKER:
//                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
//                    if (data != null) {
//                        ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
//                        if (images != null && images.size() > 0) {
//                            ImageItem imageItem = images.get(0);
//                            mPresenter.setPortrait(imageItem);
//                        }
//                    }
//                }
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 收到从Activity传递过来的回调，然后取出其中的值进行图片加载
        // 如果是我能够处理的类型
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            // 通过UCrop得到对应的Uri
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                loadPortrait(resultUri);
            }
        }else if (resultCode == RESULT_OK && requestCode == REQUEST_NAME) {
            String name = data.getStringExtra("result");
            mPresenter.update(name, null, null, false);
            mOivName.setRightText(name);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_SIGNATURE) {
            String signature = data.getStringExtra("result");
            mPresenter.update(null, null, signature, false);
            mOivSignature.setRightText(signature);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Application.showToast(R.string.data_rsp_error_unknown);
        }
    }

    /**
     * 加载Uri到当前的头像中
     *
     * @param uri Uri
     */
    private void loadPortrait(Uri uri) {
        // 得到头像地址
        mPortraitPath = uri.getPath();

        Glide.with(this)
                .load(uri)
                .asBitmap()
                .centerCrop()
                .into(mIvHeader);

//        mPresenter.setPortrait(imageItem); // 服务器更新。
        mPresenter.update("", mPortraitPath, "", false);
    }


    private void registerBR() {
        BroadcastManager.getInstance(this).register(AppConst.CHANGE_INFO_FOR_CHANGE_NAME, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPresenter.loadUserInfo();
            }
        });
    }

    private void unregisterBR() {
        BroadcastManager.getInstance(this).unregister(AppConst.CHANGE_INFO_FOR_CHANGE_NAME);
    }

    @Override
    protected MyInfoAtPresenter createPresenter() {
        return new MyInfoAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_my_info;
    }

    @Override
    public ImageView getIvHeader() {
        return mIvHeader;
    }

    @Override
    public OptionItemView getOivName() {
        return mOivName;
    }

    //手机号
    @Override
    public OptionItemView getOivAccount() {
        return mOivAccount;
    }

    @Override
    public OptionItemView getOivSignature() {
        return mOivSignature;
    }

    @Override
    public void showError(int msgId) {
        //上次失败
        UIUtils.showToast(msgId);
    }
}
