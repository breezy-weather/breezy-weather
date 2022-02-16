package wangdaye.com.geometricweather.remoteviews.presenters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import android.graphics.Color;
import android.net.Uri;
import android.util.TypedValue;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetWeekProvider;
import wangdaye.com.geometricweather.common.basic.models.options.NotificationTextColor;
import wangdaye.com.geometricweather.common.basic.models.options.WidgetWeekIconMode;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.WidgetHelper;
import wangdaye.com.geometricweather.theme.resource.ResourceHelper;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsManager;

public class WeekWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_week_setting)
        );

        RemoteViews views = getRemoteViews(context, location, config.viewStyle, config.cardStyle,
                config.cardAlpha, config.textColor, config.textSize);

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetWeekProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context, Location location, String viewStyle,
                                             String cardStyle, int cardAlpha, String textColor, int textSize) {

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        boolean dayTime = location.isDaylight();

        SettingsManager settings = SettingsManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        WidgetWeekIconMode weekIconMode = settings.getWidgetWeekIconMode();
        boolean minimalIcon = settings.isWidgetMinimalIconEnabled();

        WidgetColor color = new WidgetColor(context, cardStyle, textColor);

        RemoteViews views;
        switch (viewStyle) {
            case "3_days":
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_week_3
                                : R.layout.widget_week_3_card
                );
                break;

            default: // 5_days
                views = new RemoteViews(
                        context.getPackageName(),
                        !color.showCard
                                ? R.layout.widget_week
                                : R.layout.widget_week_card
                );
                break;
        }

        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        // weather view.
        views.setTextViewText(
                R.id.widget_week_week_1,
                WidgetHelper.getDailyWeek(context, weather, 0));
        views.setTextViewText(
                R.id.widget_week_week_2,
                WidgetHelper.getDailyWeek(context, weather, 1));
        views.setTextViewText(
                R.id.widget_week_week_3,
                WidgetHelper.getDailyWeek(context, weather, 2));
        views.setTextViewText(
                R.id.widget_week_week_4,
                WidgetHelper.getDailyWeek(context, weather, 3));
        views.setTextViewText(
                R.id.widget_week_week_5,
                WidgetHelper.getDailyWeek(context, weather, 4));

        views.setTextViewText(
                R.id.widget_week_temp,
                weather.getCurrent().getTemperature().getShortTemperature(context, temperatureUnit));
        views.setTextViewText(
                R.id.widget_week_temp_1,
                getTemp(context, weather, 0, temperatureUnit));
        views.setTextViewText(
                R.id.widget_week_temp_2,
                getTemp(context, weather, 1, temperatureUnit));
        views.setTextViewText(
                R.id.widget_week_temp_3,
                getTemp(context, weather, 2, temperatureUnit));
        views.setTextViewText(
                R.id.widget_week_temp_4,
                getTemp(context, weather, 3, temperatureUnit));
        views.setTextViewText(
                R.id.widget_week_temp_5,
                getTemp(context, weather, 4, temperatureUnit));

        views.setImageViewUri(
                R.id.widget_week_icon,
                ResourceHelper.getWidgetNotificationIconUri(
                        provider,
                        weather.getCurrent().getWeatherCode(),
                        dayTime,
                        minimalIcon,
                        color.getMinimalIconColor()
                )
        );
        boolean weekIconDaytime = isWeekIconDaytime(weekIconMode, dayTime);
        views.setImageViewUri(
                R.id.widget_week_icon_1,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime, minimalIcon, color.getMinimalIconColor(),
                        0)
        );
        views.setImageViewUri(
                R.id.widget_week_icon_2,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime, minimalIcon, color.getMinimalIconColor(),
                        1)
        );
        views.setImageViewUri(
                R.id.widget_week_icon_3,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime, minimalIcon, color.getMinimalIconColor(),
                        2)
        );
        views.setImageViewUri(
                R.id.widget_week_icon_4,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime, minimalIcon, color.getMinimalIconColor(),
                        3)
        );
        views.setImageViewUri(
                R.id.widget_week_icon_5,
                getIconDrawableUri(
                        provider, weather, weekIconDaytime, minimalIcon, color.getMinimalIconColor(),
                        4)
        );

        if (color.textColor != Color.TRANSPARENT) {
            views.setTextColor(R.id.widget_week_week_1, color.textColor);
            views.setTextColor(R.id.widget_week_week_2, color.textColor);
            views.setTextColor(R.id.widget_week_week_3, color.textColor);
            views.setTextColor(R.id.widget_week_week_4, color.textColor);
            views.setTextColor(R.id.widget_week_week_5, color.textColor);
            views.setTextColor(R.id.widget_week_temp, color.textColor);
            views.setTextColor(R.id.widget_week_temp_1, color.textColor);
            views.setTextColor(R.id.widget_week_temp_2, color.textColor);
            views.setTextColor(R.id.widget_week_temp_3, color.textColor);
            views.setTextColor(R.id.widget_week_temp_4, color.textColor);
            views.setTextColor(R.id.widget_week_temp_5, color.textColor);
        }

        // set text size.
        if (textSize != 100) {
            float contentSize = context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size)
                    * textSize / 100f;
            views.setTextViewTextSize(R.id.widget_week_week_1, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_week_week_2, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_week_week_3, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_week_week_4, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_week_week_5, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_week_temp_1, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_week_temp_2, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_week_temp_3, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_week_temp_4, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_week_temp_5, TypedValue.COMPLEX_UNIT_PX, contentSize);
        }

        // set card visibility.
        if (color.showCard) {
            views.setImageViewResource(
                    R.id.widget_week_card,
                    getCardBackgroundId(color.cardColor)
            );
            views.setInt(
                    R.id.widget_week_card,
                    "setImageAlpha",
                    (int) (cardAlpha / 100.0 * 255)
            );
        }

        // set intent.
        setOnClickPendingIntent(context, views, location, viewStyle);

        // commit.
        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetWeekProvider.class));
        return widgetIds != null && widgetIds.length > 0;
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
                        : weather.getDailyForecast().get(index).night().getWeatherCode(),
                dayTime, minimalIcon, color
        );
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                String viewType) {
        // weather.
        views.setOnClickPendingIntent(
                R.id.widget_week_weather,
                getWeatherPendingIntent(
                        context,
                        location,
                        GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_WEATHER
                )
        );

        // daily forecast.
        if (viewType.equals("3_days")) {
            views.setOnClickPendingIntent(
                    R.id.widget_week_icon_1,
                    getDailyForecastPendingIntent(
                            context,
                            location,
                            0,
                            GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1
                    )
            );
            views.setOnClickPendingIntent(
                    R.id.widget_week_icon_2,
                    getDailyForecastPendingIntent(
                            context,
                            location,
                            1,
                            GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2
                    )
            );
            views.setOnClickPendingIntent(
                    R.id.widget_week_icon_3,
                    getDailyForecastPendingIntent(
                            context,
                            location,
                            2,
                            GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3
                    )
            );
            views.setOnClickPendingIntent(
                    R.id.widget_week_icon_4,
                    getDailyForecastPendingIntent(
                            context,
                            location,
                            3,
                            GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4
                    )
            );
            views.setOnClickPendingIntent(
                    R.id.widget_week_icon_5,
                    getDailyForecastPendingIntent(
                            context,
                            location,
                            4,
                            GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5
                    )
            );
        }
    }
}
