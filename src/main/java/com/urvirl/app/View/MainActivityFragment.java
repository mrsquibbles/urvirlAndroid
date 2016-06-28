package com.urvirl.app.View;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.Controller.GroupListActivity;
import com.urvirl.app.Model.AppSingleton;
import com.urvirl.app.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Adam Fockler on 12/18/2015.
 */
public class MainActivityFragment extends Fragment
{
    private static final String TAG = "MainActivity";
    private SharedPreferences mPreferences;

    private EditText emailInput;
    private String email;

    private EditText passwordInput;
    private String password;

    private Button loginBtn;

    private TextView linkOne;
    private TextView linkTwo;

    private String deviceId;

    public MainActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        super.hasOptionsMenu();

        //ANDROID_ID
		deviceId = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
		if(deviceId.equals("9774d56d682e549c"))
			deviceId = UUID.randomUUID().toString();

        //creates the user name input in java
        emailInput = (EditText)v.findViewById(R.id.emailInput);

        //if enter is hit while the user name input is selected
        emailInput.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            passwordInput.requestFocus();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        //creates the password input in java
        passwordInput = (EditText)v.findViewById(R.id.passwordInput);

        //if enter is hit while the password input is selected
        passwordInput.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            login();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        //creates the login button in java
        loginBtn=(Button)v.findViewById(R.id.login_button);

        //tells the login button what to do
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        //sets the links
        linkOne = (TextView)v.findViewById(R.id.linkOne);
        linkOne.setText(Html.fromHtml("<a href=\"http://www.urvirl.com/privacy_policy\">View our " +
                "Privacy Policy</a>"));
        linkOne.setMovementMethod(LinkMovementMethod.getInstance());
        linkTwo = (TextView)v.findViewById(R.id.linkTwo);
        linkTwo.setText(Html.fromHtml("<a href=\"http://www.urvirl.com/users/sign_up\">No account" +
                "? Sign up here.</a>"));
        linkTwo.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

    //handles getting info for login
    private void login()
    {
        email = emailInput.getText().toString().trim();//get email
        password = passwordInput.getText().toString();//get password

        if (email.length() == 0 || password.length() == 0) {
            // input fields are empty
            Toast.makeText(getActivity(), "Please complete all the fields",
                    Toast.LENGTH_LONG).show();
            return;
        }
        else
        {
            LoginTask loginTask = new LoginTask(getActivity());
            loginTask.setMessageLoading("Logging in...");
            loginTask.execute(AppSingleton.API_ENDPOINT_URL + "users/sign_in");
        }
    }

    //States we will have a menu
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // create the shared preferences to keep the auth token in
        mPreferences = getContext().getSharedPreferences("CurrentUser", getContext().MODE_PRIVATE);

        //sets the title of the app
        getActivity().setTitle(R.string.app_name);

        setHasOptionsMenu(true);
    }

    //Which menu we will be using
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.getItem(0);
        SpannableString spanString = new SpannableString(item.getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);
    }

    //What Options in the menu will do
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        //Handle item selection
        switch(item.getItemId())
        {
            case R.id.contact_us:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"support@uvirl.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "");
                i.putExtra(Intent.EXTRA_TEXT, "");
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                }catch(android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }catch(Exception e){
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //handles login
    private class LoginTask extends UrlJsonAsyncTask
    {
        private  static final String TAG = "LoginTask";
        public LoginTask(Context context)
        {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            JSONObject json = new JSONObject();
            String response = null;


            try {
                URL object = new URL(urls[0]);

                final HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");

                // setup the returned values in case something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");
                // add the email and password to the params
                userObj.put("email", email);
                userObj.put("password", password);
                userObj.put("device_id", deviceId);
				userObj.put("device_type", "ANDROID");
                holder.put("user", userObj);

                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(holder.toString());
                wr.flush();

                final int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    response = br.readLine();
                    br.close();
                    json = new JSONObject(response);

                    //add device to online users
                    String deviceOnlineURL = AppSingleton.FIREBASE_URL + "queue/tasks";
                    Firebase.setAndroidContext(getContext());
                    Map<String, Object> post = new HashMap<String, Object>();
                    post.put("name", mPreferences.getString("user_name", ""));
                    post.put("userId", mPreferences.getInt("user_id", -1));
                    Firebase onlineRef = new Firebase(deviceOnlineURL).push();
                    onlineRef.child(mPreferences.getString("user_name", "")).setValue(post);
                }
                else if(HttpResult == HttpURLConnection.HTTP_UNAUTHORIZED)
                {
                    json.put("info", "Email and/or password are invalid. Retry!");
                }
                else
                {
                    response = con.getResponseMessage();
                    final String finalResponse = response;
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(),"Http Error " + HttpResult + "-"
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
                if (json.getBoolean("success"))
                {
                    // everything is ok
                    SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into the SharedPreferences
                    editor.putString("auth_token", json.getString("auth_token"));
                    AppSingleton.get(getActivity()).setAuthToken(json.getString("auth_token"));
                    editor.putString("email", json.getString("email"));
                    editor.putInt("user_id", json.getInt("user_id"));
                    AppSingleton.get(getContext()).setUserID(json.getInt("user_id"));
                    editor.putString("user_name", json.getString("user_name"));
                    editor.putString("password",password);
                    editor.putString("device_id", deviceId);
                    editor.putBoolean("logged_in", true);
                    editor.commit();

                    AppSingleton.get(getActivity()).setLoggedIn(true);

                    // launch the GroupList
                    Intent intent = new Intent(getActivity(), GroupListActivity.class);
                    startActivity(intent);
                }
                else
                    Toast.makeText(getActivity(), json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // something went wrong: show a Toast with the exception message
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }
}