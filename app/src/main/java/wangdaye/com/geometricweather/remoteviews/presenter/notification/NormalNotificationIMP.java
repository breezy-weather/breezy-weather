package wangdaye.com.geometricweather.remoteviews.presenter.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.widget.RemoteViews;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.remoteviews.presenter.AbstractRemoteViewsPresenter;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.utils.helpter.LunarHelper;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Normal notification utils.
 * */

public class NormalNotificationIMP extends AbstractRemoteViewsPresenter {

    public static void buildNotificationAndSendIt(Context context, @Nullable Weather weather) {
        if (weather == null) {
            return;
        }

        ResourceProvider provider = ResourcesProviderFactory.getNewInstance();

        LanguageUtils.setLanguage(
                context,
                SettingsOptionManager.getInstance(context).getLanguage()
        );

        // get sp & realTimeWeather.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = sharedPreferences.getBoolean(context.getString(R.string.key_fahrenheit), false);

        // get time & background color.
        boolean dayTime = TimeManager.isDaylight(weather);

        String style = sharedPreferences.getString(
                context.getString(R.string.key_notification_style),
                "geometric"
        );
        boolean minimalIcon = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_minimal_icon),
                false
        ) && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P;
        boolean tempIcon = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_temp_icon),
                false
        );
        boolean customColor = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_custom_color),
                false
        );
        boolean hideBigView = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_hide_big_view),
                false
        );
        boolean hideNotificationIcon = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_hide_icon),
                false
        );
        boolean hideNotificationInLockScreen = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_hide_in_lockScreen),
                false
        );
        boolean canBeCleared = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_can_be_cleared),
                false
        );

        if (style.equals("native")) {
            NativeNormalNotificationIMP.buildNotificationAndSendIt(context, weather, dayTime,
                    fahrenheit, tempIcon, hideNotificationIcon, hideNotificationInLockScreen, canBeCleared);
            return;
        }

        // background color.
        int backgroundColor = sharedPreferences.getInt(
                context.getString(R.string.key_notification_background_color),
                ContextCompat.getColor(context, R.color.notification_background_l)
        );

        // get text color.
        String textColor = sharedPreferences.getString(
                context.getString(R.string.key_notification_text_color),
                "dark"
        );
        int mainColor;
        int subColor;
        switch (textColor) {
            case "dark":
                mainColor = ContextCompat.getColor(context, R.color.colorTextDark);
                subColor = ContextCompat.getColor(context, R.color.colorTextDark2nd);
                break;
            case "grey":
                mainColor = ContextCompat.getColor(context, R.color.colorTextGrey);
                subColor = ContextCompat.getColor(context, R.color.colorTextGrey2nd);
                break;
            case "light":
            default:
                mainColor = ContextCompat.getColor(context, R.color.colorTextLight);
                subColor = ContextCompat.getColor(context, R.color.colorTextLight2nd);
                break;
        }

        // build channel.
        NotificationManager manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY,
                    GeometricWeather.getNotificationChannelName(
                            context,
                            GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY
                    ),
                    hideNotificationIcon
                            ? NotificationManager.IMPORTANCE_MIN
                            : NotificationManager.IMPORTANCE_LOW
            );
            channel.setShowBadge(false);
            manager.createNotificationChannel(channel);
        }

        // get manager & builder.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY);

        // set notification level.
        if (hideNotificationIcon) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        }

        // set notification visibility.
        if (hideNotificationInLockScreen) {
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }

        // set small icon.
        builder.setSmallIcon(
                tempIcon
                        ? WeatherHelper.getTempIconId(
                                context,
                                fahrenheit
                                        ? ValueUtils.calcFahrenheit(weather.realTime.temp)
                                        : weather.realTime.temp
                        ) : WeatherHelper.getDefaultMinimalXmlIconId(
                                weather.realTime.weatherKind,
                                dayTime
                        )
        );

        // build base view.
        builder.setContent(
                buildBaseView(
                        context, new RemoteViews(context.getPackageName(), R.layout.notification_base),
                        provider, weather,
                        dayTime, fahrenheit,
                        minimalIcon, textColor,
                        customColor, backgroundColor,
                        mainColor, subColor
                )
        );

        builder.setContentIntent(
                getWeatherPendingIntent(context, null, GeometricWeather.NOTIFICATION_ID_NORMALLY)
        );

        // build big view.
        if (hideBigView) {
            builder.setCustomBigContentView(
                    buildBaseView(
                            context, new RemoteViews(context.getPackageName(), R.layout.notification_base_big),
                            provider, weather,
                            dayTime, fahrenheit,
                            minimalIcon, textColor,
                            customColor, backgroundColor,
                            mainColor, subColor
                    )
            );
        } else {
            builder.setCustomBigContentView(
                    buildBigView(
                            context, new RemoteViews(context.getPackageName(), R.layout.notification_big),
                            provider, weather,
                            dayTime, fahrenheit,
                            minimalIcon, textColor,
                            customColor, backgroundColor,
                            mainColor, subColor
                    )
            );
        }

        // set clear flag
        if (canBeCleared) {
            // the notification can be cleared
            builder.setAutoCancel(true);
        } else {
            // the notification can not be cleared
            builder.setOngoing(true);
        }

        Notification notification = builder.build();
        if (!tempIcon && Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                notification.getClass()
                        .getMethod("setSmallIcon", Icon.class)
                        .invoke(
                                notification,
                                WeatherHelper.getMinimalIcon(
                                        provider, weather.realTime.weatherKind, dayTime)
                        );
            } catch (Exception ignore) {
                // do nothing.
            }
        }

        // commit.
        manager.notify(GeometricWeather.NOTIFICATION_ID_NORMALLY, notification);
    }

    private static RemoteViews buildBaseView(Context context, RemoteViews views,
                                             ResourceProvider provider, Weather weather,
                                             boolean dayTime, boolean fahrenheit,
                                             boolean minimalIcon, String textColor,
                                             boolean customColor, int backgroundColor,
                                             int mainColor, int subColor) {
        views.setImageViewUri(
                R.id.notification_base_icon,
                WeatherHelper.getWidgetNotificationIconUri(
                        provider, weather.realTime.weatherKind, dayTime, minimalIcon, textColor
                )
        );

        views.setTextViewText(
                R.id.notification_base_realtimeTemp,
                ValueUtils.buildAbbreviatedCurrentTemp(weather.realTime.temp, fahrenheit)
        );

        views.setTextViewText(
                R.id.notification_base_dailyTemp,
                ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit)
        );

        if (weather.aqi != null && weather.aqi.aqi >= 0) {
            views.setTextViewText(
                    R.id.notification_base_aqi_wind,
                    "AQI " + weather.aqi.aqi
            );
            int colorRes = WeatherHelper.getAqiColorResId(weather.aqi.aqi);
            if (colorRes == 0) {
                views.setTextColor(R.id.notification_base_aqi_wind, subColor);
            } else {
                views.setTextColor(
                        R.id.notification_base_aqi_wind,
                        ContextCompat.getColor(context, colorRes)
                );
            }

            String[] dates = weather.base.date.split("-");
            views.setTextViewText(
                    R.id.notification_base_lunar,
                    LanguageUtils.getLanguageCode(context).startsWith("zh")
                            ? LunarHelper.getLunarDate(dates)
                            : weather.base.city
            );
        } else {
            views.setTextViewText(
                    R.id.notification_base_aqi_wind,
                    weather.realTime.windLevel
            );
            int colorRes = WeatherHelper.getWindColorResId(weather.realTime.windSpeed);
            if (colorRes == 0) {
                views.setTextColor(R.id.notification_base_aqi_wind, subColor);
            } else {
                views.setTextColor(
                        R.id.notification_base_aqi_wind,
                        ContextCompat.getColor(context, colorRes)
                );
            }
        }

        views.setTextViewText(
                R.id.notification_base_weather,
                weather.realTime.weather
        );

        views.setTextViewText(
                R.id.notification_base_time,
                (LanguageUtils.getLanguageCode(context).startsWith("zh")
                        ? (weather.base.city + " ")
                        : "") + WidgetUtils.getWeek(context) + " " + weather.base.time
        );

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (customColor) {
                views.setInt(R.id.notification_base, "setBackgroundColor", backgroundColor);
            } else {
                views.setInt(R.id.notification_base, "setBackgroundColor", Color.TRANSPARENT);
            }

            views.setTextColor(R.id.notification_base_realtimeTemp, mainColor);
            views.setTextColor(R.id.notification_base_dailyTemp, subColor);
            views.setTextColor(R.id.notification_base_weather, mainColor);
            views.setTextColor(R.id.notification_base_lunar, subColor);
            views.setTextColor(R.id.notification_base_time, subColor);
        }

        return views;
    }

    private static RemoteViews buildBigView(Context context, RemoteViews views,
                                            ResourceProvider provider, Weather weather,
                                            boolean dayTime, boolean fahrenheit,
                                            boolean minimalIcon, String textColor,
                                            boolean customColor, int backgroundColor,
                                            int mainColor, int subColor) {
        // today
        views = buildBaseView(
                context, views, provider, weather,
                dayTime, fahrenheit,
                minimalIcon, textColor,
                customColor, backgroundColor,
                mainColor, subColor
        );

        // weekly.
        // 1
        views.setTextViewText( // set week 1.
                R.id.notification_big_week_1,
                context.getString(R.string.today)
        );
        views.setTextViewText( // set temps 1.
                R.id.notification_big_temp_1,
                ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, false, fahrenheit)
        );
        views.setImageViewUri( // set icon 1.
                R.id.notification_big_icon_1,
                WeatherHelper.getWidgetNotificationIconUri(
                        provider,
                        dayTime
                                ? weather.dailyList.get(0).weatherKinds[0]
                                : weather.dailyList.get(0).weatherKinds[1],
                        dayTime, minimalIcon, textColor
                )
        );
        // 2
        views.setTextViewText( // set week 2.
                R.id.notification_big_week_2,
                weather.dailyList.get(1).week
        );
        views.setTextViewText( // set temps 2.
                R.id.notification_big_temp_2,
                ValueUtils.buildDailyTemp(weather.dailyList.get(1).temps, false, fahrenheit)
        );
        views.setImageViewUri( // set icon 2.
                R.id.notification_big_icon_2,
                WeatherHelper.getWidgetNotificationIconUri( // get icon 2 resource id.
                        provider,
                        dayTime
                                ? weather.dailyList.get(1).weatherKinds[0]
                                : weather.dailyList.get(1).weatherKinds[1],
                        dayTime, minimalIcon, textColor
                )
        );
        // 3
        views.setTextViewText( // set week 3.
                R.id.notification_big_week_3,
                weather.dailyList.get(2).week
        );
        views.setTextViewText( // set temps 3.
                R.id.notification_big_temp_3,
                ValueUtils.buildDailyTemp(weather.dailyList.get(2).temps, false, fahrenheit)
        );
        views.setImageViewUri( // set icon 3.
                R.id.notification_big_icon_3,
                WeatherHelper.getWidgetNotificationIconUri( // get icon 3 resource id.
                        provider,
                        dayTime
                                ? weather.dailyList.get(2).weatherKinds[0]
                                : weather.dailyList.get(2).weatherKinds[1],
                        dayTime, minimalIcon, textColor
                )
        );
        // 4
        views.setTextViewText( // set week 4.
                R.id.notification_big_week_4,
                weather.dailyList.get(3).week
        );
        views.setTextViewText( // set temps 4.
                R.id.notification_big_temp_4,
                ValueUtils.buildDailyTemp(weather.dailyList.get(3).temps, false, fahrenheit)
        );
        views.setImageViewUri( // set icon 4.
                R.id.notification_big_icon_4,
                WeatherHelper.getWidgetNotificationIconUri( // get icon 4 resource id.
                        provider,
                        dayTime
                                ? weather.dailyList.get(3).weatherKinds[0]
                                : weather.dailyList.get(3).weatherKinds[1],
                        dayTime, minimalIcon, textColor
                )
        );
        // 5
        views.setTextViewText( // set week 5.
                R.id.notification_big_week_5,
                weather.dailyList.get(4).week
        );
        views.setTextViewText( // set temps 5.
                R.id.notification_big_temp_5,
                ValueUtils.buildDailyTemp(weather.dailyList.get(4).temps, false, fahrenheit)
        );
        views.setImageViewUri( // set icon 5.
                R.id.notification_big_icon_5,
                WeatherHelper.getWidgetNotificationIconUri( // get icon 5 resource id.
                        provider,
                        dayTime
                                ? weather.dailyList.get(4).weatherKinds[0]
                                : weather.dailyList.get(4).weatherKinds[1],
                        dayTime, minimalIcon, textColor
                )
        );

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (customColor) {
                views.setInt(R.id.notification_big, "setBackgroundColor", backgroundColor);
            } else {
                views.setInt(R.id.notification_big, "setBackgroundColor", Color.TRANSPARENT);
            }

            views.setTextColor(R.id.notification_big_week_1, subColor);
            views.setTextColor(R.id.notification_big_week_2, subColor);
            views.setTextColor(R.id.notification_big_week_3, subColor);
            views.setTextColor(R.id.notification_big_week_4, subColor);
            views.setTextColor(R.id.notification_big_week_5, subColor);
            views.setTextColor(R.id.notification_big_temp_1, subColor);
            views.setTextColor(R.id.notification_big_temp_2, subColor);
            views.setTextColor(R.id.notification_big_temp_3, subColor);
            views.setTextColor(R.id.notification_big_temp_4, subColor);
            views.setTextColor(R.id.notification_big_temp_5, subColor);
        }

        return views;
    }

    public static void cancelNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(GeometricWeather.NOTIFICATION_ID_NORMALLY);
        }
    }

    public static boolean isEnable(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(
                        context.getString(R.string.key_notification),
                        false
                );
    }
}
