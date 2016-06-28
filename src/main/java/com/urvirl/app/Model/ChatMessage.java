package com.urvirl.app.Model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Adam Fockler on 1/22/2016.
 */
public class ChatMessage  implements Serializable {
    private long timestamp;
    private Object userId;
    private String message;
    private String type;
    private String name;
    private String messageId;
    private String chat_id;
    private boolean read;
    private String key;
    private String imagePath;

    ChatMessage()
    {
        //empty default constructor, necessary for Firebase to be able to deserialize messages
    }

    ChatMessage(String message, String name, String type, long date, int userId)
    {
        this.message = message;
        this.name = name;
        this.type = type;
        this.timestamp = date;
        this.userId = userId;
    }

    ChatMessage(Map post)
    {
        this.message = (String) post.get("message");
        this.name = (String) post.get("name");
        this.type = (String) post.get("type");
        this.timestamp = (long) post.get("timestamp");
        this.userId = (int) post.get("userId");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateObj() {
        return new Date(timestamp);
    }

    public long getDateLong() {
        return timestamp;
    }

    public void setTimestamp(long date) {
        this.timestamp = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUserId()
    {
        int temp;
        if (userId instanceof String)
            temp = Integer.parseInt((String) userId);
        else if (userId instanceof Long)
            temp = ((Long) userId).intValue();
        else
            temp = -1;
        return temp;
    }

    public void setUserId(Object userId) {
        this.userId = userId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(Object messageId) {
        this.messageId = (String) messageId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mmaaa MM-dd");
        String dateString = formatter.format(new Date(timestamp));
        return name + " (" + dateString + ")";
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof ChatMessage)
        {
            sameSame = ((String) this.getMessageId()).equals((String) ((ChatMessage) object)
                    .getMessageId());
        }

        return sameSame;
    }
}
