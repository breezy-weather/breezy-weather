package org.breezyweather.background.polling.services.permanent.update

import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.background.polling.services.basic.ForegroundUpdateService
import org.breezyweather.background.polling.services.permanent.PermanentServiceHelper.updatePollingService
import org.breezyweather.common.basic.models.Location
import org.breezyweather.remoteviews.NotificationHelper
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.WidgetHelper

/**
 * Foreground normal update service.
 */
@AndroidEntryPoint
class ForegroundNormalUpdateService : ForegroundUpdateService() {
    override fun updateView(context: Context, location: Location) {
        WidgetHelper.updateWidgetIfNecessary(context, location)
    }

    override fun updateView(context: Context, locationList: List<Location>) {
        WidgetHelper.updateWidgetIfNecessary(context, locationList)
        NotificationHelper.updateNotificationIfNecessary(context, locationList)
    }

    override fun handlePollingResult(failed: Boolean) {
        updatePollingService(this, failed)
    }

    override val foregroundNotificationId = Notifications.ID_UPDATING_WIDGET
}
