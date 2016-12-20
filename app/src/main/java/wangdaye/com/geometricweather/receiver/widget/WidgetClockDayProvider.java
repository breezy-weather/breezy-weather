package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayAlarmService;
import wangdaye.com.geometricweather.service.widget.job.WidgetClockDayJobService;
import wangdaye.com.geometricweather.utils.JobScheduleUtils;
import wangdaye.com.geometricweather.utils.WidgetUtils;

/**
 * Widget clock day provider.
 * */

public class WidgetClockDayProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        WidgetUtils.startClockDayWidgetService(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        WidgetUtils.stopClockDayWidgetService(context);
    }
}
