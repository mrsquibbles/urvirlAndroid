package com.urvirl.app.View;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.EventListActivity;
import com.urvirl.app.Controller.Logout;
import com.urvirl.app.Controller.NotifyService;
import com.urvirl.app.Controller.SettingsActivity;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.Model.ChatMessage;
import com.urvirl.app.Model.MessageAdapter;
import com.urvirl.app.R;

import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Adam Fockler on 12/18/2015.
 */
public class GroupChatFragment extends ListFragment
{
    private SharedPreferences mPreferences;

    public static final String EXTRA_GROUP = "myapplication.GROUP";
    private static final int REQUEST_PHOTO = 1;
    private static final String TAG = "GroupChatFragment";
    private static final String STORED_INSTANCE_KEY_FILE_URI = "output_file_uri";

    private Firebase ref;
    private Firebase notificationRef;

    private MessageAdapter adapter;

    private ListView lv;

    private EditText chatInput;
    private Button sendChatBtn;
    private ImageButton mediaBtn;

    private ChatGroup group;

    AppSingleton appSingleton;

    private Uri outputFileUri;

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
        View v = inflater.inflate(R.layout.chat_fragment, container, false);
        super.hasOptionsMenu();
        setHasOptionsMenu(true);

        //get the user information
        mPreferences = getActivity().getSharedPreferences("CurrentUser",
                getActivity().MODE_PRIVATE);

        //get the group
        group = (ChatGroup)getArguments().getSerializable(EXTRA_GROUP);

        // Get a reference to our posts
        ref = appSingleton.getFirebaseConnection("chat/room-messages/"
                + group.getChat_id());
        notificationRef = appSingleton.getFirebaseConnection("queue/tasks");
        Query refQuery = ref.limitToLast(100).orderByChild("timestamp");

        //set the title
        getActivity().setTitle(group.toString());

        //create the edit text in java
        chatInput = (EditText)v.findViewById(R.id.chat_input);

        //initialize the list of messages
        adapter = new MessageAdapter(getActivity(),
                R.layout.user_chat_message, R.layout.chat_message, R.layout.media_user_message,
                R.layout.media_message, group.getGroup_color(), group.getmChat(),
                group.getGroup_id());
        setListAdapter(adapter);

        //create the list in java
        lv = (ListView) v.findViewById(android.R.id.list);;
        lv.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        lv.setDivider(null);
        lv.setAdapter(adapter);

