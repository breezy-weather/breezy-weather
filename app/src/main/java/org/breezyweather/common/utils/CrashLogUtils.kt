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
import cancelNotification
import notify
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.background.receiver.NotificationReceiver
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.common.extensions.createFileInCacheDir
import org.breezyweather.common.extensions.getUriCompat
import org.breezyweather.common.extensions.withNonCancellableContext
import org.breezyweather.common.extensions.withUIContext
import org.breezyweather.remoteviews.Notifications

/**
 * Taken from Tachiyomi
 * Apache License, Version 2.0
 *
 * https://github.com/tachiyomiorg/tachiyomi/blob/75460e01c80a75d604ae4323c14ffe73252efa9e/app/src/main/java/eu/kanade/tachiyomi/util/CrashLogUtil.kt
 */

class CrashLogUtils(private val context: Context) {

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
            App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.FLAVOR}, ${BuildConfig.VERSION_CODE}
            Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Android build ID: ${Build.DISPLAY}
            Device brand: ${Build.BRAND}
            Device manufacturer: ${Build.MANUFACTURER}
            Device name: ${Build.DEVICE}
            Device model: ${Build.MODEL}
            Device product name: ${Build.PRODUCT}
        """.trimIndent()
    }

    private fun showNotification(uri: Uri) {
        context.cancelNotification(Notifications.ID_CRASH_LOGS)

        context.notify(
            Notifications.ID_CRASH_LOGS,
            Notifications.CHANNEL_CRASH_LOGS,
        ) {
            setContentTitle(context.getString(R.string.settings_debug_dump_crash_logs_saved))
            setContentText(context.getString(R.string.settings_debug_dump_crash_logs_tap_to_open))
            setSmallIcon(R.drawable.ic_alert)

            setContentIntent(NotificationReceiver.openErrorLogPendingActivity(context, uri))
        }
    }
}