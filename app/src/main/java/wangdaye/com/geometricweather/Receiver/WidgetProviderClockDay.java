package wangdaye.com.geometricweather.Receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Service.RefreshWidgetClockDay;

/**
 * Widget provider of the widget [clock + day].
 * */

public class WidgetProviderClockDay extends AppWidgetProvider {
    // widget
    private PendingIntent pendingIntent = null;

    // TAG
//    private final String TAG = "WidgetProviderClockDay";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, RefreshWidgetClockDay.class);
        if (pendingIntent == null) {
            pendingIntent = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int hour;
        long ONE_HOUR_TIME = 1000 * 60 * 60;
        String hourSave = sharedPreferences.getString(context.getString(R.string.key_widget_time),
                context.getString(R.string.widget_time_default));
        switch (hourSave) {
            case "1hour":
                hour = 1;
                break;
            case "2hours":
                hour = 2;
                break;
            case "3hours":
                hour = 3;
                break;
            case "4hours":
                hour = 4;
                break;
            default:
                hour = 2;
                break;
        }
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), hour * ONE_HOUR_TIME, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();
        switch (action) {
            case "com.geometricweather.receiver.REFRESH_WIDGET":
                // update
                Intent intentRefresh = new Intent(context, RefreshWidgetClockDay.class);
                context.startService(intentRefresh);
                break;
        }
    }

    @Override
    public void onDisabled(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
