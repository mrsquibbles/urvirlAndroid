package com.urvirl.app.Controller;

import android.support.v4.app.Fragment;

import com.urvirl.app.View.InboxFragment;

/**
 * Created by Adam Fockler on 3/8/2016.
 */
public class InboxActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment()
    {
        return new InboxFragment();
    }
}
