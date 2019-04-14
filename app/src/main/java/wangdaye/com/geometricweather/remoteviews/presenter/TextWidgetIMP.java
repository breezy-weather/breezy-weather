package wangdaye.com.geometricweather.remoteviews.presenter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetTextProvider;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Widget text utils.
 * */

public class TextWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void refreshWidgetView(Context context, Location location, @Nullable Weather weather) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_text_setting),
                Context.MODE_PRIVATE
        );
        boolean blackText = sharedPreferences.getBoolean(
                context.getString(R.string.key_black_text),
                false
        );

        RemoteViews views = getRemoteViews(context, location, weather, blackText);
        if (views != null) {
            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetTextProvider.class),
                    views
            );
        }
    }

    public static RemoteViews getRemoteViews(Context context, Location location, @Nullable Weather weather,
                                             boolean blackText) {
        SharedPreferences defaultSharePreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false
        );
        boolean touchToRefresh = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh),
                false
        );

        RemoteViews views = buildWidgetView(context, weather, fahrenheit, blackText);
        if (weather == null) {
            return views;
        }

        setOnClickPendingIntent(context, views, location, touchToRefresh);

        return views;
    }

    private static RemoteViews buildWidgetView(Context context, @Nullable Weather weather,
                                               boolean fahrenheit, boolean blackText) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_text);
        if (weather == null) {
            return views;
        }

        int textColor;
        if (blackText) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        views.setTextViewText(
                R.id.widget_text_weather,
                weather.realTime.weather
        );
        views.setTextViewText(
                R.id.widget_text_temperature,
                ValueUtils.buildAbbreviatedCurrentTemp(weather.realTime.temp, fahrenheit)
        );

        views.setTextColor(R.id.widget_text_date, textColor);
        views.setTextColor(R.id.widget_text_weather, textColor);
        views.setTextColor(R.id.widget_text_temperature, textColor);

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetTextProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                boolean touchToRefresh) {
        // container.
        if (touchToRefresh) {
            views.setOnClickPendingIntent(
                    R.id.widget_text_container,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_TEXT_PENDING_INTENT_CODE_REFRESH
                    )
            );
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_text_container,
                    getWeatherPendingIntent(
                            context,
                            location,
                            GeometricWeather.WIDGET_TEXT_PENDING_INTENT_CODE_WEATHER
                    )
            );
        }

        // date.
        views.setOnClickPendingIntent(
                R.id.widget_text_date,
                getCalendarPendingIntent(
                        context,
                        GeometricWeather.WIDGET_TEXT_PENDING_INTENT_CODE_CALENDAR
                )
        );
    }
}
