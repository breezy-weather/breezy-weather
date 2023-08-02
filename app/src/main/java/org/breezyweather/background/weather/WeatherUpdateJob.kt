package org.breezyweather.background.weather

import android.content.Context
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.breezyweather.common.extensions.getUriCompat
import org.breezyweather.common.extensions.createFileInCacheDir
import org.breezyweather.common.extensions.isRunning
import org.breezyweather.common.extensions.workManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.bus.EventBus
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.ApiLimitReachedException
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.MissingPermissionLocationBackgroundException
import org.breezyweather.common.exceptions.MissingPermissionLocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.exceptions.SourceNotInstalledException
import org.breezyweather.common.extensions.withIOContext
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.common.utils.helpers.ShortcutsHelper
import org.breezyweather.db.repositories.LocationEntityRepository
import org.breezyweather.db.repositories.WeatherEntityRepository
import org.breezyweather.sources.LocationHelper
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.settings.SettingsManager
import org.breezyweather.sources.WeatherHelper
import retrofit2.HttpException
import java.io.File
import java.net.SocketTimeoutException
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Based on Tachiyomi LibraryUpdateJob
 * Licensed under Apache License, Version 2.0
 * https://github.com/tachiyomiorg/tachiyomi/blob/88e9fefa59b3f7f77ab3ddcab1b039f81534c83e/app/src/main/java/eu/kanade/tachiyomi/data/library/LibraryUpdateJob.kt
 */
