package com.gs.open.ui.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gs.im.data.helper.GroupHelper;
import com.gs.im.model.db.GroupMember;
import com.gs.im.model.db.User;
import com.gs.im.persistence.Account;
//import com.gs.open.temp.UserInfo;
import com.lqr.ninegridimageview.LQRNineGridImageView;
import com.lqr.ninegridimageview.LQRNineGridImageViewAdapter;
import com.gs.open.R;
import com.gs.open.app.AppConst;
//import com.gs.open.delete.db.DBManager;
//import com.gs.open.db.model.GroupMember;
import com.gs.open.ui.base.BaseActivity;
import com.gs.open.ui.base.BasePresenter;
import com.gs.base.util.LogUtils;
import com.gs.base.util.UIUtils;

import java.util.List;

import butterknife.BindView;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.gs.open.R.id.ivCard;

/**
 * 我的信息，二维码名片
 */
public class QRCodeCardActivity extends BaseActivity {

    private User mUserInfo;
    private String mGroupId;

    @BindView(R.id.ivHeader)
    ImageView mIvHeader;
    @BindView(R.id.ngiv)
    LQRNineGridImageView mNgiv;
    @BindView(R.id.tvName)
    TextView mTvName;
    @BindView(ivCard)
    ImageView mIvCard;
    @BindView(R.id.tvTip)
    TextView mTvTip;

    @Override
    public void init() {
        mGroupId = getIntent().getStringExtra("groupId");
    }

    @Override
    public void initView() {
        mTvTip.setText(UIUtils.getString(R.string.qr_code_card_tip));
    }

    public void initData() {
        if (TextUtils.isEmpty(mGroupId)) {
//            mUserInfo = DBManager.getInstance().getUserInfo(UserCache.getId());
//            mUserInfo = DBManager.getInstance().getUserInfo(Account.getUser().getId());
            mUserInfo = Account.getUser();
            if (mUserInfo != null) {
                Glide.with(this).load(mUserInfo.getPortrait()).centerCrop().into(mIvHeader);
                mTvName.setText(mUserInfo.getName());
                setQRCode(AppConst.QrCodeCommon.ADD + mUserInfo.getId());
            }
        } else {
            mNgiv.setVisibility(View.VISIBLE);
            mIvHeader.setVisibility(View.GONE);
//            Observable.just(DBManager.getInstance().getGroupsById(mGroupId))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(groups -> {
//                        if (groups == null)
//                            return;
//                        mTvName.setText(groups.getName());
//                    });
            mNgiv.setAdapter(new LQRNineGridImageViewAdapter<GroupMember>() {
                @Override
                protected void onDisplayImage(Context context, ImageView imageView, GroupMember groupMember) {
                    Glide.with(context).load(groupMember.getUser().getPortrait()).centerCrop().into(imageView);
                }
            });
//            List<GroupMember> groupMembers = DBManager.getInstance().getGroupMembers(mGroupId);
            List<GroupMember> groupMembers = GroupHelper.getMemberFromGroup(mGroupId);
            mNgiv.setImagesData(groupMembers);
            setQRCode(AppConst.QrCodeCommon.JOIN + mGroupId);
            mTvTip.setVisibility(View.GONE);
        }
    }

    private void setQRCode(String content) {
        Observable.just(QRCodeEncoder.syncEncodeQRCode(content, UIUtils.dip2Px(100)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> mIvCard.setImageBitmap(bitmap), this::loadQRCardError);
    }

    private void loadQRCardError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_qr_code_card;
    }
}
