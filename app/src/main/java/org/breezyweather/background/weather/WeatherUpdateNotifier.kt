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

package org.breezyweather.background.weather

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import cancelNotification
import notificationBuilder
import notify
import org.breezyweather.R
import breezyweather.domain.location.model.Location
import org.breezyweather.common.extensions.chop
import org.breezyweather.background.receiver.NotificationReceiver
import org.breezyweather.remoteviews.Notifications

/**
 * Based on Tachiyomi
 * Apache License, Version 2.0
 * https://github.com/tachiyomiorg/tachiyomi/blob/198604227788444f374b8702076b95fb593c8657/app/src/main/java/eu/kanade/tachiyomi/data/library/LibraryUpdateNotifier.kt
 */
class WeatherUpdateNotifier(private val context: Context) {

    /**
     * Pending intent of action that cancels the weather update
     */
    private val cancelIntent by lazy {
        NotificationReceiver.cancelWeatherUpdatePendingBroadcast(context)
    }

    /**
     * Cached progress notification to avoid creating a lot.
     */
    val progressNotificationBuilder by lazy {
        context.notificationBuilder(Notifications.CHANNEL_BACKGROUND) {
            setContentTitle(context.getString(R.string.app_name))
            setSmallIcon(R.drawable.ic_running_in_background)
            setOngoing(true)
            setOnlyAlertOnce(true)
            addAction(R.drawable.ic_close, context.getString(R.string.action_cancel), cancelIntent)
        }
    }

    /**
     * Shows the notification containing the currently updating manga and the progress.
     *
     * @param locations the manga that are being updated.
     * @param current the current progress.
     * @param total the total progress.
     */
    fun showProgressNotification(locations: List<Location>, current: Int, total: Int) {
        /*if (SettingsManager.getInstance(context).hideNotificationContent) {
            progressNotificationBuilder
                .setContentTitle(context.getString(R.string.notification_check_updates))
                .setContentText("($current/$total)")
        } else {*/
            val updatingText = locations.joinToString("\n") { it.city.chop(40) }
            progressNotificationBuilder
                .setContentTitle(context.getString(R.string.notification_updating_weather_data, current, total))
                .setStyle(NotificationCompat.BigTextStyle().bigText(updatingText))
        //}

        context.notify(
            Notifications.ID_WEATHER_PROGRESS,
            progressNotificationBuilder
                .setProgress(total, current, false)
                .build(),
        )
    }

    /**
     * Shows notification containing update entries that failed with action to open full log.
     *
     * @param failed Number of entries that failed to update.
     * @param uri Uri for error log file containing all titles that failed.
     */
    fun showUpdateErrorNotification(failed: Int, uri: Uri) {
        if (failed == 0) {
            return
        }

        context.notify(
            Notifications.ID_WEATHER_ERROR,
            Notifications.CHANNEL_BACKGROUND,
        ) {
            setContentTitle(context.resources.getString(R.string.notification_update_error, failed))
            setContentText(context.getString(R.string.action_show_errors))
            setSmallIcon(R.drawable.ic_running_in_background)

            setContentIntent(NotificationReceiver.openErrorLogPendingActivity(context, uri))
        }
    }

    /**
     * Shows notification containing update entries that were skipped.
     *
     * @param skipped Number of entries that were skipped during the update.
     */
    /*fun showUpdateSkippedNotification(skipped: Int) {
        if (skipped == 0) {
            return
        }

        context.notify(
            Notifications.ID_WEATHER_SKIPPED,
            Notifications.CHANNEL_BACKGROUND,
        ) {
            setContentTitle(context.resources.getString(R.string.notification_update_skipped, skipped))
            setContentText(context.getString(R.string.action_learn_more))
            setSmallIcon(R.drawable.ic_running_in_background)
            setContentIntent(NotificationHandler.openUrl(context, HELP_SKIPPED_URL))
        }
    }*/

    /**
     * Cancels the progress notification.
     */
    fun cancelProgressNotification() {
        context.cancelNotification(Notifications.ID_WEATHER_PROGRESS)
    }
}
