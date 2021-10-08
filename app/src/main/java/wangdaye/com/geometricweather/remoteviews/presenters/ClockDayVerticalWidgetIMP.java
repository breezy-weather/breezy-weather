package wangdaye.com.geometricweather.remoteviews.presenters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.Nullable;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Date;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetClockDayVerticalProvider;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Base;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.WidgetHelper;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.common.utils.DisplayUtils;
import wangdaye.com.geometricweather.common.utils.helpers.LunarHelper;

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
        boolean minimalIcon = settings.isWidgetMinimalIconEnabled();

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

    public static boolean isEnable(Context context) {
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
                return location.getCityName(context)
                        + "\n"
                        + weather.getCurrent().getTemperature().getTemperature(context, unit);

            case "vertical":
            case "tile":
                return weather.getCurrent().getWeatherText()
                        + " "
                        + weather.getCurrent().getTemperature().getTemperature(context, unit);

            case "mini":
                return weather.getCurrent().getWeatherText();

            case "temp":
                return weather.getCurrent().getTemperature().getShortTemperature(context, unit);
        }
        return "";
    }

    private static String getSubtitleText(Context context, Weather weather, String viewStyle,
                                          TemperatureUnit unit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetHelper.buildWidgetDayStyleText(context, weather, unit)[1];

            case "symmetry":
                return weather.getCurrent().getWeatherText() + "\n" + Temperature.getTrendTemperature(
                        context,
                        weather.getDailyForecast().get(0).night().getTemperature().getTemperature(),
                        weather.getDailyForecast().get(0).day().getTemperature().getTemperature(),
                        unit
                );

            case "tile":
            case "temp":
                return Temperature.getTrendTemperature(
                        context,
                        weather.getDailyForecast().get(0).night().getTemperature().getTemperature(),
                        weather.getDailyForecast().get(0).day().getTemperature().getTemperature(),
                        unit
                );

            case "mini":
                return weather.getCurrent().getTemperature().getTemperature(context, unit);
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
                                + Base.getTime(context, weather.getBase().getUpdateDate());

                    case "symmetry":
                        return WidgetHelper.getWeek(context)
                                + " "
                                + Base.getTime(context, weather.getBase().getUpdateDate());

                    case "tile":
                    case "vertical":
                        return location.getCityName(context)
                                + " " + WidgetHelper.getWeek(context)
                                + " " + Base.getTime(context, weather.getBase().getUpdateDate());
                }
                break;

            case "aqi":
                if (weather.getCurrent().getAirQuality().getAqiIndex() != null
                        && weather.getCurrent().getAirQuality().getAqiText() != null) {
                    return weather.getCurrent().getAirQuality().getAqiText()
                            + " ("
                            + weather.getCurrent().getAirQuality().getAqiIndex()
                            + ")";
                }
                break;

            case "wind":
                return weather.getCurrent().getWind().getDirection()
                        + " "
                        + weather.getCurrent().getWind().getLevel();

            case "lunar":
                switch (viewStyle) {
                    case "rectangle":
                        return location.getCityName(context)
                                + " "
                                + LunarHelper.getLunarDate(new Date());

                    case "symmetry":
                        return WidgetHelper.getWeek(context)
                                + " "
                                + LunarHelper.getLunarDate(new Date());

                    case "tile":
                    case "vertical":
                        return location.getCityName(context)
                                + " " + WidgetHelper.getWeek(context)
                                + " " + LunarHelper.getLunarDate(new Date());
                }
                break;

            case "sensible_time":
                return context.getString(R.string.feels_like)
                        + " "
                        + weather.getCurrent().getTemperature().getRealFeelTemperature(context, unit);
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
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_WEATHER
                )
        );

        // clock.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_light,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_normal,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_black,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_BLACK
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_1_light,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_1_normal,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_1_black,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_BLACK
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_2_light,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_2_normal,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_2_black,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_BLACK
                )
        );

        // time.
        if (subtitleData.equals("lunar")) {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_time,
                    getCalendarPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_PENDING_INTENT_CODE_CALENDAR
                    )
            );
        }
    }
}
