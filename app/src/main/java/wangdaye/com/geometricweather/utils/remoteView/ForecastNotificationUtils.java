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
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/** <br> forecast notification utils. */

public class ForecastNotificationUtils {
    // data
    private static final int NOTIFICATION_ID = 318;

    /** <br> UI. */

    public static void buildForecastAndSendIt(Context context, Weather weather, boolean today) {
        // get sp & realTimeWeather.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // get time & background color.
        boolean backgroundColor = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_background),
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
        if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_icon), false)) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        // set notification visibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_in_lockScreen), false)) {
                builder.setVisibility(Notification.VISIBILITY_SECRET);
            } else {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
        }

        // set small icon.
        builder.setSmallIcon(WeatherHelper.getMiniWeatherIcon(
                today ? weather.dailyList.get(0).weatherKinds[0] : weather.dailyList.get(1).weatherKinds[0],
                true));

        // set view
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.notification_forecast);
        int[][] imageIds = new int[2][3];
        if (today) {
            imageIds[0] = WeatherHelper.getWeatherIcon(weather.dailyList.get(0).weatherKinds[0], true);
            imageIds[1] = WeatherHelper.getWeatherIcon(weather.dailyList.get(0).weatherKinds[1], false);
        } else {
            imageIds[0] = WeatherHelper.getWeatherIcon(weather.dailyList.get(0).weatherKinds[0], true);
            imageIds[1] = WeatherHelper.getWeatherIcon(weather.dailyList.get(1).weatherKinds[0], true);
        }
        // set icon.
        view.setImageViewResource(
                R.id.notification_forecast_icon_1,
                imageIds[0][3]);
        view.setImageViewResource(
                R.id.notification_forecast_icon_2,
                imageIds[1][3]);
        // set title.
        String[] titles = new String[2];
        if (today) {
            titles[0] = context.getString(R.string.day) + " " + weather.dailyList.get(0).weathers[0];
            titles[1] = context.getString(R.string.night) + " " + weather.dailyList.get(0).weathers[1];
        } else {
            titles[0] = context.getString(R.string.today) + " " + weather.dailyList.get(0).weathers[0];
            titles[1] = context.getString(R.string.tomorrow) + " " + weather.dailyList.get(1).weathers[0];
        }
        view.setTextViewText(
                R.id.notification_forecast_title_1, titles[0]);
        view.setTextViewText(
                R.id.notification_forecast_title_2, titles[1]);
        // set content.
        String[] contents = new String[2];
        if (today) {
            contents[0] = weather.dailyList.get(0).temps[0] + "℃";
            contents[1] = weather.dailyList.get(0).temps[1] + "℃";
        } else {
            contents[0] = weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°";
            contents[1] = weather.dailyList.get(1).temps[1] + "/" + weather.dailyList.get(1).temps[0] + "°";
        }
        view.setTextViewText(
                R.id.notification_forecast_content_1,
                contents[0]);
        view.setTextViewText(
                R.id.notification_forecast_content_2,
                contents[1]);
        // set time.
        view.setTextViewText(
                R.id.notification_forecast_time,
                weather.base.city + "." + weather.base.time);
        // set background.
        if (backgroundColor) {
            view.setViewVisibility(R.id.notification_forecast_background, View.VISIBLE);
        } else {
            view.setViewVisibility(R.id.notification_forecast_background, View.GONE);
        }
        // set text color.
        view.setTextColor(R.id.notification_forecast_title_1, mainColor);
        view.setTextColor(R.id.notification_forecast_title_2, mainColor);
        view.setTextColor(R.id.notification_forecast_content_1, subColor);
        view.setTextColor(R.id.notification_forecast_content_2, subColor);
        view.setTextColor(R.id.notification_forecast_time, subColor);
        builder.setContent(view);
        // set intent.
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, IntentHelper.buildMainActivityIntent(context, null), 0);
        builder.setContentIntent(pendingIntent);

        // set sound & vibrate.
        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        // set clean flag.
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        // commit.
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /** <br> data. */

    public static boolean isEnable(Context context, boolean today) {
        if (today) {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(
                            context.getString(R.string.key_forecast_today),
                            false);
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(
                            context.getString(R.string.key_forecast_tomorrow),
                            false);
        }
    }

    public static long calcForecastDuration(Context context, boolean today, boolean doNow) {
        Calendar calendar = Calendar.getInstance();
        int realTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 *60 * 1000
                + calendar.get(Calendar.MINUTE) * 60 * 1000
                + calendar.get(Calendar.SECOND) * 1000;

        int settingsTime;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (today) {
            String[] times = sharedPreferences.getString(context.getString(R.string.key_forecast_today_time), "07:00").split(":");
            settingsTime = Integer.parseInt(times[0]) * 60 * 60 * 1000
                    + Integer.parseInt(times[1]) * 60 * 1000;
        } else {
            String[] times = sharedPreferences.getString(context.getString(R.string.key_forecast_tomorrow_time), "21:00").split(":");
            settingsTime = Integer.parseInt(times[0]) * 60 * 60 * 1000
                    + Integer.parseInt(times[1]) * 60 * 1000;
        }

        long duration = 0;
        if (isForecastTime(context, today)) {
            if (doNow) {
                duration = 0;
            } else {
                duration = 24 * 60 * 60 * 1000 + (settingsTime - realTime);
            }
        } else if (realTime < settingsTime) {
            duration = settingsTime - realTime;
        } else if (realTime > settingsTime) {
            duration = 24 * 60 * 60 * 1000 + (settingsTime - realTime);
        }

        return duration;
    }

    public static boolean isForecastTime(Context context, boolean today) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        if (today) {
            String[] times = sharedPreferences.getString(context.getString(R.string.key_forecast_today_time), "07:00").split(":");
            return Integer.parseInt(times[0]) == hour && Integer.parseInt(times[1]) == min;
        } else {
            String[] times = sharedPreferences.getString(context.getString(R.string.key_forecast_tomorrow_time), "21:00").split(":");
            return Integer.parseInt(times[0]) == hour && Integer.parseInt(times[1]) == min;
        }
    }
}
