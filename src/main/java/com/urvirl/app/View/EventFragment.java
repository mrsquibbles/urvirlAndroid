package com.urvirl.app.View;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.urvirl.app.Controller.EditEventActivity;
import com.urvirl.app.Model.Event;
import com.urvirl.app.R;

/**
 * Created by Adam Fockler on 3/24/2016.
 */
public class EventFragment extends Fragment
{
    Event event;
    public static final String EXTRA_EVENT = "myapplication.EVENT";
    public static final String NEW_EVENT = "myapplication.NEW";
    TextView eventName, eventStart, eventEnd, eventDescription;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.event_view, container, false);
        super.hasOptionsMenu();
        setHasOptionsMenu(true);

        //set the title
        getActivity().setTitle("Event");

        event = (Event)getArguments().getSerializable(EXTRA_EVENT);

        eventName = (TextView)v.findViewById(R.id.event_name);
        eventName.setText("Name: " + event.toString());
        eventStart = (TextView)v.findViewById(R.id.event_start);
        eventStart.setText("Start: " + event.getStartAt());
        eventEnd = (TextView)v.findViewById(R.id.event_end);
        if(!event.getEndAt().isEmpty())
            eventEnd.setText("End: " + event.getEndAt());
        else
            eventEnd.setText("End: no end date");
        eventDescription = (TextView)v.findViewById(R.id.event_description);
        eventDescription.setText("Description: " + event.getContent());


        return v;
    }

    //States we will have a menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.event_menu, menu);
    }

    //What Options in the menu will do
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        //Handle item selection
        switch(item.getItemId())
        {
            case R.id.edit:
                i = new Intent(getActivity(), EditEventActivity.class);
                i.putExtra(NEW_EVENT, false);
                i.putExtra(EXTRA_EVENT, event);
                startActivityForResult(i, 0);
                /*Toast.makeText(getActivity(), "Functionality still being worked on.",
                        Toast.LENGTH_LONG).show();*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
