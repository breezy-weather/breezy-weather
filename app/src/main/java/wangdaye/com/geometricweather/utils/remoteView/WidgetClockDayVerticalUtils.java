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

import java.util.Calendar;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayVerticalProvider;
import wangdaye.com.geometricweather.service.NormalUpdateService;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget clock day vertical utils.
 * */

public class WidgetClockDayVerticalUtils {

    private static final int WEATHER_PENDING_INTENT_CODE = 122;
    private static final int CLOCK_PENDING_INTENT_CODE = 222;

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_vertical_setting),
                Context.MODE_PRIVATE);
        String viewStyle = sharedPreferences.getString(context.getString(R.string.key_view_type), "rectangle");
        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);
        boolean hideSubtitle = sharedPreferences.getBoolean(context.getString(R.string.key_hide_subtitle), false);
        String subtitleData = sharedPreferences.getString(context.getString(R.string.key_subtitle_data), "time");
        boolean dayTime = TimeManager.getInstance(context).getDayTime(context, weather, false).isDayTime();

                SharedPreferences defaultSharePreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false);
        String iconStyle = defaultSharePreferences.getString(
                context.getString(R.string.key_widget_icon_style),
                "material");
        boolean touchToRefresh = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh),
                false);

        int textColor;
        if (blackText || showCard) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        RemoteViews views = buildWidgetViewDayPart(
                context, weather,
                dayTime, textColor, fahrenheit,
                iconStyle, blackText,
                viewStyle,
                hideSubtitle, subtitleData);

        views.setViewVisibility(R.id.widget_clock_day_card, showCard ? View.VISIBLE : View.GONE);

        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(
                context, CLOCK_PENDING_INTENT_CODE, intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_clock_day_clockButton, pendingIntentClock);

        PendingIntent pendingIntentWeather;
        if (touchToRefresh) {
            pendingIntentWeather = PendingIntent.getService(
                    context,
                    WEATHER_PENDING_INTENT_CODE,
                    new Intent(context, NormalUpdateService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntentWeather = PendingIntent.getActivity(
                    context,
                    WEATHER_PENDING_INTENT_CODE,
                    IntentHelper.buildMainActivityIntent(context, location),
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_clock_day_weatherButton, pendingIntentWeather);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetClockDayVerticalProvider.class),
                views);
    }

    private static RemoteViews buildWidgetViewDayPart(Context context, Weather weather,
                                                      boolean dayTime, int textColor, boolean fahrenheit,
                                                      String iconStyle, boolean blackText,
                                                      String viewStyle,
                                                      boolean hideSubtitle, String subtitleData) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_symmetry);
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_rectangle);
                break;

            case "symmetry":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_symmetry);
                break;

            case "tile":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_tile);
                break;

            case "mini":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_mini);
                break;

            case "vertical":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_vertical);
                break;
        }

        views.setImageViewResource(
                R.id.widget_clock_day_icon,
                getWeatherIconId(weather, dayTime, iconStyle, blackText));
        views.setTextViewText(
                R.id.widget_clock_day_title,
                getTitleText(weather, viewStyle, fahrenheit));
        views.setTextViewText(
                R.id.widget_clock_day_subtitle,
                getSubtitleText(weather, viewStyle, fahrenheit));
        views.setTextViewText(
                R.id.widget_clock_day_time,
                getTimeText(context, weather, viewStyle, subtitleData));

        views.setTextColor(R.id.widget_clock_day_clock, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_1, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_2, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_aa, textColor);
        views.setTextColor(R.id.widget_clock_day_title, textColor);
        views.setTextColor(R.id.widget_clock_day_subtitle, textColor);
        views.setTextColor(R.id.widget_clock_day_time, textColor);
        views.setViewVisibility(R.id.widget_clock_day_time, hideSubtitle ? View.GONE : View.VISIBLE);

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetClockDayVerticalProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    public static int getWeatherIconId(Weather weather,
                                       boolean dayTime, String iconStyle, boolean blackText) {
        return WeatherHelper.getWidgetNotificationIcon(
                weather.realTime.weatherKind, dayTime, iconStyle, blackText);
    }

    public static String getTitleText(Weather weather, String viewStyle, boolean fahrenheit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit)[0];

            case "symmetry":
                return weather.base.city + "\n" + ValueUtils.buildCurrentTemp(weather.realTime.temp, true, fahrenheit);

            case "tile":
                return weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);

            case "mini":
                return weather.realTime.weather;

            case "vertical":
                return weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);
        }
        return "";
    }

    public static String getSubtitleText(Weather weather, String viewStyle, boolean fahrenheit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit)[1];

            case "symmetry":
                return weather.realTime.weather + "\n" + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit);

            case "tile":
                return ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit);

            case "mini":
                return ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);
        }
        return "";
    }

    public static String getTimeText(Context context, Weather weather, String viewStyle, String subtitleData) {
        switch (subtitleData) {
            case "time":
                switch (viewStyle) {
                    case "rectangle":
                        return weather.base.city + " " + weather.base.time;

                    case "symmetry":
                        return WidgetUtils.getWeek(context) + " " + weather.base.time;

                    case "tile":
                    case "vertical":
                        return weather.base.city + " " + WidgetUtils.getWeek(context) + " " + weather.base.time;
                }
                break;

            case "aqi":
                if (weather.aqi != null) {
                    return weather.aqi.quality + " (" + weather.aqi.aqi + ")";
                }
                break;

            case "wind":
                return weather.realTime.windLevel + " (" + weather.realTime.windDir + weather.realTime.windSpeed + ")";

            case "lunar":
                switch (viewStyle) {
                    case "rectangle":
                        return weather.base.city + " " + LunarHelper.getLunarDate(Calendar.getInstance());

                    case "symmetry":
                        return WidgetUtils.getWeek(context) + " " + LunarHelper.getLunarDate(Calendar.getInstance());

                    case "tile":
                    case "vertical":
                        return weather.base.city + " " + WidgetUtils.getWeek(context) + " " + LunarHelper.getLunarDate(Calendar.getInstance());
                }
                break;

            default:
                return context.getString(R.string.feels_like) + " "
                        + ValueUtils.buildAbbreviatedCurrentTemp(
                        weather.realTime.sensibleTemp, GeometricWeather.getInstance().isFahrenheit());
        }
        return "";
    }
}
