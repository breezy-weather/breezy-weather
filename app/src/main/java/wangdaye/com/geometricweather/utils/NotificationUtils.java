package wangdaye.com.geometricweather.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.service.notification.alarm.NotificationAlarmService;
import wangdaye.com.geometricweather.service.notification.alarm.TodayForecastAlarmService;
import wangdaye.com.geometricweather.service.notification.alarm.TomorrowForecastAlarmService;
import wangdaye.com.geometricweather.service.notification.job.NotificationJobService;
import wangdaye.com.geometricweather.service.notification.job.TodayForecastJobService;
import wangdaye.com.geometricweather.service.notification.job.TomorrowForecastJobService;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.view.activity.MainActivity;

/**
 * Notification utils.
 * */

public class NotificationUtils {
    // data
    public static final int NOTIFICATION_ID = 7;
    public static final int FORECAST_ID = 9;

    /** <br> options. */

    public static void refreshNotification(final Context c, final Location location) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
                if(sharedPreferences.getBoolean(c.getString(R.string.key_notification), false)) {
                    buildNotificationAndSendIt(c, location.weather);
                }
            }
        }).start();
    }

    public static void startupAllOfNotificationService(final Context c) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
                if (sharedPreferences.getBoolean(c.getString(R.string.key_notification), false)) {
                    NotificationUtils.startNotificationService(c);
                }
                if (sharedPreferences.getBoolean(c.getString(R.string.key_forecast_today), false)) {
                    NotificationUtils.startTodayForecastService(c);
                }
                if (sharedPreferences.getBoolean(c.getString(R.string.key_forecast_today), false)) {
                    NotificationUtils.startTomorrowForecastService(c);
                }
            }
        });
    }

    public static void startNotificationService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.schedule(
                    context,
                    NotificationJobService.class,
                    NotificationJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, NotificationAlarmService.class));
        }
    }

    public static void stopNotificationService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.cancel(context, NotificationJobService.SCHEDULE_CODE);
        } else {
            NotificationAlarmService.cancelAlarmIntent(
                    context,
                    NotificationAlarmService.class,
                    NotificationAlarmService.ALARM_CODE);
        }
    }

    public static void startTodayForecastService(Context context) {
        stopTodayForecastService(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.scheduleForecastMission(
                    context,
                    TodayForecastJobService.class,
                    TodayForecastJobService.SCHEDULE_CODE,
                    true);
        } else {
            context.startService(new Intent(context, TodayForecastAlarmService.class));
        }
    }

    public static void stopTodayForecastService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.cancel(context, TodayForecastJobService.SCHEDULE_CODE);
        } else {
            NotificationAlarmService.cancelAlarmIntent(
                    context,
                    TodayForecastAlarmService.class,
                    TodayForecastAlarmService.ALARM_CODE);
        }
    }

    public static void startTomorrowForecastService(Context context) {
        stopTomorrowForecastService(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.scheduleForecastMission(
                    context,
                    TomorrowForecastJobService.class,
                    TomorrowForecastJobService.SCHEDULE_CODE,
                    false);
        } else {
            context.startService(new Intent(context, TomorrowForecastAlarmService.class));
        }
    }

    public static void stopTomorrowForecastService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.cancel(context, TomorrowForecastJobService.SCHEDULE_CODE);
        } else {
            NotificationAlarmService.cancelAlarmIntent(
                    context,
                    TomorrowForecastAlarmService.class,
                    TomorrowForecastAlarmService.ALARM_CODE);
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
        if (NotificationUtils.isForecastTime(context, true)) {
            if (doNow) {
                duration = 0;
            } else {
                duration = 24 * 60 * 60 * 1000 + (settingsTime - realTime) * 60 * 1000;
            }
        } else if (realTime < settingsTime) {
            duration = (settingsTime - realTime) * 60 * 1000;
        } else if (realTime > settingsTime) {
            duration = 24 * 60 * 60 * 1000 + (settingsTime - realTime);
        }

        return duration;
    }

    public static boolean isForecastTime(Context context, boolean today) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Calendar calendar = Calendar.getInstance();
        int realTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 *60 * 1000
                + calendar.get(Calendar.MINUTE) * 60 * 1000
                + calendar.get(Calendar.SECOND) * 1000;

        if (today) {
            String[] times = sharedPreferences.getString(context.getString(R.string.key_forecast_today_time), "07:00").split(":");
            int settingsTime = Integer.parseInt(times[0]) * 60 * 60 * 1000
                    + Integer.parseInt(times[1]) * 60 * 1000;
            return Math.abs(realTime - settingsTime) <= 60 * 1000;
        } else {
            String[] times = sharedPreferences.getString(context.getString(R.string.key_forecast_tomorrow_time), "21:00").split(":");
            int settingsTime = Integer.parseInt(times[0]) * 60 * 60 * 1000
                    + Integer.parseInt(times[1]) * 60 * 1000;
            return Math.abs(realTime - settingsTime) <= 60 * 1000;
        }
    }

    /** <br> UI. */

    public static void buildNotificationAndSendIt(Context context, Weather weather) {
        if (weather == null) {
            return;
        }

        // get sp & weather.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // get time & background color.
        boolean isDay = TimeUtils.getInstance(context).getDayTime(context, weather, false).isDayTime();
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
        builder.setSmallIcon(
                WeatherHelper.getMiniWeatherIcon(weather.live.weatherKind, isDay));

        // build base view.
        RemoteViews base = new RemoteViews(context.getPackageName(), R.layout.notification_base);
        int[] imageId = WeatherHelper.getWeatherIcon(weather.live.weatherKind, isDay);
        base.setImageViewResource( // set icon.
                R.id.notification_base_icon,
                imageId[3]);
        base.setTextViewText( // set title.
                R.id.notification_base_title,
                weather.live.weather + " " + weather.live.temp + "℃");
        base.setTextViewText( // set content.
                R.id.notification_base_content,
                weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°");
        base.setTextViewText( // set time.
                R.id.notification_base_time, weather.base.location + "." + weather.base.refreshTime);
        if (backgroundColor) { // set background.
            base.setViewVisibility(R.id.notification_base_background, View.VISIBLE);
        } else {
            base.setViewVisibility(R.id.notification_base_background, View.GONE);
        }
        // set text color.
        base.setTextColor(R.id.notification_base_title, mainColor);
        base.setTextColor(R.id.notification_base_content, subColor);
        base.setTextColor(R.id.notification_base_time, subColor);
        builder.setContent(base); // commit.
        // set click intent.
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // build big view.
        RemoteViews big = new RemoteViews(context.getPackageName(), R.layout.notification_big);
        // today
        imageId = WeatherHelper.getWeatherIcon(weather.live.weatherKind, isDay);
        big.setImageViewResource( // set icon.
                R.id.notification_base_icon,
                imageId[3]);
        big.setTextViewText( // set title.
                R.id.notification_base_title,
                weather.live.weather + " " + weather.live.temp + "℃");
        big.setTextViewText( // set content.
                R.id.notification_base_content,
                weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°");
        big.setTextViewText( // set time.
                R.id.notification_base_time, weather.base.location + "." + weather.base.refreshTime);
        big.setViewVisibility(R.id.notification_base_background, View.GONE);
        // 1
        big.setTextViewText( // set week 1.
                R.id.notification_big_week_1,
                context.getString(R.string.today));
        big.setTextViewText( // set temps 1.
                R.id.notification_big_temp_1,
                weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°");
        imageId = WeatherHelper.getWeatherIcon( // get icon 1 resource id.
                isDay ? weather.dailyList.get(0).weatherKinds[0] : weather.dailyList.get(0).weatherKinds[1],
                isDay);
        big.setImageViewResource( // set icon 1.
                R.id.notification_big_icon_1,
                imageId[3]);
        // 2
        big.setTextViewText( // set week 2.
                R.id.notification_big_week_2,
                weather.dailyList.get(1).week);
        big.setTextViewText( // set temps 2.
                R.id.notification_big_temp_2,
                weather.dailyList.get(1).temps[1] + "/" + weather.dailyList.get(1).temps[0] + "°");
        imageId = WeatherHelper.getWeatherIcon( // get icon 2 resource id.
                isDay ? weather.dailyList.get(1).weatherKinds[0] : weather.dailyList.get(1).weatherKinds[1],
                isDay);
        big.setImageViewResource( // set icon 2.
                R.id.notification_big_icon_2,
                imageId[3]);
        // 3
        big.setTextViewText( // set week 3.
                R.id.notification_big_week_3,
                weather.dailyList.get(2).week);
        big.setTextViewText( // set temps 3.
                R.id.notification_big_temp_3,
                weather.dailyList.get(2).temps[1] + "/" + weather.dailyList.get(2).temps[0] + "°");
        imageId = WeatherHelper.getWeatherIcon( // get icon 3 resource id.
                isDay ? weather.dailyList.get(2).weatherKinds[0] : weather.dailyList.get(2).weatherKinds[1],
                isDay);
        big.setImageViewResource( // set icon 3.
                R.id.notification_big_icon_3,
                imageId[3]);
        // 4
        big.setTextViewText( // set week 4.
                R.id.notification_big_week_4,
                weather.dailyList.get(3).week);
        big.setTextViewText( // set temps 4.
                R.id.notification_big_temp_4,
                weather.dailyList.get(3).temps[1] + "/" + weather.dailyList.get(3).temps[0] + "°");
        imageId = WeatherHelper.getWeatherIcon( // get icon 4 resource id.
                isDay ? weather.dailyList.get(3).weatherKinds[0] : weather.dailyList.get(3).weatherKinds[1],
                isDay);
        big.setImageViewResource( // set icon 4.
                R.id.notification_big_icon_4,
                imageId[3]);
        // 5
        big.setTextViewText( // set week 5.
                R.id.notification_big_week_5,
                weather.dailyList.get(4).week);
        big.setTextViewText( // set temps 5.
                R.id.notification_big_temp_5,
                weather.dailyList.get(4).temps[1] + "/" + weather.dailyList.get(4).temps[0] + "°");
        imageId = WeatherHelper.getWeatherIcon( // get icon 5 resource id.
                isDay ? weather.dailyList.get(4).weatherKinds[0] : weather.dailyList.get(4).weatherKinds[1],
                isDay);
        big.setImageViewResource( // set icon 5.
                R.id.notification_big_icon_5,
                imageId[3]);
        // set text color.
        big.setTextColor(R.id.notification_base_title, mainColor);
        big.setTextColor(R.id.notification_base_content, subColor);
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
        // set background.
        big.setViewVisibility(R.id.notification_base_background, View.GONE);
        if (backgroundColor) {
            big.setViewVisibility(R.id.notification_base_background, View.VISIBLE);
            big.setViewVisibility(R.id.notification_big_background, View.VISIBLE);
        } else {
            big.setViewVisibility(R.id.notification_base_background, View.GONE);
            big.setViewVisibility(R.id.notification_big_background, View.GONE);
        }

        // set big view.
        builder.setCustomBigContentView(big);

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

    public static void buildForecastAndSendIt(Context context, Weather weather, boolean today) {
        // get sp & weather.
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
            titles[0] = context.getString(R.string.day)
                    + " - " + weather.dailyList.get(0).weathers[0] + " " + weather.dailyList.get(0).temps[1] + "℃";
            titles[1] = context.getString(R.string.night)
                    + " - " + weather.dailyList.get(0).weathers[1] + " " + weather.dailyList.get(0).temps[0] + "℃";
        } else {
            titles[0] = context.getString(R.string.today)
                    + " - " + weather.dailyList.get(0).weathers[0]
                    + " " + weather.dailyList.get(0).temps[0] + "/" + weather.dailyList.get(0).temps[1] + "°";
            titles[1] = context.getString(R.string.tomorrow)
                    + " - " + weather.dailyList.get(1).weathers[0]
                    + " " + weather.dailyList.get(0).temps[0] + "/" + weather.dailyList.get(0).temps[1] + "°";
        }
        view.setTextViewText(
                R.id.notification_forecast_title_1, titles[0]);
        view.setTextViewText(
                R.id.notification_forecast_title_2, titles[1]);
        // set content.
        String[] contents = new String[2];
        if (today) {
            contents[0] = weather.dailyList.get(0).windDirs[0]
                    + " " + weather.dailyList.get(0).windLevels[0];
            contents[1] = weather.dailyList.get(0).windDirs[1]
                    + " " + weather.dailyList.get(0).windLevels[1];
        } else {
            contents[0] = weather.dailyList.get(0).windDirs[0]
                    + " " + weather.dailyList.get(0).windLevels[0];
            contents[1] = weather.dailyList.get(1).windDirs[0]
                    + " " + weather.dailyList.get(1).windLevels[0];
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
                weather.base.location + "." + weather.base.refreshTime);
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
        // set click intent.
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // set sound & vibrate.
        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        // set clean flag.
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        // commit.
        notificationManager.notify(FORECAST_ID, notification);
    }
}
