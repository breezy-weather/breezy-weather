package org.breezyweather.background.polling.services.permanent.update

import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.background.polling.services.basic.ForegroundUpdateService
import org.breezyweather.background.polling.services.permanent.PermanentServiceHelper
import org.breezyweather.common.basic.models.Location
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.Widgets

/**
 * Foreground normal update service.
 */
@AndroidEntryPoint
class ForegroundNormalUpdateService : ForegroundUpdateService() {
    override fun updateView(context: Context, location: Location) {
        Widgets.updateWidgetIfNecessary(context, location)
    }

    override fun updateView(context: Context, locationList: List<Location>) {
        Widgets.updateWidgetIfNecessary(context, locationList)
        Notifications.updateNotificationIfNecessary(context, locationList)
    }

    override fun handlePollingResult(updateSucceed: Boolean) {
        PermanentServiceHelper.updatePollingService(this, updateSucceed)
    }

    override val foregroundNotificationId = Notifications.ID_UPDATING_WIDGET
}
