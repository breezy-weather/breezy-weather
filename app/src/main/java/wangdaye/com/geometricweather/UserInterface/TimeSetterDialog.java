package wangdaye.com.geometricweather.UserInterface;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;

/**
 * Set today weather forecast time.
 * */

public class TimeSetterDialog extends DialogFragment {
    // widget
    private Button done;
    private Button cancel;

    private TimePicker timeSetter;

    private SettingsFragment settingsFragment;

    // data
    private int hour;
    private int minute;

    private boolean today;

// life cycle

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_time_setter, null);
        builder.setView(view);

        this.initData();
        this.initWidget(view);

        return builder.create();
    }

// initialize

    public void setModel(SettingsFragment fragment, boolean today) {
        this.settingsFragment = fragment;
        this.today = today;
    }

    private void initData() {
        Calendar calendar = Calendar.getInstance();
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
    }

    private void initWidget(View view) {
        this.done = (Button) view.findViewById(R.id.dialog_time_setter_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                String hourText;
                String minuteText;

                if (hour < 10) {
                    hourText = "0" + Integer.toString(hour);
                } else {
                    hourText = Integer.toString(hour);
                }

                if (minute < 10) {
                    minuteText = "0" + Integer.toString(minute);
                } else {
                    minuteText = Integer.toString(minute);
                }

                if (today) {
                    editor.putString(getString(R.string.key_forecast_time_today), new String(hourText + ":" + minuteText));
                } else {
                    editor.putString(getString(R.string.key_forecast_time_tomorrow), new String(hourText + ":" + minuteText));
                }
                editor.apply();

                settingsFragment.setForecastSummary();
                dismiss();
            }
        });

        this.cancel = (Button) view.findViewById(R.id.dialog_time_setter_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        this.timeSetter = (TimePicker) view.findViewById(R.id.dialog_time_setter_time_picker);
        timeSetter.setIs24HourView(true);
        timeSetter.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                TimeSetterDialog.this.hour = hourOfDay;
                TimeSetterDialog.this.minute = minute;
            }
        });
    }
}
