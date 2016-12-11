package wangdaye.com.geometricweather.service.widget.alarm;

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

import java.util.Calendar;

import wangdaye.com.geometricweather.basic.GeoAlarmService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.view.activity.MainActivity;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayWeekProvider;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget clock day week service.
 * */


public class WidgetClockDayWeekAlarmService extends GeoAlarmService {
    // data
    private static final int WEATHER_PENDING_INTENT_CODE = 16;
    private static final int CLOCK_PENDING_INTENT_CODE = 26;
    public static final int ALARM_CODE = 6;

    /** <br> life cycle. */

    public WidgetClockDayWeekAlarmService() {
        super("WidgetClockDayWeekAlarmService");
    }

    public WidgetClockDayWeekAlarmService(String name) {
        super(name);
    }

    @Override
    public Location readSettings() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.sp_widget_clock_day_week_setting), Context.MODE_PRIVATE);
        String locationName = sharedPreferences.getString(
                getString(R.string.key_location), getString(R.string.local));
        return DatabaseHelper.getInstance(this).searchLocation(locationName);
    }

    @Override
    protected void doRefresh(Location location) {
        int[] widgetIds = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, WidgetClockDayWeekProvider.class));
        if (widgetIds != null && widgetIds.length != 0) {
            requestData(location);
            setAlarmIntent(this, getClass(), ALARM_CODE);
        }
    }

    @Override
    public void updateView(Context context, Weather weather) {
        refreshWidgetView(context, weather);
    }

    /** <br> widget. */

    public static void refreshWidgetView(Context context, Weather weather) {
        if (weather == null) {
            return;
        }

        // get settings & time.
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_week_setting),
                Context.MODE_PRIVATE);
        String locationName = sharedPreferences.getString(
                context.getString(R.string.key_location), context.getString(R.string.local));
        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);
        boolean isDay = TimeUtils.getInstance(context).getDayTime(context, weather, false).isDayTime();

        // get text color.
        int textColor;
        if (blackText || showCard) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        // get remote views.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_week);

        // build view.
        int[] imageId = WeatherHelper.getWeatherIcon(weather.live.weatherKind, isDay);
        views.setImageViewResource( // set icon.
                R.id.widget_clock_day_week_icon,
                imageId[3]);
        // build date text.
        String[] solar = weather.base.date.split("-");
        String dateText = solar[1] + "-" + solar[2] + " " + weather.base.week + " / " + weather.base.moon;
        views.setTextViewText( // set date.
                R.id.widget_clock_day_week_date,
                dateText);
        // build weather text.
        String weatherText = weather.base.location + " / " + weather.live.weather + " " + weather.live.temp + "℃";
        views.setTextViewText( // set weather text.
                R.id.widget_clock_day_week_weather,
                weatherText);
        // build week texts.
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
        // set week texts.
        views.setTextViewText(
                R.id.widget_clock_day_week_week_1,
                firstWeekDay);
        views.setTextViewText(
                R.id.widget_clock_day_week_week_2,
                secondWeekDay);
        views.setTextViewText(
                R.id.widget_clock_day_week_week_3,
                weather.dailyList.get(2).week);
        views.setTextViewText(
                R.id.widget_clock_day_week_week_4,
                weather.dailyList.get(3).week);
        views.setTextViewText(
                R.id.widget_clock_day_week_week_5,
                weather.dailyList.get(4).week);
        // set temps texts.
        views.setTextViewText(
                R.id.widget_clock_day_week_temp_1,
                weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°");
        views.setTextViewText(
                R.id.widget_clock_day_week_temp_2,
                weather.dailyList.get(1).temps[1] + "/" + weather.dailyList.get(1).temps[0] + "°");
        views.setTextViewText(
                R.id.widget_clock_day_week_temp_3,
                weather.dailyList.get(2).temps[1] + "/" + weather.dailyList.get(2).temps[0] + "°");
        views.setTextViewText(
                R.id.widget_clock_day_week_temp_4,
                weather.dailyList.get(3).temps[1] + "/" + weather.dailyList.get(3).temps[0] + "°");
        views.setTextViewText(
                R.id.widget_clock_day_week_temp_5,
                weather.dailyList.get(4).temps[1] + "/" + weather.dailyList.get(4).temps[0] + "°");
        // set icons.
        views.setImageViewResource(
                R.id.widget_clock_day_week_icon_1,
                WeatherHelper.getWeatherIcon(
                        isDay ? weather.dailyList.get(0).weatherKinds[0] : weather.dailyList.get(0).weatherKinds[1],
                        isDay)[3]);
        views.setImageViewResource(
                R.id.widget_clock_day_week_icon_2,
                WeatherHelper.getWeatherIcon(
                        isDay ? weather.dailyList.get(1).weatherKinds[0] : weather.dailyList.get(1).weatherKinds[1],
                        isDay)[3]);
        views.setImageViewResource(
                R.id.widget_clock_day_week_icon_3,
                WeatherHelper.getWeatherIcon(
                        isDay ? weather.dailyList.get(2).weatherKinds[0] : weather.dailyList.get(2).weatherKinds[1],
                        isDay)[3]);
        views.setImageViewResource(
                R.id.widget_clock_day_week_icon_4,
                WeatherHelper.getWeatherIcon(
                        isDay ? weather.dailyList.get(3).weatherKinds[0] : weather.dailyList.get(3).weatherKinds[1],
                        isDay)[3]);
        views.setImageViewResource(
                R.id.widget_clock_day_week_icon_5,
                WeatherHelper.getWeatherIcon(
                        isDay ? weather.dailyList.get(4).weatherKinds[0] : weather.dailyList.get(4).weatherKinds[1],
                        isDay)[3]);
        // set text color.
        views.setTextColor(R.id.widget_clock_day_week_clock, textColor);
        views.setTextColor(R.id.widget_clock_day_week_date, textColor);
        views.setTextColor(R.id.widget_clock_day_week_weather, textColor);
        views.setTextColor(R.id.widget_clock_day_week_week_1, textColor);
        views.setTextColor(R.id.widget_clock_day_week_week_2, textColor);
        views.setTextColor(R.id.widget_clock_day_week_week_3, textColor);
        views.setTextColor(R.id.widget_clock_day_week_week_4, textColor);
        views.setTextColor(R.id.widget_clock_day_week_week_5, textColor);
        views.setTextColor(R.id.widget_clock_day_week_temp_1, textColor);
        views.setTextColor(R.id.widget_clock_day_week_temp_2, textColor);
        views.setTextColor(R.id.widget_clock_day_week_temp_3, textColor);
        views.setTextColor(R.id.widget_clock_day_week_temp_4, textColor);
        views.setTextColor(R.id.widget_clock_day_week_temp_5, textColor);
        // set card visibility.
        views.setViewVisibility(R.id.widget_clock_day_week_card, showCard ? View.VISIBLE : View.GONE);
        // set clock intent.
        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(
                context, CLOCK_PENDING_INTENT_CODE, intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_clock_day_week_clockButton, pendingIntentClock);
        // set weather intent.
        Intent intent = new Intent("com.wangdaye.geometricweather.Main")
                .putExtra(MainActivity.KEY_CITY, locationName);
        PendingIntent pendingIntentWeather = PendingIntent.getActivity(
                context, WEATHER_PENDING_INTENT_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_clock_day_week_weatherButton, pendingIntentWeather);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetClockDayWeekProvider.class),
                views);
    }
}