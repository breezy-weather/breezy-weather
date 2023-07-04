package org.breezyweather.background.polling.work.worker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import org.breezyweather.background.polling.PollingUpdateHelper
import org.breezyweather.background.polling.PollingUpdateHelper.OnPollingUpdateListener
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.utils.helpers.ShortcutsHelper
import org.breezyweather.location.LocationHelper
import org.breezyweather.remoteviews.Notifications.checkAndSendAlert
import org.breezyweather.remoteviews.Notifications.checkAndSendPrecipitationForecast
import org.breezyweather.weather.WeatherHelper

abstract class AsyncUpdateWorker(
    context: Context,
    workerParams: WorkerParameters,
    locationHelper: LocationHelper,
    weatherHelper: WeatherHelper
) : AsyncWorker(context, workerParams), OnPollingUpdateListener {
    private val mPollingUpdateHelper by lazy {
        PollingUpdateHelper(context, locationHelper, weatherHelper).also {
            it.setOnPollingUpdateListener(this)
        }
    }
    private var mFuture: SettableFuture<Result>? = null
    private var mFailed = false

    override fun doAsyncWork(future: SettableFuture<Result>) {
        mFuture = future
        mFailed = false
        mPollingUpdateHelper.pollingUpdate()
    }

    // control.
    abstract fun updateView(context: Context, location: Location)
    abstract fun updateView(context: Context, locationList: List<Location>)

    /**
     * Call [SettableFuture.set] here.
     */
    abstract fun handleUpdateResult(future: SettableFuture<Result>?, failed: Boolean)

    // interface.
    // on polling update listener.
    override fun onUpdateCompleted(
        location: Location, old: Weather?, succeed: Boolean, index: Int, total: Int
    ) {
        if (index == 0) {
            updateView(applicationContext, location)
            if (succeed) {
                checkAndSendAlert(applicationContext, location, old)
                checkAndSendPrecipitationForecast(applicationContext, location)
            } else {
                mFailed = true
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onPollingCompleted(locationList: List<Location>) {
        updateView(applicationContext, locationList)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsHelper.refreshShortcutsInNewThread(applicationContext, locationList)
        }
        handleUpdateResult(mFuture, mFailed)
    }
}
