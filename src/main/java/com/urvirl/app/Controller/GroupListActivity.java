package com.urvirl.app.Controller;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.urvirl.app.View.GroupListFragment;

/**
 * Created by Adam Fockler on 12/18/2015.
 */
public class GroupListActivity extends SingleFragmentActivity
{
    Intent i;

    @Override
    protected Fragment createFragment()
    {
        return new GroupListFragment();
    }
}
