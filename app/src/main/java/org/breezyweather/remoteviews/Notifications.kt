package org.breezyweather.remoteviews

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN
import androidx.core.content.ContextCompat
import buildNotificationChannel
import buildNotificationChannelGroup
import notificationBuilder
import notify
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.remoteviews.presenters.notification.WidgetNotificationIMP
import org.breezyweather.settings.ConfigStore
import org.breezyweather.settings.SettingsManager
import java.text.DateFormat
import kotlin.math.min

object Notifications {

    // We only have one group as we don’t have many channels
    // We name it “Breezy Weather” as LeakCanary also has its own group
    private const val GROUP_BREEZY_WEATHER = "group_breezy_weather"

    private const val CHANNEL_ALERT = "alert"
    private const val ID_ALERT_MIN = 1000
    private const val ID_ALERT_MAX = 1999
    private const val ID_ALERT_GROUP = 2000
    private const val ID_PRECIPITATION = 3000

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

    private const val ALERT_GROUP_KEY = "breezy_weather_alert_notification_group"
    private const val PREFERENCE_NOTIFICATION = "NOTIFICATION_PREFERENCE"
    private const val KEY_NOTIFICATION_ID = "NOTIFICATION_ID"
    private const val PREFERENCE_SHORT_TERM_PRECIPITATION_ALERT = "SHORT_TERM_PRECIPITATION_ALERT_PREFERENCE"
    private const val KEY_PRECIPITATION_LOCATION_KEY = "PRECIPITATION_LOCATION_KEY"
    private const val KEY_PRECIPITATION_DATE = "PRECIPITATION_DATE"

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

    fun updateNotificationIfNecessary(context: Context, locationList: List<Location>) {
        if (WidgetNotificationIMP.isEnabled(context)) {
            WidgetNotificationIMP.buildNotificationAndSendIt(context, locationList)
        }
    }

