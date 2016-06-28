package com.urvirl.app.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.NotifyService;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.Model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Adam Fockler on 3/8/2016.
 */
public class InviteFriendsFragment extends ListFragment
{
    public static final String EXTRA_GROUP = "myapplication.GROUP";

    private SharedPreferences mPreferences;
    ArrayAdapter<User> adapter;
    ArrayList<User> friends;
    ArrayList<User> members;
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

        //get the group
        group = (ChatGroup)getArguments().getSerializable(EXTRA_GROUP);
        getActivity().setTitle("Invite Friends");

        friends = new ArrayList<>();
        members = new ArrayList<>();

        getFriends();
        getMembers();

        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,friends);
        setListAdapter(adapter);

        setHasOptionsMenu(true);
    }

    public void getFriends()
    {
        GetFriendsTask getFriendsTask = new GetFriendsTask(getActivity());
        getFriendsTask.setMessageLoading("Getting friends...");
        getFriendsTask.execute(AppSingleton.API_ENDPOINT_URL + "friendships?auth_token=" +
                mPreferences.getString("auth_token", AppSingleton.get(getActivity())
                        .getAuthToken()) + "&group_id=" + group.getGroup_id());
    }

    public void getMembers()
    {
        GetMembersTask membersTask = new GetMembersTask(getActivity());
        membersTask.setMessageLoading("Getting members...");
        membersTask.execute(AppSingleton.API_ENDPOINT_URL + "groups/" +
                group.getGroup_id() + "/members?auth_token=" + mPreferences.getString("auth_token",
                AppSingleton.get(getActivity()).getAuthToken()));
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id)
    {
        boolean inGroup = false;
        final User invited = friends.get(position);
        for(User u : members)
        {
            if(u.getId() == invited.getId())
                inGroup = true;
        }
        if(inGroup)
        {
            new AlertDialog.Builder(getContext())
                    .setTitle("Remove from Group?")
                    .setMessage(friends.get(position).getUsername()
                            + " is already a member. Would you like to remove them from the group?")
                    .setCancelable(true)
                    .setPositiveButton("Remove from group", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            DestroyTask destroyTask = new DestroyTask(getActivity());
                            destroyTask.execute(AppSingleton.API_ENDPOINT_URL +
                                    "relationships/destroy?followed_id=" + group.getGroup_id()
                                    + "?auth_token=" +  mPreferences.getString("auth_token",
                                    AppSingleton.get(getActivity()).getAuthToken()) +
                                    "&follower_id=" + invited.getId());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
        else
        {
            new AlertDialog.Builder(getContext())
                    .setTitle("Add to Group?")
                    .setMessage("Do you want to add " + friends.get(position).getUsername()
                            + " to the group?")
                    .setCancelable(true)
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            JoinTask joinTask = new JoinTask(getActivity());
                            joinTask.execute(AppSingleton.API_ENDPOINT_URL +
                                    "relationships/create?followed_id=" + group.getGroup_id()
                                    + "?auth_token=" +  mPreferences.getString("auth_token",
                                    AppSingleton.get(getActivity()).getAuthToken()) +
                                    "&follower_id=" + invited.getId());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private class GetFriendsTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public GetFriendsTask(Context context)
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
                //get friends from json
                JSONArray jsonArray = json.getJSONArray("friends");

                //create list of groups
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject j = jsonArray.getJSONObject(i);
                    User u = new User();
                    u.setEmail(j.getString("email"));
                    u.setId(j.getInt("id"));
                    u.setUsername(j.getString("username"));
                    friends.add(u);
                }
                if(friends.isEmpty())
                    Toast.makeText(getActivity(), "No friends yet, go find some from the main menu!"
                            , Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                adapter.notifyDataSetChanged();

                super.onPostExecute(json);
            }
        }
    }

    private class GetMembersTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public GetMembersTask(Context context)
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
                JSONArray jsonArray = json.getJSONArray("members");

                //create list of groups
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject j = jsonArray.getJSONObject(i);
                    User u = new User();
                    u.setEmail(j.getString("email"));
                    u.setId(j.getInt("id"));
                    u.setUsername(j.getString("username"));
                    members.add(u);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                adapter.notifyDataSetChanged();

                super.onPostExecute(json);
            }
        }
    }

    private class JoinTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public JoinTask(Context context)
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
                //get members from json
                JSONArray jsonArray = json.getJSONArray("members");

                //create list of groups
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject j = jsonArray.getJSONObject(i);
                    User u = new User();
                    u.setEmail(j.getString("email"));
                    u.setId(j.getInt("id"));
                    u.setUsername(j.getString("username"));
                    members.add(u);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

    private class DestroyTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public DestroyTask(Context context)
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
                con.setRequestMethod("DELETE");

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
                adapter.notifyDataSetChanged();

                super.onPostExecute(json);
        }
    }
}
