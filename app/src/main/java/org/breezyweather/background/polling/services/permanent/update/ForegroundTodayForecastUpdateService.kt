package org.breezyweather.background.polling.services.permanent.update

import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.R
import org.breezyweather.background.polling.services.basic.ForegroundUpdateService
import org.breezyweather.common.basic.models.Location
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.presenters.notification.ForecastNotificationIMP

/**
 * Foreground Today forecast update service.
 */
@AndroidEntryPoint
class ForegroundTodayForecastUpdateService : ForegroundUpdateService() {
    override fun updateView(context: Context, location: Location) {
        if (ForecastNotificationIMP.isEnabled(this, true)) {
            ForecastNotificationIMP.buildForecastAndSendIt(context, location, true)
        }
    }

    override fun updateView(context: Context, locationList: List<Location>) {}
    override fun handlePollingResult(failed: Boolean) {
        // do nothing.
    }

    override fun getForegroundNotification(total: Int): NotificationCompat.Builder {
        return super.getForegroundNotification(total).setContentTitle(
            getString(R.string.breezy_weather) + " " + getString(R.string.notification_channel_forecast)
        )
    }

    override val foregroundNotificationId = Notifications.ID_UPDATING_TODAY_FORECAST
}