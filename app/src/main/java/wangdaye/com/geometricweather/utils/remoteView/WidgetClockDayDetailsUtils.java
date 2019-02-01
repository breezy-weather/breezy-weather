package wangdaye.com.geometricweather.utils.remoteView;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayDetailsProvider;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

/**
 * Widget clock day details utils.
 * */

public class WidgetClockDayDetailsUtils extends AbstractRemoteViewsUtils {

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_details_setting),
                Context.MODE_PRIVATE);
        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);
        String clockFont = sharedPreferences.getString(context.getString(R.string.key_clock_font), "light");
        boolean dayTime = TimeManager.getInstance(context)
                .getDayTime(context, weather, false)
                .isDayTime();

        SharedPreferences defaultSharePreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false);
        boolean minimalIcon = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_widget_minimal_icon),
                false);
        boolean touchToRefresh = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh),
                false);

        int textColor;
        if (blackText || showCard) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_details);

        views.setImageViewResource(
                R.id.widget_clock_day_icon,
                getWeatherIconId(weather, dayTime, minimalIcon, blackText));

        views.setTextViewText(
                R.id.widget_clock_day_lunar,
                getLunarText(context));

        views.setTextViewText(
                R.id.widget_clock_day_subtitle,
                getSubtitleText(weather, fahrenheit));

        views.setTextViewText(
                R.id.widget_clock_day_todayTemp,
                getTodayTempText(context, weather, fahrenheit));

        views.setTextViewText(
                R.id.widget_clock_day_sensibleTemp,
                getSensibleTempText(context, weather, fahrenheit));

        views.setTextViewText(
                R.id.widget_clock_day_aqiHumidity,
                getAQIHumidityTempText(context, weather));

        views.setTextViewText(
                R.id.widget_clock_day_wind,
                getWindText(context, weather));

        views.setTextColor(R.id.widget_clock_day_clock_light, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_normal, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_black, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_aa_light, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_aa_normal, textColor);
        views.setTextColor(R.id.widget_clock_day_clock_aa_black, textColor);
        views.setTextColor(R.id.widget_clock_day_title, textColor);
        views.setTextColor(R.id.widget_clock_day_lunar, textColor);
        views.setTextColor(R.id.widget_clock_day_subtitle, textColor);
        views.setTextColor(R.id.widget_clock_day_todayTemp, textColor);
        views.setTextColor(R.id.widget_clock_day_sensibleTemp, textColor);
        views.setTextColor(R.id.widget_clock_day_aqiHumidity, textColor);
        views.setTextColor(R.id.widget_clock_day_wind, textColor);

        views.setViewVisibility(R.id.widget_clock_day_card, showCard ? View.VISIBLE : View.GONE);

        if (clockFont == null) {
            clockFont = "light";
        }
        switch (clockFont) {
            case "light":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                break;

            case "normal":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                break;

            case "black":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.VISIBLE);
                break;
        }

        setOnClickPendingIntent(context, views, location, touchToRefresh);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetClockDayDetailsProvider.class),
                views);
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetClockDayDetailsProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    public static int getWeatherIconId(Weather weather,
                                       boolean dayTime, boolean minimalIcon, boolean blackText) {
        return WeatherHelper.getWidgetNotificationIcon(
                weather.realTime.weatherKind, dayTime, minimalIcon, blackText);
    }

    public static String getLunarText(Context context) {
        return LanguageUtils.getLanguageCode(context).startsWith("zh") ?
                (" - " + LunarHelper.getLunarDate(Calendar.getInstance())) : "";
    }

    public static String getSubtitleText(Weather weather, boolean fahrenheit) {
        return weather.base.city + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);
    }

    public static String getTodayTempText(Context context, Weather weather, boolean fahrenheit) {
        return context.getString(R.string.today)
                + " "
                + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit);
    }

    public static String getSensibleTempText(Context context, Weather weather, boolean fahrenheit) {
        return context.getString(R.string.feels_like)
                + " "
                + ValueUtils.buildCurrentTemp(weather.realTime.sensibleTemp, false, fahrenheit);
    }

    public static String getAQIHumidityTempText(Context context, Weather weather) {
        if (weather.aqi != null && weather.aqi.aqi >= 0 && !TextUtils.isEmpty(weather.aqi.quality)) {
            return "AQI " + weather.aqi.aqi + " (" + weather.aqi.quality + ")";
        } else {
            return context.getString(R.string.humidity)
                    + " " + weather.index.humidity.split(":")[1];
        }
    }

    public static String getWindText(Context context, Weather weather) {
        return context.getString(R.string.wind) + " " + weather.realTime.windLevel;
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                boolean touchToRefresh) {
        // weather.
        if (touchToRefresh) {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_weather,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_REFRESH));
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_weather,
                    getWeatherPendingIntent(
                            context,
                            location,
                            GeometricWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_WEATHER));
        }

        // clock.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_light,
                getAlarmPendingIntent(
                        context, GeometricWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_LIGHT));
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_normal,
                getAlarmPendingIntent(
                        context, GeometricWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_NORMAL));
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_black,
                getAlarmPendingIntent(
                        context, GeometricWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_BLACK));

        // title.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_title,
                getCalendarPendingIntent(
                        context, GeometricWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CALENDAR));
    }
}
