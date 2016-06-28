package com.urvirl.app.Controller;

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.urvirl.app.View.FriendSearchFragment;

/**
 * Created by Adam Fockler on 2/28/2016.
 */
public class FriendSearchActivity extends SingleFragmentActivity
{
    FriendSearchFragment fragment;

    @Override
    protected Fragment createFragment()
    {
        onSearchRequested();
        fragment = new FriendSearchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query =
                    intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    @Override
    public boolean onSearchRequested()
    {
        if ((getResources().getConfiguration().uiMode& Configuration.UI_MODE_TYPE_MASK)
                != Configuration.UI_MODE_TYPE_TELEVISION)
        {
            startSearch("", true, null, false);
            return true;
        }
        else return false;
    }

    private void doSearch(String queryStr)
    {
        fragment.getUsers(queryStr);
    }
}
