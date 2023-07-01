package org.breezyweather.background.polling.services.basic

import android.content.Intent
import androidx.core.app.NotificationCompat
import cancelNotification
import notify
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.remoteviews.Notifications

/**
 * Foreground update service.
 */
abstract class ForegroundUpdateService : UpdateService() {
    private var mFinishedCount = 0
    override fun onCreate() {
        mFinishedCount = 0
        startForeground(
            foregroundNotificationId,
            getForegroundNotification(0).build()
        )
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(
            foregroundNotificationId,
            getForegroundNotification(0).build()
        )
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        // version O.
        stopForeground(true)
        this.cancelNotification(foregroundNotificationId)
    }

    override fun stopService(updateFailed: Boolean) {
        stopForeground(true)
        this.cancelNotification(foregroundNotificationId)
        super.stopService(updateFailed)
    }

    open fun getForegroundNotification(total: Int): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, Notifications.CHANNEL_BACKGROUND).apply {
            setSmallIcon(R.drawable.ic_running_in_background)
            setContentTitle(getString(R.string.breezy_weather))
            setContentText(
                getString(R.string.notification_updating_weather_data) + if (total == 0) "" else " (" + (mFinishedCount + 1) + "/" + total + ")"
            )
            setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            priority = NotificationCompat.PRIORITY_MIN
            setProgress(0, 0, true)
            setAutoCancel(false)
            setOngoing(false)
        }
    }

    abstract val foregroundNotificationId: Int

    override fun onUpdateCompleted(
        location: Location, old: Weather?,
        succeed: Boolean, index: Int, total: Int
    ) {
        super.onUpdateCompleted(location, old, succeed, index, total)
        mFinishedCount++
        if (mFinishedCount != total) {
            this.notify(foregroundNotificationId, getForegroundNotification(total).build())
        }
    }
}
