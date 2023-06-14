package org.breezyweather.remoteviews.presenters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import org.breezyweather.BreezyWeather;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.R;
import org.breezyweather.background.receiver.widget.WidgetTextProvider;
import org.breezyweather.common.utils.DisplayUtils;
import org.breezyweather.settings.SettingsManager;

public class TextWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_text_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location, config.textColor, config.textSize, config.alignEnd);

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetTextProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context, Location location,
                                             String textColor, int textSize, boolean alignEnd) {
        SettingsManager settings = SettingsManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();

        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                alignEnd ? R.layout.widget_text_end : R.layout.widget_text
        );
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

        if (weather.getCurrent() != null) {
            if (weather.getCurrent().getWeatherText() != null) {
                views.setTextViewText(
                        R.id.widget_text_weather,
                        weather.getCurrent().getWeatherText()
                );
            }
            if (weather.getCurrent().getTemperature() != null
                && weather.getCurrent().getTemperature().getTemperature() != null) {
                views.setTextViewText(
                        R.id.widget_text_temperature,
                        weather.getCurrent().getTemperature().getShortTemperature(context, temperatureUnit)
                );
            }
        }

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

        setOnClickPendingIntent(context, views, location);

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetTextProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location) {
        // headerContainer.
        views.setOnClickPendingIntent(
                R.id.widget_text_container,
                getWeatherPendingIntent(
                        context,
                        location,
                        BreezyWeather.WIDGET_TEXT_PENDING_INTENT_CODE_WEATHER
                )
        );

        // date.
        views.setOnClickPendingIntent(
                R.id.widget_text_date,
                getCalendarPendingIntent(
                        context,
                        BreezyWeather.WIDGET_TEXT_PENDING_INTENT_CODE_CALENDAR
                )
        );
    }
}
