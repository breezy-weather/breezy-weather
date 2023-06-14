package org.breezyweather.remoteviews.presenters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Date;

import org.breezyweather.BreezyWeather;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.RelativeHumidityUnit;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Temperature;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.ResourcesProviderFactory;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.R;
import org.breezyweather.background.receiver.widget.WidgetClockDayDetailsProvider;
import org.breezyweather.common.utils.helpers.LunarHelper;
import org.breezyweather.remoteviews.WidgetHelper;
import org.breezyweather.settings.SettingsManager;

public class ClockDayDetailsWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_clock_day_details_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location,
                config.cardStyle, config.cardAlpha, config.textColor, config.textSize, config.clockFont,
                config.hideLunar
        );

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetClockDayDetailsProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context,
                                             Location location,
                                             String cardStyle, int cardAlpha,
                                             String textColor, int textSize, String clockFont,
                                             boolean hideLunar) {

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        boolean dayTime = location.isDaylight();

        SettingsManager settings = SettingsManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        boolean minimalIcon = settings.isWidgetMinimalIconEnabled();

        WidgetColor color = new WidgetColor(context, cardStyle, textColor);

        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                !color.showCard
                        ? R.layout.widget_clock_day_details
                        : R.layout.widget_clock_day_details_card
        );
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        if (weather.getCurrent() != null && weather.getCurrent().getWeatherCode() != null) {
            views.setImageViewUri(
                    R.id.widget_clock_day_icon,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            weather.getCurrent().getWeatherCode(),
                            dayTime,
                            minimalIcon,
                            color.getMinimalIconColor()
                    )
            );
        }

        views.setTextViewText(
                R.id.widget_clock_day_lunar,
                settings.getLanguage().isChinese() && !hideLunar
                        ? (" - " + LunarHelper.getLunarDate(new Date()))
                        : ""
        );


        StringBuilder builder = new StringBuilder();
        builder.append(location.getCityName(context));
        if (weather.getCurrent() != null
                && weather.getCurrent().getTemperature() != null
                && weather.getCurrent().getTemperature().getTemperature() != null) {
            builder.append(" ").append(weather.getCurrent().getTemperature().getTemperature(context, temperatureUnit));
        }

        views.setTextViewText(
                R.id.widget_clock_day_subtitle,
                builder.toString()
        );

        if (weather.getDailyForecast().size() > 0
                && weather.getDailyForecast().get(0).getDay() != null
                && weather.getDailyForecast().get(0).getNight() != null
                && weather.getDailyForecast().get(0).getDay().getTemperature() != null
                && weather.getDailyForecast().get(0).getNight().getTemperature() != null
                && weather.getDailyForecast().get(0).getDay().getTemperature().getTemperature() != null
                && weather.getDailyForecast().get(0).getNight().getTemperature().getTemperature() != null) {
            views.setTextViewText(
                    R.id.widget_clock_day_todayTemp,
                    context.getString(R.string.today) + " " + Temperature.getTrendTemperature(
                            context,
                            weather.getDailyForecast().get(0).getNight().getTemperature().getTemperature(),
                            weather.getDailyForecast().get(0).getDay().getTemperature().getTemperature(),
                            temperatureUnit
                    )
            );
        }

        if (weather.getCurrent() != null && weather.getCurrent().getTemperature() != null
                && weather.getCurrent().getTemperature().getFeelsLikeTemperature() != null) {
            views.setTextViewText(
                    R.id.widget_clock_day_sensibleTemp,
                    context.getString(R.string.feels_like)
                            + " "
                            + weather.getCurrent().getTemperature().getFeelsLikeTemperature(context, temperatureUnit)
            );
        }

        views.setTextViewText(
                R.id.widget_clock_day_aqiHumidity,
                getAQIHumidityTempText(context, weather)
        );

        if (weather.getCurrent() != null && weather.getCurrent().getWind() != null
            && !TextUtils.isEmpty(weather.getCurrent().getWind().getShortWindDescription())) {
            views.setTextViewText(
                    R.id.widget_clock_day_wind,
                    weather.getCurrent().getWind().getShortWindDescription()
            );
        }

        if (color.textColor != Color.TRANSPARENT) {
            views.setTextColor(R.id.widget_clock_day_clock_light, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_normal, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_black, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_aa_light, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_aa_normal, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_aa_black, color.textColor);
            views.setTextColor(R.id.widget_clock_day_title, color.textColor);
            views.setTextColor(R.id.widget_clock_day_lunar, color.textColor);
            views.setTextColor(R.id.widget_clock_day_subtitle, color.textColor);
            views.setTextColor(R.id.widget_clock_day_todayTemp, color.textColor);
            views.setTextColor(R.id.widget_clock_day_sensibleTemp, color.textColor);
            views.setTextColor(R.id.widget_clock_day_aqiHumidity, color.textColor);
            views.setTextColor(R.id.widget_clock_day_wind, color.textColor);
        }

        if (textSize != 100) {
            float clockSize = context.getResources().getDimensionPixelSize(R.dimen.widget_current_weather_icon_size)
                    * textSize / 100f;
            float clockAASize = context.getResources().getDimensionPixelSize(R.dimen.widget_aa_text_size)
                    * textSize / 100f;
            float contentSize = context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size)
                    * textSize / 100f;

            views.setTextViewTextSize(R.id.widget_clock_day_clock_light, TypedValue.COMPLEX_UNIT_PX, clockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_normal, TypedValue.COMPLEX_UNIT_PX, clockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_black, TypedValue.COMPLEX_UNIT_PX, clockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_aa_light, TypedValue.COMPLEX_UNIT_PX, clockAASize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_aa_normal, TypedValue.COMPLEX_UNIT_PX, clockAASize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_aa_black, TypedValue.COMPLEX_UNIT_PX, clockAASize);
            views.setTextViewTextSize(R.id.widget_clock_day_title, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_clock_day_lunar, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_clock_day_subtitle, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_clock_day_todayTemp, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_clock_day_sensibleTemp, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_clock_day_aqiHumidity, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_clock_day_wind, TypedValue.COMPLEX_UNIT_PX, contentSize);
        }

        if (color.showCard) {
            views.setImageViewResource(
                    R.id.widget_clock_day_card,
                    getCardBackgroundId(color.cardColor)
            );
            views.setInt(
                    R.id.widget_clock_day_card,
                    "setImageAlpha",
                    (int) (cardAlpha / 100.0 * 255)
            );
        }

        if (clockFont == null) {
            clockFont = "light";
        }
        switch (clockFont) {
            case "light":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                break;

            case "normal":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                break;

            case "black":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.VISIBLE);
                break;
        }

        setOnClickPendingIntent(context, views, location);

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(
                        new ComponentName(context, WidgetClockDayDetailsProvider.class)
                );
        return widgetIds != null && widgetIds.length > 0;
    }

    private static String getAQIHumidityTempText(Context context, Weather weather) {
        if (weather.getCurrent() != null
                && weather.getCurrent().getAirQuality() != null
                && weather.getCurrent().getAirQuality().getIndex(null) != null
                && weather.getCurrent().getAirQuality().getName(context, null) != null) {
            return "AQI "
                    + weather.getCurrent().getAirQuality().getIndex(null)
                    + " ("
                    + weather.getCurrent().getAirQuality().getName(context, null)
                    + ")";
        } else if (weather.getCurrent() != null && weather.getCurrent().getRelativeHumidity() != null) {
            return context.getString(R.string.humidity)
                    + " "
                    + RelativeHumidityUnit.PERCENT.getValueText(
                            context,
                            (int) WidgetHelper.getNonNullValue(
                                    weather.getCurrent().getRelativeHumidity(), 0
                            )
                    );
        } else {
            return "";
        }
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location) {
        // weather.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_weather,
                getWeatherPendingIntent(
                        context,
                        location,
                        BreezyWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_WEATHER
                )
        );

        // clock.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_light,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_normal,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_black,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_BLACK
                )
        );

        // title.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_title,
                getCalendarPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CALENDAR
                )
        );
    }
}
