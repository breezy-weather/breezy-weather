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
import wangdaye.com.geometricweather.background.receiver.widget.WidgetDayWeekProvider;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class DayWeekWidgetIMP extends AbstractRemoteViewsPresenter {

    public static void refreshWidgetView(Context context, Location location, @Nullable Weather weather) {
        WidgetConfig config = getWidgetConfig(
                context,
                context.getString(R.string.sp_widget_day_week_setting)
        );

        RemoteViews views = getRemoteViews(
                context, location, weather,
                config.viewStyle, config.cardStyle, config.cardAlpha,
                config.textColor, config.textSize, config.hideSubtitle, config.subtitleData
        );

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetDayWeekProvider.class),
                views
        );
    }

    public static RemoteViews getRemoteViews(Context context, Location location, @Nullable Weather weather,
                                             String viewStyle, String cardStyle, int cardAlpha,
                                             String textColor, int textSize,
                                             boolean hideSubtitle, String subtitleData) {
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

        // build day part.
        RemoteViews views = buildWidgetViewDayPart(
                context, provider,
                location, weather,
                dayTime, textColorInt, textSize, fahrenheit,
                minimalIcon, color.darkText,
                viewStyle, hideSubtitle, subtitleData);
        if (weather == null) {
            return views;
        }

        // set week part.

        views.setTextViewText(
                R.id.widget_day_week_week_1,
                getWeek(context, weather, 0)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_2,
                getWeek(context, weather, 1)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_3,
                getWeek(context, weather, 2)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_4,
                getWeek(context, weather, 3)
        );
        views.setTextViewText(
                R.id.widget_day_week_week_5,
                getWeek(context, weather, 4)
        );

        views.setTextViewText(
                R.id.widget_day_week_temp_1,
                getTemp(weather, fahrenheit, 0)
        );
        views.setTextViewText(
                R.id.widget_day_week_temp_2,
                getTemp(weather, fahrenheit, 1)
        );
        views.setTextViewText(
                R.id.widget_day_week_temp_3,
                getTemp(weather, fahrenheit, 2)
        );
        views.setTextViewText(
                R.id.widget_day_week_temp_4,
                getTemp(weather, fahrenheit, 3)
        );
        views.setTextViewText(
                R.id.widget_day_week_temp_5,
                getTemp(weather, fahrenheit, 4)
        );

        views.setImageViewUri(
                R.id.widget_day_week_icon_1,
                getIconDrawableUri(
                        provider, weather, dayTime,
                        minimalIcon, color.darkText,
                        0
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_2,
                getIconDrawableUri(
                        provider, weather, dayTime,
                        minimalIcon, color.darkText,
                        1
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_3,
                getIconDrawableUri(
                        provider, weather, dayTime,
                        minimalIcon, color.darkText,
                        2
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_4,
                getIconDrawableUri(
                        provider, weather, dayTime,
                        minimalIcon, color.darkText,
                        3
                )
        );
        views.setImageViewUri(
                R.id.widget_day_week_icon_5,
                getIconDrawableUri(
                        provider, weather, dayTime,
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
                                                      Location location, @Nullable Weather weather,
                                                      boolean dayTime, int textColor, int textSize,
                                                      boolean fahrenheit, boolean minimalIcon, boolean blackText,
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
        if (weather == null) {
            return views;
        }

        views.setImageViewUri(
                R.id.widget_day_week_icon,
                WeatherHelper.getWidgetNotificationIconUri(
                        helper,
                        weather.realTime.weatherKind,
                        dayTime,
                        minimalIcon,
                        blackText
                )
        );
        views.setTextViewText(
                R.id.widget_day_week_title,
                getTitleText(weather, viewStyle, fahrenheit)
        );
        views.setTextViewText(
                R.id.widget_day_week_subtitle,
                getSubtitleText(weather, viewStyle, fahrenheit)
        );
        views.setTextViewText(
                R.id.widget_day_week_time,
                getTimeText(context, location, weather, viewStyle, subtitleData)
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

    private static String getTitleText(Weather weather, String viewStyle, boolean fahrenheit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit)[0];

            case "symmetry":
                return weather.base.city + "\n" + ValueUtils.buildCurrentTemp(weather.realTime.temp, true, fahrenheit);

            case "tile":
                return weather.realTime.weather + " " + ValueUtils.buildCurrentTemp(weather.realTime.temp, false, fahrenheit);
        }
        return "";
    }

    private static String getSubtitleText(Weather weather, String viewStyle, boolean fahrenheit) {
        switch (viewStyle) {
            case "rectangle":
                return WidgetUtils.buildWidgetDayStyleText(weather, fahrenheit)[1];

            case "symmetry":
                return weather.realTime.weather + "\n" + ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit);

            case "tile":
                return ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit);
        }
        return "";
    }

    private static String getTimeText(Context context, Location location, Weather weather, String viewStyle, String subtitleData) {
        switch (subtitleData) {
            case "time":
                switch (viewStyle) {
                    case "rectangle":
                        return weather.base.city + " " + weather.base.time;

                    case "symmetry":
                        return WidgetUtils.getWeek(context) + " " + weather.base.time;

                    case "tile":
                        return weather.base.city + " " + WidgetUtils.getWeek(context) + " " + weather.base.time;
                }
                break;

            case "aqi":
                if (weather.aqi != null) {
                    return weather.aqi.quality + " (" + weather.aqi.aqi + ")";
                }
                break;

            case "wind":
                return weather.realTime.windLevel + " (" + weather.realTime.windDir + weather.realTime.windSpeed + ")";

            case "lunar":
                switch (viewStyle) {
                    case "rectangle":
                        return weather.base.city + " " + LunarHelper.getLunarDate(Calendar.getInstance());

                    case "symmetry":
                        return WidgetUtils.getWeek(context) + " " + LunarHelper.getLunarDate(Calendar.getInstance());

                    case "tile":
                        return weather.base.city + " " + WidgetUtils.getWeek(context) + " " + LunarHelper.getLunarDate(Calendar.getInstance());
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
