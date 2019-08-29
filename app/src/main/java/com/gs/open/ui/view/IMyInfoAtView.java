package com.gs.open.ui.view;

import android.widget.ImageView;

import com.lqr.optionitemview.OptionItemView;

public interface IMyInfoAtView {
    ImageView getIvHeader();

    OptionItemView getOivName();

    OptionItemView getOivAccount();

    OptionItemView getOivSignature();

    void showError(int msgId);
}
