package wangdaye.com.geometricweather.utils.remoteView;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetWeekProvider;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget week utils.
 * */

public class WidgetWeekUtils extends AbstractRemoteViewsUtils {

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        // get settings & time.
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_week_setting),
                Context.MODE_PRIVATE);
        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);

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

        // get text color.
        int textColor;
        if (blackText || showCard) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        // get remote views.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_week);

        // buildWeather view.

        views.setTextViewText(
                R.id.widget_week_week_1,
                getWeek(context, weather, 0));
        views.setTextViewText(
                R.id.widget_week_week_2,
                getWeek(context, weather, 1));
        views.setTextViewText(
                R.id.widget_week_week_3,
                getWeek(context, weather, 2));
        views.setTextViewText(
                R.id.widget_week_week_4,
                getWeek(context, weather, 3));
        views.setTextViewText(
                R.id.widget_week_week_5,
                getWeek(context, weather, 4));

        views.setTextViewText(
                R.id.widget_week_temp_1,
                getTemp(weather, fahrenheit, 0));
        views.setTextViewText(
                R.id.widget_week_temp_2,
                getTemp(weather, fahrenheit, 1));
        views.setTextViewText(
                R.id.widget_week_temp_3,
                getTemp(weather, fahrenheit, 2));
        views.setTextViewText(
                R.id.widget_week_temp_4,
                getTemp(weather, fahrenheit, 3));
        views.setTextViewText(
                R.id.widget_week_temp_5,
                getTemp(weather, fahrenheit, 4));

        views.setImageViewResource(
                R.id.widget_week_icon_1,
                getIconId(weather, dayTime, minimalIcon, blackText || showCard, 0));
        views.setImageViewResource(
                R.id.widget_week_icon_2,
                getIconId(weather, dayTime, minimalIcon, blackText || showCard, 1));
        views.setImageViewResource(
                R.id.widget_week_icon_3,
                getIconId(weather, dayTime, minimalIcon, blackText || showCard, 2));
        views.setImageViewResource(
                R.id.widget_week_icon_4,
                getIconId(weather, dayTime, minimalIcon, blackText || showCard, 3));
        views.setImageViewResource(
                R.id.widget_week_icon_5,
                getIconId(weather, dayTime, minimalIcon, blackText || showCard, 4));
        
        // set text color.
        views.setTextColor(R.id.widget_week_week_1, textColor);
        views.setTextColor(R.id.widget_week_week_2, textColor);
        views.setTextColor(R.id.widget_week_week_3, textColor);
        views.setTextColor(R.id.widget_week_week_4, textColor);
        views.setTextColor(R.id.widget_week_week_5, textColor);
        views.setTextColor(R.id.widget_week_temp_1, textColor);
        views.setTextColor(R.id.widget_week_temp_2, textColor);
        views.setTextColor(R.id.widget_week_temp_3, textColor);
        views.setTextColor(R.id.widget_week_temp_4, textColor);
        views.setTextColor(R.id.widget_week_temp_5, textColor);
        // set card visibility.
        views.setViewVisibility(R.id.widget_week_card, showCard ? View.VISIBLE : View.GONE);

        // set intent.
        setOnClickPendingIntent(context, views, location, touchToRefresh);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetWeekProvider.class),
                views);
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetWeekProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    public static String getWeek(Context context, Weather weather, int index) {
        if (index > 1) {
            return weather.dailyList.get(index).week;
        }

        String firstWeekDay;
        String secondWeekDay;
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String[] weatherDates = weather.base.date.split("-");
        if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month + 1
                && Integer.parseInt(weatherDates[2]) == day) {
            firstWeekDay = context.getString(R.string.today);
            secondWeekDay = weather.dailyList.get(1).week;
        } else if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month + 1
                && Integer.parseInt(weatherDates[2]) == day - 1) {
            firstWeekDay = context.getString(R.string.yesterday);
            secondWeekDay = context.getString(R.string.today);
        } else {
            firstWeekDay = weather.dailyList.get(0).week;
            secondWeekDay = weather.dailyList.get(1).week;
        }

        if (index == 0) {
            return firstWeekDay;
        } else {
            return secondWeekDay;
        }
    }

    public static String getTemp(Weather weather, boolean fahrenheit, int index) {
        return ValueUtils.buildDailyTemp(weather.dailyList.get(index).temps, false, fahrenheit);
    }

    public static int getIconId(Weather weather,
                                boolean dayTime, boolean minimalIcon, boolean blackText, int index) {
        return WeatherHelper.getWidgetNotificationIcon(
                weather.dailyList.get(index).weatherKinds[dayTime ? 0 : 1],
                dayTime, minimalIcon, blackText);
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                boolean touchToRefresh) {
        // weather.
        if (touchToRefresh) {
            views.setOnClickPendingIntent(
                    R.id.widget_week_weather,
                    getRefreshPendingIntent(context, GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_REFRESH));
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_week_weather,
                    getWeatherPendingIntent(context, location, GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_WEATHER));
        }
    }
}
