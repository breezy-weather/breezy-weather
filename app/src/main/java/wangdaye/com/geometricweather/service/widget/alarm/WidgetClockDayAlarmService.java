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

import wangdaye.com.geometricweather.basic.GeoAlarmService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayProvider;
import wangdaye.com.geometricweather.view.activity.MainActivity;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget clock day service.
 * */

public class WidgetClockDayAlarmService extends GeoAlarmService {
    // data
    private static final int WEATHER_PENDING_INTENT_CODE = 15;
    private static final int CLOCK_PENDING_INTENT_CODE = 25;
    public static final int ALARM_CODE = 5;

    /** <br> life cycle. */

    public WidgetClockDayAlarmService() {
        super("WidgetClockDayAlarmService");
    }

    public WidgetClockDayAlarmService(String name) {
        super(name);
    }

    @Override
    public Location readSettings() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.sp_widget_clock_day_setting), Context.MODE_PRIVATE);
        String locationName = sharedPreferences.getString(
                getString(R.string.key_location), getString(R.string.local));
        return DatabaseHelper.getInstance(this).searchLocation(locationName);
    }

    @Override
    protected void doRefresh(Location location) {
        int[] widgetIds = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, WidgetClockDayProvider.class));
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
                context.getString(R.string.sp_widget_clock_day_setting),
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
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day);

        // build view.
        int[] imageId = WeatherHelper.getWeatherIcon(weather.live.weatherKind, isDay);
        views.setImageViewResource( // set icon.
                R.id.widget_clock_day_icon,
                imageId[3]);
        // build date text.
        String[] solar = weather.base.date.split("-");
        String dateText = solar[1] + "-" + solar[2] + " " + weather.base.week + " / " + weather.base.moon;
        views.setTextViewText( // set date.
                R.id.widget_clock_day_date,
                dateText);
        // build weather text.
        String weatherText = weather.base.location + " / " + weather.live.weather + " " + weather.live.temp + "℃";
        views.setTextViewText( // set weather text.
                R.id.widget_clock_day_weather,
                weatherText);
        // set text color.
        views.setTextColor(R.id.widget_clock_day_clock, textColor);
        views.setTextColor(R.id.widget_clock_day_date, textColor);
        views.setTextColor(R.id.widget_clock_day_weather, textColor);
        // set card visibility.
        views.setViewVisibility(R.id.widget_clock_day_card, showCard ? View.VISIBLE : View.GONE);
        // set clock intent.
        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(
                context, CLOCK_PENDING_INTENT_CODE, intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_clock_day_clockButton, pendingIntentClock);
        // set weather intent.
        Intent intent = new Intent("com.wangdaye.geometricweather.Main")
                .putExtra(MainActivity.KEY_CITY, locationName);
        PendingIntent pendingIntentWeather = PendingIntent.getActivity(
                context, WEATHER_PENDING_INTENT_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_clock_day_weatherButton, pendingIntentWeather);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetClockDayProvider.class),
                views);
    }
}
