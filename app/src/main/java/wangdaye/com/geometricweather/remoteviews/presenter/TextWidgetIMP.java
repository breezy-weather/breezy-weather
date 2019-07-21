package wangdaye.com.geometricweather.remoteviews.presenter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.util.TypedValue;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetTextProvider;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;

public class TextWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location, @Nullable Weather weather) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_text_setting)
        );

        RemoteViews views = getRemoteViews(context, location, weather, config.textColor, config.textSize);

        if (views != null) {
            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetTextProvider.class),
                    views
            );
        }
    }

    public static RemoteViews getRemoteViews(Context context, Location location, @Nullable Weather weather,
                                             String textColor, int textSize) {
        SharedPreferences defaultSharePreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false
        );
        boolean touchToRefresh = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh),
                false
        );

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_text);
        if (weather == null) {
            return views;
        }

        boolean darkText = textColor.equals("dark")
                || (textColor.equals("auto") && isLightWallpaper(context));

        int textColorInt;
        if (darkText) {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        views.setTextViewText(
                R.id.widget_text_weather,
                weather.realTime.weather
        );
        views.setTextViewText(
                R.id.widget_text_temperature,
                ValueUtils.buildAbbreviatedCurrentTemp(weather.realTime.temp, fahrenheit)
        );

        views.setTextColor(R.id.widget_text_date, textColorInt);
        views.setTextColor(R.id.widget_text_weather, textColorInt);
        views.setTextColor(R.id.widget_text_temperature, textColorInt);

        if (textSize != 100) {
            float contentSize = context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size)
                    * textSize / 100f;
            float temperatureSize = DisplayUtils.spToPx(context, 48) * textSize / 100f;

            views.setTextViewTextSize(R.id.widget_text_date, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_text_weather, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_text_temperature, TypedValue.COMPLEX_UNIT_PX, temperatureSize);
        }

        setOnClickPendingIntent(context, views, location, touchToRefresh);

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
