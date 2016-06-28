package com.urvirl.app.Model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Adam Fockler on 3/24/2016.
 */
public class Event implements Serializable
{
    int id;
    String name;
    String email;
    Date startAt;
    Date endAt;
    String content;
    String createdAt;
    String updatedAt;
    int groupId;

    public Event()
    {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStartAt() {
        if (startAt == null) return "no start date";
        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssz", Locale.getDefault());
        return format.format(startAt);
    }

    public Date getStartAtDate()
    {
        if (startAt == null) return new Date();
        return startAt;
    }

    public String getStartDate()
    {
        SimpleDateFormat format =
                new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());

        if (startAt == null) return format.format(new Date());
        return format.format(startAt);
    }

    public String getStartTime()
    {
        SimpleDateFormat format =
                new SimpleDateFormat("hh:mm aa", Locale.getDefault());

        if (startAt == null) return format.format(new Date());
        return format.format(startAt);
    }

    public void setStartAt(String d) {
        if (d.equals("null")) return;
        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssz", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            if ( d.endsWith( "Z" ) ) {
                Calendar mCalendar = new GregorianCalendar();
                TimeZone mTimeZone = mCalendar.getTimeZone();
                int mGMTOffset = mTimeZone.getRawOffset();
                d = d.substring( 0, d.length() - 1) + "GMT" +
                        TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS) + ":00";
            } else {
                int inset = 6;

                String s0 = d.substring( 0, d.length() - inset );
                String s1 = d.substring( d.length() - inset, d.length() );

                d = s0 + "GMT" + s1;
            }
            startAt = format.parse(d);
            int hour = Integer.parseInt(d.substring(11,13));
            startAt.setHours(hour);
        }
        catch(ParseException pe) {
            System.out.println("Parse Exception");
            pe.printStackTrace();
        }
    }

    public void setStartDate(Date d)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startAt);
        Calendar dCal = Calendar.getInstance();
        dCal.setTime(d);
        dCal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        dCal.set(Calendar.HOUR,calendar.get(Calendar.HOUR));
        startAt = dCal.getTime();
    }

    public void setStartTime(Date d)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startAt);
        Calendar dCal = Calendar.getInstance();
        dCal.setTime(d);
        dCal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        dCal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        dCal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        startAt = dCal.getTime();
    }

    public void setStart(Date d)
    {
        startAt = d;
    }

    public String getEndAt()
    {
        if (endAt == null) return "no end date";
        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssz", Locale.getDefault());
        return format.format(endAt);
    }

    public Date getEndAtDate()
    {
        if (endAt == null) return new Date();
        return endAt;
    }

    public String getEndDate()
    {
        SimpleDateFormat format =
                new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());

        if (endAt == null) return format.format(new Date());
        return format.format(endAt);
    }

    public String getEndTime()
    {
        SimpleDateFormat format =
                new SimpleDateFormat("hh:mm aa", Locale.getDefault());

        if (endAt == null) return format.format(new Date());
        return format.format(endAt);
    }

    public void setEndAt(String d) {
        if (d.equals("null")) return;
        try {
            SimpleDateFormat format =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssz", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            if ( d.endsWith( "Z" ) ) {
                Calendar mCalendar = new GregorianCalendar();
                TimeZone mTimeZone = mCalendar.getTimeZone();
                int mGMTOffset = mTimeZone.getRawOffset();
                d = d.substring( 0, d.length() - 1) + "GMT" +
                        TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS) + ":00";
            } else {
                int inset = 6;

                String s0 = d.substring( 0, d.length() - inset );
                String s1 = d.substring( d.length() - inset, d.length() );

                d = s0 + "GMT" + s1;
            }
            endAt = format.parse(d);
            int hour = Integer.parseInt(d.substring(11,13));
            startAt.setHours(hour);
        }
        catch(ParseException pe) {
            System.out.println("Parse Exception");
            pe.printStackTrace();
        }
    }

    public void setEndDate(Date d)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endAt);
        Calendar dCal = Calendar.getInstance();
        dCal.setTime(d);
        dCal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        dCal.set(Calendar.HOUR,calendar.get(Calendar.HOUR));
        endAt = dCal.getTime();
    }

    public void setEndTime(Date d)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endAt);
        Calendar dCal = Calendar.getInstance();
        dCal.setTime(d);
        dCal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        dCal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        dCal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        endAt = dCal.getTime();
    }

    public void setEnd(Date d)
    {
        endAt = d;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}
