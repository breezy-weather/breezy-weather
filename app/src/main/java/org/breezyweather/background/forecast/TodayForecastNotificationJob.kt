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

package org.breezyweather.background.forecast

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.breezyweather.common.extensions.cancelNotification
import org.breezyweather.common.extensions.isRunning
import org.breezyweather.common.extensions.setForegroundSafely
import org.breezyweather.common.extensions.workManager
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.Notifications
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@HiltWorker
class TodayForecastNotificationJob @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
) : CoroutineWorker(context, workerParams) {

    private val notifier = ForecastNotificationNotifier(context)

    override suspend fun doWork(): Result {
        setForegroundSafely()

        return try {
            val location = locationRepository.getFirstLocation(withParameters = false)
            if (location != null) {
                notifier.showComplete(
                    location.copy(
                        weather = weatherRepository.getWeatherByLocationId(
                            location.formattedId,
                            withDaily = true,
                            withHourly = false,
                            withMinutely = false,
                            withAlerts = false
                        )
                    ),
                    today = true
                )
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
            notifier.showProgress(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            } else {
                0
            }
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
            var delay = (setTimes[0] - realTimes[0]).hours.inWholeMinutes + (setTimes[1] - realTimes[1])
            if (delay <= 0 || nextDay) {
                delay += 1.days.inWholeMinutes
            }
            return delay
        }
    }
}
