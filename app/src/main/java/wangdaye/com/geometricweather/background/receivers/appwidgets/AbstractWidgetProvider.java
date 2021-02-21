package wangdaye.com.geometricweather.background.receivers.appwidgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;

/**
 * Abstract widget provider.
 * */
public class AbstractWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        PollingManager.resetAllBackgroundTask(context, true);
    }
}
