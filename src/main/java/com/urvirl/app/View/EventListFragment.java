package com.urvirl.app.View;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.EditEventActivity;
import com.urvirl.app.Controller.EventActivity;
import com.urvirl.app.Controller.NotifyService;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.Model.Event;
import com.urvirl.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Adam Fockler on 3/24/2016.
 */
public class EventListFragment extends ListFragment
{

    private SharedPreferences mPreferences;
    public static final String EXTRA_EVENT = "myapplication.EVENT";
    public static final String EXTRA_GROUP = "myapplication.GROUP";
    public static final String NEW_EVENT = "myapplication.NEW";
    private  static final String TAG = "EventListFragment";
    ArrayAdapter<Event> adapter;
    ArrayList<Event> mEvents;
    ChatGroup group;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Intent intent = new Intent();
        intent.setAction(NotifyService.ACTION);
        intent.putExtra(NotifyService.STOP_SERVICE_BROADCAST_KEY,
                NotifyService.RQS_STOP_SERVICE);

        // Broadcast the given intent to all interested BroadcastReceivers
        getActivity().sendBroadcast(intent);

        super.onCreate(savedInstanceState);
        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);
        getActivity().setTitle("Events");

        //get the group
        group = (ChatGroup)getArguments().getSerializable(EXTRA_GROUP);

        mEvents = new ArrayList<>();

        getEvents();

        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,mEvents);
        setListAdapter(adapter);

        setHasOptionsMenu(true);
    }

    private void getEvents()
    {
        EventTask eventTask = new EventTask(getActivity());
        eventTask.setMessageLoading("Getting events...");
        eventTask.execute(AppSingleton.API_ENDPOINT_URL + "groups/" + group.getGroup_id()
                + "/events");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Event event = (Event)(getListAdapter().getItem(position));
        Intent i = new Intent(getActivity(), EventActivity.class);
        i.putExtra(EXTRA_EVENT, event);
        startActivityForResult(i, 0);
    }

    //States we will have a menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.event_list_menu, menu);
    }

    //What Options in the menu will do
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        //Handle item selection
        switch(item.getItemId())
        {
            case R.id.create_event:
                i = new Intent(getActivity(), EditEventActivity.class);
                i.putExtra(NEW_EVENT, true);
                i.putExtra(EXTRA_GROUP, group.getGroup_id());
                startActivityForResult(i, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class EventTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public EventTask(Context context)
        {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            String response = null;
            JSONObject json = new JSONObject();

            try {
                URL object = new URL(urls[0]);

                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("GET");

                // setup the returned values in case something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");

                HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                    response = br.readLine();
                    br.close();
                    json = new JSONObject(response);
                }
                else
                {
                    response = con.getResponseMessage();
                    final String finalResponse = response;
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "Http Error " + HttpResult + "-"
                                    + finalResponse, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json)
        {
            try {
                //get events from json
                JSONArray jsonArray = json.getJSONArray("events");

                //create list of events
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject j = jsonArray.getJSONObject(i);
                    final Event event = new Event();
                    event.setId(j.getInt("id"));
                    event.setName(j.getString("name"));
                    event.setEmail(j.getString("email"));
                    event.setStartAt(j.getString("start_at"));
                    event.setEndAt(j.getString("end_at"));
                    event.setContent(j.getString("content"));
                    event.setCreatedAt(j.getString("created_at"));
                    event.setUpdatedAt(j.getString("updated_at"));
                    event.setGroupId(j.getInt("group_id"));
                    mEvents.add(event);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                adapter.notifyDataSetChanged();

                super.onPostExecute(json);
            }
        }
    }
}
