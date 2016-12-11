package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayCenterAlarmService;
import wangdaye.com.geometricweather.service.widget.job.WidgetClockDayCenterJobService;
import wangdaye.com.geometricweather.utils.JobSchedulerUtils;

/**
 * Widget clock day center provider.
 * */

public class WidgetClockDayCenterProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.schedule(
                    context,
                    WidgetClockDayCenterJobService.class,
                    WidgetClockDayCenterJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetClockDayCenterAlarmService.class));
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.cancel(context, WidgetClockDayCenterJobService.SCHEDULE_CODE);
        } else {
            WidgetClockDayCenterAlarmService.cancelAlarmIntent(
                    context,
                    WidgetClockDayCenterAlarmService.class,
                    WidgetClockDayCenterAlarmService.ALARM_CODE);
        }
    }
}
