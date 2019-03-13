package wangdaye.com.geometricweather.background.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import wangdaye.com.geometricweather.background.ServiceHelper;

/**
 * Widget clock day week provider.
 * */

public class WidgetClockDayWeekProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ServiceHelper.startAwakePollingUpdateService(context);
    }
}
