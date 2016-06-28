package com.urvirl.app.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.firebase.client.Firebase;
import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.GroupListActivity;
import com.urvirl.app.Controller.GroupMembersActivity;
import com.urvirl.app.Controller.InviteFriendsActivity;
import com.urvirl.app.Controller.MemberInvitesActivity;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.R;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Adam Fockler on 2/29/2016.
 */
public class SettingsFragment extends Fragment
{
    private SharedPreferences mPreferences;

    public static final String EXTRA_GROUP = "myapplication.GROUP";
    private EditText groupNameTF,ownerTF,descriptionTF;
    private ToggleButton privateGroupTB,memberEventsTB;
    private Button edit,inviteFriends,inviteMembers,members,delete;
    private TextView textView1,textView2;

    private String groupName,owner,description,groupID;
    private boolean privateGroup,memberEvents;

    private ChatGroup group;
    private Intent i;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.create_group_fragment, container, false);
        super.hasOptionsMenu();

        getActivity().setTitle("Settings");

        //get the user information
        mPreferences = getActivity().getSharedPreferences("CurrentUser", getActivity().MODE_PRIVATE);

        //get the group
        group = (ChatGroup)getArguments().getSerializable(EXTRA_GROUP);

        //create the objects and set information
        groupNameTF = (EditText)v.findViewById(R.id.group_name);
        groupNameTF.setText(group.toString());
        ownerTF = (EditText)v.findViewById(R.id.owner);
        ownerTF.setText(group.getTeacher());
        descriptionTF = (EditText)v.findViewById(R.id.description);
        descriptionTF.setText(group.getDescription());
        textView1 = (TextView)v.findViewById(R.id.group_private);
        privateGroupTB = (ToggleButton)v.findViewById(R.id.group_private_tb);
        privateGroupTB.setChecked(group.isPrivacy());
        textView2 = (TextView)v.findViewById(R.id.member_events);
        memberEventsTB = (ToggleButton)v.findViewById(R.id.member_events_tb);
        memberEventsTB.setChecked(group.isMember_can_edit());

        //edit button
        edit = (Button)v.findViewById(R.id.create_group);
        edit.setText("Edit Group");
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!groupNameTF.getText().toString().isEmpty()) {
                    groupName = groupNameTF.getText().toString();
                    owner = ownerTF.getText().toString();
                    description = descriptionTF.getText().toString();
                    privateGroup = privateGroupTB.isChecked();
                    memberEvents = memberEventsTB.isChecked();

                    //edit group in backend
                    EditGroupTask editGroupTaskTask = new EditGroupTask(getActivity());
                    editGroupTaskTask.setMessageLoading("Editing Group...");
                    String url = (AppSingleton.API_ENDPOINT_URL + "groups/" +
                            group.getGroup_id() + "/edit?group_name=" + groupName +
                            "&group_owner=" + owner + "&group_description=" + description +
                            "&chat_id=" + groupID + "&privacy=" + privateGroup + "&members_events="
                            + memberEvents + "&auth_token=" + mPreferences.getString("auth_token",
                            AppSingleton.get(getActivity()).getAuthToken()) + "&group_id="
                            + group.getGroup_id()).replace(" ","%20");
                    editGroupTaskTask.execute(url);

                } else
                    Toast.makeText(getActivity(), "Group must have a name!",
                            Toast.LENGTH_LONG).show();
            }
        });

        //invite friends button
        inviteFriends = (Button)v.findViewById(R.id.invite_friend_btn);
        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(getActivity(),InviteFriendsActivity.class);
                i.putExtra(EXTRA_GROUP, group);
                startActivityForResult(i, 0);
            }
        });

        //invite members button
        inviteMembers = (Button)v.findViewById(R.id.invite_member_btn);
        inviteMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(getActivity(),MemberInvitesActivity.class);
                i.putExtra(EXTRA_GROUP,group);
                startActivityForResult(i,0);
            }
        });

        //members button
        members = (Button)v.findViewById(R.id.members_btn);
        members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(getActivity(),GroupMembersActivity.class);
                i.putExtra(EXTRA_GROUP, group);
                startActivityForResult(i, 0);
            }
        });

        //delete button
        delete = (Button)v.findViewById(R.id.delete_btn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Group?")
                        .setMessage("Are you sure you want to delete the group?")
                        .setCancelable(true)
                        .setPositiveButton("Delete group", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Firebase ref = AppSingleton.get(getActivity())
                                        .getFirebaseConnection("chat/room-messages/");
                                ref.child(group.getChat_id()).removeValue();
                                DeleteTask deleteTask = new DeleteTask(getActivity());
                                deleteTask.setMessageLoading("Deleting Group...");
                                String url = (AppSingleton.API_ENDPOINT_URL +
                                        "groups/" +  group.getGroup_id()) + "?auth_token=" +
                                        mPreferences.getString("auth_token", AppSingleton
                                                .get(getActivity()).getAuthToken());
                                deleteTask.execute(url);
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
        });

        //if not the group owner
        if(group.getUser_id() != AppSingleton.get(getActivity()).getUserID())
        {
            groupNameTF.setVisibility(View.GONE);
            ownerTF.setVisibility(View.GONE);
            descriptionTF.setVisibility(View.GONE);
            textView1.setVisibility(View.GONE);
            privateGroupTB.setVisibility(View.GONE);
            textView2.setVisibility(View.GONE);
            memberEventsTB.setVisibility(View.GONE);
            inviteMembers.setVisibility(View.GONE);
            members.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            edit.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red));
            edit.setText("Leave Group");
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext())
                    .setTitle("Leave Group?")
                    .setMessage("Are you sure you would like to leave the group?")
                    .setCancelable(true)
                    .setPositiveButton("Leave group", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            DeleteTask deleteTask = new DeleteTask(getActivity());
                            deleteTask.setMessageLoading("Leaving Group...");
                            String url = (AppSingleton.API_ENDPOINT_URL +
                                    "relationships/destroy?auth_token=" +
                                    mPreferences.getString("auth_token", AppSingleton.get(
                                    getActivity()).getAuthToken()) + "&followed_id=" +
                                    group.getGroup_id());
                            deleteTask.execute(url);
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
            });
            inviteFriends.setText("Members in Group");
            inviteFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    i = new Intent(getActivity(),GroupMembersActivity.class);
                    i.putExtra(EXTRA_GROUP, group);
                    startActivityForResult(i, 0);
                }
            });
        }

        return v;
    }

    //handles editing
    private class EditGroupTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public EditGroupTask(Context context)
        {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            JSONObject json = new JSONObject();
            String response = null;


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
                final int FINALHttpResult = HttpResult;
                if (HttpResult == HttpURLConnection.HTTP_OK);
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

        }
    }

    //handles editing
    private class DeleteTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public DeleteTask(Context context)
        {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            JSONObject json = new JSONObject();
            String response = null;


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
                final int FINALHttpResult = HttpResult;
                if (HttpResult == HttpURLConnection.HTTP_OK);
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
            i = new Intent(getActivity(),GroupListActivity.class);
            startActivityForResult(i, 0);
        }
    }
}
