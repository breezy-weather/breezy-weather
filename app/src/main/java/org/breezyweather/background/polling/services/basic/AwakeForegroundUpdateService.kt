package org.breezyweather.background.polling.services.basic

import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.background.polling.PollingManager.resetAllBackgroundTask
import org.breezyweather.common.basic.models.Location
import org.breezyweather.remoteviews.NotificationHelper
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.WidgetHelper

/**
 * Awake foreground update service.
 */
@AndroidEntryPoint
class AwakeForegroundUpdateService : ForegroundUpdateService() {
    override fun updateView(context: Context, location: Location) {
        WidgetHelper.updateWidgetIfNecessary(context, location)
    }

    override fun updateView(context: Context, locationList: List<Location>) {
        WidgetHelper.updateWidgetIfNecessary(context, locationList)
        NotificationHelper.updateNotificationIfNecessary(context, locationList)
    }

    override fun handlePollingResult(failed: Boolean) {
        resetAllBackgroundTask(this, false)
    }

    override val foregroundNotificationId = Notifications.ID_UPDATING_AWAKE
}
