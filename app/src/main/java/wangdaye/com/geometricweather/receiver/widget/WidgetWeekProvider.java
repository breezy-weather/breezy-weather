package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import wangdaye.com.geometricweather.service.widget.alarm.WidgetWeekAlarmService;
import wangdaye.com.geometricweather.service.widget.job.WidgetWeekJobService;
import wangdaye.com.geometricweather.utils.JobSchedulerUtils;

/**
 * Widget week provider.
 * */

public class WidgetWeekProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.schedule(
                    context,
                    WidgetWeekJobService.class,
                    WidgetWeekJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetWeekAlarmService.class));
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.cancel(context, WidgetWeekJobService.SCHEDULE_CODE);
        } else {
            WidgetWeekAlarmService.cancelAlarmIntent(
                    context,
                    WidgetWeekAlarmService.class,
                    WidgetWeekAlarmService.ALARM_CODE);
        }
    }
}

