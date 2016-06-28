package com.urvirl.app.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Model.AppSingleton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Adam Fockler on 1/7/2016.
 */
public class Logout extends UrlJsonAsyncTask
{
    private  static final String TAG = "Logout";
    SharedPreferences mPreferences;
    HttpURLConnection con;
    int HttpResult = 0;

    public Logout(Context context)
    {
        super(context);
        mPreferences = context.getSharedPreferences("CurrentUser", context.MODE_PRIVATE);
    }

    @Override
    protected JSONObject doInBackground(String... urls)
    {
        JSONObject userObj = new JSONObject();
        JSONObject holder = new JSONObject();
        String response;
        JSONObject json = new JSONObject();

        try {
            URL object = new URL(urls[0]);

            con = (HttpURLConnection) object.openConnection();
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestMethod("DELETE");

            con.connect();
            HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                response = br.readLine();
                br.close();
                json = new JSONObject(response);
                System.out.println(con.getResponseMessage());
                SharedPreferences.Editor editor = context.getSharedPreferences("CurrentUser",
                        context.MODE_PRIVATE).edit();
                editor.clear();
                editor.putBoolean("logged_in", false);
                editor.commit();
                AppSingleton.get(context).clearGroups();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject json)
    {
        try {
            if(HttpResult != HttpURLConnection.HTTP_OK)
            {
                Toast.makeText(context, "Http Error " + HttpResult + "-" + con.getResponseMessage(),
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                AppSingleton.get(context).setLoggedIn(false);

                Intent intent = new Intent();
                intent.setAction(NotifyService.ACTION);
                intent.putExtra(NotifyService.STOP_SERVICE_BROADCAST_KEY,
                        NotifyService.RQS_STOP_SERVICE);

                // Broadcast the given intent to all interested BroadcastReceivers
                context.sendBroadcast(intent);
                Intent i = new Intent(context, MainActivity.class);
                context.startActivity(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            super.onPostExecute(json);
        }
    }
}
