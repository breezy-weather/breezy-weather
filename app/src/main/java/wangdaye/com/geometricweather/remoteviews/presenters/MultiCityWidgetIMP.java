package wangdaye.com.geometricweather.remoteviews.presenters;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;
import androidx.annotation.IntRange;

import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetMultiCityProvider;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsManager;

public class MultiCityWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, List<Location> locationList) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_multi_city)
        );

        RemoteViews views = getRemoteViews(
                context, locationList,
                config.cardStyle, config.cardAlpha, config.textColor, config.textSize
        );

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetMultiCityProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context,
                                             List<Location> locationList,
                                             String cardStyle, int cardAlpha,
                                             String textColor, int textSize) {
        Location location = locationList.get(0);
        Weather weather = location.getWeather();
        boolean dayTime = location.isDaylight();

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        SettingsManager settings = SettingsManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        boolean minimalIcon = settings.isWidgetMinimalIconEnabled();

        WidgetColor color = new WidgetColor(context, cardStyle, textColor);

        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                !color.showCard
                        ? R.layout.widget_multi_city_horizontal
                        : R.layout.widget_multi_city_horizontal_card
        );

        // city 1.
        views.setViewVisibility(R.id.widget_multi_city_horizontal_weather_1, View.VISIBLE);
        if (weather != null) {
            views.setTextViewText(
                    R.id.widget_multi_city_horizontal_title_1,
                    location.getCityName(context)
            );

            views.setImageViewUri(
                    R.id.widget_multi_city_horizontal_icon_1,
                    ResourceHelper.getWidgetNotificationIconUri(
                            provider,
                            dayTime
                                    ? weather.getDailyForecast().get(0).day().getWeatherCode()
                                    : weather.getDailyForecast().get(0).night().getWeatherCode(),
                            dayTime,
                            minimalIcon,
                            color.getMinimalIconColor()
                    )
            );

            views.setTextViewText(
                    R.id.widget_multi_city_horizontal_content_1,
                    Temperature.getTrendTemperature(
                            context,
                            weather.getDailyForecast().get(0).night().getTemperature().getTemperature(),
                            weather.getDailyForecast().get(0).day().getTemperature().getTemperature(),
                            temperatureUnit
                    )
            );
        }
        setOnClickPendingIntent(context, views, location,
                R.id.widget_multi_city_horizontal_weather_1, 0);

        // city 2.
        if (locationList.size() >= 2) {
            location = locationList.get(1);
            weather = location.getWeather();
            dayTime = location.isDaylight();

            views.setViewVisibility(R.id.widget_multi_city_horizontal_weather_2, View.VISIBLE);
            if (weather != null) {
                views.setTextViewText(
                        R.id.widget_multi_city_horizontal_title_2,
                        location.getCityName(context)
                );

                views.setImageViewUri(
                        R.id.widget_multi_city_horizontal_icon_2,
                        ResourceHelper.getWidgetNotificationIconUri(
                                provider,
                                dayTime
                                        ? weather.getDailyForecast().get(0).day().getWeatherCode()
                                        : weather.getDailyForecast().get(0).night().getWeatherCode(),
                                dayTime,
                                minimalIcon,
                                color.getMinimalIconColor()
                        )
                );

                views.setTextViewText(
                        R.id.widget_multi_city_horizontal_content_2,
                        Temperature.getTrendTemperature(
                                context,
                                weather.getDailyForecast().get(0).night().getTemperature().getTemperature(),
                                weather.getDailyForecast().get(0).day().getTemperature().getTemperature(),
                                temperatureUnit
                        )
                );
            }
            setOnClickPendingIntent(context, views, location,
                    R.id.widget_multi_city_horizontal_weather_2, 1);
        } else {
            views.setViewVisibility(R.id.widget_multi_city_horizontal_weather_2, View.GONE);
        }

        // city 3.
        if (locationList.size() >= 3) {
            location = locationList.get(2);
            weather = location.getWeather();
            dayTime = location.isDaylight();

            views.setViewVisibility(R.id.widget_multi_city_horizontal_weather_3, View.VISIBLE);
            if (weather != null) {
                views.setTextViewText(
                        R.id.widget_multi_city_horizontal_title_3,
                        location.getCityName(context)
                );

                views.setImageViewUri(
                        R.id.widget_multi_city_horizontal_icon_3,
                        ResourceHelper.getWidgetNotificationIconUri(
                                provider,
                                dayTime
                                        ? weather.getDailyForecast().get(0).day().getWeatherCode()
                                        : weather.getDailyForecast().get(0).night().getWeatherCode(),
                                dayTime,
                                minimalIcon,
                                color.getMinimalIconColor()
                        )
                );

                views.setTextViewText(
                        R.id.widget_multi_city_horizontal_content_3,
                        Temperature.getTrendTemperature(
                                context,
                                weather.getDailyForecast().get(0).night().getTemperature().getTemperature(),
                                weather.getDailyForecast().get(0).day().getTemperature().getTemperature(),
                                temperatureUnit
                        )
                );
            }
            setOnClickPendingIntent(context, views, location,
                    R.id.widget_multi_city_horizontal_weather_3, 2);
        } else {
            views.setViewVisibility(R.id.widget_multi_city_horizontal_weather_3, View.GONE);
        }

        if (color.textColor != Color.TRANSPARENT) {
            views.setTextColor(R.id.widget_multi_city_horizontal_title_1, color.textColor);
            views.setTextColor(R.id.widget_multi_city_horizontal_title_2, color.textColor);
            views.setTextColor(R.id.widget_multi_city_horizontal_title_3, color.textColor);
            views.setTextColor(R.id.widget_multi_city_horizontal_content_1, color.textColor);
            views.setTextColor(R.id.widget_multi_city_horizontal_content_2, color.textColor);
            views.setTextColor(R.id.widget_multi_city_horizontal_content_3, color.textColor);
        }

        if (textSize != 100) {
            float titleSize = context.getResources().getDimensionPixelSize(R.dimen.widget_title_text_size)
                    * textSize / 100f;
            float contentSize = context.getResources().getDimensionPixelSize(R.dimen.widget_content_text_size)
                    * textSize / 100f;

            views.setTextViewTextSize(R.id.widget_multi_city_horizontal_title_1, TypedValue.COMPLEX_UNIT_PX, titleSize);
            views.setTextViewTextSize(R.id.widget_multi_city_horizontal_title_2, TypedValue.COMPLEX_UNIT_PX, titleSize);
            views.setTextViewTextSize(R.id.widget_multi_city_horizontal_title_3, TypedValue.COMPLEX_UNIT_PX, titleSize);
            views.setTextViewTextSize(R.id.widget_multi_city_horizontal_content_1, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_multi_city_horizontal_content_2, TypedValue.COMPLEX_UNIT_PX, contentSize);
            views.setTextViewTextSize(R.id.widget_multi_city_horizontal_content_3, TypedValue.COMPLEX_UNIT_PX, contentSize);
        }

        if (color.showCard) {
            views.setImageViewResource(
                    R.id.widget_multi_city_horizontal_card,
                    getCardBackgroundId(color.cardColor)
            );
            views.setInt(
                    R.id.widget_multi_city_horizontal_card,
                    "setImageAlpha",
                    (int) (cardAlpha / 100.0 * 255)
            );
        }

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(
                        new ComponentName(context, WidgetMultiCityProvider.class)
                );
        return widgetIds != null && widgetIds.length > 0;
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                @IdRes int resId, @IntRange(from = 0, to = 2) int index) {
        views.setOnClickPendingIntent(
                resId,
                getWeatherPendingIntent(
                        context,
                        location,
                        GeometricWeather.WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_1 + 2 * index
                )
        );
    }
}
