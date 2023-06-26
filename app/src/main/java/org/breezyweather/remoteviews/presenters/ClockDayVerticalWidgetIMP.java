package org.breezyweather.remoteviews.presenters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import java.util.Date;

import org.breezyweather.BreezyWeather;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit;
import org.breezyweather.common.basic.models.weather.Temperature;
import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.theme.resource.ResourceHelper;
import org.breezyweather.theme.resource.ResourcesProviderFactory;
import org.breezyweather.theme.resource.providers.ResourceProvider;
import org.breezyweather.R;
import org.breezyweather.background.receiver.widget.WidgetClockDayVerticalProvider;
import org.breezyweather.common.utils.DisplayUtils;
import org.breezyweather.common.utils.helpers.LunarHelper;
import org.breezyweather.remoteviews.WidgetHelper;
import org.breezyweather.settings.SettingsManager;

public class ClockDayVerticalWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_clock_day_vertical_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location,
                config.viewStyle, config.cardStyle, config.cardAlpha, config.textColor, config.textSize,
                config.hideSubtitle, config.subtitleData, config.clockFont
        );

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetClockDayVerticalProvider.class),
                views
        );
    }


    public static RemoteViews getRemoteViews(Context context, Location location,
                                             String viewStyle, String cardStyle, int cardAlpha,
                                             String textColor, int textSize,
                                             boolean hideSubtitle, String subtitleData, String clockFont) {
        boolean dayTime = location.isDaylight();

        SettingsManager settings = SettingsManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        boolean minimalIcon = settings.isWidgetUsingMonochromeIcons();

        WidgetColor color = new WidgetColor(context, cardStyle, textColor);

        RemoteViews views = buildWidgetViewDayPart(
                context, location,
                temperatureUnit,
                color,
                dayTime, textSize,
                minimalIcon,
                clockFont, viewStyle,
                hideSubtitle, subtitleData
        );

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

        setOnClickPendingIntent(context, views, location, subtitleData);

        return views;
    }

    private static RemoteViews buildWidgetViewDayPart(Context context, Location location,
                                                      TemperatureUnit temperatureUnit,
                                                      WidgetColor color,
                                                      boolean dayTime, int textSize,
                                                      boolean minimalIcon,
                                                      String clockFont, String viewStyle,
                                                      boolean hideSubtitle, String subtitleData) {
        Weather weather = location.getWeather();
        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                !color.showCard
                        ? R.layout.widget_clock_day_symmetry
                        : R.layout.widget_clock_day_symmetry_card
        );
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_clock_day_rectangle
                                : R.layout.widget_clock_day_rectangle_card
                );
                break;

            case "symmetry":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_clock_day_symmetry
                                : R.layout.widget_clock_day_symmetry_card
                );
                break;

            case "tile":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_clock_day_tile
                                : R.layout.widget_clock_day_tile_card
                );
                break;

            case "mini":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_clock_day_mini
                                : R.layout.widget_clock_day_mini_card
                );
                break;

            case "vertical":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_clock_day_vertical
                                : R.layout.widget_clock_day_vertical_card
                );
                break;

            case "temp":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_clock_day_temp
                                : R.layout.widget_clock_day_temp_card
                );
                break;
        }
        if (weather == null) {
            return views;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

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
                R.id.widget_clock_day_title,
                getTitleText(context, location, viewStyle, temperatureUnit)
        );
        views.setTextViewText(
                R.id.widget_clock_day_subtitle,
                getSubtitleText(context, weather, viewStyle, temperatureUnit)
        );
        views.setTextViewText(
                R.id.widget_clock_day_time,
                getTimeText(context, location, viewStyle, subtitleData, temperatureUnit)
        );

        if (color.textColor != Color.TRANSPARENT) {
            views.setTextColor(R.id.widget_clock_day_clock_light, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_normal, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_black, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_aa_light, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_aa_normal, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_aa_black, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_1_light, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_1_normal, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_1_black, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_2_light, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_2_normal, color.textColor);
            views.setTextColor(R.id.widget_clock_day_clock_2_black, color.textColor);
            views.setTextColor(R.id.widget_clock_day_date, color.textColor);
            views.setTextColor(R.id.widget_clock_day_title, color.textColor);
            views.setTextColor(R.id.widget_clock_day_subtitle, color.textColor);
            views.setTextColor(R.id.widget_clock_day_time, color.textColor);
        }

        if (textSize != 100) {
            float clockSize = context.getResources().getDimensionPixelSize(R.dimen.widget_current_weather_icon_size)
                    * textSize / 100f;
            float clockAASize = context.getResources().getDimensionPixelSize(R.dimen.widget_aa_text_size)
                    * textSize / 100f;
            float verticalClockSize = DisplayUtils.spToPx(context, 64) * textSize / 100f;

            views.setTextViewTextSize(R.id.widget_clock_day_clock_light, TypedValue.COMPLEX_UNIT_PX, clockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_normal, TypedValue.COMPLEX_UNIT_PX, clockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_black, TypedValue.COMPLEX_UNIT_PX, clockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_aa_light, TypedValue.COMPLEX_UNIT_PX, clockAASize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_aa_normal, TypedValue.COMPLEX_UNIT_PX, clockAASize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_aa_black, TypedValue.COMPLEX_UNIT_PX, clockAASize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_1_light, TypedValue.COMPLEX_UNIT_PX, verticalClockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_1_normal, TypedValue.COMPLEX_UNIT_PX, verticalClockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_1_black, TypedValue.COMPLEX_UNIT_PX, verticalClockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_2_light, TypedValue.COMPLEX_UNIT_PX, verticalClockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_2_normal, TypedValue.COMPLEX_UNIT_PX, verticalClockSize);
            views.setTextViewTextSize(R.id.widget_clock_day_clock_2_black, TypedValue.COMPLEX_UNIT_PX, verticalClockSize);

            views.setTextViewTextSize(R.id.widget_clock_day_date, TypedValue.COMPLEX_UNIT_PX,
                    getTitleSize(context, viewStyle) * textSize / 100f);
            views.setTextViewTextSize(R.id.widget_clock_day_title, TypedValue.COMPLEX_UNIT_PX,
                    getTitleSize(context, viewStyle) * textSize / 100f);
            views.setTextViewTextSize(R.id.widget_clock_day_subtitle, TypedValue.COMPLEX_UNIT_PX,
                    getSubtitleSize(context, viewStyle) * textSize / 100f);
            views.setTextViewTextSize(R.id.widget_clock_day_time, TypedValue.COMPLEX_UNIT_PX,
                    getTimeSize(context, viewStyle) * textSize / 100f);
        }

        views.setViewVisibility(R.id.widget_clock_day_time, hideSubtitle ? View.GONE : View.VISIBLE);

        if (clockFont == null) {
            clockFont = "light";
        }
        switch (clockFont) {
            case "light":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_auto, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_light, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_dark, View.GONE);
                break;

            case "normal":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_auto, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_light, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_dark, View.GONE);
                break;

            case "black":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.VISIBLE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_auto, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_light, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_analogContainer_dark, View.GONE);
                break;

            case "analog":
                views.setViewVisibility(R.id.widget_clock_day_clock_lightContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_normalContainer, View.GONE);
                views.setViewVisibility(R.id.widget_clock_day_clock_blackContainer, View.GONE);
                views.setViewVisibility(
                        R.id.widget_clock_day_clock_analogContainer_auto,
                        color.showCard && color.cardColor == WidgetColor.ColorType.AUTO
                                ? View.VISIBLE
                                : View.GONE
                );
                views.setViewVisibility(
                        R.id.widget_clock_day_clock_analogContainer_light,
                        color.showCard && color.cardColor == WidgetColor.ColorType.AUTO
                                ? View.GONE
                                : (color.darkText ? View.GONE : View.VISIBLE)
                );
                views.setViewVisibility(
                        R.id.widget_clock_day_clock_analogContainer_dark,
                        color.showCard && color.cardColor == WidgetColor.ColorType.AUTO
                                ? View.GONE
                                : (color.darkText ? View.VISIBLE : View.GONE)
                );
                break;
        }

        return views;
    }

    public static boolean isInUse(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(
                        new ComponentName(context, WidgetClockDayVerticalProvider.class)
                );
        return widgetIds != null && widgetIds.length > 0;
    }

    @Nullable
    private static String getTitleText(Context context, Location location,
                                       String viewStyle, TemperatureUnit unit) {
        Weather weather = location.getWeather();
        if (weather == null) {
            return null;
        }
        switch (viewStyle) {
            case "rectangle":
                return WidgetHelper.buildWidgetDayStyleText(context, weather, unit)[0];

            case "symmetry":
                if (weather.getCurrent() != null
                        && weather.getCurrent().getTemperature() != null
                        && weather.getCurrent().getTemperature().getTemperature() != null) {
                    return location.getCityName(context)
                            + "\n"
                            + weather.getCurrent().getTemperature().getTemperature(context, unit);
                } else {
                    return location.getCityName(context);
                }

            case "vertical":
            case "tile":
                if (weather.getCurrent() != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (!TextUtils.isEmpty(weather.getCurrent().getWeatherText())) {
                        stringBuilder.append(weather.getCurrent().getWeatherText());
                    }
                    if (weather.getCurrent().getTemperature() != null
                        && weather.getCurrent().getTemperature().getTemperature() != null) {
                        if (!TextUtils.isEmpty(weather.getCurrent().getWeatherText())) {
                            stringBuilder.append(" ");
                        }
                        stringBuilder.append(weather.getCurrent().getTemperature().getTemperature(context, unit));
                    }
                    return stringBuilder.toString();
                }

            case "mini":
                if (weather.getCurrent() != null && !TextUtils.isEmpty(weather.getCurrent().getWeatherText())) {
                    return weather.getCurrent().getWeatherText();
                }

            case "temp":
                if (weather.getCurrent() != null
                        && weather.getCurrent().getTemperature() != null
                        && weather.getCurrent().getTemperature().getTemperature() != null) {
                    return weather.getCurrent().getTemperature().getShortTemperature(context, unit);
                }
        }
        return "";
    }

    private static String getSubtitleText(Context context, Weather weather, String viewStyle,
                                          TemperatureUnit unit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetHelper.buildWidgetDayStyleText(context, weather, unit)[1];

            case "symmetry":
                if (weather.getCurrent() != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (!TextUtils.isEmpty(weather.getCurrent().getWeatherText())) {
                        stringBuilder.append(weather.getCurrent().getWeatherText());
                    }
                    if (weather.getDailyForecast().size() > 0
                            && weather.getDailyForecast().get(0).getDay() != null
                            && weather.getDailyForecast().get(0).getDay().getTemperature() != null
                            && weather.getDailyForecast().get(0).getDay().getTemperature().getTemperature() != null
                            && weather.getDailyForecast().get(0).getNight() != null
                            && weather.getDailyForecast().get(0).getNight().getTemperature() != null
                            && weather.getDailyForecast().get(0).getNight().getTemperature().getTemperature() != null
                    ) {
                        if (!TextUtils.isEmpty(weather.getCurrent().getWeatherText())) {
                            stringBuilder.append(" ");
                        }
                        stringBuilder.append(Temperature.getTrendTemperature(
                                        context,
                                        weather.getDailyForecast().get(0).getNight().getTemperature().getTemperature(),
                                        weather.getDailyForecast().get(0).getDay().getTemperature().getTemperature(),
                                        unit
                                )
                        );
                    }
                    return stringBuilder.toString();
                }

            case "tile":
            case "temp":
                if (weather.getDailyForecast().size() > 0
                        && weather.getDailyForecast().get(0).getDay() != null
                        && weather.getDailyForecast().get(0).getDay().getTemperature() != null
                        && weather.getDailyForecast().get(0).getDay().getTemperature().getTemperature() != null
                        && weather.getDailyForecast().get(0).getNight() != null
                        && weather.getDailyForecast().get(0).getNight().getTemperature() != null
                        && weather.getDailyForecast().get(0).getNight().getTemperature().getTemperature() != null
                ) {
                    return Temperature.getTrendTemperature(
                            context,
                            weather.getDailyForecast().get(0).getNight().getTemperature().getTemperature(),
                            weather.getDailyForecast().get(0).getDay().getTemperature().getTemperature(),
                            unit
                    );
                }

            case "mini":
                if (weather.getCurrent() != null
                        && weather.getCurrent().getTemperature() != null
                        && weather.getCurrent().getTemperature().getTemperature() != null) {
                    return weather.getCurrent().getTemperature().getTemperature(context, unit);
                }
        }
        return "";
    }

    @Nullable
    private static String getTimeText(Context context, Location location,
                                      String viewStyle, String subtitleData, TemperatureUnit unit) {
        Weather weather = location.getWeather();
        if (weather == null) {
            return null;
        }
        switch (subtitleData) {
            case "time":
                switch (viewStyle) {
                    case "rectangle":
                        return location.getCityName(context)
                                + " "
                                + DisplayUtils.getTime(context, weather.getBase().getUpdateDate(), location.getTimeZone());

                    case "symmetry":
                        return WidgetHelper.getWeek(context, location.getTimeZone())
                                + " "
                                + DisplayUtils.getTime(context, weather.getBase().getUpdateDate(), location.getTimeZone());

                    case "tile":
                    case "vertical":
                        return location.getCityName(context)
                                + " " + WidgetHelper.getWeek(context, location.getTimeZone())
                                + " " + DisplayUtils.getTime(context, weather.getBase().getUpdateDate(), location.getTimeZone());
                }
                return null;

            case "aqi":
                if (weather.getCurrent() != null
                        && weather.getCurrent().getAirQuality() != null
                        && weather.getCurrent().getAirQuality().getIndex(null) != null
                        && weather.getCurrent().getAirQuality().getName(context, null) != null) {
                    return weather.getCurrent().getAirQuality().getName(context, null)
                            + " ("
                            + weather.getCurrent().getAirQuality().getIndex(null)
                            + ")";
                }
                return null;

            case "wind":
                if (weather.getCurrent() != null
                        && weather.getCurrent().getWind() != null
                        && weather.getCurrent().getWind().getDirection() != null
                        && weather.getCurrent().getWind().getLevel() != null) {
                    return weather.getCurrent().getWind().getDirection()
                            + " "
                            + weather.getCurrent().getWind().getLevel();
                }
                return null;

            case "lunar":
                switch (viewStyle) {
                    case "rectangle":
                        return location.getCityName(context)
                                + " "
                                + LunarHelper.getLunarDate(new Date());

                    case "symmetry":
                        return WidgetHelper.getWeek(context, location.getTimeZone())
                                + " "
                                + LunarHelper.getLunarDate(new Date());

                    case "tile":
                    case "vertical":
                        return location.getCityName(context)
                                + " " + WidgetHelper.getWeek(context, location.getTimeZone())
                                + " " + LunarHelper.getLunarDate(new Date());
                }
                return null;

            case "feels_like":
                if (weather.getCurrent() != null
                        && weather.getCurrent().getTemperature() != null
                        && weather.getCurrent().getTemperature().getFeelsLikeTemperature() != null) {
                    return context.getString(R.string.temperature_feels_like)
                            + " "
                            + weather.getCurrent().getTemperature().getFeelsLikeTemperature(context, unit);
                }
                return null;
        }
        return getCustomSubtitle(context, subtitleData, location, weather);
    }

    private static float getTitleSize(Context context, String viewStyle) {
        switch (viewStyle) {
            case "rectangle":
            case "symmetry":
            case "tile":
            case "mini":
            case "vertical":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size);

            case "temp":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_title_text_size);
        }
        return 0;
    }

    private static float getSubtitleSize(Context context, String viewStyle) {
        switch (viewStyle) {
            case "rectangle":
            case "symmetry":
            case "tile":
            case "mini":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size);

            case "temp":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_subtitle_text_size);
        }
        return 0;
    }

    private static float getTimeSize(Context context, String viewStyle) {
        switch (viewStyle) {
            case "rectangle":
            case "symmetry":
            case "tile":
            case "vertical":
            case "mini":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_time_text_size);
        }
        return 0;
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                String subtitleData) {
        // weather.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_weather,
                getWeatherPendingIntent(
                        context,
                        location,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_WEATHER
                )
        );

        // clock.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_light,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_normal,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_black,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_BLACK
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_1_light,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_1_normal,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_1_black,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_BLACK
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_2_light,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_2_normal,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_2_black,
                getAlarmPendingIntent(
                        context,
                        BreezyWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_BLACK
                )
        );

        // time.
        if (subtitleData.equals("lunar")) {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_time,
                    getCalendarPendingIntent(
                            context,
                            BreezyWeather.WIDGET_DAY_PENDING_INTENT_CODE_CALENDAR
                    )
            );
        }
    }
}
