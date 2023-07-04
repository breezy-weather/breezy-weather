package org.breezyweather.background.polling.work.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.breezyweather.background.polling.PollingManager.resetTomorrowForecastBackgroundTask
import org.breezyweather.common.basic.models.Location
import org.breezyweather.location.LocationHelper
import org.breezyweather.remoteviews.presenters.notification.ForecastNotificationIMP.buildForecastAndSendIt
import org.breezyweather.remoteviews.presenters.notification.ForecastNotificationIMP.isEnabled
import org.breezyweather.weather.WeatherHelper

@HiltWorker
class TomorrowForecastUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    locationHelper: LocationHelper,
    weatherHelper: WeatherHelper
) : AsyncUpdateWorker(context, workerParams, locationHelper, weatherHelper) {
    override fun updateView(context: Context, location: Location) {
        if (isEnabled(context, false)) {
            buildForecastAndSendIt(context, location, false)
        }
    }

    override fun updateView(context: Context, locationList: List<Location>) {}
    @SuppressLint("RestrictedApi")
    override fun handleUpdateResult(future: SettableFuture<Result>?, failed: Boolean) {
        future?.set(if (failed) Result.failure() else Result.success())
        resetTomorrowForecastBackgroundTask(
            applicationContext,
            forceRefresh = false,
            nextDay = true
        )
    }
}
