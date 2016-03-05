package wangdaye.com.geometricweather.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Service.NotificationService;
import wangdaye.com.geometricweather.Service.TimeService;
import wangdaye.com.geometricweather.Service.TodayForecastService;
import wangdaye.com.geometricweather.Service.TomorrowForecastService;

/**
 * My receiver.
 * */

public class MyReceiver extends BroadcastReceiver {

    // TAG
//    private final String TAG = "MyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
                // power on the phone
                if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_switch), false)) {
                    Intent intentNotification = new Intent(context, NotificationService.class);
                    context.startService(intentNotification);
                }

                if (sharedPreferences.getBoolean(context.getString(R.string.key_timing_forecast_switch_today), false)
                        || sharedPreferences.getBoolean(context.getString(R.string.key_timing_forecast_switch_tomorrow), false)) {
                    Intent intentTimeService = new Intent(context, TimeService.class);
                    context.startService(intentTimeService);
                }
                break;

            case Intent.ACTION_TIME_TICK:
                this.sendForecast(context, sharedPreferences);
                break;

            case Intent.ACTION_TIME_CHANGED:
                this.sendForecast(context, sharedPreferences);
                break;
        }
    }

    private void sendForecast(Context context, SharedPreferences sharedPreferences) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (sharedPreferences.getBoolean(context.getString(R.string.key_timing_forecast_switch_today), false)) {
            String[] time = sharedPreferences.getString(context.getString(R.string.key_forecast_time_today), "07:00").split(":");
            if (hour == Integer.parseInt(time[0]) && minute == Integer.parseInt(time[1])) {
                Intent intentForecast = new Intent(context, TodayForecastService.class);
                context.startService(intentForecast);
            }
        }

        if (sharedPreferences.getBoolean(context.getString(R.string.key_timing_forecast_switch_tomorrow), false)) {
            String[] time = sharedPreferences.getString(context.getString(R.string.key_forecast_time_tomorrow), "21:00").split(":");
            if (hour == Integer.parseInt(time[0]) && minute == Integer.parseInt(time[1])) {
                Intent intentForecast = new Intent(context, TomorrowForecastService.class);
                context.startService(intentForecast);
            }
        }
    }
}
