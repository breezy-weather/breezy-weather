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

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetDayProvider;
import wangdaye.com.geometricweather.service.NormalUpdateService;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget day utils.
 * */

public class WidgetDayUtils {

    private static final int PENDING_INTENT_CODE = 111;

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

        RemoteViews views = buildWidgetView(
                context, weather,
                dayTime, fahrenheit, iconStyle,
                viewStyle, showCard, blackText,
                hideSubtitle, subtitleData);
        views.setViewVisibility(R.id.widget_day_card, showCard ? View.VISIBLE : View.GONE);

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
        views.setOnClickPendingIntent(R.id.widget_day_button, pendingIntent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetDayProvider.class),
                views);
    }

    private static RemoteViews buildWidgetView(Context context, Weather weather,
                                               boolean dayTime, boolean fahrenheit, String iconStyle,
                                               String viewStyle, boolean showCard, boolean blackText,
                                               boolean hideSubtitle, String subtitleData) {
        int imageId = WeatherHelper.getWidgetNotificationIcon(
                weather.realTime.weatherKind, dayTime, iconStyle, blackText);
        int textColor;
        if (viewStyle.equals("pixel") || viewStyle.equals("nano")) {
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
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_day_symmetry);
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_rectangle);

                String[] texts = WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit);

                views.setImageViewResource(R.id.widget_day_icon, imageId);
                views.setTextViewText(R.id.widget_day_title, texts[0]);
                views.setTextViewText(R.id.widget_day_subtitle, texts[1]);

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_subtitle, textColor);
                views.setTextColor(R.id.widget_day_time, textColor);
                views.setViewVisibility(R.id.widget_day_time, hideSubtitle ? View.GONE : View.VISIBLE);
                break;

            case "symmetry":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_symmetry);

                views.setImageViewResource(R.id.widget_day_icon, imageId);
                views.setTextViewText(
                        R.id.widget_day_title,
                        weather.base.city + "\n" + ValueUtils.buildCurrentTemp(weather.realTime.temp, true, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_subtitle,
                        weather.realTime.weather + "\n" + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit));

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_subtitle, textColor);
                views.setTextColor(R.id.widget_day_time, textColor);
                views.setViewVisibility(R.id.widget_day_time, hideSubtitle ? View.GONE : View.VISIBLE);
                break;

            case "tile":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_tile);

                views.setImageViewResource(R.id.widget_day_icon, imageId);
                views.setTextViewText(
                        R.id.widget_day_title,
                        weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_subtitle,
                        ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit));

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_subtitle, textColor);
                views.setTextColor(R.id.widget_day_time, textColor);
                views.setViewVisibility(R.id.widget_day_time, hideSubtitle ? View.GONE : View.VISIBLE);
                break;

            case "mini":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_mini);

                views.setImageViewResource(R.id.widget_day_icon, imageId);
                views.setTextViewText(
                        R.id.widget_day_title,
                        weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit));

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_time, textColor);
                views.setViewVisibility(R.id.widget_day_time, hideSubtitle ? View.GONE : View.VISIBLE);
                break;

            case "nano":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_nano);

                views.setImageViewResource(R.id.widget_day_icon, imageId);
                views.setTextViewText(
                        R.id.widget_day_title,
                        ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit));

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setViewVisibility(R.id.widget_day_time, hideSubtitle ? View.GONE : View.VISIBLE);
                break;

            case "pixel":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_pixel);

                views.setImageViewResource(R.id.widget_day_icon, imageId);
                views.setTextViewText(
                        R.id.widget_day_title,
                        ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit));
                views.setTextViewText(
                        R.id.widget_day_subtitle,
                        weather.dailyList.get(0).date.split("-", 2)[1] + " " + weather.dailyList.get(0).week);

                views.setTextColor(R.id.widget_day_title, textColor);
                views.setTextColor(R.id.widget_day_subtitle, textColor);
                views.setViewVisibility(R.id.widget_day_subtitle, hideSubtitle ? View.GONE : View.VISIBLE);
                break;
        }
        switch (subtitleData) {
            case "time":
                switch (viewStyle) {
                    case "rectangle":
                        views.setTextViewText(
                                R.id.widget_day_time,
                                weather.base.city + " " + weather.base.time);
                        break;

                    case "symmetry":
                        views.setTextViewText(
                                R.id.widget_day_time,
                                weather.dailyList.get(0).week + " " + weather.base.time);
                        break;

                    case "tile":
                        views.setTextViewText(
                                R.id.widget_day_time,
                                weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);
                        break;

                    case "mini":
                        views.setTextViewText(
                                R.id.widget_day_time,
                                weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);
                        break;
                }
                break;

            case "aqi":
                if (weather.aqi != null) {
                    views.setTextViewText(
                            R.id.widget_day_time,
                            weather.aqi.quality + " (" + weather.aqi.aqi + ")");
                }
                break;

            case "wind":
                views.setTextViewText(
                        R.id.widget_day_time,
                        weather.realTime.windLevel + " (" + weather.realTime.windDir + weather.realTime.windSpeed + ")");
                break;

            default:
                views.setTextViewText(
                        R.id.widget_day_time,
                        context.getString(R.string.feels_like) + " "
                                + ValueUtils.buildAbbreviatedCurrentTemp(
                                weather.realTime.sensibleTemp, GeometricWeather.getInstance().isFahrenheit()));
                break;
        }
        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetDayProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }
}
