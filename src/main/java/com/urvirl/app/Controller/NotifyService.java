package com.urvirl.app.Controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.Model.ChatGroup;
import com.urvirl.app.R;

import java.util.ArrayList;

/**
 * Created by Adam Fockler on 3/8/2016.
 */
public class NotifyService extends Service
{
    public final static String ACTION = "NotifyServiceAction";
    public final static String STOP_SERVICE_BROADCAST_KEY = "StopServiceBroadcastKey";
    public final static int RQS_STOP_SERVICE = 1;
    public static final String EXTRA_GROUP = "myapplication.GROUP";
    public static final String TAG = "NotifyService";
    private int unreadMessages;
    private ArrayList<ChatGroup> mGroups = new ArrayList<>();

    NotifyServiceReceiver notifyServiceReceiver;


    private static SharedPreferences mPreferences;
    private static AppSingleton appSingleton;

    @Override
    public void onCreate()
    {
        appSingleton = AppSingleton.get(getApplicationContext());
        notifyServiceReceiver = new NotifyServiceReceiver();
        //get the user information
        mPreferences = getApplicationContext().getSharedPreferences("CurrentUser",
                getApplicationContext().MODE_PRIVATE);
        Gson gson = new Gson();
        ChatGroup cg;
        String json;
        for(int i=0;i<mPreferences.getInt("numGroups", 0);i++)
        {
            json = mPreferences.getString("group" + i, "");
            cg = gson.fromJson(json, ChatGroup.class);
            mGroups.add(cg);
        }
        unreadMessages = 0;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, filter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        final Firebase notificationRef = appSingleton.getFirebaseConnection("queue/tasks");
        notificationRef.addValueEventListener(new ValueEventListener() {
            // Retrieve new posts as they are added to the database
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot messages : snapshot.getChildren()) {
                    try {
                        String chat_id = messages.child("chat_room").getValue().toString();
                        int user_id = 0;
                        Object temp = messages.child("userId").getValue();
                        if (temp != null && temp instanceof String)
                            user_id = Integer.parseInt((String) temp);
                        else if (temp != null && temp instanceof Long)
                            user_id = ((Long) temp).intValue();
                        else {
                            assert temp != null;
                            System.out.println("User id is " + temp.getClass().getName());
                        }
                        String name = messages.child("name").getValue().toString();
                        String message = messages.child("message").getValue().toString();
                        for(ChatGroup cg : mGroups)
                        {
                            if (cg.getChat_id().equals(chat_id))
                            {
                                if(mPreferences.getInt("user_id",appSingleton.getUserID()) == user_id) break;
                                String id = messages.getKey();
                                if (!appSingleton.hasBeenNotified(id)) {
                                    appSingleton.addToNotified(id);
                                    unreadMessages++;
                                    getNotification(cg, name, message);
                                    break;
                                }
                            }
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

        return START_STICKY;
    }

    private void getNotification(ChatGroup group, String name, String message)
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);

        // Send Notification
        String notificationTitle = "urvirl";
        String notificationText = "New message in " + group.toString() + ", "
                + name + ": " + message;

        Intent myIntent = new Intent(getBaseContext(), GroupChatActivity.class);
        myIntent.putExtra(EXTRA_GROUP, group);

        NotificationCompat.Builder  mBuilder =
                new NotificationCompat.Builder(this);

        mBuilder.setContentTitle(notificationTitle);
        mBuilder.setContentText(notificationText);
        mBuilder.setTicker(notificationTitle);
        mBuilder.setSmallIcon(R.drawable.urvirl_logo_1024x604);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);

        // Increase notification number every time a new notification arrives //
        mBuilder.setNumber(unreadMessages);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(GroupChatActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack //
        stackBuilder.addNextIntent(myIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationID allows you to update the notification later on. //
        mNotificationManager.notify(0, mBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public class NotifyServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            int rqs = arg1.getIntExtra(STOP_SERVICE_BROADCAST_KEY, 0);

            if (rqs == RQS_STOP_SERVICE)
            {
                stopSelf();
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        .cancelAll();
                unreadMessages = 0;
            }
        }
    }
}
