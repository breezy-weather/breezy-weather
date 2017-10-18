package wangdaye.com.geometricweather.utils.remoteView;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import wangdaye.com.geometricweather.receiver.widget.WidgetDayWeekProvider;
import wangdaye.com.geometricweather.service.NormalUpdateService;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget day week utils.
 * */

public class WidgetDayWeekUtils {

    private static final int PENDING_INTENT_CODE = 114;

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_day_week_setting),
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

        // build day part.
        RemoteViews views = buildWidgetViewDayPart(
                context, weather,
                dayTime, textColor, fahrenheit,
                iconStyle, blackText, viewStyle,
                hideSubtitle, subtitleData);

        // set week part.

        views.setTextViewText(
                R.id.widget_day_week_week_1,
                getWeek(context, weather, 0));
        views.setTextViewText(
                R.id.widget_day_week_week_2,
                getWeek(context, weather, 1));
        views.setTextViewText(
                R.id.widget_day_week_week_3,
                getWeek(context, weather, 2));
        views.setTextViewText(
                R.id.widget_day_week_week_4,
                getWeek(context, weather, 3));
        views.setTextViewText(
                R.id.widget_day_week_week_5,
                getWeek(context, weather, 4));

        views.setTextViewText(
                R.id.widget_day_week_temp_1,
                getTemp(weather, fahrenheit, 0));
        views.setTextViewText(
                R.id.widget_day_week_temp_2,
                getTemp(weather, fahrenheit, 1));
        views.setTextViewText(
                R.id.widget_day_week_temp_3,
                getTemp(weather, fahrenheit, 2));
        views.setTextViewText(
                R.id.widget_day_week_temp_4,
                getTemp(weather, fahrenheit, 3));
        views.setTextViewText(
                R.id.widget_day_week_temp_5,
                getTemp(weather, fahrenheit, 4));

        views.setImageViewResource(
                R.id.widget_day_week_icon_1,
                getIconId(weather, dayTime, iconStyle, blackText, 0));
        views.setImageViewResource(
                R.id.widget_day_week_icon_2,
                getIconId(weather, dayTime, iconStyle, blackText, 1));
        views.setImageViewResource(
                R.id.widget_day_week_icon_3,
                getIconId(weather, dayTime, iconStyle, blackText, 2));
        views.setImageViewResource(
                R.id.widget_day_week_icon_4,
                getIconId(weather, dayTime, iconStyle, blackText, 3));
        views.setImageViewResource(
                R.id.widget_day_week_icon_5,
                getIconId(weather, dayTime, iconStyle, blackText, 4));

        // set text color.
        views.setTextColor(R.id.widget_day_week_week_1, textColor);
        views.setTextColor(R.id.widget_day_week_week_2, textColor);
        views.setTextColor(R.id.widget_day_week_week_3, textColor);
        views.setTextColor(R.id.widget_day_week_week_4, textColor);
        views.setTextColor(R.id.widget_day_week_week_5, textColor);
        views.setTextColor(R.id.widget_day_week_temp_1, textColor);
        views.setTextColor(R.id.widget_day_week_temp_2, textColor);
        views.setTextColor(R.id.widget_day_week_temp_3, textColor);
        views.setTextColor(R.id.widget_day_week_temp_4, textColor);
        views.setTextColor(R.id.widget_day_week_temp_5, textColor);

        // set card visibility.
        views.setViewVisibility(R.id.widget_day_week_card, showCard ? View.VISIBLE : View.GONE);

        // set intent.
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
        views.setOnClickPendingIntent(R.id.widget_day_week_button, pendingIntent);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetDayWeekProvider.class),
                views);
    }

    private static RemoteViews buildWidgetViewDayPart(Context context, Weather weather,
                                                      boolean dayTime, int textColor, boolean fahrenheit,
                                                      String iconStyle, boolean blackText, String viewStyle,
                                                      boolean hideSubtitle, String subtitleData) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_symmetry);
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_rectangle);
                break;

            case "symmetry":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_symmetry);
                break;

            case "tile":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_tile);
                break;
        }
        
        views.setImageViewResource(
                R.id.widget_day_week_icon, 
                getWeatherIconId(weather, dayTime, iconStyle, blackText));
        views.setTextViewText(
                R.id.widget_day_week_title, 
                getTitleText(weather, viewStyle, fahrenheit));
        views.setTextViewText(
                R.id.widget_day_week_subtitle, 
                getSubtitleText(weather, viewStyle, fahrenheit));
        views.setTextViewText(
                R.id.widget_day_week_time, 
                getTimeText(context, weather, viewStyle, subtitleData));

        views.setTextColor(R.id.widget_day_week_title, textColor);
        views.setTextColor(R.id.widget_day_week_subtitle, textColor);
        views.setTextColor(R.id.widget_day_week_time, textColor);
        views.setViewVisibility(R.id.widget_day_week_time, hideSubtitle ? View.GONE : View.VISIBLE);
        
        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetDayWeekProvider.class));
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
                                boolean dayTime, String iconStyle, boolean blackText, int index) {
        return WeatherHelper.getWidgetNotificationIcon(
                weather.dailyList.get(index).weatherKinds[dayTime ? 0 : 1],
                dayTime, iconStyle, blackText);
    }
}
