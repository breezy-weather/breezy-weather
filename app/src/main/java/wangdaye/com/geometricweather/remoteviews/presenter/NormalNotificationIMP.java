package wangdaye.com.geometricweather.remoteviews.presenter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
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

        LanguageUtils.setLanguage(context, GeometricWeather.getInstance().getLanguage());

        // get sp & realTimeWeather.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = sharedPreferences.getBoolean(context.getString(R.string.key_fahrenheit), false);

        // get time & background color.
        boolean dayTime = TimeManager.getInstance(context)
                .getDayTime(context, weather, false)
                .isDayTime();
        boolean minimalIcon = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_minimal_icon),
                false
        );
        boolean tempIcon = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_temp_icon),
                false
        );
        boolean backgroundColor = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_background),
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
                context,
                GeometricWeather.NOTIFICATION_CHANNEL_ID_NORMALLY
        );

        // set notification level.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && hideNotificationIcon) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_MAX);
        }

        // set notification visibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_in_lockScreen), false)) {
                builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
            } else {
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            }
        }

        // set small icon.
        builder.setSmallIcon(
                tempIcon
                        ? WeatherHelper.getTempIconId(
                                context,
                                fahrenheit
                                        ? ValueUtils.calcFahrenheit(weather.realTime.temp)
                                        : weather.realTime.temp
                        ) : WeatherHelper.getMiniIconId(
                                provider,
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
                        backgroundColor, mainColor, subColor
                )
        );

        builder.setContentIntent(
                getWeatherPendingIntent(context, null, 0)
        );

        // build big view.
        if (hideBigView) {
            builder.setCustomBigContentView(
                    buildBaseView(
                            context, new RemoteViews(context.getPackageName(), R.layout.notification_base_big),
                            provider, weather,
                            dayTime, fahrenheit,
                            minimalIcon, textColor,
                            backgroundColor, mainColor, subColor
                    )
            );
        } else {
            builder.setCustomBigContentView(
                    buildBigView(
                            context, new RemoteViews(context.getPackageName(), R.layout.notification_big),
                            provider, weather,
                            dayTime, fahrenheit,
                            minimalIcon, textColor,
                            backgroundColor, mainColor, subColor
                    )
            );
        }

        // set clear flag
        if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_can_be_cleared), false)) {
            // the notification can be cleared
            builder.setAutoCancel(true);
        } else {
            // the notification can not be cleared
            builder.setOngoing(true);
        }

        // commit.
        manager.notify(GeometricWeather.NOTIFICATION_ID_NORMALLY, builder.build());
    }

    private static RemoteViews buildBaseView(Context context, RemoteViews views,
                                             ResourceProvider helper, Weather weather,
                                             boolean dayTime, boolean fahrenheit,
                                             boolean minimalIcon, String textColor,
                                             boolean backgroundColor, int mainColor, int subColor) {
        views.setImageViewBitmap(
                R.id.notification_base_icon,
                drawableToBitmap(
                        WeatherHelper.getWidgetNotificationIcon(
                                helper, weather.realTime.weatherKind, dayTime, minimalIcon, textColor)
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

        if (backgroundColor) {
            views.setViewVisibility(R.id.notification_base_background, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.notification_base_background, View.GONE);
        }

        views.setTextColor(R.id.notification_base_realtimeTemp, mainColor);
        views.setTextColor(R.id.notification_base_dailyTemp, subColor);
        views.setTextColor(R.id.notification_base_weather, mainColor);
        views.setTextColor(R.id.notification_base_lunar, subColor);
        views.setTextColor(R.id.notification_base_time, subColor);

        return views;
    }

    private static RemoteViews buildBigView(Context context, RemoteViews views,
                                            ResourceProvider helper, Weather weather,
                                            boolean dayTime, boolean fahrenheit,
                                            boolean minimalIcon, String textColor,
                                            boolean backgroundColor, int mainColor, int subColor) {
        // today
        views = buildBaseView(
                context, views, helper, weather,
                dayTime, fahrenheit,
                minimalIcon, textColor,
                backgroundColor, mainColor, subColor);

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
        views.setImageViewBitmap( // set icon 1.
                R.id.notification_big_icon_1,
                drawableToBitmap(
                        WeatherHelper.getWidgetNotificationIcon(
                                helper,
                                dayTime
                                        ? weather.dailyList.get(0).weatherKinds[0]
                                        : weather.dailyList.get(0).weatherKinds[1],
                                dayTime, minimalIcon, textColor
                        )
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
        views.setImageViewBitmap( // set icon 2.
                R.id.notification_big_icon_2,
                drawableToBitmap(
                        WeatherHelper.getWidgetNotificationIcon( // get icon 2 resource id.
                                helper,
                                dayTime
                                        ? weather.dailyList.get(1).weatherKinds[0]
                                        : weather.dailyList.get(1).weatherKinds[1],
                                dayTime, minimalIcon, textColor
                        )
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
        views.setImageViewBitmap( // set icon 3.
                R.id.notification_big_icon_3,
                drawableToBitmap(
                        WeatherHelper.getWidgetNotificationIcon( // get icon 3 resource id.
                                helper,
                                dayTime
                                        ? weather.dailyList.get(2).weatherKinds[0]
                                        : weather.dailyList.get(2).weatherKinds[1],
                                dayTime, minimalIcon, textColor
                        )
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
        views.setImageViewBitmap( // set icon 4.
                R.id.notification_big_icon_4,
                drawableToBitmap(
                        WeatherHelper.getWidgetNotificationIcon( // get icon 4 resource id.
                                helper,
                                dayTime
                                        ? weather.dailyList.get(3).weatherKinds[0]
                                        : weather.dailyList.get(3).weatherKinds[1],
                                dayTime, minimalIcon, textColor
                        )
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
        views.setImageViewBitmap( // set icon 5.
                R.id.notification_big_icon_5,
                drawableToBitmap(
                        WeatherHelper.getWidgetNotificationIcon( // get icon 5 resource id.
                                helper,
                                dayTime
                                        ? weather.dailyList.get(4).weatherKinds[0]
                                        : weather.dailyList.get(4).weatherKinds[1],
                                dayTime, minimalIcon, textColor
                        )
                )
        );

        views.setViewVisibility(R.id.notification_base_background, View.GONE);
        if (backgroundColor) {
            views.setViewVisibility(R.id.notification_base_background, View.VISIBLE);
            views.setViewVisibility(R.id.notification_big_background, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.notification_base_background, View.GONE);
            views.setViewVisibility(R.id.notification_big_background, View.GONE);
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
