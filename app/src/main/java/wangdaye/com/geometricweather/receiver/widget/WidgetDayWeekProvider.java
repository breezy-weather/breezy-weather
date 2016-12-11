package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import wangdaye.com.geometricweather.service.widget.alarm.WidgetDayWeekAlarmService;
import wangdaye.com.geometricweather.service.widget.job.WidgetDayWeekJobService;
import wangdaye.com.geometricweather.utils.JobSchedulerUtils;

/**
 * Widget day week provider.
 * */

public class WidgetDayWeekProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.schedule(
                    context,
                    WidgetDayWeekJobService.class,
                    WidgetDayWeekJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetDayWeekAlarmService.class));
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.cancel(context, WidgetDayWeekJobService.SCHEDULE_CODE);
        } else {
            WidgetDayWeekAlarmService.cancelAlarmIntent(
                    context,
                    WidgetDayWeekAlarmService.class,
                    WidgetDayWeekAlarmService.ALARM_CODE);
        }
    }
}