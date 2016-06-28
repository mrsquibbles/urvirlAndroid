package com.urvirl.app.Controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.urvirl.app.View.MainActivityFragment;

/**
 * Created by Adam Fockler on 12/18/2015.
 */

public class MainActivity extends SingleFragmentActivity
{
    private SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }

    @Override
    public void onResume()
    {
        super.onResume();

       if (mPreferences.getBoolean("logged_in",false))
        {
            //Stop the NotifyService
            Intent intent = new Intent();
            intent.setAction(NotifyService.ACTION);
            intent.putExtra(NotifyService.STOP_SERVICE_BROADCAST_KEY,
                    NotifyService.RQS_STOP_SERVICE);
            sendBroadcast(intent);

            //Go to the group list
            Intent i = new Intent(this, GroupListActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected Fragment createFragment()
    {
        return new MainActivityFragment();
    }
}