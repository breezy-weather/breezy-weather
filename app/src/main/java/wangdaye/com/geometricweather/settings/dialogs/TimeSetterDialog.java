package wangdaye.com.geometricweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialog;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Time setter dialog.
 * */

public class TimeSetterDialog extends GeoDialog
        implements View.OnClickListener, TimePicker.OnTimeChangedListener {

    private OnTimeChangedListener mListener;

    private int mHour;
    private int mMinute;
    private boolean mToday = true;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_time_setter, null, false);
        initData();
        initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    public void setIsToday(boolean today) {
        mToday = today;
    }

    private void initData() {
        Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
    }

    private void initWidget(View view) {
        Button done = view.findViewById(R.id.dialog_time_setter_done);
        done.setOnClickListener(this);

        Button cancel = view.findViewById(R.id.dialog_time_setter_cancel);
        cancel.setOnClickListener(this);

        TimePicker timePicker = view.findViewById(R.id.dialog_time_setter_time_picker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(this);
    }

    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_time_setter_container);
    }

    // interface.

    // on time changed listener.

    public interface OnTimeChangedListener {
        void timeChanged();
    }

    public void setOnTimeChangedListener(OnTimeChangedListener l) {
        mListener = l;
    }

    // on time changed listener.

    @Override
    public void onTimeChanged(TimePicker timePicker, int i, int i1) {
        mHour = i;
        mMinute = i1;
    }

    // on click.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_time_setter_cancel:
                dismiss();
                break;

            case R.id.dialog_time_setter_done:
                String hourText;
                String minuteText;

                if (mHour < 10) {
                    hourText = "0" + mHour;
                } else {
                    hourText = Integer.toString(mHour);
                }

                if (mMinute < 10) {
                    minuteText = "0" + mMinute;
                } else {
                    minuteText = Integer.toString(mMinute);
                }

                SharedPreferences.Editor editor
                        = PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit();
                if (mToday) {
                    SettingsOptionManager.getInstance(requireActivity())
                            .setTodayForecastTime(hourText + ":" + minuteText);
                    editor.putString(getString(R.string.key_forecast_today_time), hourText + ":" + minuteText);
                } else {
                    SettingsOptionManager.getInstance(requireActivity())
                            .setTomorrowForecastTime(hourText + ":" + minuteText);
                    editor.putString(getString(R.string.key_forecast_tomorrow_time), hourText + ":" + minuteText);
                }
                editor.apply();

                if (mListener != null) {
                    mListener.timeChanged();
                }

                dismiss();
                break;
        }
    }
}
