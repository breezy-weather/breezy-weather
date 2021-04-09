package wangdaye.com.geometricweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.settings.ConfigStore;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Time setter dialog.
 * */

public class TimeSetterDialog extends GeoDialog
        implements View.OnClickListener, TimePicker.OnTimeChangedListener {

    private int mHour;
    private int mMinute;
    private boolean mToday = true;

    public static final String ACTION_SET_TIME = "com.wangdaye.geometricweather.SET_TIME";
    public static final String KEY_HOUR = "hour";
    public static final String KEY_MINUTE = "minute";
    public static final String KEY_TODAY = "today";

    public static TimeSetterDialog getInstance(boolean today) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_TODAY, today);

        TimeSetterDialog dialog = new TimeSetterDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_time_setter, container, false);
        initData();
        initWidget(view);
        return view;
    }

    private void initData() {
        Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        Bundle bundle = getArguments();
        if (bundle == null) {
            mToday = true;
        } else {
            mToday = bundle.getBoolean(KEY_TODAY, true);
        }
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

    // interface.

    // on time changed listener.

    @Override
    public void onTimeChanged(TimePicker timePicker, int i, int i1) {
        mHour = i;
        mMinute = i1;
    }

    // on click.

    @SuppressLint("NonConstantResourceId")
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

                ConfigStore.Editor editor = ConfigStore.getInstance(requireActivity()).edit();
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

                Intent intent = new Intent(ACTION_SET_TIME);
                intent.putExtra(KEY_HOUR, mHour);
                intent.putExtra(KEY_MINUTE, mMinute);
                intent.putExtra(KEY_TODAY, mToday);
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);

                dismiss();
                break;
        }
    }
}
