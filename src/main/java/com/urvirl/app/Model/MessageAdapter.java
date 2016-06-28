package com.urvirl.app.Model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jsonhelper.UrlJsonAsyncTask;
import com.urvirl.app.R;
import com.urvirl.app.View.PictureUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Adam Fockler on 1/22/2016.
 */
public class MessageAdapter extends BaseAdapter
{
    private Context context;
    private static final String DIALOG_IMAGE = "image";
    private int layoutResourceId,userLayoutResourceId,userMediaResourceId,mediaResourceId,groupId;
    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private SharedPreferences mPreferences;
    private String userColor;
    private File outputFile;
    ImageView imageView;
    ProgressBar loading;
    ChatMessage message;
    String path;
    AppSingleton appSingleton;

    public MessageAdapter(Activity context, int layoutResourceId, int userLayoutResourceId,
                          int userMediaResourceId, int mediaResourceId,
                          String userColor, ArrayList<ChatMessage> messages, int groupId)
    {
        appSingleton = AppSingleton.get(context);
        this.userLayoutResourceId = userLayoutResourceId;
        mPreferences = context.getSharedPreferences("CurrentUser", context.MODE_PRIVATE);
        this.layoutResourceId = layoutResourceId;
        this.userMediaResourceId = mediaResourceId;
        this.mediaResourceId = userMediaResourceId;
        this.userColor = userColor;
        this.context = context;
        this.messages = messages;
        this.groupId = groupId;
        Collections.sort(messages, new CustomComparator());
    }

    @Override
    public int getViewTypeCount()
    {
        return 2;
    }

    @Override
    public int getItemViewType(int position)
    {
        return (messages.get(position).getName()
                .equals(mPreferences.getString("user_name", ""))) ? 0 : 1;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        int type = getItemViewType(position);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        if(type == 0)
        {
            //inflate the layout for other people's messages
            if(messages.get(position).getType().equals("default"))
                convertView = inflater.inflate(layoutResourceId, parent, false);
            else
                convertView = inflater.inflate(mediaResourceId, parent, false);
        }
        else
        {
            //inflate the layout for messages from user
            if(messages.get(position).getType().equals("default"))
                convertView = inflater.inflate(userLayoutResourceId, parent, false);
            else
                convertView = inflater.inflate(userMediaResourceId, parent, false);
        }

        message = messages.get(position);

        if (message != null)
        {
            TextView txtTitle = (TextView) convertView.findViewById(R.id.txtTitle);
            if (txtTitle != null)
                txtTitle.setText(message.getTitle());
            if(message.getType().equals("default"))
            {
                TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
                if (txtMessage != null)
                    txtMessage.setText(message.getMessage());
                if (type == 0)
                {
                    if(!userColor.equals("#4E008E"))
                        txtMessage.setTextColor(Color.parseColor(userColor));
                    else
                        txtMessage.setTextColor(Color.parseColor("#FDFDFD"));
                }
            }
            else
            {
                loading = (ProgressBar)convertView.findViewById(R.id.loading);
				imageView = (ImageView)convertView.findViewById(R.id.mediaMessage);
                imageView.setVisibility(View.GONE);
                BitmapDrawable b;
				try
				{
                    message.getImagePath().isEmpty();
                    loading.setVisibility(View.GONE);
                    b = PictureUtils.getScaledDrawable(((Activity) context),
                            message.getImagePath());
                    imageView.setImageBitmap(b.getBitmap());
                    imageView.setVisibility(View.VISIBLE);
				}catch(NullPointerException n){
                    GetImage getImage = new GetImage(context);
                    getImage.setMessageLoading("");
                    getImage.execute(message.getKey().trim());
				}
					
            }
        }

        return convertView;
    }

    @Override
     public void notifyDataSetChanged()
    {
        Collections.sort(messages, new CustomComparator());

        super.notifyDataSetChanged();
    }

    public void refresh(ArrayList<ChatMessage> messages)
    {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void clear()
    {
        messages.clear();
    }

    public class CustomComparator implements Comparator<ChatMessage>
    {
        @Override
        public int compare(ChatMessage message1, ChatMessage message2)
        {
            return message1.getDateObj().compareTo(message2.getDateObj());
        }
    }

    private class GetImage extends UrlJsonAsyncTask
    {
        int HttpResult = 0;

        public GetImage(Context context)
        {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls)
        {
            String key =  "uploads/photo/photo/" + groupId + "/" + urls[0];
            System.out.println(key);

            try {
                File outputDir = context.getCacheDir(); // context being the Activity pointer
                outputFile = File.createTempFile("downloaded-myImage", ".png", outputDir);
                path = outputFile.toString();
                message.setImagePath(path);
				
                // Create an S3 client
                AmazonS3 s3 = new AmazonS3Client(appSingleton.getCredentials());

                // Set the region of your S3 bucket
                s3.setRegion(Region.getRegion(Regions.US_EAST_1));

                // Initializes TransferUtility
                TransferUtility transferUtility = new TransferUtility(s3, context);
				
                // Starts a download
                TransferObserver observer = transferUtility.download("urvirl2015", key, outputFile);
                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {

                    }

                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        if(bytesTotal == 0) return;
                        int percentage = (int)(bytesCurrent/bytesTotal * 100);
                        if(percentage == 100)
                        {
                            Activity activity = (Activity)context;
                            BitmapDrawable b = null;
                            b = PictureUtils.getScaledDrawable(activity, path);
                            imageView.setImageBitmap(b.getBitmap());
                            imageView.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.GONE);
                        }
                        // Do something in the callback.
                    }

                    public void onError(int id, Exception e) {
                        e.printStackTrace();
                    }
                });
            }catch (IOException io){
                io.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

            JSONObject json = new JSONObject();
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json)
        {
        }
    }
}