package wangdaye.com.geometricweather.remoteviews.presenter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import androidx.core.content.ContextCompat;

import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetWeekProvider;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.resource.ResourceHelper;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.manager.TimeManager;

public class WeekWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_week_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location, config.cardStyle, config.cardAlpha, config.textColor, config.textSize);

        if (views != null) {
            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetWeekProvider.class),
                    views
            );
        }
    }

    public static RemoteViews getRemoteViews(Context context, Location location,
                                             String cardStyle, int cardAlpha, String textColor, int textSize) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_week);
        Weather weather = location.getWeather();
        if (weather == null) {
            return views;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        boolean dayTime = TimeManager.isDaylight(location);

        SettingsOptionManager settings = SettingsOptionManager.getInstance(context);
        TemperatureUnit temperatureUnit = settings.getTemperatureUnit();
        boolean minimalIcon = settings.isWidgetMinimalIconEnabled();
        boolean touchToRefresh = settings.isWidgetClickToRefreshEnabled();

        WidgetColor color = new WidgetColor(context, dayTime, cardStyle, textColor);

        // get text color.
        int textColorInt;
        if (color.darkText) {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        // weather view.
        views.setTextViewText(
                R.id.widget_week_week_1,
                WidgetUtils.getDailyWeek(context, weather, 0));
        views.setTextViewText(
                R.id.widget_week_week_2,
                WidgetUtils.getDailyWeek(context, weather, 1));
        views.setTextViewText(
                R.id.widget_week_week_3,
                WidgetUtils.getDailyWeek(context, weather, 2));
        views.setTextViewText(
                R.id.widget_week_week_4,
                WidgetUtils.getDailyWeek(context, weather, 3));
        views.setTextViewText(
                R.id.widget_week_week_5,
                WidgetUtils.getDailyWeek(context, weather, 4));

        views.setTextViewText(
                R.id.widget_week_temp_1,
                getTemp(weather, 0, temperatureUnit));
        views.setTextViewText(
                R.id.widget_week_temp_2,
                getTemp(weather, 1, temperatureUnit));
        views.setTextViewText(
                R.id.widget_week_temp_3,
                getTemp(weather, 2, temperatureUnit));
        views.setTextViewText(
                R.id.widget_week_temp_4,
                getTemp(weather, 3, temperatureUnit));
        views.setTextViewText(
                R.id.widget_week_temp_5,
                getTemp(weather, 4, temperatureUnit));

        views.setImageViewUri(
                R.id.widget_week_icon_1,
                getIconDrawableUri(
                        provider, weather, dayTime, minimalIcon, color.darkText,
                        0)
        );
        views.setImageViewUri(
                R.id.widget_week_icon_2,
                getIconDrawableUri(
                        provider, weather, dayTime, minimalIcon, color.darkText,
                        1)
        );
        views.setImageViewUri(
                R.id.widget_week_icon_3,
                getIconDrawableUri(
                        provider, weather, dayTime, minimalIcon, color.darkText,
                        2)
        );
        views.setImageViewUri(
                R.id.widget_week_icon_4,
                getIconDrawableUri(
                        provider, weather, dayTime, minimalIcon, color.darkText,
                        3)
        );
        views.setImageViewUri(
                R.id.widget_week_icon_5,
                getIconDrawableUri(
                        provider, weather, dayTime, minimalIcon, color.darkText,
                        4)
        );

        // set text color.
        views.setTextColor(R.id.widget_week_week_1, textColorInt);
        views.setTextColor(R.id.widget_week_week_2, textColorInt);
        views.setTextColor(R.id.widget_week_week_3, textColorInt);
        views.setTextColor(R.id.widget_week_week_4, textColorInt);
        views.setTextColor(R.id.widget_week_week_5, textColorInt);
        views.setTextColor(R.id.widget_week_temp_1, textColorInt);
        views.setTextColor(R.id.widget_week_temp_2, textColorInt);
        views.setTextColor(R.id.widget_week_temp_3, textColorInt);
        views.setTextColor(R.id.widget_week_temp_4, textColorInt);
        views.setTextColor(R.id.widget_week_temp_5, textColorInt);

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
                    getCardBackgroundId(context, color.darkCard, cardAlpha)
            );
            views.setViewVisibility(R.id.widget_week_card, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_week_card, View.GONE);
        }

        // set intent.
        setOnClickPendingIntent(context, views, location, touchToRefresh);

        // commit.
        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetWeekProvider.class));
        return widgetIds != null && widgetIds.length > 0;
    }

    private static String getTemp(Weather weather, int index, TemperatureUnit unit) {
        return Temperature.getTrendTemperature(
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
                        : weather.getDailyForecast().get(index).night().getWeatherCode(),
                dayTime, minimalIcon, blackText
        );
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                boolean touchToRefresh) {
        // weather.
        if (touchToRefresh) {
            views.setOnClickPendingIntent(
                    R.id.widget_week_weather,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_REFRESH
                    )
            );
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_week_weather,
                    getWeatherPendingIntent(
                            context,
                            location,
                            GeometricWeather.WIDGET_WEEK_PENDING_INTENT_CODE_WEATHER
                    )
            );
        }
    }
}
