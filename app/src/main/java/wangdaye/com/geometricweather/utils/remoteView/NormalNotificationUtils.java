package wangdaye.com.geometricweather.utils.remoteView;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Normal notification utils.
 * */

public class NormalNotificationUtils {

    private static final int NOTIFICATION_ID = 317;

    public static void buildNotificationAndSendIt(Context context, Weather weather) {
        if (weather == null) {
            return;
        }

        // get sp & realTimeWeather.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean fahrenheit = sharedPreferences.getBoolean(context.getString(R.string.key_fahrenheit), false);

        // get time & background color.
        boolean dayTime = TimeManager.getInstance(context).getDayTime(context, weather, false).isDayTime();
        String iconStyle = sharedPreferences.getString(
                context.getString(R.string.key_notification_icon_style),
                "material");
        boolean tempIcon = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_temp_icon),
                false);
        boolean backgroundColor = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_background),
                false);
        boolean hideBigView = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_hide_big_view),
                false);

        // get text color.
        String textColor = sharedPreferences.getString(
                context.getString(R.string.key_notification_text_color),
                "grey");
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

        // get manager & builder.
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // set notification level.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_icon), false)) {
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
                tempIcon ?
                        ValueUtils.getTempIconId(
                                fahrenheit ?
                                        ValueUtils.calcFahrenheit(weather.realTime.temp)
                                        :
                                        weather.realTime.temp)
                        :
                        WeatherHelper.getMiniWeatherIcon(weather.realTime.weatherKind, dayTime));

        // buildWeather base view.
        RemoteViews base = new RemoteViews(context.getPackageName(), R.layout.notification_base);

        int imageId = WeatherHelper.getWidgetNotificationIcon(
                weather.realTime.weatherKind, dayTime, iconStyle, textColor);
        base.setImageViewResource(
                R.id.notification_base_icon,
                imageId);

        base.setTextViewText(
                R.id.notification_base_realtimeTemp,
                ValueUtils.buildAbbreviatedCurrentTemp(weather.realTime.temp, fahrenheit));

        base.setTextViewText(
                R.id.notification_base_dailyTemp,
                ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit));

        if (weather.aqi != null && !TextUtils.isEmpty(weather.aqi.aqi)) {
            base.setTextViewText(
                    R.id.notification_base_aqi_wind,
                    "AQI " + weather.aqi.aqi);
            int colorRes = WeatherHelper.getAqiColorResId(Integer.parseInt(weather.aqi.aqi));
            if (colorRes == 0) {
                base.setTextColor(R.id.notification_base_aqi_wind, subColor);
            } else {
                base.setTextColor(
                        R.id.notification_base_aqi_wind,
                        ContextCompat.getColor(context, colorRes));
            }
        } else {
            base.setTextViewText(
                    R.id.notification_base_aqi_wind,
                    weather.realTime.windLevel);
            int colorRes = WeatherHelper.getWindColorResId(weather.realTime.windSpeed);
            if (colorRes == 0) {
                base.setTextColor(R.id.notification_base_aqi_wind, subColor);
            } else {
                base.setTextColor(
                        R.id.notification_base_aqi_wind,
                        ContextCompat.getColor(context, colorRes));
            }
        }

        base.setTextViewText(
                R.id.notification_base_weather,
                weather.realTime.weather);

        base.setTextViewText(
                R.id.notification_base_time, weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);

        if (backgroundColor) {
            base.setViewVisibility(R.id.notification_base_background, View.VISIBLE);
        } else {
            base.setViewVisibility(R.id.notification_base_background, View.GONE);
        }

        base.setTextColor(R.id.notification_base_realtimeTemp, mainColor);
        base.setTextColor(R.id.notification_base_dailyTemp, subColor);
        base.setTextColor(R.id.notification_base_weather, mainColor);
        base.setTextColor(R.id.notification_base_time, subColor);

        builder.setContent(base);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, IntentHelper.buildMainActivityIntent(context, null), 0);
        builder.setContentIntent(pendingIntent);

        if (!hideBigView) {
            // buildWeather big view.
            RemoteViews big = new RemoteViews(context.getPackageName(), R.layout.notification_big);

            // today
            imageId = WeatherHelper.getWidgetNotificationIcon(
                    weather.realTime.weatherKind, dayTime, iconStyle, textColor);
            big.setImageViewResource(
                    R.id.notification_base_icon,
                    imageId);

            big.setTextViewText(
                    R.id.notification_base_realtimeTemp,
                    ValueUtils.buildAbbreviatedCurrentTemp(weather.realTime.temp, fahrenheit));

            big.setTextViewText(
                    R.id.notification_base_dailyTemp,
                    ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, true, fahrenheit));

            if (weather.aqi != null && !TextUtils.isEmpty(weather.aqi.aqi)) {
                big.setTextViewText(
                        R.id.notification_base_aqi_wind,
                        "AQI " + weather.aqi.aqi);
                int colorRes = WeatherHelper.getAqiColorResId(Integer.parseInt(weather.aqi.aqi));
                if (colorRes == 0) {
                    big.setTextColor(R.id.notification_base_aqi_wind, subColor);
                } else {
                    big.setTextColor(
                            R.id.notification_base_aqi_wind,
                            ContextCompat.getColor(context, colorRes));
                }
            } else {
                big.setTextViewText(
                        R.id.notification_base_aqi_wind,
                        weather.realTime.windLevel);
                int colorRes = WeatherHelper.getWindColorResId(weather.realTime.windSpeed);
                if (colorRes == 0) {
                    big.setTextColor(R.id.notification_base_aqi_wind, subColor);
                } else {
                    big.setTextColor(
                            R.id.notification_base_aqi_wind,
                            ContextCompat.getColor(context, colorRes));
                }
            }

            big.setTextViewText(
                    R.id.notification_base_weather,
                    weather.realTime.weather);

            big.setTextViewText(
                    R.id.notification_base_time, weather.base.city + " " + weather.dailyList.get(0).week + " " + weather.base.time);

            big.setViewVisibility(R.id.notification_base_background, View.GONE);

            // weekly.

            // 1
            big.setTextViewText( // set week 1.
                    R.id.notification_big_week_1,
                    context.getString(R.string.today));
            big.setTextViewText( // set temps 1.
                    R.id.notification_big_temp_1,
                    ValueUtils.buildDailyTemp(weather.dailyList.get(0).temps, false, fahrenheit));
            imageId = WeatherHelper.getWidgetNotificationIcon( // get icon 1 resource id.
                    dayTime ? weather.dailyList.get(0).weatherKinds[0] : weather.dailyList.get(0).weatherKinds[1],
                    dayTime, iconStyle, textColor);
            big.setImageViewResource( // set icon 1.
                    R.id.notification_big_icon_1,
                    imageId);
            // 2
            big.setTextViewText( // set week 2.
                    R.id.notification_big_week_2,
                    weather.dailyList.get(1).week);
            big.setTextViewText( // set temps 2.
                    R.id.notification_big_temp_2,
                    ValueUtils.buildDailyTemp(weather.dailyList.get(1).temps, false, fahrenheit));
            imageId = WeatherHelper.getWidgetNotificationIcon( // get icon 2 resource id.
                    dayTime ? weather.dailyList.get(1).weatherKinds[0] : weather.dailyList.get(1).weatherKinds[1],
                    dayTime, iconStyle, textColor);
            big.setImageViewResource( // set icon 2.
                    R.id.notification_big_icon_2,
                    imageId);
            // 3
            big.setTextViewText( // set week 3.
                    R.id.notification_big_week_3,
                    weather.dailyList.get(2).week);
            big.setTextViewText( // set temps 3.
                    R.id.notification_big_temp_3,
                    ValueUtils.buildDailyTemp(weather.dailyList.get(2).temps, false, fahrenheit));
            imageId = WeatherHelper.getWidgetNotificationIcon( // get icon 3 resource id.
                    dayTime ? weather.dailyList.get(2).weatherKinds[0] : weather.dailyList.get(2).weatherKinds[1],
                    dayTime, iconStyle, textColor);
            big.setImageViewResource( // set icon 3.
                    R.id.notification_big_icon_3,
                    imageId);
            // 4
            big.setTextViewText( // set week 4.
                    R.id.notification_big_week_4,
                    weather.dailyList.get(3).week);
            big.setTextViewText( // set temps 4.
                    R.id.notification_big_temp_4,
                    ValueUtils.buildDailyTemp(weather.dailyList.get(3).temps, false, fahrenheit));
            imageId = WeatherHelper.getWidgetNotificationIcon( // get icon 4 resource id.
                    dayTime ? weather.dailyList.get(3).weatherKinds[0] : weather.dailyList.get(3).weatherKinds[1],
                    dayTime, iconStyle, textColor);
            big.setImageViewResource( // set icon 4.
                    R.id.notification_big_icon_4,
                    imageId);
            // 5
            big.setTextViewText( // set week 5.
                    R.id.notification_big_week_5,
                    weather.dailyList.get(4).week);
            big.setTextViewText( // set temps 5.
                    R.id.notification_big_temp_5,
                    ValueUtils.buildDailyTemp(weather.dailyList.get(4).temps, false, fahrenheit));
            imageId = WeatherHelper.getWidgetNotificationIcon( // get icon 5 resource id.
                    dayTime ? weather.dailyList.get(4).weatherKinds[0] : weather.dailyList.get(4).weatherKinds[1],
                    dayTime, iconStyle, textColor);
            big.setImageViewResource( // set icon 5.
                    R.id.notification_big_icon_5,
                    imageId);

            big.setViewVisibility(R.id.notification_base_background, View.GONE);
            if (backgroundColor) {
                big.setViewVisibility(R.id.notification_base_background, View.VISIBLE);
                big.setViewVisibility(R.id.notification_big_background, View.VISIBLE);
            } else {
                big.setViewVisibility(R.id.notification_base_background, View.GONE);
                big.setViewVisibility(R.id.notification_big_background, View.GONE);
            }

            big.setTextColor(R.id.notification_base_realtimeTemp, mainColor);
            big.setTextColor(R.id.notification_base_dailyTemp, subColor);
            big.setTextColor(R.id.notification_base_weather, mainColor);
            big.setTextColor(R.id.notification_base_time, subColor);
            big.setTextColor(R.id.notification_big_week_1, subColor);
            big.setTextColor(R.id.notification_big_week_2, subColor);
            big.setTextColor(R.id.notification_big_week_3, subColor);
            big.setTextColor(R.id.notification_big_week_4, subColor);
            big.setTextColor(R.id.notification_big_week_5, subColor);
            big.setTextColor(R.id.notification_big_temp_1, subColor);
            big.setTextColor(R.id.notification_big_temp_2, subColor);
            big.setTextColor(R.id.notification_big_temp_3, subColor);
            big.setTextColor(R.id.notification_big_temp_4, subColor);
            big.setTextColor(R.id.notification_big_temp_5, subColor);

            // set big view.
            builder.setCustomBigContentView(big);
        }

        // get notification.
        Notification notification = builder.build();

        // set clear flag
        if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_can_be_cleared), false)) {
            // the notification can be cleared
            notification.flags = Notification.FLAG_AUTO_CANCEL;
        } else {
            // the notification can not be cleared
            notification.flags = Notification.FLAG_ONGOING_EVENT;
        }

        // commit.
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public static void cancelNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }

    public static boolean isEnable(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(
                        context.getString(R.string.key_notification),
                        false);
    }
}
