package com.urvirl.app.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Adam Fockler on 12/18/2015.
 */
public class ChatGroup implements Serializable
{
    private UUID id;
    private String name,teacher,created_at,updated_at,chat_id,description,group_color;
    private int user_id,group_id;
    private boolean privacy,member_can_edit,joined;

    ArrayList<ChatMessage> mChat;

    public ChatGroup()
    {
        id = UUID.randomUUID();
        mChat = new ArrayList<>();
    }

    ChatGroup(String name, String teacher, String created_at, String updated_at, String chat_id,
              String description, String group_color, int user_id, int group_id, boolean privacy,
              boolean member_can_edit)
    {
        id = UUID.randomUUID();
        this.name = name;
        this.teacher = teacher;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.chat_id = chat_id;
        this.description = description;
        this.group_color = group_color;
        this.user_id = user_id;
        this.group_id = group_id;
        this.privacy = privacy;
        this.member_can_edit = member_can_edit;
        mChat = new ArrayList<>();
    }

    public UUID getId()
    {
        return id;
    }

    public String toString()
    {
        return name;
    }

    public String getTeacher()
    {
        return teacher;
    }

    public String getCreated_at()
    {
        return created_at;
    }

    public String getUpdated_at()
    {
        return updated_at;
    }

    public String getChat_id()
    {
        return chat_id;
    }

    public String getDescription()
    {
        return description;
    }

    public String getGroup_color()
    {
        if(this.group_color.equals("null"))
            this.group_color = "#4E008E";
        return group_color;
    }

    public int getUser_id()
    {
        return user_id;
    }

    public int getGroup_id()
    {
        return group_id;
    }

    public boolean isPrivacy()
    {
        return privacy;
    }

    public boolean isMember_can_edit()
    {
        return member_can_edit;
    }

    public ArrayList<ChatMessage> getmChat()
    {
        return mChat;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setTeacher(String teacher)
    {
        this.teacher = teacher;
    }

    public void setCreated_at(String created_at)
    {
        this.created_at = created_at;
    }

    public void setUpdated_at(String updated_at)
    {
        this.updated_at = updated_at;
    }

    public void setChat_id(String chat_id)
    {
        this.chat_id = chat_id;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setGroup_color(String group_color)
    {
        this.group_color = group_color;
    }

    public void setUser_id(int user_id)
    {
        this.user_id = user_id;
    }

    public void setGroup_id(int group_id)
    {
        this.group_id = group_id;
    }

    public void setPrivacy(boolean privacy)
    {
        this.privacy = privacy;
    }

    public void setMember_can_edit(boolean member_can_edit)
    {
        this.member_can_edit = member_can_edit;
    }

    public boolean isJoined()
    {
        return joined;
    }

    public void setJoined(boolean joined)
    {
        this.joined = joined;
    }

    public void addToChat(ChatMessage message)
    {
        mChat.add(message);
    }

    public boolean contains(ChatMessage message)
    {
        for(ChatMessage cm : mChat)
        {
            if(cm.equals(message))
                return true;
        }
        return false;
    }

    public void clear()
    {
        mChat.clear();
    }
}
