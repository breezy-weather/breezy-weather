package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayAlarmService;
import wangdaye.com.geometricweather.service.widget.job.WidgetClockDayJobService;
import wangdaye.com.geometricweather.utils.JobSchedulerUtils;

/**
 * Widget clock day provider.
 * */

public class WidgetClockDayProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.schedule(
                    context,
                    WidgetClockDayJobService.class,
                    WidgetClockDayJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetClockDayAlarmService.class));
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.cancel(context, WidgetClockDayJobService.SCHEDULE_CODE);
        } else {
            WidgetClockDayAlarmService.cancelAlarmIntent(
                    context,
                    WidgetClockDayAlarmService.class,
                    WidgetClockDayAlarmService.ALARM_CODE);
        }
    }
}
