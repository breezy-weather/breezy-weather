package wangdaye.com.geometricweather.utils.remoteView;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.AlarmClock;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayCenterProvider;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget clock day center utils.
 * */

public class WidgetClockDayCenterUtils {
    // data
    private static final int WEATHER_PENDING_INTENT_CODE = 122;
    private static final int CLOCK_PENDING_INTENT_CODE = 222;

    /** <br> UI. */

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        // get settings & time.
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_center_setting),
                Context.MODE_PRIVATE);
        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);
        boolean hideRefreshTime = sharedPreferences.getBoolean(context.getString(R.string.key_hide_refresh_time), false);
        boolean isDay = TimeUtils.getInstance(context).getDayTime(context, weather, false).isDayTime();

        // get text color.
        int textColor;
        if (blackText || showCard) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        // get remote views.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_center);

        // buildWeather view.
        int[] imageId = WeatherHelper.getWeatherIcon(weather.realTime.weatherKind, isDay);
        views.setImageViewResource( // set icon.
                R.id.widget_clock_day_center_icon,
                imageId[3]);
        // buildWeather weather & temps text.
        String[] texts = WidgetUtils.buildWidgetDayStyleText(weather);
        views.setTextViewText( // set weather.
                R.id.widget_clock_day_center_weather,
                texts[0]);
        views.setTextViewText( // set temps.
                R.id.widget_clock_day_center_temp,
                texts[1]);
        views.setTextViewText( // set time.
                R.id.widget_clock_day_center_refreshTime,
                weather.base.city + "." + weather.base.time);
        // set text color.
        views.setTextColor(R.id.widget_clock_day_center_clock, textColor);
        views.setTextColor(R.id.widget_clock_day_center_weather, textColor);
        views.setTextColor(R.id.widget_clock_day_center_temp, textColor);
        views.setTextColor(R.id.widget_clock_day_center_refreshTime, textColor);
        // set card visibility.
        views.setViewVisibility(R.id.widget_clock_day_center_card, showCard ? View.VISIBLE : View.GONE);
        // set refresh time visibility.
        views.setViewVisibility(R.id.widget_clock_day_center_refreshTime, hideRefreshTime ? View.GONE : View.VISIBLE);
        // set clock intent.
        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(
                context, CLOCK_PENDING_INTENT_CODE, intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_clock_day_center_clockButton, pendingIntentClock);
        // set weather intent.
        PendingIntent pendingIntentWeather = PendingIntent.getActivity(
                context,
                WEATHER_PENDING_INTENT_CODE,
                IntentHelper.buildMainActivityIntent(context, location),
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_clock_day_center_weatherButton, pendingIntentWeather);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetClockDayCenterProvider.class),
                views);
    }

    /** <br> data. */

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetClockDayCenterProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }
}
