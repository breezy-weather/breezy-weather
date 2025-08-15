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
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import breezyweather.data.location.LocationRepository
import breezyweather.data.weather.WeatherRepository
import breezyweather.domain.location.model.Location
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import org.breezyweather.BuildConfig
import org.breezyweather.background.updater.AppUpdateChecker
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.extensions.createFileInCacheDir
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.getUriCompat
import org.breezyweather.common.extensions.isOnline
import org.breezyweather.common.extensions.isRunning
import org.breezyweather.common.extensions.setForegroundSafely
import org.breezyweather.common.extensions.withIOContext
import org.breezyweather.common.extensions.workManager
import org.breezyweather.common.options.NotificationStyle
import org.breezyweather.common.source.LocationResult
import org.breezyweather.common.source.RefreshError
import org.breezyweather.common.source.WeatherResult
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.presenters.MultiCityWidgetIMP
import org.breezyweather.sources.RefreshHelper
import org.breezyweather.sources.SourceManager
import org.breezyweather.ui.main.utils.RefreshErrorType
import java.io.File
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.minutes

/**
 * Based on Mihon LibraryUpdateJob
 * Licensed under Apache License, Version 2.0
 * https://github.com/mihonapp/mihon/blob/88e9fefa59b3f7f77ab3ddcab1b039f81534c83e/app/src/main/java/eu/kanade/tachiyomi/data/library/LibraryUpdateJob.kt
 */
