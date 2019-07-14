package wangdaye.com.geometricweather.remoteviews.presenter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.receiver.widget.WidgetWeekProvider;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class WeekWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void refreshWidgetView(Context context, Location location, @Nullable Weather weather) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_week_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location, weather, config.cardStyle, config.cardAlpha, config.textColor, config.textSize);

        if (views != null) {
            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetWeekProvider.class),
                    views
            );
        }
    }

    public static RemoteViews getRemoteViews(Context context, Location location, @Nullable Weather weather,
                                             String cardStyle, int cardAlpha, String textColor, int textSize) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_week);
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
                getWeek(context, weather, 0));
        views.setTextViewText(
                R.id.widget_week_week_2,
                getWeek(context, weather, 1));
        views.setTextViewText(
                R.id.widget_week_week_3,
                getWeek(context, weather, 2));
        views.setTextViewText(
                R.id.widget_week_week_4,
                getWeek(context, weather, 3));
        views.setTextViewText(
                R.id.widget_week_week_5,
                getWeek(context, weather, 4));

        views.setTextViewText(
                R.id.widget_week_temp_1,
                getTemp(weather, fahrenheit, 0));
        views.setTextViewText(
                R.id.widget_week_temp_2,
                getTemp(weather, fahrenheit, 1));
        views.setTextViewText(
                R.id.widget_week_temp_3,
                getTemp(weather, fahrenheit, 2));
        views.setTextViewText(
                R.id.widget_week_temp_4,
                getTemp(weather, fahrenheit, 3));
        views.setTextViewText(
                R.id.widget_week_temp_5,
                getTemp(weather, fahrenheit, 4));

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

    private static String getWeek(Context context, Weather weather, int index) {
        if (index > 1) {
            return weather.dailyList.get(index).week;
        }

        String firstWeekDay;
        String secondWeekDay;
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String[] weatherDates = weather.base.date.split("-");
        if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month + 1
                && Integer.parseInt(weatherDates[2]) == day) {
            firstWeekDay = context.getString(R.string.today);
            secondWeekDay = weather.dailyList.get(1).week;
        } else if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month + 1
                && Integer.parseInt(weatherDates[2]) == day - 1) {
            firstWeekDay = context.getString(R.string.yesterday);
            secondWeekDay = context.getString(R.string.today);
        } else {
            firstWeekDay = weather.dailyList.get(0).week;
            secondWeekDay = weather.dailyList.get(1).week;
        }

        if (index == 0) {
            return firstWeekDay;
        } else {
            return secondWeekDay;
        }
    }

    private static String getTemp(Weather weather, boolean fahrenheit, int index) {
        return ValueUtils.buildDailyTemp(weather.dailyList.get(index).temps, false, fahrenheit);
    }

    private static Uri getIconDrawableUri(ResourceProvider helper, Weather weather,
                                          boolean dayTime, boolean minimalIcon, boolean blackText,
                                          int index) {
        return WeatherHelper.getWidgetNotificationIconUri(
                helper, weather.dailyList.get(index).weatherKinds[dayTime ? 0 : 1],
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
