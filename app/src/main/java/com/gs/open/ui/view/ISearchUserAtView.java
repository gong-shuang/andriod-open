package com.gs.open.ui.view;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gs.factory.model.card.UserCard;

import java.util.List;

public interface ISearchUserAtView {

    EditText getEtSearchContent();

    RelativeLayout getRlNoResultTip();

    LinearLayout getLlSearch();

    void showError(int str);

    void onSearchDone(List<UserCard> userCards);
}
