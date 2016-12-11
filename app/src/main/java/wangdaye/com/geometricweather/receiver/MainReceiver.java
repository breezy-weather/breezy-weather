package wangdaye.com.geometricweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.NotificationUtils;

/**
 * My receiver.
 * */

public class MainReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if (sharedPreferences.getBoolean(context.getString(R.string.key_notification), false)) {
                    NotificationUtils.startNotificationService(context);
                }
                if (sharedPreferences.getBoolean(context.getString(R.string.key_forecast_today), false)) {
                    NotificationUtils.startTodayForecastService(context);
                }
                if (sharedPreferences.getBoolean(context.getString(R.string.key_forecast_today), false)) {
                    NotificationUtils.startTomorrowForecastService(context);
                }
                break;
        }
    }
}