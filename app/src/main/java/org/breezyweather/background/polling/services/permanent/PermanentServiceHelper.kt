package org.breezyweather.background.polling.services.permanent

import android.content.Context
import android.content.Intent
import android.os.Build
import org.breezyweather.background.polling.services.permanent.observer.TimeObserverService
import org.breezyweather.common.basic.models.options.BackgroundUpdateMethod
import org.breezyweather.settings.SettingsManager

/**
 * Service helper.
 */
object PermanentServiceHelper {
    fun startPollingService(context: Context) {
        val settings = SettingsManager.getInstance(context)
        if (settings.backgroundUpdateMethod === BackgroundUpdateMethod.NOTIFICATION) {
            val intent = Intent(context, TimeObserverService::class.java).apply {
                putExtra(TimeObserverService.KEY_CONFIG_CHANGED, true)
                putExtra(
                    TimeObserverService.KEY_POLLING_RATE,
                    settings.updateInterval.intervalInHour
                )
                putExtra(
                    TimeObserverService.KEY_TODAY_FORECAST_TIME,
                    if (settings.isTodayForecastEnabled) settings.todayForecastTime else null
                )
                putExtra(
                    TimeObserverService.KEY_TOMORROW_FORECAST_TIME,
                    if (settings.isTomorrowForecastEnabled) settings.tomorrowForecastTime else null
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun updatePollingService(context: Context, pollingFailed: Boolean) {
        if (SettingsManager.getInstance(context).backgroundUpdateMethod === BackgroundUpdateMethod.NOTIFICATION) {
            val intent = Intent(context, TimeObserverService::class.java)
            intent.putExtra(TimeObserverService.KEY_POLLING_FAILED, pollingFailed)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun stopPollingService(context: Context) {
        val intent = Intent(context, TimeObserverService::class.java)
        context.stopService(intent)
    }
}
