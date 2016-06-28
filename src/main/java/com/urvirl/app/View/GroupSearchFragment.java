package com.urvirl.app.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.NotifyService;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adam Fockler on 2/28/2016.
 */
public class GroupSearchFragment extends ListFragment
{
    private SharedPreferences mPreferences;
    ListAdapter adapter;
    ArrayList<ChatGroup> mGroups = new ArrayList<>();
    String query;
    EditText searchInput;
    ListView lv;
    Button searchBtn;

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

        //get the user information
        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);

        //set the title
        getActivity().setTitle("Find Groups");

        //get initial list of groups
        query = "";
        getGroups(query);

        //initialize the list of messages
        adapter = new ListAdapter(getActivity(),mGroups);
        setListAdapter(adapter);

        //States we will have a menu
        setHasOptionsMenu(true);
    }

    public void getGroups(String q)
    {
        query = q.trim().replace(" ","%20");
        ConnectTask groupSearchTask = new ConnectTask(getActivity());
        groupSearchTask.setMessageLoading("Getting groups...");
        groupSearchTask.execute(AppSingleton.API_ENDPOINT_URL + "groups?search=" + query
                + "&user_id=" + mPreferences.getInt("user_id",
                AppSingleton.get(getActivity()).getUserID()));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        final ChatGroup group = (ChatGroup)(getListAdapter().getItem(position));
        if(group.isJoined())
        {
            new AlertDialog.Builder(getContext())
                    .setTitle("Already Joined!")
                    .setMessage("You have already joined this group, access it from the dashboard.")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else
        {
            new AlertDialog.Builder(getContext())
                    .setTitle("Join Group")
                    .setMessage("Are you sure you want to join " + group.toString() + "?")
                    .setPositiveButton("Join Group", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ConnectTask groupSearchTask = new ConnectTask(getActivity());
                            groupSearchTask.setMessageLoading("Joining " + group.toString() + "...");
                            groupSearchTask.execute(AppSingleton.API_ENDPOINT_URL
                                    + "relationships/create?followed_id=" + group.getGroup_id()
                                    + "&auth_token=" + mPreferences.getString("auth_token",
                                    AppSingleton.get(getActivity()).getAuthToken()));
                            group.setJoined(true);

                            adapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    //Creates our menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
    }

    //What Options in the menu will do
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        //Handle item selection
        switch(item.getItemId())
        {
            case R.id.search_btn:
                getActivity().onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ListAdapter extends BaseAdapter {

        private List<ChatGroup> groups;
        Context mContext;

        public ListAdapter(Context context, List<ChatGroup> groups)
        {
            mContext = context;
            this.groups = groups;
        }

        @Override
        public int getCount() {
            return groups.size();
        }

        @Override
        public Object getItem(int position) {
            return groups.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.search_item, null);
            }

            ChatGroup group = groups.get(position);

            if (group != null) {
                TextView groupName = (TextView) v.findViewById(R.id.group_name);
                TextView joined = (TextView) v.findViewById(R.id.joined);
                if (groupName != null) {
                    groupName.setText(group.toString());
                }
                if (joined != null) {
                    if(group.isJoined())
                        joined.setText(R.string.joined);
                    else
                        joined.setText("");
                }
            }

            return v;
        }
    }

    private class ConnectTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public ConnectTask(Context context)
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
                //get groups from json
                JSONArray newGroups = json.getJSONArray("groups");
                JSONArray joined = json.getJSONArray("joined");

                mGroups.clear();

                //adds new groups to list
                for (int i = 0; i < newGroups.length(); i++)
                {
                    JSONObject j = newGroups.getJSONObject(i);
                    final ChatGroup group = new ChatGroup();
                    group.setGroup_id(j.getInt("id"));
                    group.setName(j.getString("name"));
                    group.setTeacher(j.getString("teacher"));
                    group.setUser_id(j.getInt("user_id"));
                    group.setCreated_at(j.getString("created_at"));
                    group.setUpdated_at(j.getString("updated_at"));
                    group.setChat_id(j.getString("chat_id"));
                    group.setPrivacy(j.getBoolean("privacy"));
                    group.setDescription(j.getString("description"));
                    group.setMember_can_edit(j.getBoolean("members_can_edit"));
                    group.setGroup_color(j.getString("group_color"));
                    group.setJoined(false);
                    for(int g = 0; g < joined.length(); g++)
                    {
                        if(group.getGroup_id() == joined.getInt(g))
                            group.setJoined(true);
                    }
                    mGroups.add(group);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(mGroups.isEmpty())
                    Toast.makeText(getActivity(), "No results found", Toast.LENGTH_LONG).show();

                adapter.notifyDataSetChanged();

                super.onPostExecute(json);
            }
        }
    }
}
