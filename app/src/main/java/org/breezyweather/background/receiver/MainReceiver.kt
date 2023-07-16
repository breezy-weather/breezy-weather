package org.breezyweather.background.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.breezyweather.background.weather.WeatherUpdateJob

/**
 * Main receiver.
 */
class MainReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action.isNullOrEmpty()) return
        when (action) {
            // TODO: Do we really need this?
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_WALLPAPER_CHANGED -> WeatherUpdateJob.startNow(context)
        }
    }
}