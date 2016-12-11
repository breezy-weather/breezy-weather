package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import wangdaye.com.geometricweather.service.widget.alarm.WidgetDayAlarmService;
import wangdaye.com.geometricweather.service.widget.job.WidgetDayJobService;
import wangdaye.com.geometricweather.utils.JobSchedulerUtils;

/**
 * Widget day provider.
 * */

public class WidgetDayProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.schedule(
                    context,
                    WidgetDayJobService.class,
                    WidgetDayJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetDayAlarmService.class));
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerUtils.cancel(context, WidgetDayJobService.SCHEDULE_CODE);
        } else {
            WidgetDayAlarmService.cancelAlarmIntent(
                    context,
                    WidgetDayAlarmService.class,
                    WidgetDayAlarmService.ALARM_CODE);
        }
    }
}
