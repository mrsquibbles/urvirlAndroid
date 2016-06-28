package com.urvirl.app.Controller;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.urvirl.app.View.CreateGroupFragment;

/**
 * Created by Adam Fockler on 2/25/2016.
 */
public class CreateGroupActivity extends SingleFragmentActivity
{
    Intent i;

    @Override
    protected Fragment createFragment()
    {
        return new CreateGroupFragment();
    }
}
