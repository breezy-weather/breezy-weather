package wangdaye.com.geometricweather.utils.remoteView;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayHorizontalProvider;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget clock day horizontal utils.
 * */

public class WidgetClockDayHorizontalUtils {

    private static final int WEATHER_PENDING_INTENT_CODE = 121;
    private static final int CLOCK_PENDING_INTENT_CODE = 221;

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_horizontal_setting),
                Context.MODE_PRIVATE);
        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);
        boolean dayTime = TimeManager.getInstance(context).getDayTime(context, weather, false).isDayTime();

        boolean fahrenheit = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.key_fahrenheit), false);

        int textColor;
        if (blackText || showCard) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_horizontal);

        int[] imageId = WeatherHelper.getWeatherIcon(weather.realTime.weatherKind, dayTime);
        views.setImageViewResource(
                R.id.widget_clock_day_icon,
                imageId[3]);

        String dateText = weather.base.date.split("-", 2)[1] + " " + weather.dailyList.get(0).week;
        views.setTextViewText(
                R.id.widget_clock_day_title,
                dateText);

        String weatherText = weather.base.city + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);
        views.setTextViewText(
                R.id.widget_clock_day_subtitle,
                weatherText);

        views.setTextColor(R.id.widget_clock_day_clock, textColor);
        views.setTextColor(R.id.widget_clock_day_title, textColor);
        views.setTextColor(R.id.widget_clock_day_subtitle, textColor);

        views.setViewVisibility(R.id.widget_clock_day_card, showCard ? View.VISIBLE : View.GONE);

        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(
                context,
                CLOCK_PENDING_INTENT_CODE,
                intentClock,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_clock_day_clockButton, pendingIntentClock);

        PendingIntent pendingIntentWeather = PendingIntent.getActivity(
                context,
                WEATHER_PENDING_INTENT_CODE,
                IntentHelper.buildMainActivityIntent(context, location),
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_clock_day_weatherButton, pendingIntentWeather);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetClockDayHorizontalProvider.class),
                views);
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetClockDayHorizontalProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }
}
