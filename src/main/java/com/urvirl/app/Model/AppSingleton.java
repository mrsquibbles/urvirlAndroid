package com.urvirl.app.Model;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.firebase.client.Firebase;

import java.util.ArrayList;

/**
 * Created by Adam Fockler on 12/18/2015.
 */
public class AppSingleton
{
    public final static String API_ENDPOINT_URL = "http://mighty-mesa-2159.herokuapp.com/v1/";
    public final static String FIREBASE_URL = "https://urvirl.firebaseio.com/";
    private static final String TAG = "AppSingleton";
    private String authToken;
    private static AppSingleton sAppSingleton;
    private Context mAppContext;
    private ArrayList<ChatGroup> mGroups;
    private ArrayList<String> notified;
    private boolean loggedIn;
    private int userID;

    private AppSingleton(Context appContext)
    {
        mAppContext = appContext;
        mGroups = new ArrayList<>();
        notified = new ArrayList<>();
    }

    public static AppSingleton get(Context c)
    {
        if(sAppSingleton == null)
        {
            sAppSingleton = new AppSingleton(c.getApplicationContext());
        }
        return sAppSingleton;
    }

    public void addGroups(ArrayList<ChatGroup> groups)
    {
        mGroups = groups;
    }

    public ChatGroup getGroup(String g)
    {
        ChatGroup group;

        for(ChatGroup temp : mGroups)
        {
            if(temp.getChat_id().equals(g))
                return temp;
        }

        return null;
    }

    public ArrayList<ChatGroup> getGroups()
    {
        return mGroups;
    }

    public ChatGroup getGroup(int id)
    {
        System.out.println("Looking for " + id);
        for (ChatGroup c: mGroups)
        {
            System.out.println(c.getGroup_id());
            if(c.getGroup_id() == id) return c;
        }

        return null;
    }

    public void clearGroups()
    {
        mGroups.clear();
    }

    public Firebase getFirebaseConnection(String url)
    {
        String URLString = FIREBASE_URL + url;
        Firebase.setAndroidContext(mAppContext);
        return new Firebase(URLString);
    }

    public boolean has(String chat_id)
    {
        boolean bool = false;
        for(ChatGroup group : mGroups)
        {
            if(group.getChat_id().equals(chat_id))
                bool = true;
        }
        return bool;
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn)
    {
        this.loggedIn = loggedIn;
    }

    public int getUserID()
    {
        return userID;
    }

    public void setUserID(int userID)
    {
        this.userID = userID;
    }

    public  boolean equalLists(ArrayList<ChatGroup> a)
    {
        // Check for sizes and nulls
        if ((a.size() != mGroups.size()) || a.isEmpty())
            return false;

        if (a == null && mGroups == null) return true;

        return a.equals(mGroups);
    }

    public String getAuthToken()
    {
        return authToken;
    }

    public void setAuthToken(String authToken)
    {
        this.authToken = authToken;
    }

    public boolean hasBeenNotified(String id)
    {
        return notified.contains(id);
    }

    public void addToNotified(String id)
    {
        notified.add(id);
    }

    public CognitoCachingCredentialsProvider getCredentials()
    {
        return new CognitoCachingCredentialsProvider(
                mAppContext,    /* get the context for the application */
                "us-east-1:93025ea4-adea-4e96-9a1b-6e01cd7e34ad",    /* Identity Pool ID */
                Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
        );
    }
}
