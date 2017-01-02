package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import wangdaye.com.geometricweather.basic.GeoAlarmService;
import wangdaye.com.geometricweather.basic.GeoJobService;
import wangdaye.com.geometricweather.service.alarm.PollingAlarmService;
import wangdaye.com.geometricweather.service.alarm.TodayForecastAlarmService;
import wangdaye.com.geometricweather.service.alarm.TomorrowForecastAlarmService;
import wangdaye.com.geometricweather.service.job.PollingJobService;
import wangdaye.com.geometricweather.service.job.TodayForecastJobService;
import wangdaye.com.geometricweather.service.job.TomorrowForecastJobService;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;
import wangdaye.com.geometricweather.utils.remoteView.NormalNotificationUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayCenterUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayWeekUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetDayPixelUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetDayUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetDayWeekUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetWeekUtils;

/**
 * Service helper.
 * */

public class ServiceHelper {

    /** <br> total. */

    public static void startupAllService(Context context) {
        if (WidgetDayUtils.isEnable(context)
                || WidgetDayPixelUtils.isEnable(context)
                || WidgetWeekUtils.isEnable(context)
                || WidgetDayWeekUtils.isEnable(context)
                || WidgetClockDayUtils.isEnable(context)
                || WidgetClockDayCenterUtils.isEnable(context)
                || WidgetClockDayWeekUtils.isEnable(context)
                || NormalNotificationUtils.isEnable(context)) {

            startPollingService(context);
        }

        if (ForecastNotificationUtils.isEnable(context, true)) {
            startForecastService(context, true);
        }
        if (ForecastNotificationUtils.isEnable(context, false)) {
            startForecastService(context, false);
        }
    }

    /** <br> polling service. */

    public static void startPollingService(Context context) {
        stopPollingService(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(context, PollingAlarmService.class);
            context.startService(intent);
        } else {
            GeoJobService.scheduleCycleJob(
                    context,
                    PollingJobService.class,
                    PollingJobService.SCHEDULE_CODE);
        }
    }

    public static void stopPollingService(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            GeoAlarmService.cancelAlarmIntent(
                    context,
                    PollingAlarmService.class,
                    PollingAlarmService.ALARM_CODE);
        } else {
            GeoJobService.cancel(context, PollingJobService.SCHEDULE_CODE);
        }
    }

    public static boolean isNeedShutdownPollingService(Context context) {
        return !WidgetDayUtils.isEnable(context)
                && !WidgetDayPixelUtils.isEnable(context)
                && !WidgetWeekUtils.isEnable(context)
                && !WidgetDayWeekUtils.isEnable(context)
                && !WidgetClockDayUtils.isEnable(context)
                && !WidgetClockDayCenterUtils.isEnable(context)
                && !WidgetClockDayWeekUtils.isEnable(context)
                && !NormalNotificationUtils.isEnable(context);
    }

    /** <br> forecast. */

    public static void startForecastService(Context context, boolean today) {
        stopForecastService(context, today);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(
                    context,
                    today ? TodayForecastAlarmService.class : TomorrowForecastAlarmService.class);
            context.startService(intent);
        } else {
            GeoJobService.scheduleDelayJob(
                    context,
                    today ? TodayForecastJobService.class : TomorrowForecastJobService.class,
                    today ? TodayForecastJobService.SCHEDULE_CODE : TomorrowForecastJobService.SCHEDULE_CODE,
                    today);
        }
    }

    public static void stopForecastService(Context context, boolean today) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            GeoAlarmService.cancelAlarmIntent(
                    context,
                    today ? TodayForecastAlarmService.class : TomorrowForecastAlarmService.class,
                    today ? TodayForecastAlarmService.ALARM_CODE : TomorrowForecastAlarmService.ALARM_CODE);
        } else {
            GeoJobService.cancel(
                    context,
                    today ? TodayForecastJobService.SCHEDULE_CODE : TomorrowForecastJobService.SCHEDULE_CODE);
        }
    }
}
