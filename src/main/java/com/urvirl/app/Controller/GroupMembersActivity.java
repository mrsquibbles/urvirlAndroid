package com.urvirl.app.Controller;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.Window;
import android.view.WindowManager;

import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.View.GroupMembersFragment;

/**
 * Created by Adam Fockler on 3/8/2016.
 */
public class GroupMembersActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        //to be able to pass the group id to the chat fragment
        Bundle args = new Bundle();
        ChatGroup group = (ChatGroup)getIntent().getSerializableExtra(GroupMembersFragment.EXTRA_GROUP);
        args.putSerializable(GroupMembersFragment.EXTRA_GROUP, group);

        //Set color of group
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(group.getGroup_color()));

            ActionBar bar = getSupportActionBar();
            bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(group.getGroup_color())));
        }

        GroupMembersFragment fragment = new GroupMembersFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
