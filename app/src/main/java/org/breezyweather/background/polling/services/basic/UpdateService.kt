package org.breezyweather.background.polling.services.basic

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import org.breezyweather.background.polling.PollingUpdateHelper
import org.breezyweather.background.polling.PollingUpdateHelper.OnPollingUpdateListener
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.common.utils.helpers.ShortcutsHelper
import org.breezyweather.location.LocationHelper
import org.breezyweather.remoteviews.NotificationHelper
import org.breezyweather.weather.WeatherHelper
import javax.inject.Inject

abstract class UpdateService : Service(), OnPollingUpdateListener {
    private var mPollingHelper: PollingUpdateHelper? = null

    var mLocationHelper: LocationHelper? = null
        @Inject set

    var mWeatherHelper: WeatherHelper? = null
        @Inject set

    private var mDelayController: AsyncHelper.Controller? = null
    private var mFailed = false

    override fun onCreate() {
        super.onCreate()
        mFailed = false
        mPollingHelper = PollingUpdateHelper(this, mLocationHelper!!, mWeatherHelper!!).also {
            it.setOnPollingUpdateListener(this)
            it.pollingUpdate()
        }
        mDelayController = AsyncHelper.delayRunOnIO({ stopService(true) }, (30 * 1000).toLong())
    }

    override fun onDestroy() {
        super.onDestroy()
        mDelayController?.let {
            it.cancel()
            mDelayController = null
        }
        mPollingHelper?.let {
            it.setOnPollingUpdateListener(null)
            it.cancel()
            mPollingHelper = null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // control.
    abstract fun updateView(context: Context, location: Location)
    abstract fun updateView(context: Context, locationList: List<Location>)
    abstract fun handlePollingResult(updateSucceed: Boolean)
    open fun stopService(updateFailed: Boolean) {
        handlePollingResult(updateFailed)
        stopSelf()
    }

    // interface.
    // on polling update listener.
    override fun onUpdateCompleted(
        location: Location, old: Weather?,
        succeed: Boolean, index: Int, total: Int
    ) {
        if (index == 0) {
            updateView(this, location)
            if (succeed) {
                NotificationHelper.checkAndSendAlert(this, location, old)
                NotificationHelper.checkAndSendPrecipitationForecast(this, location)
            } else {
                mFailed = true
            }
        }
    }

    override fun onPollingCompleted(locationList: List<Location>) {
        updateView(this, locationList)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsHelper.refreshShortcutsInNewThread(this, locationList)
        }
        stopService(mFailed)
    }
}
