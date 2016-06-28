package com.urvirl.app.Controller;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.urvirl.app.R;
import com.urvirl.app.View.TypefaceUtil;

/**
 * Created by Adam Fockler on 12/18/2015.
 */

public abstract class SingleFragmentActivity extends AppCompatActivity
{
    protected abstract Fragment createFragment();
    Intent i;

    @Override
    public void onCreate(Bundle saveInstanceState)
    {
        super.onCreate(saveInstanceState);

        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/tw-cen-mt-bold.ttf");
        setContentView(R.layout.activity_fragment);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
        if(fragment == null)
        {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    @Override
    public void onPause()
    {
        i = new Intent(getBaseContext(), NotifyService.class);
        ComponentName notifyService = startService(i);
        super.onPause();
    }

    @Override
    public void onStop()
    {
        i = new Intent(getBaseContext(), NotifyService.class);
        ComponentName notifyService = startService(i);
        super.onStop();
    }

    @Override
    public void onResume()
    {
        Intent intent = new Intent();
        intent.setAction(NotifyService.ACTION);
        intent.putExtra(NotifyService.STOP_SERVICE_BROADCAST_KEY,
                NotifyService.RQS_STOP_SERVICE);

        // Broadcast the given intent to all interested BroadcastReceivers
        sendBroadcast(intent);

        super.onResume();
    }
}