package com.urvirl.app.View;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.NotifyService;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Adam Fockler on 3/17/2016.
 */
public class MemberInvitesFragment extends Fragment
{
    public static final String EXTRA_GROUP = "myapplication.GROUP";

    private EditText inviteInput;
    private Button inviteBtn;

    private ChatGroup group;

    AppSingleton appSingleton;

    SharedPreferences mPreferences;

    String multipleInvites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Intent intent = new Intent();
        intent.setAction(NotifyService.ACTION);
        intent.putExtra(NotifyService.STOP_SERVICE_BROADCAST_KEY,
                NotifyService.RQS_STOP_SERVICE);

        // Broadcast the given intent to all interested BroadcastReceivers
        getActivity().sendBroadcast(intent);
        appSingleton = AppSingleton.get(getActivity());
        View v = inflater.inflate(R.layout.member_invites, container, false);

        //get the user information
        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);

        //get the group
        group = (ChatGroup)getArguments().getSerializable(EXTRA_GROUP);

        //set the title
        getActivity().setTitle(group.toString());

        //create the edit text in java
        inviteInput = (EditText)v.findViewById(R.id.invite_input);

        //create the send button
        inviteBtn = (Button)v.findViewById(R.id.invite_btn);
        //tell the send button what to do
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multipleInvites = inviteInput.getText().toString().replace(" ", "%20");
                inviteInput.setText("");
                if(multipleInvites.isEmpty())
                    Toast.makeText(getActivity(), "Please enter at least one email",
                            Toast.LENGTH_LONG).show();
                else
                {
                    InviteTask inviteTask = new InviteTask(getActivity());
                    inviteTask.setMessageLoading("Sending Invites...");
                    inviteTask.execute(AppSingleton.API_ENDPOINT_URL + "groups/" +
                            group.getGroup_id() + "/multiple_invites?group_id=" +
                            group.getGroup_id() + "&auth_token=" + mPreferences
                            .getString("auth_token", appSingleton.getAuthToken()) +
                            "&multiple_invites=" + multipleInvites +
                            "&user_id=" + mPreferences.getInt("user_id",appSingleton.getUserID()));
                }
            }
        });

        return v;
    }



    private class InviteTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public InviteTask(Context context)
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

        }
    }
}
