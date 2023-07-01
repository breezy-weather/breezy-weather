package org.breezyweather.background.polling.services.permanent.observer

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import org.breezyweather.remoteviews.Notifications

class FakeForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startForeground(
                Notifications.ID_RUNNING_IN_BACKGROUND,
                TimeObserverService.getForegroundNotification(this, false)
            )
        } else {
            startForeground(
                Notifications.ID_RUNNING_IN_BACKGROUND,
                TimeObserverService.getForegroundNotification(this, true)
            )
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder? = null
}
