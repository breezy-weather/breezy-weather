package wangdaye.com.geometricweather.background.polling

import android.content.Context
import android.widget.Toast
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.common.bus.EventBus
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper
import wangdaye.com.geometricweather.db.DatabaseHelper
import wangdaye.com.geometricweather.location.LocationHelper
import wangdaye.com.geometricweather.weather.WeatherHelper
import wangdaye.com.geometricweather.weather.WeatherHelper.OnRequestWeatherListener

/**
 * Polling updateRotation helper.
 */
class PollingUpdateHelper(
    private val context: Context,
    private val locationHelper: LocationHelper,
    private val weatherHelper: WeatherHelper
) {
    private var ioController: AsyncHelper.Controller? = null
    private var locationList = emptyList<Location>().toMutableList()
    private var listener: OnPollingUpdateListener? = null

    interface OnPollingUpdateListener {

        fun onUpdateCompleted(
            location: Location,
            old: Weather?,
            succeed: Boolean,
            index: Int,
            total: Int
        )
        fun onPollingCompleted(locationList: List<Location>?)
    }

    // control.

    fun pollingUpdate() {
        ioController = AsyncHelper.runOnIO({ emitter ->
            val list = DatabaseHelper.getInstance(context).readLocationList().map {
                it.copy(weather = DatabaseHelper.getInstance(context).readWeather(it))
            }
            emitter.send(list, true)
        }, { locations: List<Location>?, _: Boolean ->
            locations?.let {
                locationList = it.toMutableList()
                requestData(0, false)
            }
        })
    }

    fun cancel() {
        ioController?.cancel()
        locationHelper.cancel()
        weatherHelper.cancel()
    }

    private fun requestData(position: Int, located: Boolean) {
        val old = locationList[position].weather

        if (old?.isValid(0.25f) == true) {
            locationList[position] = locationList[position].copy(weather = old)
            RequestWeatherCallback(position, locationList.size).requestWeatherSuccess(locationList[position])
            return
        }

        if (locationList[position].isCurrentPosition && !located) {
            locationHelper.requestLocation(
                context,
                locationList[position],
                true,
                RequestLocationCallback(position, locationList.size)
            )
            return
        }

        weatherHelper.requestWeather(
            context,
            locationList[position],
            RequestWeatherCallback(position, locationList.size)
        )
    }

    // interface.

    fun setOnPollingUpdateListener(l: OnPollingUpdateListener?) {
        listener = l
    }

    // on request location listener.

    private inner class RequestLocationCallback(
        private val index: Int,
        private val total: Int
    ) : LocationHelper.OnRequestLocationListener {

        override fun requestLocationSuccess(requestLocation: Location) {
            locationList[index] = requestLocation

            if (requestLocation.isUsable) {
                requestData(index, true)
            } else {
                requestLocationFailed(requestLocation)
                Toast.makeText(
                    context,
                    context.getString(R.string.feedback_not_yet_location),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun requestLocationFailed(requestLocation: Location) {
            if (locationList[index].isUsable) {
                requestData(index, true)
            } else {
                RequestWeatherCallback(index, total).requestWeatherFailed(locationList[index])
            }
        }
    }

    // on request weather listener.

    private inner class RequestWeatherCallback(
        private val index: Int,
        private val total: Int
    ) : OnRequestWeatherListener {

        override fun requestWeatherSuccess(requestLocation: Location) {
            val old = locationList[index].weather

            if (requestLocation.weather != null
                && (old == null || requestLocation.weather.base.timeStamp != old.base.timeStamp)) {
                locationList[index] = requestLocation

                EventBus.instance
                    .with(Location::class.java)
                    .postValue(requestLocation)

                listener?.onUpdateCompleted(requestLocation, old, true, index, total)

                checkToRequestNextOrCompleted()
            } else {
                requestWeatherFailed(requestLocation)
            }
        }

        override fun requestWeatherFailed(requestLocation: Location) {
            val old = locationList[index].weather
            locationList[index] = requestLocation

            listener?.onUpdateCompleted(requestLocation, old, false, index, total)

            checkToRequestNextOrCompleted()
        }

        private fun checkToRequestNextOrCompleted() {
            if (index + 1 < total) {
                requestData(index + 1, false)
            } else {
                listener?.onPollingCompleted(locationList)
            }
        }
    }
}