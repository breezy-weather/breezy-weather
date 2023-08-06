/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.background.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.breezyweather.BuildConfig.APPLICATION_ID as ID
import org.breezyweather.background.weather.WeatherUpdateJob

/**
 * Taken partially from Tachiyomi
 * License Apache, Version 2.0
 * https://github.com/tachiyomiorg/tachiyomi/blob/75460e01c80a75d604ae4323c14ffe73252efa9e/app/src/main/java/eu/kanade/tachiyomi/data/notification/NotificationReceiver.kt
 */

/**
 * Global [BroadcastReceiver] that runs on UI thread
 * Pending Broadcasts should be made from here.
 * NOTE: Use local broadcasts if possible.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // Cancel weather update and dismiss notification
            ACTION_CANCEL_WEATHER_UPDATE -> cancelWeatherUpdate(context)
        }
    }

    /**
     * Method called when user wants to stop a weather update
     *
     * @param context context of application
     */
    private fun cancelWeatherUpdate(context: Context) {
        WeatherUpdateJob.stop(context)
    }

    companion object {
        private const val NAME = "NotificationReceiver"

        private const val ACTION_CANCEL_WEATHER_UPDATE = "$ID.$NAME.CANCEL_WEATHER_UPDATE"

        /**
         * Returns [PendingIntent] that starts a service which stops the weather update
         *
         * @param context context of application
         * @return [PendingIntent]
         */
        internal fun cancelWeatherUpdatePendingBroadcast(context: Context): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_CANCEL_WEATHER_UPDATE
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        /**
         * Returns [PendingIntent] that opens the error log file in an external viewer
         *
         * @param context context of application
         * @param uri uri of error log file
         * @return [PendingIntent]
         */
        internal fun openErrorLogPendingActivity(context: Context, uri: Uri): PendingIntent {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                setDataAndType(uri, "text/plain")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
    }
}