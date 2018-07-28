package wangdaye.com.geometricweather.utils.remoteView;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetTextProvider;
import wangdaye.com.geometricweather.service.NormalUpdateService;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

/**
 * Widget text utils.
 * */

public class WidgetTextUtils {

    private static final int PENDING_INTENT_CODE = 115;

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_text_setting),
                Context.MODE_PRIVATE);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);

        SharedPreferences defaultSharePreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false);
        boolean touchToRefresh = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh),
                false);

        RemoteViews views = buildWidgetView(context, weather, fahrenheit, blackText);

        PendingIntent pendingIntent;
        if (touchToRefresh) {
            pendingIntent = PendingIntent.getService(
                    context,
                    PENDING_INTENT_CODE,
                    new Intent(context, NormalUpdateService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    PENDING_INTENT_CODE,
                    IntentHelper.buildMainActivityIntent(context, location),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_text_button, pendingIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetTextProvider.class),
                views);
    }

    private static RemoteViews buildWidgetView(Context context,
                                               Weather weather, boolean fahrenheit, boolean blackText) {
        int textColor;
        if (blackText) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_text);

        views.setTextViewText(
                R.id.widget_text_weather,
                getWeather(weather));
        views.setTextViewText(
                R.id.widget_text_temperature,
                getTemperature(weather, fahrenheit));

        views.setTextColor(R.id.widget_text_date, textColor);
        views.setTextColor(R.id.widget_text_weather, textColor);
        views.setTextColor(R.id.widget_text_temperature, textColor);

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetTextProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    public static String getWeather(Weather weather) {
        return weather.realTime.weather;
    }

    public static String getTemperature(Weather weather, boolean fahrenheit) {
        return ValueUtils.buildAbbreviatedCurrentTemp(weather.realTime.temp, fahrenheit);
    }
}
