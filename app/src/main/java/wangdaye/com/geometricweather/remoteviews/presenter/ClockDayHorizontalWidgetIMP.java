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
import wangdaye.com.geometricweather.background.receiver.widget.WidgetClockDayHorizontalProvider;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class ClockDayHorizontalWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void refreshWidgetView(Context context, Location location, @Nullable Weather weather) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_clock_day_horizontal_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location, weather,
                config.cardStyle, config.cardAlpha, config.textColor, config.textSize, config.clockFont
        );

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetClockDayHorizontalProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context,
                                             Location location, @Nullable Weather weather,
                                             String cardStyle, int cardAlpha,
                                             String textColor, int textSize, String clockFont) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_horizontal);
        if (weather == null) {
            return views;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

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

        int textColorInt;
        if (color.darkText) {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColorInt = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        views.setImageViewUri(
                R.id.widget_clock_day_icon,
                WeatherHelper.getWidgetNotificationIconUri(
                        provider,
                        weather.realTime.weatherKind,
                        dayTime,
                        minimalIcon,
                        color.darkText
                )
        );

        views.setTextViewText(
                R.id.widget_clock_day_lunar,
                LanguageUtils.getLanguageCode(context).startsWith("zh")
                        ? (" - " + LunarHelper.getLunarDate(Calendar.getInstance()))
                        : ""
        );

        views.setTextViewText(
                R.id.widget_clock_day_subtitle,
                weather.base.city
                        + " "
                        + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit)
        );

        views.setTextColor(R.id.widget_clock_day_clock_light, textColorInt);
        views.setTextColor(R.id.widget_clock_day_clock_normal, textColorInt);
        views.setTextColor(R.id.widget_clock_day_clock_black, textColorInt);
        views.setTextColor(R.id.widget_clock_day_clock_aa_light, textColorInt);
        views.setTextColor(R.id.widget_clock_day_clock_aa_normal, textColorInt);
        views.setTextColor(R.id.widget_clock_day_clock_aa_black, textColorInt);
        views.setTextColor(R.id.widget_clock_day_title, textColorInt);
        views.setTextColor(R.id.widget_clock_day_lunar, textColorInt);
        views.setTextColor(R.id.widget_clock_day_subtitle, textColorInt);

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
        }

        if (color.showCard) {
            views.setImageViewResource(
                    R.id.widget_clock_day_card,
                    getCardBackgroundId(context, color.darkCard, cardAlpha)
            );
            views.setViewVisibility(R.id.widget_clock_day_card, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_clock_day_card, View.GONE);
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

        setOnClickPendingIntent(context, views, location, touchToRefresh);

        return views;
    }

    public static boolean isEnable(Context context) {
        int[] widgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(
                        new ComponentName(context, WidgetClockDayHorizontalProvider.class)
                );
        return widgetIds != null && widgetIds.length > 0;
    }

    private static void setOnClickPendingIntent(Context context, RemoteViews views, Location location,
                                                boolean touchToRefresh) {
        // weather.
        if (touchToRefresh) {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_weather,
                    getRefreshPendingIntent(
                            context,
                            GeometricWeather.WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_REFRESH
                    )
            );
        } else {
            views.setOnClickPendingIntent(
                    R.id.widget_clock_day_weather,
                    getWeatherPendingIntent(
                            context,
                            location,
                            GeometricWeather.WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_WEATHER
                    )
            );
        }

        // clock.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_light,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_LIGHT
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_normal,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_NORMAL
                )
        );
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_clock_black,
                getAlarmPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_BLACK
                )
        );

        // title.
        views.setOnClickPendingIntent(
                R.id.widget_clock_day_title,
                getCalendarPendingIntent(
                        context,
                        GeometricWeather.WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CALENDAR
                )
        );
    }
}
