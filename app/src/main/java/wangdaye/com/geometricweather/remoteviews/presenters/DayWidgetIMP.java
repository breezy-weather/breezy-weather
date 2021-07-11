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
import wangdaye.com.geometricweather.background.receiver.widget.WidgetDayProvider;
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

public class DayWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_day_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location,
                config.viewStyle, config.cardStyle, config.cardAlpha,
                config.textColor, config.textSize, config.hideSubtitle, config.subtitleData
        );

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetDayProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context,
                                             Location location,
                                             String viewStyle, String cardStyle, int cardAlpha,
                                             String textColor, int textSize,
                                             boolean hideSubtitle, String subtitleData) {

        boolean dayTime = location.isDaylight();

        SettingsManager settings = SettingsManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        boolean minimalIcon = settings.isWidgetMinimalIconEnabled();
        boolean touchToRefresh = settings.isWidgetClickToRefreshEnabled();

        WidgetColor color;
        if (viewStyle.equals("pixel") || viewStyle.equals("nano")
                || viewStyle.equals("oreo") || viewStyle.equals("oreo_google_sans")
                || viewStyle.equals("temp")) {
            color = new WidgetColor(context, "none", textColor);
        } else {
            color = new WidgetColor(context, cardStyle, textColor);
        }

        RemoteViews views = buildWidgetView(
                context, location, temperatureUnit,
                color,
                dayTime, minimalIcon,
                viewStyle, textSize,
                hideSubtitle, subtitleData);
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        if (color.showCard) {
            views.setImageViewResource(
                    R.id.widget_day_card,
                    getCardBackgroundId(color.cardColor)
            );
            views.setInt(
                    R.id.widget_day_card,
                    "setImageAlpha",
                    (int) (cardAlpha / 100.0 * 255)
            );
        }

        setOnClickPendingIntent(context, views, location, viewStyle, subtitleData, touchToRefresh);

        return views;
    }

    private static RemoteViews buildWidgetView(Context context, Location location, 
                                               TemperatureUnit temperatureUnit,
                                               WidgetColor color,
                                               boolean dayTime, boolean minimalIcon,
                                               String viewStyle, int textSize,
                                               boolean hideSubtitle, String subtitleData) {
        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                !color.showCard
                        ? R.layout.widget_day_symmetry
                        : R.layout.widget_day_symmetry_card
        );
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_rectangle
                                : R.layout.widget_day_rectangle_card
                );
                break;

            case "symmetry":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_symmetry
                                : R.layout.widget_day_symmetry_card
                );
                break;

            case "tile":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_tile
                                : R.layout.widget_day_tile_card
                );
                break;

            case "mini":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_mini
                                : R.layout.widget_day_mini_card
                );
                break;

            case "nano":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_nano
                                : R.layout.widget_day_nano_card
                );
                break;

            case "pixel":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_pixel
                                : R.layout.widget_day_pixel_card
                        );
                break;

            case "vertical":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_vertical
                                : R.layout.widget_day_vertical_card
                );
                break;

            case "oreo":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_oreo
                                : R.layout.widget_day_oreo_card
                );
                break;

            case "oreo_google_sans":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_oreo_google_sans
                                : R.layout.widget_day_oreo_google_sans_card
                );
                break;

            case "temp":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_day_temp
                                : R.layout.widget_day_temp_card
                );
                break;
        }
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        views.setImageViewUri(
                R.id.widget_day_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                        provider,
                        weather.getCurrent().getWeatherCode(),
                        dayTime,
                        minimalIcon,
                        color.getMinimalIconColor()
                )
        );
        if (!viewStyle.equals("oreo") && !viewStyle.equals("oreo_google_sans")) {
            views.setTextViewText(
                    R.id.widget_day_title,
                    getTitleText(context, location, viewStyle, temperatureUnit)
            );
        }
        if (viewStyle.equals("vertical")) {
            boolean negative = temperatureUnit.getTemperature(
                    weather.getCurrent().getTemperature().getTemperature()) < 0;
            views.setViewVisibility(
                    R.id.widget_day_sign,
                    negative ? View.VISIBLE : View.GONE
            );
        }
        views.setTextViewText(
                R.id.widget_day_subtitle,
                getSubtitleText(context, weather, viewStyle, temperatureUnit)
        );
        if (!viewStyle.equals("pixel")) {
            views.setTextViewText(
                    R.id.widget_day_time,
                    getTimeText(context, location, weather, viewStyle, subtitleData, temperatureUnit)
            );
        }

        if (color.textColor != Color.TRANSPARENT) {
            views.setTextColor(R.id.widget_day_title, color.textColor);
            views.setTextColor(R.id.widget_day_sign, color.textColor);
            views.setTextColor(R.id.widget_day_symbol, color.textColor);
            views.setTextColor(R.id.widget_day_subtitle, color.textColor);
            views.setTextColor(R.id.widget_day_time, color.textColor);
        }

        if (textSize != 100) {
            float signSymbolSize = context.getResources().getDimensionPixelSize(R.dimen.widget_current_weather_icon_size)
                    * textSize / 100f;

            views.setTextViewTextSize(R.id.widget_day_title, TypedValue.COMPLEX_UNIT_PX,
                    getTitleSize(context, viewStyle) * textSize / 100f);

            views.setTextViewTextSize(R.id.widget_day_sign, TypedValue.COMPLEX_UNIT_PX, signSymbolSize);
            views.setTextViewTextSize(R.id.widget_day_symbol, TypedValue.COMPLEX_UNIT_PX, signSymbolSize);

            views.setTextViewTextSize(R.id.widget_day_subtitle, TypedValue.COMPLEX_UNIT_PX,
                    getSubtitleSize(context, viewStyle) * textSize / 100f);

            views.setTextViewTextSize(R.id.widget_day_time, TypedValue.COMPLEX_UNIT_PX,
                    getTimeSize(context, viewStyle) * textSize / 100f);
        }

        views.setViewVisibility(R.id.widget_day_time, hideSubtitle ? View.GONE : View.VISIBLE);

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetDayProvider.class));
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
            case "mini":
                return weather.getCurrent().getWeatherText()
                        + " "
                        + weather.getCurrent().getTemperature().getTemperature(context, unit);

            case "nano":
            case "pixel":
                return weather.getCurrent().getTemperature().getTemperature(context, unit);

            case "temp":
                return weather.getCurrent().getTemperature().getShortTemperature(context, unit);

            case "vertical":
                return String.valueOf(
                        Math.abs(
                                unit.getTemperature(
                                        weather.getCurrent().getTemperature().getTemperature()
                                )
                        )
                );
        }
        return "";
    }

    private static String getSubtitleText(Context context, Weather weather, String viewStyle, TemperatureUnit unit) {
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

            case "vertical":
                return weather.getCurrent().getWeatherText() + " " + Temperature.getTrendTemperature(
                        context,
                        weather.getDailyForecast().get(0).night().getTemperature().getTemperature(),
                        weather.getDailyForecast().get(0).day().getTemperature().getTemperature(),
                        unit
                );

            case "oreo":
                return weather.getCurrent().getTemperature().getTemperature(context, unit);

            case "oreo_google_sans":
                return unit.getLongTemperatureText(context, weather.getCurrent().getTemperature().getTemperature());
        }
        return "";
    }

    private static String getTimeText(Context context, Location location, Weather weather,
                                      String viewStyle, String subtitleData, TemperatureUnit unit) {
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
                    case "mini":
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
                    case "mini":
                    case "vertical":
                        return location.getCityName(context)
                                + " " + WidgetHelper.getWeek(context)
                                + " " + LunarHelper.getLunarDate(new Date());
                }
                break;

            case "sensible_time":
                return context.getString(R.string.feels_like) + " "
                        + weather.getCurrent().getTemperature().getShortRealFeeTemperature(context, unit);
        }
        return getCustomSubtitle(context, subtitleData, location, weather);
    }

    private static float getTitleSize(Context context, String viewStyle) {
        switch (viewStyle) {
            case "rectangle":
            case "symmetry":
            case "tile":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size);

            case "mini":
            case "nano":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_subtitle_text_size);

            case "pixel":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_design_title_text_size);

            case "vertical":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_current_weather_icon_size);

            case "oreo":
            case "oreo_google_sans":
            case "temp":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_large_title_text_size);
        }
        return 0;
    }

    private static float getSubtitleSize(Context context, String viewStyle) {
        switch (viewStyle) {
            case "rectangle":
            case "symmetry":
            case "tile":
            case "vertical":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size);

            case "oreo":
            case "oreo_google_sans":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_large_title_text_size);
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

            case "pixel":
                return context.getResources().getDimensionPixelSize(R.dimen.widget_subtitle_text_size);
        }
        return 0;
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                String viewStyle, String subtitleData, boolean touchToRefresh) {
        // weather.
        if (touchToRefresh) {
            views.setOnClickPendingIntent(
                    R.id.widget_day_weather,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_PENDING_INTENT_CODE_REFRESH
                    )
            );
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_day_weather,
                    getWeatherPendingIntent(
                            context,
                            location,
                            GeometricWeather.WIDGET_DAY_PENDING_INTENT_CODE_WEATHER
                    )
            );
        }

        // title.
        if (viewStyle.equals("oreo") || viewStyle.equals("oreo_google_sans")) {
            views.setOnClickPendingIntent(
                    R.id.widget_day_title,
                    getCalendarPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_PENDING_INTENT_CODE_CALENDAR
                    )
            );
        }

        // time.
        if (viewStyle.equals("pixel") || subtitleData.equals("lunar")) {
            views.setOnClickPendingIntent(
                    R.id.widget_day_time,
                    getCalendarPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_PENDING_INTENT_CODE_CALENDAR
                    )
            );
        } else if (!touchToRefresh && subtitleData.equals("time")) {
            views.setOnClickPendingIntent(
                    R.id.widget_day_time,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_PENDING_INTENT_CODE_REFRESH
                    )
            );
        }
    }
}
