package wangdaye.com.geometricweather.utils.remoteView;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetDayWeekProvider;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.ChartStyleManager;

/**
 * Widget day week utils.
 * */

public class WidgetDayWeekUtils {
    // data
    private static final int PENDING_INTENT_CODE = 114;

    /** <br> UI. */

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
        boolean hideRefreshTime = sharedPreferences.getBoolean(context.getString(R.string.key_hide_refresh_time), false);
        boolean dayTime = TimeUtils.getInstance(context).getDayTime(context, weather, false).isDayTime();

        boolean fahrenheit = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.key_fahrenheit), false);

        int textColor;
        if (blackText || showCard) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        // build day part.
        RemoteViews views = buildWidgetViewDayPart(
                context, weather, dayTime, textColor, fahrenheit, viewStyle, hideRefreshTime);

        // set week icons.
        switch (ChartStyleManager.getInstance(context).getPreviewTime()) {
            case ChartStyleManager.PREVIEW_TIME_DAY:
                dayTime = true;
                break;

            case ChartStyleManager.PREVIEW_TIME_NIGHT:
                dayTime = false;
                break;
        }

        views.setImageViewResource(
                R.id.widget_day_week_icon_1,
                WeatherHelper.getWeatherIcon(
                        dayTime ? weather.dailyList.get(0).weatherKinds[0] : weather.dailyList.get(0).weatherKinds[1],
                        dayTime)[3]);
        views.setImageViewResource(
                R.id.widget_day_week_icon_2,
                WeatherHelper.getWeatherIcon(
                        dayTime ? weather.dailyList.get(1).weatherKinds[0] : weather.dailyList.get(1).weatherKinds[1],
                        dayTime)[3]);
        views.setImageViewResource(
                R.id.widget_day_week_icon_3,
                WeatherHelper.getWeatherIcon(
                        dayTime ? weather.dailyList.get(2).weatherKinds[0] : weather.dailyList.get(2).weatherKinds[1],
                        dayTime)[3]);
        views.setImageViewResource(
                R.id.widget_day_week_icon_4,
                WeatherHelper.getWeatherIcon(
                        dayTime ? weather.dailyList.get(3).weatherKinds[0] : weather.dailyList.get(3).weatherKinds[1],
                        dayTime)[3]);
        views.setImageViewResource(
                R.id.widget_day_week_icon_5,
                WeatherHelper.getWeatherIcon(
                        dayTime ? weather.dailyList.get(4).weatherKinds[0] : weather.dailyList.get(4).weatherKinds[1],
                        dayTime)[3]);

        // set week texts.
        String firstWeekDay;
        String secondWeekDay;
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String[] weatherDates = weather.base.date.split("-");
        if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month
                && Integer.parseInt(weatherDates[2]) == day) {
            firstWeekDay = context.getString(R.string.today);
            secondWeekDay = weather.dailyList.get(1).week;
        } else if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month
                && Integer.parseInt(weatherDates[2]) == day - 1) {
            firstWeekDay = context.getString(R.string.yesterday);
            secondWeekDay = context.getString(R.string.today);
        } else {
            firstWeekDay = weather.dailyList.get(0).week;
            secondWeekDay = weather.dailyList.get(1).week;
        }

        views.setTextViewText(
                R.id.widget_day_week_week_1,
                firstWeekDay);
        views.setTextViewText(
                R.id.widget_day_week_week_2,
                secondWeekDay);
        views.setTextViewText(
                R.id.widget_day_week_week_3,
                weather.dailyList.get(2).week);
        views.setTextViewText(
                R.id.widget_day_week_week_4,
                weather.dailyList.get(3).week);
        views.setTextViewText(
                R.id.widget_day_week_week_5,
                weather.dailyList.get(4).week);
        // set temps texts.
        views.setTextViewText(
                R.id.widget_day_week_temp_1,
                ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, false, fahrenheit));
        views.setTextViewText(
                R.id.widget_day_week_temp_2,
                ValueUtils.buildDailyTemp(weather.dailyList.get(1).temps, false, fahrenheit));
        views.setTextViewText(
                R.id.widget_day_week_temp_3,
                ValueUtils.buildDailyTemp(weather.dailyList.get(2).temps, false, fahrenheit));
        views.setTextViewText(
                R.id.widget_day_week_temp_4,
                ValueUtils.buildDailyTemp(weather.dailyList.get(3).temps, false, fahrenheit));
        views.setTextViewText(
                R.id.widget_day_week_temp_5,
                ValueUtils.buildDailyTemp(weather.dailyList.get(4).temps, false, fahrenheit));

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
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                PENDING_INTENT_CODE,
                IntentHelper.buildMainActivityIntent(context, location),
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_day_week_button, pendingIntent);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetDayWeekProvider.class),
                views);
    }

    private static RemoteViews buildWidgetViewDayPart(Context context, Weather weather,
                                                      boolean dayTime, int textColor, boolean fahrenheit,
                                                      String viewStyle, boolean hideRefreshTime) {
        int[] imageId = WeatherHelper.getWeatherIcon(weather.realTime.weatherKind, dayTime);
        RemoteViews views;
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_rectangle);

                String[] texts = WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit);

                views.setImageViewResource(R.id.widget_day_week_icon, imageId[3]);
                views.setTextViewText(R.id.widget_day_week_title, texts[0]);
                views.setTextViewText(R.id.widget_day_week_subtitle, texts[1]);
                views.setTextViewText(
                        R.id.widget_day_week_time,
                        weather.base.city + " " + weather.base.time);

                views.setTextColor(R.id.widget_day_week_title, textColor);
                views.setTextColor(R.id.widget_day_week_subtitle, textColor);
                views.setTextColor(R.id.widget_day_week_time, textColor);
                views.setViewVisibility(R.id.widget_day_week_time, hideRefreshTime ? View.GONE : View.VISIBLE);
                return views;

            case "symmetry":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_symmetry);

                views.setImageViewResource(R.id.widget_day_week_icon, imageId[3]);
                views.setTextViewText(
                        R.id.widget_day_week_title,
                        weather.base.city + "\n" + ValueUtils.buildCurrentTemp(weather.realTime.temp, true, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_week_subtitle,
                        weather.realTime.weather + "\n" + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_week_time,
                        weather.dailyList.get(0).week + " " + weather.base.time);

                views.setTextColor(R.id.widget_day_week_title, textColor);
                views.setTextColor(R.id.widget_day_week_subtitle, textColor);
                views.setTextColor(R.id.widget_day_week_time, textColor);
                views.setViewVisibility(R.id.widget_day_week_time, hideRefreshTime ? View.GONE : View.VISIBLE);
                return views;

            case "tile":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_tile);

                views.setImageViewResource(R.id.widget_day_week_icon, imageId[3]);
                views.setTextViewText(
                        R.id.widget_day_week_title,
                        weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_week_subtitle,
                        ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_week_time,
                        weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);

                views.setTextColor(R.id.widget_day_week_title, textColor);
                views.setTextColor(R.id.widget_day_week_subtitle, textColor);
                views.setTextColor(R.id.widget_day_week_time, textColor);
                views.setViewVisibility(R.id.widget_day_week_time, hideRefreshTime ? View.GONE : View.VISIBLE);
                return views;
        }
        return new RemoteViews(context.getPackageName(), R.layout.widget_day_week_symmetry);
    }

    /** <br> data. */

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetDayWeekProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }
}
