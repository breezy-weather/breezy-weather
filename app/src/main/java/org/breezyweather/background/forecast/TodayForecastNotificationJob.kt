package org.breezyweather.background.forecast

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import cancelNotification
import org.breezyweather.common.extensions.isRunning
import org.breezyweather.common.extensions.workManager
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.settings.SettingsManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TodayForecastNotificationJob(
    private val context: Context, workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notifier = ForecastNotificationNotifier(context)

    override suspend fun doWork(): Result {
        try {
            setForeground(getForegroundInfo())
        } catch (e: IllegalStateException) {
            LogHelper.log(msg = "Not allowed to set foreground job")
            e.message?.let { LogHelper.log(msg = it) }
        }

        return try {
            val locationList = LocationEntityRepository.readLocationList()
            if (locationList.isNotEmpty()) {
                val location = locationList[0].copy(weather = WeatherEntityRepository.readWeather(locationList[0]))
                notifier.showComplete(location, today = true)
            } else {
                // No location added yet, skipping
            }
            Result.success()
        } catch (e: Exception) {
            e.message?.let { LogHelper.log(msg = it) }
            Result.failure()
        } finally {
            context.cancelNotification(Notifications.ID_UPDATING_TODAY_FORECAST)

            // Add a new job in 24 hours
            setupTask(context, nextDay = true)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            Notifications.ID_UPDATING_TODAY_FORECAST,
            notifier.showProgress(today = true).build(),
        )
    }

    companion object {
        private const val TAG = "ForecastNotificationToday"

        fun isRunning(context: Context): Boolean {
            return context.workManager.isRunning(TAG)
        }

        fun setupTask(context: Context, nextDay: Boolean) {
            val settings = SettingsManager.getInstance(context)
            if (settings.isTodayForecastEnabled) {
                val request = OneTimeWorkRequestBuilder<TodayForecastNotificationJob>()
                    .setInitialDelay(
                        getForecastAlarmDelayInMinutes(settings.todayForecastTime, nextDay),
                        TimeUnit.MINUTES
                    )
                    .addTag(TAG)
                    .build()
                context.workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, request)
            } else {
                context.workManager.cancelUniqueWork(TAG)
            }
        }

        fun stop(context: Context) {
            context.workManager.cancelUniqueWork(TAG)
        }

        private fun getForecastAlarmDelayInMinutes(time: String, nextDay: Boolean): Long {
            val realTimes = intArrayOf(
                Calendar.getInstance()[Calendar.HOUR_OF_DAY],
                Calendar.getInstance()[Calendar.MINUTE]
            )
            val setTimes = intArrayOf(
                time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt(),
                time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()
            )
            var delay = (setTimes[0] - realTimes[0]) * 60 + (setTimes[1] - realTimes[1])
            if (delay <= 0 || nextDay) {
                delay += 24 * 60
            }
            return delay.toLong()
        }
    }
}
