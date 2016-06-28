package com.urvirl.app.Controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.urvirl.app.Model.Event;
import com.urvirl.app.View.EditEventFragment;
import com.urvirl.app.View.EventListFragment;

/**
 * Created by Adam Fockler on 3/24/2016.
 */
public class EditEventActivity extends SingleFragmentActivity
{
    Event event;
    int group;
    Boolean newEvent;

    @Override
    protected Fragment createFragment()
    {
        //to be able to pass the group id to the chat fragment
        Bundle args = new Bundle();
        newEvent = getIntent().getBooleanExtra(EventListFragment.NEW_EVENT,true);
        group = getIntent().getIntExtra(EventListFragment.EXTRA_GROUP, 0);
        event = (Event)getIntent().getSerializableExtra(EventListFragment.EXTRA_EVENT);
        args.putSerializable(EditEventFragment.EXTRA_EVENT, event);
        args.putBoolean(EditEventFragment.NEW_EVENT, newEvent);
        args.putInt(EditEventFragment.EXTRA_GROUP,group);

        EditEventFragment fragment = new EditEventFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
