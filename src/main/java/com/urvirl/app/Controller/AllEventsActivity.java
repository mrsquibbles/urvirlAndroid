package com.urvirl.app.Controller;

import android.support.v4.app.Fragment;

import com.urvirl.app.View.AllEventsFragment;

/**
 * Created by Squbbles MKV on 3/30/2016.
 */
public class AllEventsActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new AllEventsFragment();
    }
}
