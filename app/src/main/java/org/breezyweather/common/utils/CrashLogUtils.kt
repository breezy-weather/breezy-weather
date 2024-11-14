/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.background.receiver.NotificationReceiver
import org.breezyweather.common.extensions.cancelNotification
import org.breezyweather.common.extensions.createFileInCacheDir
import org.breezyweather.common.extensions.getUriCompat
import org.breezyweather.common.extensions.notify
import org.breezyweather.common.extensions.withNonCancellableContext
import org.breezyweather.common.extensions.withUIContext
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.remoteviews.Notifications

/**
 * Taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/aa498360db90350f2642e6320dc55e7d474df1fd/app/src/main/java/eu/kanade/tachiyomi/util/CrashLogUtil.kt
 */

class CrashLogUtils(
    private val context: Context,
) {

    suspend fun dumpLogs() = withNonCancellableContext {
        try {
            val file = context.createFileInCacheDir("breezyweather_crash_logs.txt")
            Runtime.getRuntime().exec("logcat *:E -d -f ${file.absolutePath}").waitFor()
            file.appendText(getDebugInfo())

            showNotification(file.getUriCompat(context))
        } catch (e: Throwable) {
            e.printStackTrace()
            withUIContext { SnackbarHelper.showSnackbar("Failed to get logs") }
        }
    }

    fun getDebugInfo(): String {
        return """
            App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.FLAVOR}, ${BuildConfig.COMMIT_SHA}, ${BuildConfig.VERSION_CODE}
            Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}); build ${Build.DISPLAY}
            Device brand: ${Build.BRAND}
            Device manufacturer: ${Build.MANUFACTURER}
            Device name: ${Build.DEVICE} (${Build.PRODUCT})
            Device model: ${Build.MODEL}
        """.trimIndent()
    }

    private fun showNotification(uri: Uri) {
        context.cancelNotification(Notifications.ID_CRASH_LOGS)

        context.notify(
            Notifications.ID_CRASH_LOGS,
            Notifications.CHANNEL_CRASH_LOGS
        ) {
            setContentTitle(context.getString(R.string.settings_debug_dump_crash_logs_saved))
            setContentText(context.getString(R.string.settings_debug_dump_crash_logs_tap_to_open))
            setSmallIcon(R.drawable.ic_alert)

            setContentIntent(NotificationReceiver.openErrorLogPendingActivity(context, uri))
        }
    }
}
