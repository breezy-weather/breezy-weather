/*
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

package org.breezyweather.background.updater

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import org.breezyweather.R
import org.breezyweather.background.receiver.NotificationReceiver
import org.breezyweather.background.updater.model.Release
import org.breezyweather.common.extensions.notificationBuilder
import org.breezyweather.common.extensions.notify
import org.breezyweather.remoteviews.Notifications

internal class AppUpdateNotifier(
    private val context: Context,
) {

    private val notificationBuilder = context.notificationBuilder(Notifications.CHANNEL_APP_UPDATE)

    /**
     * Call to show notification.
     *
     * @param id id of the notification channel.
     */
    private fun NotificationCompat.Builder.show(id: Int = Notifications.ID_APP_UPDATER) {
        context.notify(id, build())
    }

    fun cancel() {
        NotificationReceiver.dismissNotification(context, Notifications.ID_APP_UPDATER)
    }

    fun promptOldAndroidVersion() {
        with(notificationBuilder) {
            setContentTitle(context.getString(R.string.about_update_check_eol))
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            clearActions()
        }
        notificationBuilder.show()
    }

    @SuppressLint("LaunchActivityFromNotification")
    fun promptUpdate(release: Release) {
        /*val updateIntent = NotificationReceiver.downloadAppUpdatePendingBroadcast(
            context,
            release.getDownloadLink(),
            release.version,
        )*/

        val releaseIntent = Intent(Intent.ACTION_VIEW, release.releaseLink.toUri()).run {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            PendingIntent.getActivity(
                context,
                release.hashCode(),
                this,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        with(notificationBuilder) {
            setContentTitle(context.getString(R.string.notification_app_update_available))
            setContentText(release.version)
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            // setContentIntent(updateIntent)
            setContentIntent(releaseIntent)

            clearActions()
            addAction(
                android.R.drawable.stat_sys_download_done,
                context.getString(R.string.action_download),
                // updateIntent,
                releaseIntent
            )
            /*addAction(
                R.drawable.ic_info_24dp,
                context.getString(R.string.whats_new),
                releaseIntent,
            )*/
        }
        notificationBuilder.show()
    }
}
