package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;

/**
 * Widget clock day center provider.
 * */

public class WidgetClockDayCenterProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ServiceHelper.startPollingService(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (ServiceHelper.isNeedShutdownPollingService(context)) {
            ServiceHelper.stopPollingService(context);
        }
    }
}
