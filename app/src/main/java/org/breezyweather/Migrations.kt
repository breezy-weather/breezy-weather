package org.breezyweather

import android.content.Context
import org.breezyweather.background.forecast.TodayForecastNotificationJob
import org.breezyweather.background.forecast.TomorrowForecastNotificationJob
import org.breezyweather.background.weather.WeatherUpdateJob
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
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
            if (oldVersion > 0) { // Not fresh install
                if (oldVersion < 40500) {
                    // Clean up all weather data due to:
                    // - formattedId change
                    // - old current location weather data that was kept
                    LocationEntityRepository.regenerateAllFormattedId()
                    WeatherEntityRepository.deleteAllWeather()
                }
            }

            SettingsManager.getInstance(context).lastVersionCode = BuildConfig.VERSION_CODE

            // Always set up background tasks to ensure they're running
            WeatherUpdateJob.setupTask(context) // This will also refresh data immediately
            TodayForecastNotificationJob.setupTask(context, false)
            TomorrowForecastNotificationJob.setupTask(context, false)

            return oldVersion != 0
        }

        return false
    }
}