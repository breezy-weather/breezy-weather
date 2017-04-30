package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;

/**
 * Widget clock day week provider.
 * */

public class WidgetClockDayWeekProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ServiceHelper.startupService(context, false);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (!ServiceHelper.hasNormalView(context)) {
            ServiceHelper.stopNormalService(context, false);
        }
    }
}
