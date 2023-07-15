package org.breezyweather

import android.content.Context
import org.breezyweather.background.forecast.TodayForecastNotificationJob
import org.breezyweather.background.forecast.TomorrowForecastNotificationJob
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.settings.SettingsManager

object Migrations {

    /**
     * Performs a migration when the application is updated.
     *
     * @return true if a migration is performed, false otherwise.
     */
    fun upgrade(
        context: Context
    ): Boolean {
        val lastVersionCode = SettingsManager.getInstance(context).lastVersionCode
        val oldVersion = lastVersionCode
        if (oldVersion < BuildConfig.VERSION_CODE) {
            SettingsManager.getInstance(context).lastVersionCode = BuildConfig.VERSION_CODE

            // Always set up background tasks to ensure they're running
            WeatherUpdateJob.setupTask(context)
            TodayForecastNotificationJob.setupTask(context, false)
            TomorrowForecastNotificationJob.setupTask(context, false)

            // Fresh install
            if (oldVersion == 0) {
                return false
            }

            // We don’t have migrations yet, but they should be added here in the future
            /*if (oldVersion < 40200) {

            }*/

            return true
        }

        return false
    }
}