@HiltWorker
class WeatherUpdateJob @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val refreshHelper: RefreshHelper,
    private val sourceManager: SourceManager,
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val updateChecker: AppUpdateChecker,
) : CoroutineWorker(context, workerParams) {

    private val notifier = WeatherUpdateNotifier(context)

    private var locationsToUpdate: List<Location> = mutableListOf()

    override suspend fun doWork(): Result {
        if (tags.contains(WORK_NAME_AUTO)) {
            // Find a running manual worker. If exists, try again later
            if (context.workManager.isRunning(WORK_NAME_MANUAL)) {
                return Result.retry()
            }
        }

        // Exit early in case there is no network and Android still executes the job
        if (!context.isOnline()) {
            return Result.retry()
        }

        setForegroundSafely()

        // Set the last update time to now
        SettingsManager.getInstance(context).weatherUpdateLastTimestamp = Date().time

        val locationFormattedId = inputData.getString(KEY_LOCATION)
        addLocationToQueue(locationFormattedId)

        return withIOContext {
            try {
                updateWeatherData()
                Result.success()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    // Assume success although cancelled
                    Result.success()
                } else {
                    e.printStackTrace()
                    Result.failure()
                }
            } finally {
                notifier.cancelProgressNotification()
                /*if ((BuildConfig.FLAVOR != "freenet" && SettingsManager.getInstance(context).isAppUpdateCheckEnabled) ||
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    ) {*/
                if (BuildConfig.FLAVOR != "freenet" && SettingsManager.getInstance(context).isAppUpdateCheckEnabled) {
                    try {
                        updateChecker.checkForUpdate(context, forceCheck = false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notifier = WeatherUpdateNotifier(context)
        return ForegroundInfo(
            Notifications.ID_WEATHER_PROGRESS,
            notifier.progressNotificationBuilder.build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
        )
    }

    /**
     * Adds list of locations to be updated.
     *
     * @param locationFormattedId the ID of the location to update, or null if automatic detection.
     */
    private suspend fun addLocationToQueue(locationFormattedId: String?) {
        locationsToUpdate = if (locationFormattedId != null) {
            val location = locationRepository.getLocation(locationFormattedId)
            if (location != null) {
                listOf(
                    location.copy(
                        weather = weatherRepository.getWeatherByLocationId(location.formattedId)
                    )
                )
            } else {
                emptyList()
            }
        } else {
            val locationList = when {
                // Should be getAllLocations(), but some rare users have 100+ locations. No need to refresh all of them
                // in that case, they don't actually use them every day, they just add them as "bookmarks"
                refreshHelper.isBroadcastSourcesEnabled(context) -> locationRepository.getXLocations(5)
                SettingsManager.getInstance(context).isWidgetNotificationEnabled &&
                    SettingsManager.getInstance(context).widgetNotificationStyle == NotificationStyle.CITIES ->
                    locationRepository.getXLocations(4)
                MultiCityWidgetIMP.isInUse(context) -> locationRepository.getXLocations(3)
                else -> locationRepository.getXLocations(1)
            }

            locationList
                .map {
                    it.copy(
                        weather = weatherRepository.getWeatherByLocationId(it.formattedId)
                    )
                }
                .filterIndexed { i, location ->
                    // Only refresh secondary locations once a day as we only need daily info
                    i == 0 ||
                        location.weather?.base?.refreshTime == null ||
                        location.weather!!.base.refreshTime!!.getIsoFormattedDate(location) <
                        Date().getFormattedDate("yyyy-MM-dd")
                }
                .toMutableList()
        }
    }

    /**
     * Method that updates weather in [locationsToUpdate]. It's called in a background thread, so it's safe
     * to do heavy operations or network calls here.
     * For each weather it calls [updateLocation] and updates the notification showing the current
     * progress.
     *
     * @return an observable delivering the progress of each update.
     */
    private suspend fun updateWeatherData() {
        val progressCount = AtomicInteger(0)
        val currentlyUpdatingLocation = CopyOnWriteArrayList<Location>()
        val newUpdates = CopyOnWriteArrayList<Pair<Location, Location>>()
        val skippedUpdates = CopyOnWriteArrayList<Pair<Location, String?>>()
        val failedUpdates = CopyOnWriteArrayList<Pair<Location, String?>>()

        /**
         * Update coordinates if locations to update contains a current location
         */
        val updateCoordinatesErrors = if (locationsToUpdate.any { it.isCurrentPosition }) {
            updateCoordinates()
        } else {
            emptyList()
        }

        locationsToUpdate.forEach { location ->
            withUpdateNotification(
                currentlyUpdatingLocation,
                progressCount,
                location
            ) {
                // TODO: Implement this, it’s a good idea
                /*if (location.updateStrategy != UpdateStrategy.ALWAYS_UPDATE) {
                    skippedUpdates.add(location to context.getString(R.string.skipped_reason_not_always_update))
                } else {*/
                try {
                    val locationResult = updateLocation(location)
                    locationResult.errors.forEach {
                        val shortMessage = it.getMessage(context, sourceManager)
                        if (it.error != RefreshErrorType.NETWORK_UNAVAILABLE &&
                            it.error != RefreshErrorType.SERVER_TIMEOUT
                        ) {
                            failedUpdates.add(locationResult.location to shortMessage)
                        } else {
                            skippedUpdates.add(locationResult.location to shortMessage)
                        }
                    }
                    if (!locationResult.location.isUsable) {
                        // Report coordinate update errors only if we can’t re-use last known coordinates
                        updateCoordinatesErrors.forEach {
                            val shortMessage = it.getMessage(context, sourceManager)
                            failedUpdates.add(locationResult.location to shortMessage)
                        }
                    }
                    if (locationResult.location.isUsable && !locationResult.location.needsGeocodeRefresh) {
                        val ignoreCaching = SphericalUtil.computeDistanceBetween(
                            LatLng(locationResult.location.latitude, locationResult.location.longitude),
                            LatLng(location.latitude, location.longitude)
                        ) > RefreshHelper.CACHING_DISTANCE_LIMIT
                        val weatherResult = updateWeather(
                            locationResult.location,
                            location.longitude != locationResult.location.longitude ||
                                location.latitude != locationResult.location.latitude,
                            ignoreCaching
                        )
                        newUpdates.add(
                            location to locationResult.location.copy(weather = weatherResult.weather)
                        )
                        weatherResult.errors.forEach {
                            failedUpdates.add(location to it.getMessage(context, sourceManager))
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    val errorMessage = if (e.message.isNullOrEmpty()) {
                        context.getString(RefreshErrorType.DATA_REFRESH_FAILED.shortMessage)
                    } else {
                        e.message
                    }
                    failedUpdates.add(location to errorMessage)
                }
                // }
            }
        }

        notifier.cancelProgressNotification()

        if (newUpdates.isNotEmpty()) {
            // We updated at least one location, so we need to reload location list and make some post-actions
            val locationList = locationRepository.getAllLocations().toMutableList()
            for (i in locationList.indices) {
                locationList[i] = locationList[i].copy(
                    weather = weatherRepository.getWeatherByLocationId(locationList[i].formattedId)
                )
            }

            // Update widgets and notification-widget
            refreshHelper.updateWidgetIfNecessary(context, locationList)
            refreshHelper.updateNotificationIfNecessary(context, locationList)

            // Update shortcuts
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                refreshHelper.refreshShortcuts(applicationContext, locationList)
            }

            val location = locationList[0]
            val indexOfFirstLocation = newUpdates.firstOrNull { it.first.formattedId == location.formattedId }

            // Send alert and precipitation for the first location
            if (indexOfFirstLocation != null) {
                Notifications.checkAndSendAlert(
                    applicationContext,
                    location,
                    locationsToUpdate.firstOrNull { it.formattedId == location.formattedId }?.weather
                )
                Notifications.checkAndSendPrecipitation(applicationContext, location)
            }

            refreshHelper.broadcastDataIfNecessary(
                context,
                locationList,
                newUpdates.map { it.first.formattedId }.toTypedArray()
            )

            // Inform main activity that we updated location
            newUpdates.forEach {
                EventBus.instance
                    .with(Location::class.java)
                    .postValue(it.second)
            }
        }

        if (failedUpdates.isNotEmpty()) {
            val errorFile = writeErrorFile(failedUpdates)
            notifier.showUpdateErrorNotification(
                failedUpdates.groupBy { it.first }.size,
                errorFile.getUriCompat(context)
            )
        }
        /*if (skippedUpdates.isNotEmpty()) {
            notifier.showUpdateSkippedNotification(skippedUpdates.size)
        }*/
    }

    /**
     * Updates the current location coordinates.
     *
     * @return errors if any
     */
    private suspend fun updateCoordinates(): List<RefreshError> {
        return refreshHelper.updateCurrentCoordinates(context, true)
    }

    /**
     * Updates the location with updated coordinates and reverse geocoding.
     *
     * @param location the location to update.
     * @return location updated.
     */
    private suspend fun updateLocation(location: Location): LocationResult {
        return refreshHelper.getLocation(context, location)
    }

    /**
     * Updates the weather for the given location and adds them to the database.
     *
     * @param location the location to update.
     * @return weather.
     */
    private suspend fun updateWeather(
        location: Location,
        coordinatesChanged: Boolean,
        ignoreCaching: Boolean,
    ): WeatherResult {
        return refreshHelper.getWeather(
            context,
            location,
            coordinatesChanged,
            ignoreCaching
        )
    }

    private suspend fun withUpdateNotification(
        updatingLocation: CopyOnWriteArrayList<Location>,
        completed: AtomicInteger,
        location: Location,
        block: suspend () -> Unit,
    ) {
        coroutineScope {
            ensureActive()

            updatingLocation.add(location)
            notifier.showProgressNotification(
                updatingLocation,
                completed.get(),
                locationsToUpdate.size
            )

            block()

            ensureActive()

            updatingLocation.remove(location)
            completed.getAndIncrement()
            notifier.showProgressNotification(
                updatingLocation,
                completed.get(),
                locationsToUpdate.size
            )
        }
    }

    /**
     * Writes basic file of update errors to cache dir.
     */
    private fun writeErrorFile(errors: List<Pair<Location, String?>>): File {
        try {
            if (errors.isNotEmpty()) {
                val file = context.createFileInCacheDir("breezyweather_update_errors.txt")
                file.bufferedWriter().use { out ->
                    out.write("Errors during refresh\n\n")
                    // Error file format:
                    // ! Location
                    //   - Error
                    errors.groupBy({ it.first }, { it.second }).forEach { (location, errors) ->
                        out.write("\n! ${location.getPlace(context, showCurrentPositionInPriority = true)}\n")
                        errors.forEach {
                            out.write("  - $it\n")
                        }
                    }
                }
                return file
            }
        } catch (_: Exception) {}
        return File("")
    }

    companion object {
        private const val TAG = "WeatherUpdate"
        private const val WORK_NAME_AUTO = "WeatherUpdate-auto"
        private const val WORK_NAME_MANUAL = "WeatherUpdate-manual"

        /**
         * Key for location to update.
         */
        private const val KEY_LOCATION = "location"

        private const val MINUTES_PER_HOUR: Long = 60
        private const val BACKOFF_DELAY_MINUTES: Long = 10

        fun cancelAllWorks(context: Context) {
            context.workManager.cancelAllWorkByTag(TAG)
        }

        fun setupTask(
            context: Context,
        ) {
            val settings = SettingsManager.getInstance(context)
            val pollingRate = settings.updateInterval.interval
            if (pollingRate != null && pollingRate > 15.minutes) {
                val constraints = Constraints(
                    requiredNetworkType = NetworkType.CONNECTED,
                    requiresBatteryNotLow = settings.ignoreUpdatesWhenBatteryLow
                )

                val request = PeriodicWorkRequestBuilder<WeatherUpdateJob>(
                    pollingRate.inWholeMinutes,
                    TimeUnit.MINUTES,
                    BACKOFF_DELAY_MINUTES,
                    TimeUnit.MINUTES
                )
                    .addTag(TAG)
                    .addTag(WORK_NAME_AUTO)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                    .build()

                context.workManager.enqueueUniquePeriodicWork(
                    WORK_NAME_AUTO,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            } else {
                context.workManager.cancelUniqueWork(WORK_NAME_AUTO)
            }
        }

        fun startNow(
            context: Context,
            location: Location? = null,
        ): Boolean {
            val wm = context.workManager
            if (wm.isRunning(TAG)) {
                // Already running either as a scheduled or manual job
                return false
            }

            val inputData = workDataOf(
                KEY_LOCATION to location?.formattedId
            )
            val request = OneTimeWorkRequestBuilder<WeatherUpdateJob>()
                .addTag(TAG)
                .addTag(WORK_NAME_MANUAL)
                .setInputData(inputData)
                .build()
            wm.enqueueUniqueWork(WORK_NAME_MANUAL, ExistingWorkPolicy.KEEP, request)

            return true
        }

        fun stop(context: Context) {
            val wm = context.workManager
            val workQuery = WorkQuery.Builder.fromTags(listOf(TAG))
                .addStates(listOf(WorkInfo.State.RUNNING))
                .build()
            wm.getWorkInfos(workQuery).get()
                // Should only return one work but just in case
                .forEach {
                    wm.cancelWorkById(it.id)

                    // Re-enqueue cancelled scheduled work
                    if (it.tags.contains(WORK_NAME_AUTO)) {
                        setupTask(context)
                    }
                }
        }
    }
}
