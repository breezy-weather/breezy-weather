package wangdaye.com.geometricweather.location.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import wangdaye.com.geometricweather.GeometricWeather
import wangdaye.com.geometricweather.R

/**
 * Location service.
 */
abstract class LocationService {

    class Result(var latitude: Float, var longitude: Float) {

        var district = ""
        var city = ""
        var province = ""
        var country = ""

        var inChina = false
        var hasGeocodeInformation = false

        fun setGeocodeInformation(country: String,
                                  province: String,
                                  city: String,
                                  district: String) {
            hasGeocodeInformation = true

            this.country = country
            this.province = province
            this.city = city
            this.district = district
        }
    }

    abstract suspend fun getLocation(context: Context): Result?
    abstract fun getPermissions(): Array<String>

    open fun hasPermissions(context: Context?): Boolean {
        val permissions = getPermissions()
        for (p in permissions) {
            if (ActivityCompat.checkSelfPermission(context!!, p) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getLocationNotificationChannel(context: Context): NotificationChannel {
        val channel = NotificationChannel(
                GeometricWeather.NOTIFICATION_CHANNEL_ID_LOCATION,
                GeometricWeather.getNotificationChannelName(
                        context, GeometricWeather.NOTIFICATION_CHANNEL_ID_LOCATION),
                NotificationManager.IMPORTANCE_MIN)
        channel.setShowBadge(false)
        channel.lightColor = ContextCompat.getColor(context, R.color.colorPrimary)
        return channel
    }

    fun getLocationNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, GeometricWeather.NOTIFICATION_CHANNEL_ID_LOCATION)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle(context.getString(R.string.feedback_request_location))
                .setContentText(context.getString(R.string.feedback_request_location_in_background))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setAutoCancel(true)
                .setProgress(0, 0, true)
                .build()
    }
}