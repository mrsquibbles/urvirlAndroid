package com.urvirl.app.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TimePicker;

import com.urvirl.app.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Adam Fockler on 3/24/2016.
 */
public class TimePickerFragment extends DialogFragment{
    public static final String EXTRA_DATE = "myapplication.DATE";
    TimePicker timePicker;
    Date mDate;

    public static TimePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_DATE, date);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        Intent i = new Intent();
        i.putExtra(EXTRA_DATE, mDate);

        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, i);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        mDate = (Date)getArguments().getSerializable(EXTRA_DATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);

        View v = getActivity().getLayoutInflater()
                .inflate(R.layout.time_dialog, null);

        timePicker = (TimePicker)v.findViewById(R.id.dialog_time_timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mDate.setHours(hourOfDay);
                mDate.setMinutes(minute);
            }
        });
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Time")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .create();
    }
}
