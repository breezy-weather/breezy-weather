package wangdaye.com.geometricweather.remoteviews.presenters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.Nullable;

import android.graphics.Color;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Date;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetDayWeekProvider;
import wangdaye.com.geometricweather.common.basic.models.options.NotificationTextColor;
import wangdaye.com.geometricweather.common.basic.models.options.WidgetWeekIconMode;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Base;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.WidgetHelper;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.common.utils.helpers.LunarHelper;

public class DayWeekWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_day_week_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location,
                config.viewStyle, config.cardStyle, config.cardAlpha,
                config.textColor, config.textSize, config.hideSubtitle, config.subtitleData
        );

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetDayWeekProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context, Location location,
                                             String viewStyle, String cardStyle, int cardAlpha,
                                             String textColor, int textSize,
                                             boolean hideSubtitle, String subtitleData) {
        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        boolean dayTime = location.isDaylight();

        SettingsManager settings = SettingsManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        WidgetWeekIconMode weekIconMode = settings.getWidgetWeekIconMode();
        boolean minimalIcon = settings.isWidgetMinimalIconEnabled();

        WidgetColor color = new WidgetColor(context, cardStyle, textColor);

        // build day part.
        RemoteViews views = buildWidgetViewDayPart(
                context, provider,
                location, temperatureUnit,
                color,
                dayTime, textSize,
                minimalIcon,
                viewStyle, hideSubtitle, subtitleData);
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        // set week part.

        views.setTextViewText(
                R.id.widget_day_week_week_1,
                WidgetHelper.getDailyWeek(context, weather, 0)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_2,
                WidgetHelper.getDailyWeek(context, weather, 1)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_3,
                WidgetHelper.getDailyWeek(context, weather, 2)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_4,
                WidgetHelper.getDailyWeek(context, weather, 3)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_5,
                WidgetHelper.getDailyWeek(context, weather, 4)
        );

        views.setTextViewText(
                R.id.widget_day_week_temp_1,
                getTemp(context, weather, 0, temperatureUnit)
        );
        views.setTextViewText(
                R.id.widget_day_week_temp_2,
                getTemp(context, weather, 1, temperatureUnit)
        );
        views.setTextViewText(
                R.id.widget_day_week_temp_3,
                getTemp(context, weather, 2, temperatureUnit)
        );
        views.setTextViewText(
                R.id.widget_day_week_temp_4,
                getTemp(context, weather, 3, temperatureUnit)
        );
        views.setTextViewText(
                R.id.widget_day_week_temp_5,
                getTemp(context, weather, 4, temperatureUnit)
        );

        boolean weekIconDaytime = isWeekIconDaytime(weekIconMode, dayTime);
        views.setImageViewUri(
                R.id.widget_day_week_icon_1,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime,
                        minimalIcon, color.getMinimalIconColor(),
                        0
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_2,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime,
                        minimalIcon, color.getMinimalIconColor(),
                        1
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_3,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime,
                        minimalIcon, color.getMinimalIconColor(),
                        2
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_4,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime,
                        minimalIcon, color.getMinimalIconColor(),
                        3
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_5,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime,
                        minimalIcon, color.getMinimalIconColor(),
                        4
                )
        );

        // set text color.
        if (color.textColor != Color.TRANSPARENT) {
            views.setTextColor(R.id.widget_day_week_week_1, color.textColor);
            views.setTextColor(R.id.widget_day_week_week_2, color.textColor);
            views.setTextColor(R.id.widget_day_week_week_3, color.textColor);
            views.setTextColor(R.id.widget_day_week_week_4, color.textColor);
            views.setTextColor(R.id.widget_day_week_week_5, color.textColor);
            views.setTextColor(R.id.widget_day_week_temp_1, color.textColor);
            views.setTextColor(R.id.widget_day_week_temp_2, color.textColor);
            views.setTextColor(R.id.widget_day_week_temp_3, color.textColor);
            views.setTextColor(R.id.widget_day_week_temp_4, color.textColor);
            views.setTextColor(R.id.widget_day_week_temp_5, color.textColor);
        }

        // set text size.
        if (textSize != 100) {
            float contentSize = context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size)
                    * textSize / 100f;
            views.setTextViewTextSize(R.id.widget_day_week_week_1, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_week_2, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_week_3, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_week_4, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_week_5, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_temp_1, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_temp_2, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_temp_3, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_temp_4, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_temp_5, TypedValue.COMPLEX_UNIT_PX, contentSize);
        }

        // set card.
        if (color.showCard) {
            views.setImageViewResource(
                    R.id.widget_day_week_card,
                    getCardBackgroundId(color.cardColor)
            );
            views.setInt(
                    R.id.widget_day_week_card,
                    "setImageAlpha",
                    (int) (cardAlpha / 100.0 * 255)
            );
        }

        // set intent.
        setOnClickPendingIntent(context, views, location, subtitleData);

        return views;
    }

    private static RemoteViews buildWidgetViewDayPart(Context context, ResourceProvider helper,
                                                      Location location, TemperatureUnit temperatureUnit,
                                                      WidgetColor color,
                                                      boolean dayTime, int textSize,
                                                      boolean minimalIcon,
                                                      String viewStyle, boolean hideSubtitle, String subtitleData) {
        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                !color.showCard
                        ? R.layout.widget_day_week_symmetry
                        : R.layout.widget_day_week_symmetry_card
        );
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_week_rectangle
                                : R.layout.widget_day_week_rectangle_card
                );
                break;

            case "symmetry":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_week_symmetry
                                : R.layout.widget_day_week_symmetry_card

                );
                break;

            case "tile":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_week_tile
                                : R.layout.widget_day_week_tile_card
                );
                break;
        }
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        views.setImageViewUri(
                R.id.widget_day_week_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                        helper,
                        weather.getCurrent().getWeatherCode(),
                        dayTime,
                        minimalIcon,
                        color.getMinimalIconColor()
                )
        );
        views.setTextViewText(
                R.id.widget_day_week_title,
                getTitleText(context, location, viewStyle, temperatureUnit)
        );
        views.setTextViewText(
                R.id.widget_day_week_subtitle,
                getSubtitleText(context, weather, viewStyle, temperatureUnit)
        );
        views.setTextViewText(
                R.id.widget_day_week_time,
                getTimeText(context, location, viewStyle, subtitleData, temperatureUnit)
        );

        if (color.textColor != Color.TRANSPARENT) {
            views.setTextColor(R.id.widget_day_week_title, color.textColor);
            views.setTextColor(R.id.widget_day_week_subtitle, color.textColor);
            views.setTextColor(R.id.widget_day_week_time, color.textColor);
        }

        if (textSize != 100) {
            float contentSize = context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size)
                    * textSize / 100f;
            float timeSize = context.getResources().getDimensionPixelSize(R.dimen.widget_time_text_size)
                    * textSize / 100f;
            views.setTextViewTextSize(R.id.widget_day_week_title, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_subtitle, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_day_week_time, TypedValue.COMPLEX_UNIT_PX, timeSize);
        }

        views.setViewVisibility(R.id.widget_day_week_time, hideSubtitle ? View.GONE : View.VISIBLE);

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(
                        new ComponentName(context, WidgetDayWeekProvider.class)
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

            case "tile":
                return weather.getCurrent().getWeatherText()
                        + " "
                        + weather.getCurrent().getTemperature().getTemperature(context, unit);
        }
        return "";
    }

    private static String getSubtitleText(Context context, Weather weather, String viewStyle,
                                          TemperatureUnit unit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetHelper.buildWidgetDayStyleText(context, weather, unit)[1];

            case "tile":
                return Temperature.getTrendTemperature(
                        context,
                        weather.getDailyForecast().get(0).night().getTemperature().getTemperature(),
                        weather.getDailyForecast().get(0).day().getTemperature().getTemperature(),
                        unit
                );

            case "symmetry":
                return weather.getCurrent().getWeatherText() + "\n" + Temperature.getTrendTemperature(
                        context,
                        weather.getDailyForecast().get(0).night().getTemperature().getTemperature(),
                        weather.getDailyForecast().get(0).day().getTemperature().getTemperature(),
                        unit
                );
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
                        return location.getCityName(context)
                                + " "
                                + WidgetHelper.getWeek(context)
                                + " "
                                + Base.getTime(context, weather.getBase().getUpdateDate());
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
                        return location.getCityName(context)
                                + " "
                                + WidgetHelper.getWeek(context)
                                + " "
                                + LunarHelper.getLunarDate(new Date());
                }
                break;

            case "sensible_time":
                return context.getString(R.string.feels_like) + " "
                        + weather.getCurrent().getTemperature().getShortRealFeeTemperature(context, unit);
        }
        return getCustomSubtitle(context, subtitleData, location, weather);
    }

    private static String getTemp(Context context, Weather weather, int index, TemperatureUnit unit) {
        return Temperature.getTrendTemperature(
                context,
                weather.getDailyForecast().get(index).night().getTemperature().getTemperature(),
                weather.getDailyForecast().get(index).day().getTemperature().getTemperature(),
                unit
        );
    }

    private static Uri getIconDrawableUri(ResourceProvider helper, Weather weather,
                                          boolean dayTime, boolean minimalIcon, NotificationTextColor color,
                                          int index) {
        return ResourceHelper.getWidgetNotificationIconUri(
                helper,
                dayTime
                        ? weather.getDailyForecast().get(index).day().getWeatherCode()
                        : weather.getDailyForecast().get(index).night().getWeatherCode() ,
                dayTime, minimalIcon, color
        );
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                String subtitleData) {
        // weather.
        views.setOnClickPendingIntent(
                R.id.widget_day_week_weather,
                getWeatherPendingIntent(
                        context,
                        location,
                        GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_WEATHER
                )
        );

        // daily forecast.
        views.setOnClickPendingIntent(
                R.id.widget_day_week_icon_1,
                getDailyForecastPendingIntent(
                        context,
                        location,
                        0,
                        GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_day_week_icon_2,
                getDailyForecastPendingIntent(
                        context,
                        location,
                        1,
                        GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_day_week_icon_3,
                getDailyForecastPendingIntent(
                        context,
                        location,
                        2,
                        GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_day_week_icon_4,
                getDailyForecastPendingIntent(
                        context,
                        location,
                        3,
                        GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_day_week_icon_5,
                getDailyForecastPendingIntent(
                        context,
                        location,
                        4,
                        GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5
                )
        );

        // time.
        if (subtitleData.equals("lunar")) {
            views.setOnClickPendingIntent(
                    R.id.widget_day_week_subtitle,
                    getCalendarPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR
                    )
            );
        }
    }
}
