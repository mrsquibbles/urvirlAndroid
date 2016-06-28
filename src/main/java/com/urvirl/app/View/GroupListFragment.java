package com.urvirl.app.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.AllEventsActivity;
import com.urvirl.app.Controller.CreateGroupActivity;
import com.urvirl.app.Controller.FriendSearchActivity;
import com.urvirl.app.Controller.GroupChatActivity;
import com.urvirl.app.Controller.GroupSearchActivity;
import com.urvirl.app.Controller.InboxActivity;
import com.urvirl.app.Controller.Logout;
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

/**
 * Created by Adam Fockler on 12/18/2015.
 */
public class GroupListFragment extends ListFragment
{
    private SharedPreferences mPreferences;
    public static final String EXTRA_GROUP = "myapplication.GROUP";
    public static final String EXTRA_QUERY = "QUERY";
    private  static final String TAG = "GroupListFragment";
    ArrayAdapter<ChatGroup> adapter;
    ArrayList<ChatGroup> mGroups;

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
        getActivity().setTitle("Groups");

        mGroups = new ArrayList<>();

        getGroups();

        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,mGroups);
        setListAdapter(adapter);

        setHasOptionsMenu(true);
    }

    private void getGroups()
    {
        GroupTask groupTask = new GroupTask(getActivity());
        groupTask.setMessageLoading("Getting groups...");
        groupTask.execute(AppSingleton.API_ENDPOINT_URL + "users/" +
                mPreferences.getInt("user_id",AppSingleton.get(getContext()).getUserID())
                + "/dashboard?auth_token=" + mPreferences.getString("auth_token",AppSingleton.get(
                getActivity()).getAuthToken()));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        ChatGroup group = (ChatGroup)(getListAdapter().getItem(position));
        Intent i = new Intent(getActivity(), GroupChatActivity.class);
        i.putExtra(EXTRA_GROUP, group);
        startActivityForResult(i, 0);
    }

    //States we will have a menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.urvirl_menu, menu);
    }

    //What Options in the menu will do
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        //Handle item selection
        switch(item.getItemId())
        {
            case R.id.contact_us:
                i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"support@uvirl.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "");
                i.putExtra(Intent.EXTRA_TEXT, "");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                }catch(android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    e.printStackTrace();
                }
                return true;
            case R.id.log_out:
                Logout logout = new Logout(getActivity());
                logout.setMessageLoading("Logging out...");
                logout.execute(AppSingleton.API_ENDPOINT_URL + "sign_out?user[email]="
                        + mPreferences.getString("email", ""), "&user[device_token]=" + mPreferences
                        .getString("auth_token", AppSingleton.get(getActivity()).getAuthToken()));
                return true;
            case R.id.create_group:
                i = new Intent(getActivity(), CreateGroupActivity.class);
                startActivityForResult(i, 0);
                return true;
            case R.id.find_groups:
                i = new Intent(getActivity(), GroupSearchActivity.class);
                startActivityForResult(i, 0);
                return true;
            case R.id.find_friends:
                i = new Intent(getActivity(), FriendSearchActivity.class);
                startActivityForResult(i, 0);
                return true;
            case R.id.inbox:
                i = new Intent(getActivity(), InboxActivity.class);
                startActivityForResult(i, 0);
                return true;
            case R.id.my_events:
                i = new Intent(getActivity(), AllEventsActivity.class);
                startActivityForResult(i,0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GroupTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public GroupTask(Context context)
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
                JSONArray jsonArray = json.getJSONArray("groups");

                //create list of groups
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject j = jsonArray.getJSONObject(i);
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
                    mGroups.add(group);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(HttpResult == HttpURLConnection.HTTP_INTERNAL_ERROR)
                {
                    ArrayList<ChatGroup> temp = AppSingleton.get(getActivity()).getGroups();
                    if(temp.isEmpty())
                    {
                        new AlertDialog.Builder(context)
                                .setTitle("Error")
                                .setMessage("Something went wrong, please try again.")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        Logout logout = new Logout(getActivity());
                                        logout.setMessageLoading("Logging out...");
                                        logout.execute(AppSingleton.API_ENDPOINT_URL +
                                                "sign_out?user[email]=" + mPreferences.getString
                                                ("email", ""));

                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    else
                    {
                        mGroups = AppSingleton.get(getActivity()).getGroups();
                        Gson gson = new Gson();
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt("numGroups", mGroups.size());
                        for(int i=0;i<mGroups.size();i++)
                        {
                            String mString = gson.toJson(mGroups.get(i));
                            editor.putString("group" + i,mString);
                        }
                        editor.commit();
                    }
                }
                else
                {
                    AppSingleton.get(getActivity()).addGroups(mGroups);
                    Gson gson = new Gson();
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putInt("numGroups", mGroups.size());
                    for(int i=0;i<mGroups.size();i++)
                    {
                        String mString = gson.toJson(mGroups.get(i));
                        editor.putString("group" + i,mString);
                    }
                    editor.commit();
                }

                adapter.notifyDataSetChanged();

                super.onPostExecute(json);
            }
        }
    }
}