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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.NotifyService;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.User;
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
public class FriendSearchFragment extends ListFragment
{
    private SharedPreferences mPreferences;
    ListAdapter adapter;
    ArrayList<User> mUsers = new ArrayList<>();
    String query;

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
        getActivity().setTitle("Find Friends");

        //get initial list of groups
        query = "";
        getUsers(query);

        //initialize the list of messages
        adapter = new ListAdapter(getActivity(),mUsers);
        setListAdapter(adapter);

        //States we will have a menu
        setHasOptionsMenu(true);
    }

    public void getUsers(String query)
    {
        this.query = query.trim().replace(" ","%20");
        ConnectTask groupSearchTask = new ConnectTask(getActivity());
        groupSearchTask.setMessageLoading("Getting users...");
        groupSearchTask.execute(AppSingleton.API_ENDPOINT_URL + "users?search=" + query
                + "&user_id=" + mPreferences.getInt("user_id",
                AppSingleton.get(getActivity()).getUserID()));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        final User user = (User)(getListAdapter().getItem(position));
        if(user.getStatus().equals("pending") || user.getStatus().equals("accepted"))
        {
            new AlertDialog.Builder(getContext())
                    .setTitle("Already Friends!")
                    .setMessage("You are already friends, or the request is pending.")
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
                    .setTitle("Send Request")
                    .setMessage("Are you sure you want to befriend " + user.toString() + "?")
                    .setPositiveButton("Send Request", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ConnectTask groupSearchTask = new ConnectTask(getActivity());
                            groupSearchTask.setMessageLoading("Sending request to " + user.toString() + "...");
                            groupSearchTask.execute(AppSingleton.API_ENDPOINT_URL
                                    + "friendships/create?friend_id=" + user.getId()
                                    + "&auth_token=" + mPreferences.getString("auth_token",
                                    AppSingleton.get(getActivity()).getAuthToken()));

                            Toast.makeText(getActivity(), "Request Sent", Toast.LENGTH_LONG).show();

                            user.setStatus("pending");

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

        private List<User> users;
        Context mContext;

        public ListAdapter(Context context, List<User> users)
        {
            mContext = context;
            this.users = users;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
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

            User user = users.get(position);

            if (user != null) {
                TextView userName = (TextView) v.findViewById(R.id.group_name);
                TextView status = (TextView) v.findViewById(R.id.joined);
                if (userName != null) {
                    userName.setText(user.toString());
                }
                if (status != null) {
                    status.setText(user.getStatus());
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
                JSONArray users = json.getJSONArray("users");
                JSONArray friends = json.getJSONArray("friends");

                mUsers.clear();

                //adds new groups to list
                for (int i = 0; i < users.length(); i++)
                {
                    JSONObject j = users.getJSONObject(i);
                    if (j.getString("email").equals(mPreferences.getString("email",""))) continue;
                    User u = new User();
                    u.setEmail(j.getString("email"));
                    u.setId(j.getInt("id"));
                    u.setUsername(j.getString("username"));
                    for(int g = 0; g < friends.length(); g++)
                    {
                        JSONObject o = friends.getJSONObject(g);
                        if(u.getId() == o.getInt("friend_id"))
                        {
                            u.setStatus(o.getString("status"));
                            u.setUser_id(o.getInt("user_id"));
                            u.setFriend_id(o.getInt("friend_id"));
                        }
                    }

                    mUsers.add(u);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(mUsers.isEmpty())
                    Toast.makeText(getActivity(), "No results found", Toast.LENGTH_LONG).show();

                adapter.notifyDataSetChanged();

                super.onPostExecute(json);
            }
        }
    }
}
