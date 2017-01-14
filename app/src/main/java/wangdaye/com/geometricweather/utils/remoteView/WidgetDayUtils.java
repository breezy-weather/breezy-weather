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

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetDayProvider;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget day utils.
 * */

public class WidgetDayUtils {
    // data
    private static final int PENDING_INTENT_CODE = 111;

    /** <br> UI. */

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        if (weather == null) {
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_day_setting),
                Context.MODE_PRIVATE);
        String viewStyle = sharedPreferences.getString(context.getString(R.string.key_view_type), "rectangle");
        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);
        boolean hideRefreshTime = sharedPreferences.getBoolean(context.getString(R.string.key_hide_refresh_time), false);
        boolean dayTime = TimeUtils.getInstance(context).getDayTime(context, weather, false).isDayTime();

        boolean fahrenheit = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.key_fahrenheit), false);

        RemoteViews views = buildWidgetView(
                context, weather, dayTime, fahrenheit, viewStyle, showCard, blackText, hideRefreshTime);
        views.setViewVisibility(R.id.widget_day_card, showCard ? View.VISIBLE : View.GONE);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                PENDING_INTENT_CODE,
                IntentHelper.buildMainActivityIntent(context, location),
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_day_button, pendingIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetDayProvider.class),
                views);
    }

    private static RemoteViews buildWidgetView(Context context, Weather weather,
                                               boolean dayTime, boolean fahrenheit,
                                               String viewStyle, boolean showCard, boolean blackText, boolean hideRefreshTime) {
        int[] imageId = WeatherHelper.getWeatherIcon(weather.realTime.weatherKind, dayTime);
        int textColor;
        if (viewStyle.equals("pixel")) {
            if (blackText) {
                textColor = ContextCompat.getColor(context, R.color.colorTextDark);
            } else {
                textColor = ContextCompat.getColor(context, R.color.colorTextLight);
            }
        } else {
            if (blackText || showCard) {
                textColor = ContextCompat.getColor(context, R.color.colorTextDark);
            } else {
                textColor = ContextCompat.getColor(context, R.color.colorTextLight);
            }
        }
        RemoteViews views;
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_rectangle);

                String[] texts = WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit);

                views.setImageViewResource(R.id.widget_day_icon, imageId[3]);
                views.setTextViewText(R.id.widget_day_title, texts[0]);
                views.setTextViewText(R.id.widget_day_subtitle, texts[1]);
                views.setTextViewText(
                        R.id.widget_day_time,
                        weather.base.city + " " + weather.base.time);

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_subtitle, textColor);
                views.setTextColor(R.id.widget_day_time, textColor);
                views.setViewVisibility(R.id.widget_day_time, hideRefreshTime ? View.GONE : View.VISIBLE);
                return views;

            case "symmetry":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_symmetry);

                views.setImageViewResource(R.id.widget_day_icon, imageId[3]);
                views.setTextViewText(
                        R.id.widget_day_title,
                        weather.base.city + "\n" + ValueUtils.buildCurrentTemp(weather.realTime.temp, true, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_subtitle,
                        weather.realTime.weather + "\n" + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_time,
                        weather.dailyList.get(0).week + " " + weather.base.time);

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_subtitle, textColor);
                views.setTextColor(R.id.widget_day_time, textColor);
                views.setViewVisibility(R.id.widget_day_time, hideRefreshTime ? View.GONE : View.VISIBLE);
                return views;

            case "tile":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_tile);

                views.setImageViewResource(R.id.widget_day_icon, imageId[3]);
                views.setTextViewText(
                        R.id.widget_day_title,
                        weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_subtitle,
                        ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_time,
                        weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_subtitle, textColor);
                views.setTextColor(R.id.widget_day_time, textColor);
                views.setViewVisibility(R.id.widget_day_time, hideRefreshTime ? View.GONE : View.VISIBLE);
                return views;

            case "mini":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_mini);

                views.setImageViewResource(R.id.widget_day_icon, imageId[3]);
                views.setTextViewText(
                        R.id.widget_day_title,
                        weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_time,
                        weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_time, textColor);
                views.setViewVisibility(R.id.widget_day_time, hideRefreshTime ? View.GONE : View.VISIBLE);
                return views;

            case "pixel":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_pixel);

                views.setImageViewResource(R.id.widget_day_icon, imageId[3]);
                views.setTextViewText(
                        R.id.widget_day_title,
                        ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_subtitle,
                        weather.dailyList.get(0).date.split("-", 2)[1] + " " + weather.dailyList.get(0).week);

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_subtitle, textColor);
                return views;

        }
        return new RemoteViews(context.getPackageName(), R.layout.widget_day_symmetry);
    }

    /** <br> data. */

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetDayProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }
}
