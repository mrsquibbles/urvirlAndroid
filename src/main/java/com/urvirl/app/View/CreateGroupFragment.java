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
import android.widget.ToggleButton;

import com.firebase.client.Firebase;
import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.GroupChatActivity;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adam Fockler on 2/25/2016.
 */
public class CreateGroupFragment extends Fragment
{
    private SharedPreferences mPreferences;

    private EditText groupNameTF,ownerTF,descriptionTF;
    private ToggleButton privateGroupTB,memberEventsTB;
    private Button create,extra1,extra2,extra3,extra4;

    private String groupName,owner,description,groupID;
    private boolean privateGroup,memberEvents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.create_group_fragment, container, false);
        super.hasOptionsMenu();

        getActivity().setTitle("Create Group");

        //get the user information
        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);

        groupNameTF = (EditText)v.findViewById(R.id.group_name);
        ownerTF = (EditText)v.findViewById(R.id.owner);
        descriptionTF = (EditText)v.findViewById(R.id.description);

        privateGroupTB = (ToggleButton)v.findViewById(R.id.group_private_tb);
        privateGroupTB.setChecked(true);
        memberEventsTB = (ToggleButton)v.findViewById(R.id.member_events_tb);

        create = (Button)v.findViewById(R.id.create_group);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!groupNameTF.getText().toString().isEmpty())
                {
                    groupName = groupNameTF.getText().toString();
                    owner = ownerTF.getText().toString();
                    description = descriptionTF.getText().toString();
                    privateGroup = privateGroupTB.isChecked();
                    memberEvents = memberEventsTB.isChecked();

                    //add group to firebase
                    String groupMetadataURL = AppSingleton.FIREBASE_URL + "chat/room-metadata/";
                    Firebase.setAndroidContext(getContext());
                    Map<String, Object> post = new HashMap<String, Object>();
                    post.put("name", groupName);
                    post.put("createdByUserId", mPreferences.getInt("user_id",
                            AppSingleton.get(getActivity()).getUserID()));
                    post.put("type", "public");
                    Firebase groupMetadataRef = new Firebase(groupMetadataURL).push();
                    groupMetadataRef.setValue(post);

                    //update the groupId
                    groupID = groupMetadataRef.getKey();
                    post = new HashMap<String, Object>();
                    post.put("id", groupID);
                    groupMetadataRef.child(groupID).updateChildren(post);

                    //add group to backend
                    CreateGroupTask createGroupTask = new CreateGroupTask(getActivity());
                    createGroupTask.setMessageLoading("Creating Group...");
                    createGroupTask.execute(AppSingleton.API_ENDPOINT_URL + "groups");
                }
                else
                    Toast.makeText(getActivity(), "Group must have a name!", Toast.LENGTH_LONG).show();
            }
        });

        extra1 = (Button)v.findViewById(R.id.invite_friend_btn);
        extra1.setVisibility(View.INVISIBLE);
        extra2 = (Button)v.findViewById(R.id.invite_member_btn);
        extra2.setVisibility(View.INVISIBLE);
        extra3 = (Button)v.findViewById(R.id.members_btn);
        extra3.setVisibility(View.INVISIBLE);
        extra4 = (Button)v.findViewById(R.id.delete_btn);
        extra4.setVisibility(View.INVISIBLE);

        return v;
    }

    //handles login
    private class CreateGroupTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public CreateGroupTask(Context context)
        {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            JSONObject groupObject = new JSONObject();
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
                groupObject.put("group_name", groupName);
                groupObject.put("group_owner", owner);
                groupObject.put("group_description", description);
                groupObject.put("chat_id", groupID);
                groupObject.put("privacy", privateGroup);
                groupObject.put("members_events", memberEvents);
                groupObject.put("auth_token", mPreferences.getString("auth_token",
                        AppSingleton.get(getActivity()).getAuthToken()));

                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(groupObject.toString());
                wr.flush();

                HttpResult = con.getResponseCode();
                final int FINALHttpResult = HttpResult;
                if (HttpResult == HttpURLConnection.HTTP_OK)
                {
                    object = new URL(AppSingleton.API_ENDPOINT_URL + "users/" +
                            mPreferences.getInt("user_id",AppSingleton.get(getContext())
                                    .getUserID()) + "/dashboard?auth_token=" + mPreferences
                            .getString("auth_token", AppSingleton.get(getActivity())
                                    .getAuthToken()));

                    con = (HttpURLConnection) object.openConnection();
                    con.setDoInput(true);
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept", "application/json");
                    con.setRequestMethod("GET");

                    // setup the returned values in case something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");

                    HttpResult = con.getResponseCode();
                    final int FINALResult = HttpResult;
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
                                Toast.makeText(getActivity(), "Http Error " + FINALResult + "-"
                                        + finalResponse, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                else if(HttpResult == HttpURLConnection.HTTP_UNAUTHORIZED)
                {
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
                //get groups from json
                JSONArray jsonArray = json.getJSONArray("groups");

                //create list of groups
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject j = jsonArray.getJSONObject(i);
                    if(j.getString("chat_id").equals(groupID))
                    {
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
                        Intent intent = new Intent(getActivity(), GroupChatActivity.class);
                        intent.putExtra(GroupListFragment.EXTRA_GROUP, group);
                        startActivityForResult(intent, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