@HiltWorker
class WeatherUpdateJob @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationHelper: LocationHelper,
    private val weatherHelper: WeatherHelper
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

        try {
            setForeground(getForegroundInfo())
        } catch (e: IllegalStateException) {
            LogHelper.log(msg = "Not allowed to set foreground job")
        }

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
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notifier = WeatherUpdateNotifier(context)
        return ForegroundInfo(
            Notifications.ID_WEATHER_PROGRESS,
            notifier.progressNotificationBuilder.build(),
        )
    }

    /**
     * Adds list of locations to be updated.
     *
     * @param locationFormattedId the ID of the location to update, or null if all locations.
     */
    private fun addLocationToQueue(locationFormattedId: String?) {
        locationsToUpdate = if (locationFormattedId != null) {
            val location = LocationEntityRepository.readLocation(locationFormattedId)
            if (location != null) {
                listOf(location.copy(weather = WeatherEntityRepository.readWeather(location)))
            } else emptyList()
        } else {
            val locationList = LocationEntityRepository.readLocationList().toMutableList()
            for (i in locationList.indices) {
                locationList[i] = locationList[i].copy(
                    weather = WeatherEntityRepository.readWeather(locationList[i])
                )
            }
            locationList
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
        val semaphore = Semaphore(5)
        val progressCount = AtomicInteger(0)
        val currentlyUpdatingLocation = CopyOnWriteArrayList<Location>()
        val newUpdates = CopyOnWriteArrayList<Pair<Location, Location>>()
        val skippedUpdates = CopyOnWriteArrayList<Pair<Location, String?>>()
        val failedUpdates = CopyOnWriteArrayList<Pair<Location, String?>>()

        coroutineScope {
            locationsToUpdate.groupBy { it.weatherSource }.values
                .map { locationInSource ->
                    async {
                        semaphore.withPermit {
                            locationInSource.forEach { location ->
                                ensureActive()

                                withUpdateNotification(
                                    currentlyUpdatingLocation,
                                    progressCount,
                                    location,
                                ) {
                                    // TODO: Implement this, it’s a good idea
                                    /*if (location.updateStrategy != UpdateStrategy.ALWAYS_UPDATE) {
                                        skippedUpdates.add(location to context.getString(R.string.skipped_reason_not_always_update))
                                    } else {*/
                                    // Don’t refresh if we still have data from less than 15 minutes
                                    // Allow a by-pass when in debug mode to be able to debug test
                                    if (location.weather?.isValid(0.25f) == true && !BreezyWeather.instance.debugMode) {
                                        skippedUpdates.add(location to context.getString(R.string.weather_refresh_skipped_already_recently_updated))
                                    } else {
                                        try {
                                            val locationToProceed = if (location.isCurrentPosition) {
                                                try {
                                                    updateLocation(location)
                                                } catch (e: Throwable) {
                                                    // If we failed to locate, let this silently fail
                                                    // and refresh weather with latest known position
                                                    if (location.isUsable) location else throw e
                                                }
                                            } else location

                                            val weather = updateWeather(locationToProceed)
                                            newUpdates.add(location to locationToProceed.copy(weather = weather))
                                        } catch (e: Throwable) {
                                            val errorMessage = when (e) {
                                                is NoNetworkException -> context.getString(RequestErrorType.NETWORK_UNAVAILABLE.shortMessage)
                                                is HttpException -> {
                                                    when (e.code()) {
                                                        401, 403 -> context.getString(RequestErrorType.API_UNAUTHORIZED.shortMessage)
                                                        409, 429 -> context.getString(RequestErrorType.API_LIMIT_REACHED.shortMessage)
                                                        else -> {
                                                            e.printStackTrace()
                                                            if (e.message.isNullOrEmpty()) {
                                                                context.getString(RequestErrorType.WEATHER_REQ_FAILED.shortMessage)
                                                            } else e.message
                                                        }
                                                    }
                                                }
                                                is ApiLimitReachedException -> context.getString(RequestErrorType.API_LIMIT_REACHED.shortMessage)
                                                is SocketTimeoutException -> context.getString(RequestErrorType.SERVER_TIMEOUT.shortMessage)
                                                is ApiKeyMissingException -> context.getString(RequestErrorType.API_KEY_REQUIRED_MISSING.shortMessage)
                                                is InvalidLocationException -> context.getString(RequestErrorType.INVALID_LOCATION.shortMessage)
                                                is LocationException -> context.getString(RequestErrorType.LOCATION_FAILED.shortMessage)
                                                is MissingPermissionLocationException -> context.getString(RequestErrorType.ACCESS_LOCATION_PERMISSION_MISSING.shortMessage)
                                                is MissingPermissionLocationBackgroundException -> context.getString(RequestErrorType.ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING.shortMessage)
                                                is ReverseGeocodingException -> context.getString(RequestErrorType.REVERSE_GEOCODING_FAILED.shortMessage)
                                                is SecondaryWeatherException -> context.getString(RequestErrorType.SECONDARY_WEATHER_FAILED.shortMessage)
                                                is MissingFieldException, is SerializationException, is ParsingException -> {
                                                    e.printStackTrace()
                                                    if (e.message.isNullOrEmpty()) {
                                                        context.getString(RequestErrorType.PARSING_ERROR.shortMessage)
                                                    } else e.message
                                                }
                                                is SourceNotInstalledException -> context.getString(RequestErrorType.SOURCE_NOT_INSTALLED.shortMessage)
                                                else -> {
                                                    e.printStackTrace()
                                                    if (e.message.isNullOrEmpty()) {
                                                        context.getString(RequestErrorType.WEATHER_REQ_FAILED.shortMessage)
                                                    } else e.message
                                                }
                                            }

                                            failedUpdates.add(location to errorMessage)
                                        }
                                    }
                                    //}
                                }
                            }
                        }
                    }
                }
                .awaitAll()
        }

        notifier.cancelProgressNotification()

        if (newUpdates.isNotEmpty()) {
            // We updated at least one location, so we need to reload location list and make some post-actions
            val locationList = LocationEntityRepository.readLocationList().toMutableList()
            for (i in locationList.indices) {
                locationList[i] = locationList[i].copy(
                    weather = WeatherEntityRepository.readWeather(locationList[i])
                )
            }

            // Update widgets and notification-widget
            Widgets.updateWidgetIfNecessary(context, locationList)
            Notifications.updateNotificationIfNecessary(context, locationList)

            // Update shortcuts
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutsHelper.refreshShortcutsInNewThread(applicationContext, locationList)
            }

            val location = locationList[0]
            val indexOfFirstLocation =
                newUpdates.firstOrNull { it.first.formattedId == location.formattedId }

            // Send alert and precipitation for first location
            if (indexOfFirstLocation != null) {
                Widgets.updateWidgetIfNecessary(context, location)
                // TODO: We only send alert and precipitation forecast for first location for historical reason, but this should be reworked
                Notifications.checkAndSendAlert(applicationContext, location, locationsToUpdate.firstOrNull { it.formattedId == location.formattedId }?.weather)
                Notifications.checkAndSendPrecipitationForecast(applicationContext, location)
            }

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
                failedUpdates.size,
                errorFile.getUriCompat(context),
            )
        }
        /*if (skippedUpdates.isNotEmpty()) {
            notifier.showUpdateSkippedNotification(skippedUpdates.size)
        }*/
    }

    /**
     * Updates the current location.
     *
     * @param location the location to update.
     * @return location updated.
     */
    private suspend fun updateLocation(location: Location): Location {
        return locationHelper.getCurrentLocationWithReverseGeocoding(
            context,
            location,
            true
        )
    }

    /**
     * Updates the weather for the given location and adds them to the database.
     *
     * @param location the location to update.
     * @return weather.
     */
    private suspend fun updateWeather(location: Location): Weather {
        return weatherHelper.getWeather(
            context,
            location
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
                locationsToUpdate.size,
            )

            block()

            ensureActive()

            updatingLocation.remove(location)
            completed.getAndIncrement()
            notifier.showProgressNotification(
                updatingLocation,
                completed.get(),
                locationsToUpdate.size,
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
                    // ! Error
                    //   - Location
                    errors.groupBy({ it.second }, { it.first }).forEach { (error, locations) ->
                        out.write("\n! ${error}\n")
                        locations.forEach {
                            out.write("  - ${it.city}\n")
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
            context: Context
        ) {
            val settings = SettingsManager.getInstance(context)
            val pollingRate = settings.updateInterval.intervalInHour
            if (pollingRate != null && pollingRate > 0.25f) {
                val constraints = Constraints(
                    requiredNetworkType = NetworkType.CONNECTED,
                    requiresBatteryNotLow = settings.ignoreUpdatesWhenBatteryLow,
                )

                val request = PeriodicWorkRequestBuilder<WeatherUpdateJob>(
                    (pollingRate * MINUTES_PER_HOUR).toLong(),
                    TimeUnit.MINUTES,
                    BACKOFF_DELAY_MINUTES,
                    TimeUnit.MINUTES,
                )
                    .addTag(TAG)
                    .addTag(WORK_NAME_AUTO)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                    .build()

                context.workManager.enqueueUniquePeriodicWork(WORK_NAME_AUTO, ExistingPeriodicWorkPolicy.UPDATE, request)
            } else {
                context.workManager.cancelUniqueWork(WORK_NAME_AUTO)
            }
        }

        fun startNow(
            context: Context,
            location: Location? = null
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
