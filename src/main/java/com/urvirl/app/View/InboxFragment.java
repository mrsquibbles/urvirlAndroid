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
import com.urvirl.app.Model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Adam Fockler MKV on 3/8/2016.
 */
public class InboxFragment extends ListFragment
{
    private SharedPreferences mPreferences;
    ArrayAdapter<User> adapter;
    ArrayList<User> requests;

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
        getActivity().setTitle("Inbox");

        requests = new ArrayList<>();

        getRequests();

        adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,requests);
        setListAdapter(adapter);

        setHasOptionsMenu(true);
    }

    public void getRequests()
    {
        InboxTask inboxTask = new InboxTask(getActivity());
        inboxTask.setMessageLoading("Getting friend requests...");
        inboxTask.execute(AppSingleton.API_ENDPOINT_URL + "users/" + mPreferences.getInt("user_id",
                AppSingleton.get(getActivity()).getUserID()) + "/inbox?auth_token=" +
                mPreferences.getString("auth_token", AppSingleton.get(getActivity())
                        .getAuthToken()));
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id)
    {
        new AlertDialog.Builder(getContext())
                .setTitle("Friend Request")
                .setMessage("Do you want to accept " + requests.get(position).getUsername() + "'s request?")
                .setCancelable(true)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RequestTask requestTask = new RequestTask(getActivity());
                        requestTask.execute(AppSingleton.API_ENDPOINT_URL + "friendships/" +
                        AppSingleton.get(getActivity()).getUserID() + "/accept?auth_token=" +
                        mPreferences.getString("auth_token", AppSingleton.get(getActivity())
                        .getAuthToken()) + "&friend_id=" + requests.get(position).getId());
                        requests.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RequestTask requestTask = new RequestTask(getActivity());
                        requestTask.execute(AppSingleton.API_ENDPOINT_URL + "friendships/" +
                        AppSingleton.get(getActivity()).getUserID() + "/decline?auth_token=" +
                        mPreferences.getString("auth_token", AppSingleton.get(getActivity())
                        .getAuthToken()) + "&friend_id=" + requests.get(position).getId());
                        requests.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private class InboxTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public InboxTask(Context context)
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
                JSONArray jsonArray = json.getJSONArray("pending_friends");

                //create list of groups
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject j = jsonArray.getJSONObject(i);
                    User u = new User();
                    u.setEmail(j.getString("email"));
                    u.setId(j.getInt("id"));
                    u.setUsername(j.getString("username"));
                    requests.add(u);
                }
                if(requests.isEmpty())
                    Toast.makeText(getActivity(), "Inbox Empty", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                adapter.notifyDataSetChanged();

                super.onPostExecute(json);
            }
        }
    }

    private class RequestTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public RequestTask(Context context)
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
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");

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
    }
}
