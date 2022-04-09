package wangdaye.com.geometricweather.settings.dialogs;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.settings.SettingsManager;

public class TimeSetterDialog extends GeoDialog{

    public static final String ACTION_SET_TIME = "com.wangdaye.geometricweather.SET_TIME";
    public static final String KEY_HOUR = "hour";
    public static final String KEY_MINUTE = "minute";
    public static final String KEY_TODAY = "today";

    public static void show(Context context,
                            boolean isToday) {
        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.dialog_time_setter, null, false);

        Calendar calendar = Calendar.getInstance();
        AtomicInteger hour = new AtomicInteger(calendar.get(Calendar.HOUR_OF_DAY));
        AtomicInteger minute = new AtomicInteger(calendar.get(Calendar.MINUTE));

        TimePicker timePicker = view.findViewById(R.id.dialog_time_setter_time_picker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener((timePicker1, i, i1) -> {
            hour.set(i);
            minute.set(i1);
        });

        new MaterialAlertDialogBuilder(context)
                .setTitle(
                        isToday
                                ? R.string.settings_title_forecast_today_time
                                : R.string.settings_title_forecast_tomorrow_time
                )
                .setView(view)
                .setPositiveButton(R.string.done, (dialogInterface, i) -> {
                    String hourText;
                    String minuteText;

                    if (hour.get() < 10) {
                        hourText = "0" + hour.get();
                    } else {
                        hourText = Integer.toString(hour.get());
                    }

                    if (minute.get() < 10) {
                        minuteText = "0" + minute.get();
                    } else {
                        minuteText = Integer.toString(minute.get());
                    }

                    if (isToday) {
                        SettingsManager.getInstance(context).setTodayForecastTime(
                                hourText + ":" + minuteText);
                    } else {
                        SettingsManager.getInstance(context).setTomorrowForecastTime(
                                hourText + ":" + minuteText);
                    }

                    Intent intent = new Intent(ACTION_SET_TIME);
                    intent.putExtra(KEY_HOUR, hour.get());
                    intent.putExtra(KEY_MINUTE, minute.get());
                    intent.putExtra(KEY_TODAY, isToday);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                })
                .show();
    }
}