    private fun getNotificationBuilder(
        context: Context, @DrawableRes iconId: Int,
        title: String, subtitle: String, content: String?,
        intent: PendingIntent
    ): NotificationCompat.Builder {
        return context.notificationBuilder(CHANNEL_ALERT).apply {
            setSmallIcon(iconId)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher))
            setContentTitle(title)
            setSubText(subtitle)
            setContentText(content)
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            setContentIntent(intent)
        }
    }

    // FIXME: Duplicate issue, see #73
    fun checkAndSendAlert(context: Context, location: Location, oldResult: Weather?) {
        val weather = location.weather
        if (weather == null || !SettingsManager.getInstance(context).isAlertPushEnabled) return

        val alertList: MutableList<Alert> = ArrayList()
        if (oldResult == null) {
            alertList.addAll(weather.alertList)
        } else {
            val idSet: MutableSet<Long> = HashSet()
            val desSet: MutableSet<String> = HashSet()
            for (alert in oldResult.alertList) {
                idSet.add(alert.alertId)
                desSet.add(alert.description)
            }
            for (alert in weather.alertList) {
                if (!idSet.contains(alert.alertId)
                    && !desSet.contains(alert.description)
                ) {
                    alertList.add(alert)
                }
            }
        }
        alertList.forEach { alert ->
            sendAlertNotification(
                context, location, alert, alertList.size > 1
            )
        }
    }

    private fun sendAlertNotification(
        context: Context, location: Location, alert: Alert, inGroup: Boolean
    ) {
        val notificationId = getAlertNotificationId(context)
        context.notify(
            notificationId,
            buildSingleAlertNotification(context, location, alert, inGroup, notificationId)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
            context.notify(
                ID_ALERT_GROUP,
                buildAlertGroupSummaryNotification(context, location, alert, notificationId)
            )
        }
    }

    @SuppressLint("InlinedApi")
    private fun buildSingleAlertNotification(
        context: Context, location: Location, alert: Alert, inGroup: Boolean, notificationId: Int
    ): Notification {
        // FIXME: Timezone
        val time = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.DEFAULT).format(alert.startDate)
        val builder = getNotificationBuilder(
            context,
            R.drawable.ic_alert,
            alert.description,
            time,
            alert.content,
            PendingIntent.getActivity(
                context,
                notificationId,
                IntentHelper.buildMainActivityShowAlertsIntent(location),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        ).setStyle(
            NotificationCompat.BigTextStyle()
                .setBigContentTitle(alert.description)
                .setSummaryText(time)
                .bigText(alert.content)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inGroup) {
            builder.setGroup(ALERT_GROUP_KEY)
        }
        return builder.build()
    }

    @SuppressLint("InlinedApi")
    private fun buildAlertGroupSummaryNotification(
        context: Context, location: Location, alert: Alert, notificationId: Int
    ): Notification {
        return context.notificationBuilder(CHANNEL_ALERT).apply {
            setSmallIcon(R.drawable.ic_alert)
            setContentTitle(alert.description)
            setGroup(ALERT_GROUP_KEY)
            color = getColor(context, location)
            setGroupSummary(true)
            setOnlyAlertOnce(true)
            setContentIntent(
                PendingIntent.getActivity(
                    context,
                    notificationId,
                    IntentHelper.buildMainActivityShowAlertsIntent(location),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }.build()
    }

    private fun getAlertNotificationId(context: Context): Int {
        val config = ConfigStore(context, PREFERENCE_NOTIFICATION)
        var id = config.getInt(KEY_NOTIFICATION_ID, ID_ALERT_MIN) + 1
        if (id > ID_ALERT_MAX) {
            id = ID_ALERT_MIN
        }
        config.edit()
            .putInt(KEY_NOTIFICATION_ID, id)
            .apply()
        return id
    }

    // precipitation.
    @SuppressLint("InlinedApi")
    fun checkAndSendPrecipitationForecast(context: Context, location: Location) {
        if (!SettingsManager.getInstance(context).isPrecipitationPushEnabled || location.weather == null) return
        val weather = location.weather
        val config = ConfigStore(context, PREFERENCE_SHORT_TERM_PRECIPITATION_ALERT)
        val timestamp = config.getLong(KEY_PRECIPITATION_DATE, 0)

        // we only send precipitation alert once a day.
        if (isSameDay(timestamp, System.currentTimeMillis())) return
        if (isShortTermLiquid(weather) || isLiquidDay(weather)) {
            context.notify(
                ID_PRECIPITATION,
                getNotificationBuilder(
                    context,
                    R.drawable.ic_precipitation,
                    context.getString(R.string.precipitation_forecast),
                    weather.dailyForecast[0].date.getFormattedDate(
                        location.timeZone,
                        context.getString(R.string.date_format_widget_long)
                    ),
                    context.getString(
                        if (isShortTermLiquid(weather)) R.string.notification_precipitation_short_term else R.string.notification_precipitation_today
                    ),
                    PendingIntent.getActivity(
                        context,
                        Notifications.ID_PRECIPITATION,
                        IntentHelper.buildMainActivityIntent(location),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                ).build()
            )
            config.edit()
                .putString(KEY_PRECIPITATION_LOCATION_KEY, location.formattedId)
                .putLong(KEY_PRECIPITATION_DATE, System.currentTimeMillis())
                .apply()
        }
    }

    private fun isLiquidDay(weather: Weather): Boolean {
        return (weather.dailyForecast.getOrNull(0)?.day?.weatherCode != null
                && weather.dailyForecast[0].day!!.weatherCode!!.isPrecipitation)
                || (weather.dailyForecast.getOrNull(0)?.night?.weatherCode != null
                && weather.dailyForecast[0].night!!.weatherCode!!.isPrecipitation)
    }

    private fun isShortTermLiquid(weather: Weather): Boolean {
        for (i in 0 until min(4, weather.hourlyForecast.size)) {
            if (weather.hourlyForecast.getOrNull(i)?.weatherCode != null
                && weather.hourlyForecast[i].weatherCode!!.isPrecipitation) {
                return true
            }
        }
        return false
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val day1 = time1 / 1000 / 60 / 60 / 24
        val day2 = time2 / 1000 / 60 / 60 / 24
        return day1 != day2
    }

    @ColorInt
    private fun getColor(context: Context, location: Location): Int {
        return ContextCompat.getColor(
            context,
            if (location.isDaylight) R.color.lightPrimary_5 else R.color.darkPrimary_5
        )
    }
}