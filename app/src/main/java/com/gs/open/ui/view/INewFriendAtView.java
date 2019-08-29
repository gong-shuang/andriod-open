package com.gs.open.ui.view;


import android.widget.LinearLayout;

import com.lqr.recyclerview.LQRRecyclerView;

public interface INewFriendAtView {
    LinearLayout getLlNoNewFriend();

    LinearLayout getLlHasNewFriend();

    LQRRecyclerView getRvNewFriend();
}