        //tell the list to update when messages are updated
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                lv.setSelection(adapter.getCount() - 1);
            }
        });

        //create the send button
        sendChatBtn = (Button)v.findViewById(R.id.send_btn);
        //tell the send button what to do
        sendChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get message and clear input
                String messageString = chatInput.getText().toString();
                if (!messageString.isEmpty()) {
                    chatInput.setText("");

                    //send message to server
                    Map<String, Object> post = new HashMap<String, Object>();
                    post.put("name", mPreferences.getString("user_name", ""));
                    post.put("message", messageString);
                    post.put("userId", mPreferences.getInt("user_id", appSingleton.getUserID()));
                    post.put("timestamp", System.currentTimeMillis());
                    post.put("type", "default");
                    Firebase messageRef = ref.push();
                    messageRef.setValue(post);

                    //add extra information and send to notification task
                    post.put("chat_room", group.getChat_id());
                    UUID signature = UUID.randomUUID();
                    post.put("signature", signature);
                    notificationRef.child(signature.toString()).setValue(post);

                    //update the messageId
                    String msg = messageRef.getKey();
                    post = new HashMap<String, Object>();
                    post.put("messageId", msg);
                    ref.child(msg).updateChildren(post);
                }
            }
        });

        //refQuery
        refQuery.addValueEventListener(new ValueEventListener() {
            // Retrieve new posts as they are added to the database
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                adapter.clear();
                group.clear();
                for (DataSnapshot messages : snapshot.getChildren())
                {
                    ChatMessage message = messages.getValue(ChatMessage.class);
                    group.addToChat(message);
                }
                adapter.refresh(group.getmChat());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError)
            {
            }
        });

        mediaBtn = (ImageButton)v.findViewById(R.id.media_btn);
        mediaBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openImageIntent();
            }
        });

        // if camera is not available, disable camera functionality
        PackageManager pm = getActivity().getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) &&
                !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            mediaBtn.setEnabled(false);
        }

        return v;
    }

    private void openImageIntent() {

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory()
                + File.separator + "urvirl" + File.separator);
        root.mkdirs();
        final String fname = "img_"+ System.currentTimeMillis() +".png";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getContext().getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName,
                    res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/png");
        galleryIntent.setAction(Intent.ACTION_PICK);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(
                new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, REQUEST_PHOTO);
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );

        if ( outputFileUri != null ) {
            outState.putString( STORED_INSTANCE_KEY_FILE_URI, outputFileUri.toString() );
        }
    }

    @Override
    public void onViewStateRestored( Bundle savedInstanceState ) {
        super.onViewStateRestored( savedInstanceState );

        if ( savedInstanceState != null ) {
            final String outputFileUriStr = savedInstanceState.getString(
                    STORED_INSTANCE_KEY_FILE_URI );
            if ( outputFileUriStr != null && !outputFileUriStr.isEmpty() ) {
                outputFileUri = Uri.parse( outputFileUriStr );
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUEST_PHOTO) {
                final boolean isCamera;
                if (data == null || data.getData() == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                }

                File temp;
                if (isCamera) {
                    temp = new File(outputFileUri.getPath());
                } else {
                    temp = new File(data.getData().getPath() + ".png");
                }

                String key = mPreferences.getInt("user_id", appSingleton.getUserID()) + "-" +
                        group.getGroup_id() + "-" + UUID.randomUUID();
                System.out.println(key);

                // Create an S3 client
                AmazonS3 s3 = new AmazonS3Client(appSingleton.getCredentials());


                // Set the region of your S3 bucket
                s3.setRegion(Region.getRegion(Regions.US_EAST_1));

                // Initializes TransferUtility
                TransferUtility transferUtility = new TransferUtility(s3, getContext());

                // Starts a download
                TransferObserver observer = transferUtility.upload("urvirl2015",
                        "uploads/photo/photo/" + group.getGroup_id() + "/" + key, temp);

                //send message to server
                Map<String, Object> post = new HashMap<String, Object>();
                post.put("name", mPreferences.getString("user_name", ""));
                post.put("message", "");
                post.put("userId", mPreferences.getInt("user_id", appSingleton.getUserID()));
                post.put("timestamp", System.currentTimeMillis());
                post.put("type", "media");
                post.put("key",key);
                Firebase messageRef = ref.push();
                messageRef.setValue(post);

                //add extra information and send to notification task
                post.put("chat_room", group.getChat_id());
                UUID signature = UUID.randomUUID();
                post.put("signature", signature);
                notificationRef.child(signature.toString()).setValue(post);

                //update the messageId
                String msg = messageRef.getKey();
                post = new HashMap<String, Object>();
                post.put("messageId", msg);
                ref.child(msg).updateChildren(post);

                ImageUploadTask imageUploadTask = new ImageUploadTask(getContext());
                imageUploadTask.setMessageLoading("");
                imageUploadTask.execute(AppSingleton.API_ENDPOINT_URL + "groups/" +
                        group.getGroup_id() + "/upload_photo?auth_token=" +
                        mPreferences.getString("auth_token",appSingleton.getAuthToken()) +
                        "&photo_key=" + key);
            }
        }
    }

    //States we will have a menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.group_menu, menu);
    }

    //What Options in the menu will do
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        //Handle item selection
        switch(item.getItemId())
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.contact_us:
                i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"support@uvirl.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "");
                i.putExtra(Intent.EXTRA_TEXT, "");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                }catch(android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "There are no email clients installed.",
                            Toast.LENGTH_SHORT).show();
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
            case R.id.settings:
                i = new Intent(getActivity(), SettingsActivity.class);
                i.putExtra(EXTRA_GROUP, group);
                startActivityForResult(i, 0);
                return true;
            case R.id.events:
                i = new Intent(getActivity(), EventListActivity.class);
                i.putExtra(EXTRA_GROUP, group);
                startActivityForResult(i, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    //handles login
    private class ImageUploadTask extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public ImageUploadTask(Context context)
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

                HttpResult = con.getResponseCode();
                final int FINALHttpResult = HttpResult;
                if (HttpResult == HttpURLConnection.HTTP_OK)
                {
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
        }
    }
}