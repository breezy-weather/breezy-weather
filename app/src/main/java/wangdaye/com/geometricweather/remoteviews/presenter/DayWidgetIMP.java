package wangdaye.com.geometricweather.remoteviews.presenter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetDayProvider;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class DayWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void updateWidgetView(Context context, Location location, @Nullable Weather weather) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_day_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location, weather,
                config.viewStyle, config.cardStyle, config.cardAlpha,
                config.textColor, config.textSize, config.hideSubtitle, config.subtitleData
        );

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetDayProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context,
                                             Location location, @Nullable Weather weather,
                                             String viewStyle, String cardStyle, int cardAlpha,
                                             String textColor, int textSize,
                                             boolean hideSubtitle, String subtitleData) {

        boolean dayTime = TimeManager.isDaylight(weather);

        SharedPreferences defaultSharePreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false
        );
        boolean minimalIcon = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_widget_minimal_icon),
                false
        );
        boolean touchToRefresh = defaultSharePreferences.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh),
                false
        );

        WidgetColor color = new WidgetColor(context, dayTime, cardStyle, textColor);
        if (viewStyle.equals("pixel") || viewStyle.equals("nano") || viewStyle.equals("oreo")) {
            color.showCard = false;
            color.darkText = textColor.equals("dark")
                    || (textColor.equals("auto") && isLightWallpaper(context));
        }

        RemoteViews views = buildWidgetView(
                context, location, weather,
                dayTime, fahrenheit, minimalIcon,
                viewStyle, color, textSize,
                hideSubtitle, subtitleData);
        if (weather == null) {
            return views;
        }

        if (color.showCard) {
            views.setImageViewResource(
                    R.id.widget_day_card,
                    getCardBackgroundId(context, color.darkCard, cardAlpha)
            );
            views.setViewVisibility(R.id.widget_day_card, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_day_card, View.GONE);
        }

        setOnClickPendingIntent(context, views, location, viewStyle, subtitleData, touchToRefresh);

        return views;
    }

    private static RemoteViews buildWidgetView(Context context, Location location, @Nullable Weather weather,
                                               boolean dayTime, boolean fahrenheit, boolean minimalIcon,
                                               String viewStyle, WidgetColor color, int textSize,
                                               boolean hideSubtitle, String subtitleData) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_day_symmetry);
        switch (viewStyle) {
            case "rectangle":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_rectangle);
                break;

            case "symmetry":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_symmetry);
                break;

            case "tile":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_tile);
                break;

            case "mini":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_mini);
                break;

            case "nano":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_nano);
                break;

            case "pixel":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_pixel);
                break;

            case "vertical":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_vertical);
                break;

            case "oreo":
                views = new RemoteViews(context.getPackageName(), R.layout.widget_day_oreo);
                break;
        }
        if (weather == null) {
            return views;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        int textColorInt;
        if (color.darkText) {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        views.setImageViewUri(
                R.id.widget_day_icon,
                WeatherHelper.getWidgetNotificationIconUri(
                        provider,
                        weather.realTime.weatherKind,
                        dayTime,
                        minimalIcon,
                        color.darkText
                )
        );
        if (!viewStyle.equals("oreo")) {
            views.setTextViewText(
                    R.id.widget_day_title,
                    getTitleText(weather, viewStyle, fahrenheit)
            );
        }
        if (viewStyle.equals("vertical")) {
            boolean negative = fahrenheit
                    ? ValueUtils.calcFahrenheit(weather.realTime.temp) < 0
                    : weather.realTime.temp < 0;
            views.setViewVisibility(
                    R.id.widget_day_sign,
                    negative ? View.VISIBLE : View.GONE
            );
        }
        views.setTextViewText(
                R.id.widget_day_subtitle,
                getSubtitleText(weather, viewStyle, fahrenheit)
        );
        if (!viewStyle.equals("pixel")) {
            views.setTextViewText(
                    R.id.widget_day_time,
                    getTimeText(context, location, weather, viewStyle, subtitleData)
            );
        }

        views.setTextColor(R.id.widget_day_title, textColorInt);
        views.setTextColor(R.id.widget_day_sign, textColorInt);
        views.setTextColor(R.id.widget_day_symbol, textColorInt);
        views.setTextColor(R.id.widget_day_subtitle, textColorInt);
        views.setTextColor(R.id.widget_day_time, textColorInt);

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

    private static String getTitleText(Weather weather, String viewStyle, boolean fahrenheit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit)[0];

            case "symmetry":
                return weather.base.city
                        + "\n"
                        + ValueUtils.buildCurrentTemp(weather.realTime.temp, true, fahrenheit);

            case "tile":
            case "mini":
                return weather.realTime.weather
                        + " "
                        + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);

            case "nano":
            case "pixel":
                return ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);

            case "vertical":
                return ValueUtils.buildAbsCurrentTemp(weather.realTime.temp, fahrenheit);
        }
        return "";
    }

    private static String getSubtitleText(Weather weather, String viewStyle, boolean fahrenheit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit)[1];

            case "symmetry":
                return weather.realTime.weather
                        + "\n"
                        + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit);

            case "tile":
                return ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit);

            case "vertical":
                return weather.realTime.weather
                        + " "
                        + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, false, fahrenheit);

            case "oreo":
                return ValueUtils.buildCurrentTemp(weather.realTime.temp, true, fahrenheit);
        }
        return "";
    }

    private static String getTimeText(Context context, Location location, Weather weather,
                                      String viewStyle, String subtitleData) {
        switch (subtitleData) {
            case "time":
                switch (viewStyle) {
                    case "rectangle":
                        return weather.base.city + " " + weather.base.time;

                    case "symmetry":
                        return WidgetUtils.getWeek(context) + " " + weather.base.time;

                    case "tile":
                    case "mini":
                    case "vertical":
                        return weather.base.city
                                + " " + WidgetUtils.getWeek(context)
                                + " " + weather.base.time;
                }
                break;

            case "aqi":
                if (weather.aqi != null) {
                    return weather.aqi.quality + " (" + weather.aqi.aqi + ")";
                }
                break;

            case "wind":
                return weather.realTime.windLevel
                        + " (" + weather.realTime.windDir + weather.realTime.windSpeed + ")";

            case "lunar":
                switch (viewStyle) {
                    case "rectangle":
                        return weather.base.city
                                + " "
                                + LunarHelper.getLunarDate(Calendar.getInstance());

                    case "symmetry":
                        return WidgetUtils.getWeek(context)
                                + " "
                                + LunarHelper.getLunarDate(Calendar.getInstance());

                    case "tile":
                    case "mini":
                    case "vertical":
                        return weather.base.city
                                + " " + WidgetUtils.getWeek(context)
                                + " " + LunarHelper.getLunarDate(Calendar.getInstance());
                }
                break;

            case "sensible_time":
                return context.getString(R.string.feels_like) + " "
                        + ValueUtils.buildAbbreviatedCurrentTemp(
                                weather.realTime.sensibleTemp,
                                SettingsOptionManager.getInstance(context).isFahrenheit()
                        );
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
        if (viewStyle.equals("oreo")) {
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
