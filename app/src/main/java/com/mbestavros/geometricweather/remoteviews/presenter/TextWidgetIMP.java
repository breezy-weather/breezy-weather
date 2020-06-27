package com.mbestavros.geometricweather.remoteviews.presenter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import androidx.core.content.ContextCompat;

import android.util.TypedValue;
import android.widget.RemoteViews;

import com.mbestavros.geometricweather.GeometricWeather;
import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.basic.model.location.Location;
import com.mbestavros.geometricweather.background.receiver.widget.WidgetTextProvider;
import com.mbestavros.geometricweather.basic.model.option.unit.TemperatureUnit;
import com.mbestavros.geometricweather.basic.model.weather.Weather;
import com.mbestavros.geometricweather.settings.SettingsOptionManager;
import com.mbestavros.geometricweather.utils.DisplayUtils;

public class TextWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_text_setting)
        );

        RemoteViews views = getRemoteViews(context, location, config.textColor, config.textSize);

        if (views != null) {
            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetTextProvider.class),
                    views
            );
        }
    }

    public static RemoteViews getRemoteViews(Context context, Location location,
                                             String textColor, int textSize) {
        SettingsOptionManager settings = SettingsOptionManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        boolean touchToRefresh = settings.isWidgetClickToRefreshEnabled();

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_text);
        Weather weather = location.getWeather();
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
                weather.getCurrent().getWeatherText()
        );
        views.setTextViewText(
                R.id.widget_text_temperature,
                weather.getCurrent().getTemperature().getShortTemperature(context, temperatureUnit)
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
        // headerContainer.
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
