package org.breezyweather.background.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import org.breezyweather.background.polling.PollingManager.resetAllBackgroundTask

/**
 * Main receiver.
 */
class MainReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action.isNullOrEmpty()) return
        when (action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_WALLPAPER_CHANGED -> resetAllBackgroundTask(context, true)
        }
    }
}