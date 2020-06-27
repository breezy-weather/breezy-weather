package com.mbestavros.geometricweather.remoteviews.presenter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Date;

import com.mbestavros.geometricweather.GeometricWeather;
import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.basic.model.location.Location;
import com.mbestavros.geometricweather.background.receiver.widget.WidgetDayWeekProvider;
import com.mbestavros.geometricweather.basic.model.option.WidgetWeekIconMode;
import com.mbestavros.geometricweather.basic.model.option.unit.TemperatureUnit;
import com.mbestavros.geometricweather.basic.model.weather.Base;
import com.mbestavros.geometricweather.basic.model.weather.Temperature;
import com.mbestavros.geometricweather.basic.model.weather.Weather;
import com.mbestavros.geometricweather.resource.ResourceHelper;
import com.mbestavros.geometricweather.resource.provider.ResourceProvider;
import com.mbestavros.geometricweather.resource.provider.ResourcesProviderFactory;
import com.mbestavros.geometricweather.settings.SettingsOptionManager;
import com.mbestavros.geometricweather.utils.helpter.LunarHelper;
import com.mbestavros.geometricweather.utils.manager.TimeManager;
import com.mbestavros.geometricweather.remoteviews.WidgetUtils;

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

        boolean dayTime = TimeManager.isDaylight(location);

        SettingsOptionManager settings = SettingsOptionManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        WidgetWeekIconMode weekIconMode = settings.getWidgetWeekIconMode();
        boolean minimalIcon = settings.isWidgetMinimalIconEnabled();
        boolean touchToRefresh = settings.isWidgetClickToRefreshEnabled();

        WidgetColor color = new WidgetColor(context, dayTime, cardStyle, textColor);

        int textColorInt;
        if (color.darkText) {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        // build day part.
        RemoteViews views = buildWidgetViewDayPart(
                context, provider,
                location, temperatureUnit,
                dayTime, textColorInt, textSize,
                minimalIcon, color.darkText,
                viewStyle, hideSubtitle, subtitleData);
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        // set week part.

        views.setTextViewText(
                R.id.widget_day_week_week_1,
                WidgetUtils.getDailyWeek(context, weather, 0)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_2,
                WidgetUtils.getDailyWeek(context, weather, 1)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_3,
                WidgetUtils.getDailyWeek(context, weather, 2)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_4,
                WidgetUtils.getDailyWeek(context, weather, 3)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_5,
                WidgetUtils.getDailyWeek(context, weather, 4)
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
                        minimalIcon, color.darkText,
                        0
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_2,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime,
                        minimalIcon, color.darkText,
                        1
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_3,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime,
                        minimalIcon, color.darkText,
                        2
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_4,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime,
                        minimalIcon, color.darkText,
                        3
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_5,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime,
                        minimalIcon, color.darkText,
                        4
                )
        );

        // set text color.
        views.setTextColor(R.id.widget_day_week_week_1, textColorInt);
        views.setTextColor(R.id.widget_day_week_week_2, textColorInt);
        views.setTextColor(R.id.widget_day_week_week_3, textColorInt);
        views.setTextColor(R.id.widget_day_week_week_4, textColorInt);
        views.setTextColor(R.id.widget_day_week_week_5, textColorInt);
        views.setTextColor(R.id.widget_day_week_temp_1, textColorInt);
        views.setTextColor(R.id.widget_day_week_temp_2, textColorInt);
        views.setTextColor(R.id.widget_day_week_temp_3, textColorInt);
        views.setTextColor(R.id.widget_day_week_temp_4, textColorInt);
        views.setTextColor(R.id.widget_day_week_temp_5, textColorInt);

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

        // set card visibility.
        if (color.showCard) {
            views.setImageViewResource(
                    R.id.widget_day_week_card,
                    getCardBackgroundId(context, color.darkCard, cardAlpha)
            );
            views.setViewVisibility(R.id.widget_day_week_card, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_day_week_card, View.GONE);
        }

        // set intent.
        setOnClickPendingIntent(context, views, location, subtitleData, touchToRefresh);

        return views;
    }

    private static RemoteViews buildWidgetViewDayPart(Context context, ResourceProvider helper,
                                                      Location location, TemperatureUnit temperatureUnit,
                                                      boolean dayTime, int textColor, int textSize,
                                                      boolean minimalIcon, boolean blackText,
                                                      String viewStyle, boolean hideSubtitle, String subtitleData) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_symmetry);
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_rectangle);
                break;

            case "symmetry":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_symmetry);
                break;

            case "tile":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_week_tile);
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
                        blackText
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

        views.setTextColor(R.id.widget_day_week_title, textColor);
        views.setTextColor(R.id.widget_day_week_subtitle, textColor);
        views.setTextColor(R.id.widget_day_week_time, textColor);

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
                return WidgetUtils.buildWidgetDayStyleText(context, weather, unit)[0];

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
                return WidgetUtils.buildWidgetDayStyleText(context, weather, unit)[1];

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
                        return WidgetUtils.getWeek(context)
                                + " "
                                + Base.getTime(context, weather.getBase().getUpdateDate());

                    case "tile":
                        return location.getCityName(context)
                                + " "
                                + WidgetUtils.getWeek(context)
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
                        return WidgetUtils.getWeek(context)
                                + " "
                                + LunarHelper.getLunarDate(new Date());

                    case "tile":
                        return location.getCityName(context)
                                + " "
                                + WidgetUtils.getWeek(context)
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
                                          boolean dayTime, boolean minimalIcon, boolean blackText,
                                          int index) {
        return ResourceHelper.getWidgetNotificationIconUri(
                helper,
                dayTime
                        ? weather.getDailyForecast().get(index).day().getWeatherCode()
                        : weather.getDailyForecast().get(index).night().getWeatherCode() ,
                dayTime, minimalIcon, blackText
        );
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                String subtitleData, boolean touchToRefresh) {
        // weather.
        if (touchToRefresh) {
            views.setOnClickPendingIntent(
                    R.id.widget_day_week_weather,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_REFRESH
                    )
            );
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_day_week_weather,
                    getWeatherPendingIntent(
                            context,
                            location,
                            GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_WEATHER
                    )
            );
        }

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
        } else if (!touchToRefresh && subtitleData.equals("time")) {
            views.setOnClickPendingIntent(
                    R.id.widget_day_week_subtitle,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_DAY_WEEK_PENDING_INTENT_CODE_REFRESH
                    )
            );
        }
    }
}
