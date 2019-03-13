package wangdaye.com.geometricweather.remote.utils;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.background.ServiceHelper;

abstract class AbstractRemoteViewsUtils {

    static PendingIntent getWeatherPendingIntent(Context context,
                                                 @Nullable Location location, int requestCode) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                IntentHelper.buildMainActivityIntent(context, location),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static PendingIntent getRefreshPendingIntent(Context context, int requestCode) {
        return PendingIntent.getService(
                context,
                requestCode,
                ServiceHelper.getAwakePollingUpdateServiceIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static PendingIntent getAlarmPendingIntent(Context context, int requestCode) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                new Intent(AlarmClock.ACTION_SHOW_ALARMS),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static PendingIntent getCalendarPendingIntent(Context context, int requestCode) {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, System.currentTimeMillis());
        return PendingIntent.getActivity(
                context,
                requestCode,
                new Intent(Intent.ACTION_VIEW).setData(builder.build()),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
