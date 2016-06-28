package com.urvirl.app.Controller;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.Window;
import android.view.WindowManager;

import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.View.GroupChatFragment;
import com.urvirl.app.View.GroupListFragment;

/**
 * Created by Adam Fockler on 12/18/2015.
 */
public class GroupChatActivity  extends SingleFragmentActivity
{
    Intent i;
    ChatGroup group;

    @Override
    protected Fragment createFragment()
    {
        //to be able to pass the group id to the chat fragment
        Bundle args = new Bundle();
        group = (ChatGroup)getIntent().getSerializableExtra(GroupListFragment.EXTRA_GROUP);
        args.putSerializable(GroupChatFragment.EXTRA_GROUP, group);

        //Set color of group
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(group.getGroup_color()));

            ActionBar bar = getSupportActionBar();
            bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(group.getGroup_color())));
        }

        GroupChatFragment fragment = new GroupChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                group = (ChatGroup)getIntent().getSerializableExtra(GroupListFragment.EXTRA_GROUP);
            }
    }
}
}