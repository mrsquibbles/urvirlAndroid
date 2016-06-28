package com.urvirl.app.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.EventListActivity;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.Model.Event;
import com.urvirl.app.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Adam Fockler on 3/24/2016.
 */
public class EditEventFragment extends Fragment
{

    Event event;
    public static final String EXTRA_EVENT = "myapplication.EVENT";
    public static final String NEW_EVENT = "myapplication.NEW";
    public static final String EXTRA_GROUP = "myapplication.GROUP";
    private static final String DIALOG_DATE = "date";
    private static final int REQUEST_START_DATE = 0;
    private static final int REQUEST_END_DATE = 1;
    private static final int REQUEST_START_TIME = 2;
    private static final int REQUEST_END_TIME = 3;

    SharedPreferences mPreferences;

    EditText eventName, eventContent;
    Button startDate, startTime, endDate, endTime, send;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.edit_event_fragment, container, false);
        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);

        //set the title
        getActivity().setTitle("New Event");

        if(!getArguments().getBoolean(NEW_EVENT))
        {
            //set the title
            getActivity().setTitle("Edit Event");
            //get the event
            event = (Event) getArguments().getSerializable(EXTRA_EVENT);
        }
        else
        {
            event = new Event();
            event.setStart(new Date());
            event.setEnd(new Date());
            event.setGroupId(getArguments().getInt(EXTRA_GROUP));
        }

        eventName = (EditText)v.findViewById(R.id.event_name_edit);
        eventName.setText(event.toString());
        eventName.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                event.setName(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });
        eventContent = (EditText)v.findViewById(R.id.event_description_edit);
        eventContent.setText(event.getContent());
        eventContent.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                event.setContent(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // this space intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // this one too
            }
        });

        startDate = (Button)v.findViewById(R.id.start_date_btn);
        updateStartDate();
        startDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(event.getStartAtDate());
                dialog.setTargetFragment(EditEventFragment.this, REQUEST_START_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });
        startTime = (Button)v.findViewById(R.id.start_time_btn);
        updateStartTime();
        startTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment
                        .newInstance(event.getStartAtDate());
                dialog.setTargetFragment(EditEventFragment.this, REQUEST_START_TIME);
                dialog.show(fm, DIALOG_DATE);
            }
        });
        endDate = (Button)v.findViewById(R.id.end_date_btn);
        updateEndDate();
        endDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(event.getEndAtDate());
                dialog.setTargetFragment(EditEventFragment.this, REQUEST_END_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });
        endTime = (Button)v.findViewById(R.id.end_time_btn);
        updateEndTime();
        endTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment
                        .newInstance(event.getEndAtDate());
                dialog.setTargetFragment(EditEventFragment.this, REQUEST_END_TIME);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        send = (Button)v.findViewById(R.id.send_btn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                if(!getArguments().getBoolean(NEW_EVENT)) {
                    String name = event.toString().trim().replace(" ", "%20");
                    String content = event.getContent().trim().replace(" ","%20");
                    SimpleDateFormat format =
                            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    String start = format.format(event.getStartAtDate());
                    String end = format.format(event.getEndAtDate());
                    String url = AppSingleton.API_ENDPOINT_URL + "groups/"
                            + event.getGroupId() + "/events/" + event.getId() + "/edit" +
                            "?id=" + event.getId() + "&group_id=" + event.getGroupId() +
                            "&name=" + name + "&start_at=" + start +
                            "Z&end_at=" + end + "Z&content=" + content;
                    EditEventTask editEventTask = new EditEventTask(getActivity());
                    editEventTask.setMessageLoading("Editing event...");
                    editEventTask.execute(url);
                    ChatGroup c = AppSingleton.get(getActivity()).getGroup(event.getGroupId());
                    i = new Intent(getActivity(), EventListActivity.class);
                    i.putExtra(EXTRA_GROUP, c);
                    startActivityForResult(i, 0);
                }
                else
                {
                    CreateEventTask createEventTask = new CreateEventTask(getActivity());
                    createEventTask.setMessageLoading("Creatinging event...");
                    createEventTask.execute(AppSingleton.API_ENDPOINT_URL + "groups/"
                            + event.getGroupId() + "/events/");
                    ChatGroup c = AppSingleton.get(getActivity()).getGroup(event.getGroupId());
                    i = new Intent(getActivity(), EventListActivity.class);
                    i.putExtra(EXTRA_GROUP, c);
                    startActivityForResult(i, 0);
                }
            }
        });
        if(!getArguments().getBoolean(NEW_EVENT)) send.setText("Edit Event");
        else send.setText("Create Event");



        return v;
    }

    public void updateStartDate()
    {
        startDate.setText(event.getStartDate());
    }

    public void updateStartTime()
    {
        startTime.setText(event.getStartTime());
    }

    public void updateEndDate()
    {
        endDate.setText(event.getEndDate());
    }

    public void updateEndTime()
    {
        endTime.setText(event.getEndTime());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_START_DATE)
        {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            event.setStartDate(date);
            updateStartDate();
        }
        if (requestCode == REQUEST_START_TIME)
        {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            event.setStartTime(date);
            updateStartTime();
        }
        if (requestCode == REQUEST_END_DATE)
        {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            event.setEndDate(date);
            updateEndDate();
        }
        if (requestCode == REQUEST_END_TIME)
        {
            Date date = (Date)data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            event.setEndTime(date);
            updateEndTime();
        }
    }

    //handles login
    private class CreateEventTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public CreateEventTask(Context context)
        {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            JSONObject eventObject = new JSONObject();
            JSONObject json = new JSONObject();
            String response = null;


            try {
                URL object = new URL(urls[0]);

                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");

                // setup the returned values in case something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");
                // add the email and password to the params
                eventObject.put("group_id", event.getGroupId());
                eventObject.put("name", event.toString());
                SimpleDateFormat format =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                String start = format.format(event.getStartAtDate()) + "Z";
                eventObject.put("start_at", start);
                String end = format.format(event.getEndAtDate()) + "Z";
                eventObject.put("end_at", end);
                eventObject.put("content", event.getContent());
                eventObject.put("auth_token", mPreferences.getString("auth_token",
                        AppSingleton.get(getActivity()).getAuthToken()));

                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(eventObject.toString());
                wr.flush();

                HttpResult = con.getResponseCode();
                final int FINALHttpResult = HttpResult;
                if (HttpResult == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
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
                            Toast.makeText(getActivity(),"Http Error " + FINALHttpResult + "-"
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
                if (json.getBoolean("success"))
                {
                    // everything is ok
                }
                else
                    Toast.makeText(getActivity(), json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // something went wrong: show a Toast with the exception message
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

    //handles login
    private class EditEventTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public EditEventTask(Context context)
        {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            JSONObject eventObject = new JSONObject();
            JSONObject json = new JSONObject();
            String response = null;


            try {
                URL object = new URL(urls[0]);

                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                //con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("GET");

                // setup the returned values in case something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");

                HttpResult = con.getResponseCode();
                final int FINALHttpResult = HttpResult;
                if (HttpResult == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
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
                            Toast.makeText(getActivity(),"Http Error " + FINALHttpResult + "-"
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
                if (json.getBoolean("success"))
                {
                    // everything is ok
                }
                else
                    Toast.makeText(getActivity(), json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // something went wrong: show a Toast with the exception message
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }
}
