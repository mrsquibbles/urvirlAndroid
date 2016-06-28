package com.urvirl.app.View;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.NotifyService;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.Model.Event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Adam Fockler on 3/30/2016.
 */
public class AllEventsFragment extends Fragment{
    private SharedPreferences mPreferences;
    public static final String EXTRA_EVENT = "myapplication.EVENT";
    public static final String EXTRA_GROUP = "myapplication.GROUP";
    private static final String TAG = "EventListFragment";
    ArrayAdapter<Event> adapter;
    ArrayList<Event> mEvents;
    ChatGroup group;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = new Intent();
        intent.setAction(NotifyService.ACTION);
        intent.putExtra(NotifyService.STOP_SERVICE_BROADCAST_KEY,
                NotifyService.RQS_STOP_SERVICE);

        // Broadcast the given intent to all interested BroadcastReceivers
        getActivity().sendBroadcast(intent);

        super.onCreate(savedInstanceState);
        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);
        getActivity().setTitle("All Events");

        mEvents = new ArrayList<>();

        getEvents();

        setHasOptionsMenu(true);
    }

    public boolean isEventInCal(Context context, Event event)
    {
        String[] proj =
            new String[]{
                    CalendarContract.Instances._ID,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.EVENT_ID};
        Cursor cursor =
                CalendarContract.Instances.query(context.getContentResolver(), proj,
                        event.getStartAtDate().getTime(), event.getEndAtDate().getTime(), "\"" +
                                event.toString() + "\"");
        if (cursor.getCount() > 0) return true;
        else return false;
    }

    private void getEvents() {
        EventTask eventTask = new EventTask(getActivity());
        eventTask.setMessageLoading("Getting events...");
        eventTask.execute(AppSingleton.API_ENDPOINT_URL + "users/" + mPreferences.getInt("user_id",
                AppSingleton.get(getActivity()).getUserID())
                + "/all_events?auth_token=" + mPreferences.getString("auth_token",
                AppSingleton.get(getActivity()).getAuthToken()));
    }

    private class EventTask extends UrlJsonAsyncTask {
        int HttpResult = 0;

        public EventTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
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
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                    response = br.readLine();
                    br.close();
                    json = new JSONObject(response);
                } else {
                    response = con.getResponseMessage();
                    final String finalResponse = response;
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "Http Error " + HttpResult + "-"
                                    + finalResponse, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                System.out.println(json.toString());
                //get events from json
                JSONArray jsonArray = json.getJSONArray("events");

                System.out.println("There are " + jsonArray.length() + " events");

                //create list of events
                for (int i = 0; i < jsonArray.length(); i++) {
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

                //adds events to calendar if they do not exist
                for(Event e : mEvents)
                {
                    if(!isEventInCal(getContext(),e))
                    {
                        System.out.println("Event does not exist");
                        Calendar beginTime = Calendar.getInstance();
                        beginTime.setTime(e.getStartAtDate());
                        Calendar endTime = Calendar.getInstance();
                        endTime.setTime(e.getEndAtDate());
                        Intent i = new Intent(Intent.ACTION_INSERT)
                                .setData(CalendarContract.Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                                .putExtra(CalendarContract.Events.TITLE, e.toString())
                                .putExtra(CalendarContract.Events.DESCRIPTION, e.getContent())
                                .putExtra(Intent.EXTRA_EMAIL, e.getEmail());
                        startActivity(i);
                    }
                    else
                        System.out.println("Event does exist");
                }

                //opens the calendar
                long startMillis = System.currentTimeMillis();
                Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, startMillis);
                Intent i = new Intent(Intent.ACTION_VIEW)
                        .setData(builder.build());
                startActivity(i);


                super.onPostExecute(json);
            }
        }
    }
}
/* extends ListFragment {

    private SharedPreferences mPreferences;
    public static final String EXTRA_EVENT = "myapplication.EVENT";
    public static final String EXTRA_GROUP = "myapplication.GROUP";
    private static final String TAG = "EventListFragment";
    ArrayAdapter<Event> adapter;
    ArrayList<Event> mEvents;
    ChatGroup group;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = new Intent();
        intent.setAction(NotifyService.ACTION);
        intent.putExtra(NotifyService.STOP_SERVICE_BROADCAST_KEY,
                NotifyService.RQS_STOP_SERVICE);

        // Broadcast the given intent to all interested BroadcastReceivers
        getActivity().sendBroadcast(intent);

        super.onCreate(savedInstanceState);
        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);
        getActivity().setTitle("All Events");

        mEvents = new ArrayList<>();

        getEvents();

        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, mEvents);
        setListAdapter(adapter);

        setHasOptionsMenu(true);
    }

    private void getEvents() {
        EventTask eventTask = new EventTask(getActivity());
        eventTask.setMessageLoading("Getting events...");
        eventTask.execute(AppSingleton.API_ENDPOINT_URL + "users/" + mPreferences.getInt("user_id",
                AppSingleton.get(getActivity()).getUserID())
                + "/all_events?auth_token=" + mPreferences.getString("auth_token",
                AppSingleton.get(getActivity()).getAuthToken()));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Event event = (Event) (getListAdapter().getItem(position));
        Intent i = new Intent(getActivity(), EventActivity.class);
        i.putExtra(EXTRA_EVENT, event);
        startActivityForResult(i, 0);
    }

    private class EventTask extends UrlJsonAsyncTask {
        int HttpResult = 0;

        public EventTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
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
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                    response = br.readLine();
                    br.close();
                    json = new JSONObject(response);
                } else {
                    response = con.getResponseMessage();
                    final String finalResponse = response;
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "Http Error " + HttpResult + "-"
                                    + finalResponse, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                System.out.println(json.toString());
                //get events from json
                JSONArray jsonArray = json.getJSONArray("events");

                //create list of events
                for (int i = 0; i < jsonArray.length(); i++) {
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
}*/