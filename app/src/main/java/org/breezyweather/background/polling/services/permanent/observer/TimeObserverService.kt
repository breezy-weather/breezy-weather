package org.breezyweather.background.polling.services.permanent.observer

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.breezyweather.R
import org.breezyweather.background.polling.services.permanent.update.ForegroundNormalUpdateService
import org.breezyweather.background.polling.services.permanent.update.ForegroundTodayForecastUpdateService
import org.breezyweather.background.polling.services.permanent.update.ForegroundTomorrowForecastUpdateService
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.settings.SettingsManager
import java.util.*

/**
 * Time observer service.
 */
class TimeObserverService : Service() {
    private inner class TimeTickReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            when (action) {
                Intent.ACTION_TIME_TICK -> doRefreshWork()
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                    sLastUpdateNormalViewTime = System.currentTimeMillis()
                    sLastTodayForecastTime = System.currentTimeMillis()
                    sLastTomorrowForecastTime = System.currentTimeMillis()
                    doRefreshWork()
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()
        initData()
        registerReceiver()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        readData(intent)
        doRefreshWork()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
        stopForeground(true)
    }

    private fun initData() {
        sPollingRate = 1.5f
        sTodayForecastTime = SettingsManager.DEFAULT_TODAY_FORECAST_TIME
        sTomorrowForecastTime = SettingsManager.DEFAULT_TOMORROW_FORECAST_TIME
        sLastUpdateNormalViewTime = System.currentTimeMillis()
        sLastTodayForecastTime = System.currentTimeMillis()
        sLastTomorrowForecastTime = System.currentTimeMillis()
    }

    private fun readData(intent: Intent?) {
        if (intent != null) {
            if (intent.getBooleanExtra(KEY_CONFIG_CHANGED, false)) {
                sPollingRate = intent.getFloatExtra(KEY_POLLING_RATE, 1.5f)
                sLastTodayForecastTime = System.currentTimeMillis()
                sLastTomorrowForecastTime = System.currentTimeMillis()
                sTodayForecastTime = intent.getStringExtra(KEY_TODAY_FORECAST_TIME)
                sTomorrowForecastTime = intent.getStringExtra(KEY_TOMORROW_FORECAST_TIME)
            }
            if (intent.getBooleanExtra(KEY_POLLING_FAILED, false)) {
                sLastUpdateNormalViewTime = pollingInterval?.let {
                    System.currentTimeMillis() - it + 15 * 60 * 1000
                } ?: 0
            }
        }
    }

    private fun doRefreshWork() {
        if (pollingInterval != null && System.currentTimeMillis() - sLastUpdateNormalViewTime > pollingInterval) {
            sLastUpdateNormalViewTime = System.currentTimeMillis()
            val intent = Intent(this, ForegroundNormalUpdateService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }
        if (!sTodayForecastTime.isNullOrEmpty() && isForecastTime(sTodayForecastTime, sLastTodayForecastTime)) {
            sLastTodayForecastTime = System.currentTimeMillis()
            val intent = Intent(this, ForegroundTodayForecastUpdateService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }
        if (!sTomorrowForecastTime.isNullOrEmpty() && isForecastTime(sTomorrowForecastTime, sLastTomorrowForecastTime)) {
            sLastTomorrowForecastTime = System.currentTimeMillis()
            val intent = Intent(this, ForegroundTomorrowForecastUpdateService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        sReceiver = TimeTickReceiver()
        registerReceiver(sReceiver, filter)
    }

    private fun unregisterReceiver() {
        if (sReceiver != null) {
            unregisterReceiver(sReceiver)
            sReceiver = null
        }
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(
                Notifications.ID_RUNNING_IN_BACKGROUND,
                getForegroundNotification(this, true)
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startForeground(
                Notifications.ID_RUNNING_IN_BACKGROUND,
                getForegroundNotification(this, false)
            )
            startService(Intent(this, FakeForegroundService::class.java))
        } else {
            startForeground(
                Notifications.ID_RUNNING_IN_BACKGROUND,
                getForegroundNotification(this, true)
            )
            startService(Intent(this, FakeForegroundService::class.java))
        }
    }

    private val pollingInterval: Long? = sPollingRate?.times(1000)?.times(60)?.times(60)?.toLong()

    companion object {
        private var sReceiver: TimeTickReceiver? = null
        private var sPollingRate: Float? = null
        private var sLastUpdateNormalViewTime: Long = 0
        private var sLastTodayForecastTime: Long = 0
        private var sLastTomorrowForecastTime: Long = 0
        private var sTodayForecastTime: String? = null
        private var sTomorrowForecastTime: String? = null

        const val KEY_CONFIG_CHANGED = "config_changed"
        const val KEY_POLLING_FAILED = "polling_failed"
        const val KEY_POLLING_RATE = "polling_rate"
        const val KEY_TODAY_FORECAST_TIME = "today_forecast_time"
        const val KEY_TOMORROW_FORECAST_TIME = "tomorrow_forecast_time"

        fun getForegroundNotification(context: Context, setIcon: Boolean): Notification {
            return NotificationCompat.Builder(context, Notifications.CHANNEL_BACKGROUND).apply {
                setSmallIcon(if (setIcon) R.drawable.ic_running_in_background else 0)
                setContentTitle(context.getString(R.string.breezy_weather))
                setContentText(context.getString(R.string.notification_running_in_background))
                setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                priority = NotificationCompat.PRIORITY_MIN
                setAutoCancel(true)
            }.build()
        }

        private fun isForecastTime(time: String?, lastForecastTime: Long): Boolean {
            if (time.isNullOrEmpty()) return false
            val splittedTime = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (splittedTime.size != 2) return false

            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = splittedTime[0].toInt()
            calendar[Calendar.MINUTE] = splittedTime[1].toInt()
            val configTime = calendar.timeInMillis
            val currentTime = System.currentTimeMillis()
            return configTime in (lastForecastTime + 1)..currentTime
        }
    }
}