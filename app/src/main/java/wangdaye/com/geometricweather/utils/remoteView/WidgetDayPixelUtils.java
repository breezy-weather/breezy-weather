package wangdaye.com.geometricweather.utils.remoteView;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetDayPixelProvider;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget day pixel utils.
 * */

public class WidgetDayPixelUtils {
    // data
    private static final int PENDING_INTENT_CODE = 112;

    /** <br> UI. */

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_day_pixel_setting),
                Context.MODE_PRIVATE);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);
        boolean dayTime = TimeUtils.getInstance(context).getDayTime(context, weather, false).isDayTime();

        int textColor;
        if (blackText) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_day_pixel);

        int[] imageId = WeatherHelper.getWeatherIcon(weather.realTime.weatherKind, dayTime);
        views.setImageViewResource(
                R.id.widget_day_pixel_icon,
                imageId[3]);

        views.setTextViewText(
                R.id.widget_day_pixel_temp,
                weather.realTime.temp + "℃");
        views.setTextViewText(
                R.id.widget_day_pixel_date,
                weather.base.date.split("-", 2)[1] + " " + weather.base.city);

        views.setTextColor(R.id.widget_day_pixel_temp, textColor);
        views.setTextColor(R.id.widget_day_pixel_date, textColor);

        // set intent.
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                PENDING_INTENT_CODE,
                IntentHelper.buildMainActivityIntent(context, location),
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_day_button, pendingIntent);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetDayPixelProvider.class),
                views);
    }

    /** <br> data. */

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetDayPixelProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }
}
