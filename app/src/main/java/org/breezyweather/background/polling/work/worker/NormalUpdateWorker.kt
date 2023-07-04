package org.breezyweather.background.polling.work.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.breezyweather.common.basic.models.Location
import org.breezyweather.location.LocationHelper
import org.breezyweather.remoteviews.Notifications.updateNotificationIfNecessary
import org.breezyweather.remoteviews.Widgets.updateWidgetIfNecessary
import org.breezyweather.weather.WeatherHelper

@HiltWorker
class NormalUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    locationHelper: LocationHelper,
    weatherHelper: WeatherHelper
) : AsyncUpdateWorker(context, workerParams, locationHelper, weatherHelper) {
    override fun updateView(context: Context, location: Location) {
        updateWidgetIfNecessary(context, location)
    }

    override fun updateView(context: Context, locationList: List<Location>) {
        updateWidgetIfNecessary(context, locationList)
        updateNotificationIfNecessary(context, locationList)
    }

    @SuppressLint("RestrictedApi")
    override fun handleUpdateResult(future: SettableFuture<Result>?, failed: Boolean) {
        future?.set(if (failed) Result.retry() else Result.success())
    }
}
