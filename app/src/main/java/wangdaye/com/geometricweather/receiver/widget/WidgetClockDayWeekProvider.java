package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayWeekAlarmService;
import wangdaye.com.geometricweather.service.widget.job.WidgetClockDayWeekJobService;
import wangdaye.com.geometricweather.utils.JobSchedulerUtils;

/**
 * Widget clock day week provider.
 * */

public class WidgetClockDayWeekProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.schedule(
                    context,
                    WidgetClockDayWeekJobService.class,
                    WidgetClockDayWeekJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetClockDayWeekAlarmService.class));
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.cancel(context, WidgetClockDayWeekJobService.SCHEDULE_CODE);
        } else {
            WidgetClockDayWeekAlarmService.cancelAlarmIntent(
                    context,
                    WidgetClockDayWeekAlarmService.class,
                    WidgetClockDayWeekAlarmService.ALARM_CODE);
        }
    }
}
