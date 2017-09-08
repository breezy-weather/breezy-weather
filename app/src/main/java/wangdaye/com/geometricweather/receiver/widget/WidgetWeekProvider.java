package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import wangdaye.com.geometricweather.service.PollingService;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;

/**
 * Widget week provider.
 * */

public class WidgetWeekProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ServiceHelper.startupService(context, PollingService.FORCE_REFRESH_TYPE_ALL, true);
    }
}

