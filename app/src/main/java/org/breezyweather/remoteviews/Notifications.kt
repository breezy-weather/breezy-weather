package org.breezyweather.remoteviews

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN
import buildNotificationChannel
import buildNotificationChannelGroup
import org.breezyweather.R

object Notifications {

    // We only have one group as we don’t have many channels
    // We name it “Breezy Weather” as LeakCanary also has its own group
    private const val GROUP_BREEZY_WEATHER = "group_breezy_weather"

    const val CHANNEL_ALERT = "alert"
    const val ID_ALERT_MIN = 1000
    const val ID_ALERT_MAX = 1999
    const val ID_ALERT_GROUP = 2000
    const val ID_PRECIPITATION = 3000

    const val CHANNEL_FORECAST = "forecast"
    const val ID_TODAY_FORECAST = 2
    const val ID_TOMORROW_FORECAST = 3
    const val ID_UPDATING_TODAY_FORECAST = 7
    const val ID_UPDATING_TOMORROW_FORECAST = 8

    const val CHANNEL_WIDGET = "widget"
    const val ID_WIDGET = 1
    const val ID_UPDATING_WIDGET = 6

    const val CHANNEL_BACKGROUND = "background"
    const val ID_RUNNING_IN_BACKGROUND = 5
    const val ID_UPDATING_AWAKE = 9

    private val deprecatedChannels = listOf(
        "normally"
    )

    /**
     * Initialize channels so that the user can disable them, even if didn’t receive a notification yet
     */
    fun createChannels(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        // Delete old notification channels
        deprecatedChannels.forEach(notificationManager::deleteNotificationChannel)

        notificationManager.createNotificationChannelGroupsCompat(
            listOf(
                buildNotificationChannelGroup(GROUP_BREEZY_WEATHER) {
                    setName(context.getString(R.string.breezy_weather))
                }
            ),
        )

        notificationManager.createNotificationChannelsCompat(
            listOf(
                buildNotificationChannel(CHANNEL_ALERT, IMPORTANCE_HIGH) {
                    setName(context.getString(R.string.notification_channel_alerts))
                    setGroup(GROUP_BREEZY_WEATHER)
                },
                buildNotificationChannel(CHANNEL_FORECAST, IMPORTANCE_DEFAULT) {
                    setName(context.getString(R.string.notification_channel_forecast))
                    setGroup(GROUP_BREEZY_WEATHER)
                },
                buildNotificationChannel(CHANNEL_WIDGET, IMPORTANCE_DEFAULT) {
                    setName(context.getString(R.string.notification_channel_widget))
                    setGroup(GROUP_BREEZY_WEATHER)
                    setShowBadge(false)
                },
                buildNotificationChannel(CHANNEL_BACKGROUND, IMPORTANCE_MIN) {
                    setName(context.getString(R.string.notification_channel_background_services))
                    setGroup(GROUP_BREEZY_WEATHER)
                    setShowBadge(false)
                },
            )
        )
    }
}