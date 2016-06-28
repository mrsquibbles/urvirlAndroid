package com.urvirl.app.Controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.urvirl.app.Model.Event;
import com.urvirl.app.View.EventFragment;
import com.urvirl.app.View.EventListFragment;

/**
 * Created by Adam Fockler on 3/24/2016.
 */
public class EventActivity extends SingleFragmentActivity
{
    Event event;

    @Override
    protected Fragment createFragment()
    {
        //to be able to pass the group id to the chat fragment
        Bundle args = new Bundle();
        event = (Event)getIntent().getSerializableExtra(EventListFragment.EXTRA_EVENT);
        args.putSerializable(EventListFragment.EXTRA_EVENT, event);

        EventFragment fragment = new EventFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
