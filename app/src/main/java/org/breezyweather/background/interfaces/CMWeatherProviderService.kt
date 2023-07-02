package org.breezyweather.background.interfaces

import cyanogenmod.providers.WeatherContract
import cyanogenmod.weather.RequestInfo
import cyanogenmod.weather.WeatherInfo
import cyanogenmod.weather.WeatherInfo.DayForecast
import cyanogenmod.weatherservice.ServiceRequest
import cyanogenmod.weatherservice.ServiceRequestResult
import cyanogenmod.weatherservice.WeatherProviderService
import dagger.hilt.android.AndroidEntryPoint
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.location.LocationHelper
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.WeatherHelper
import org.breezyweather.weather.WeatherHelper.OnRequestWeatherListener
import javax.inject.Inject

/**
 * CM weather provider service.
 */
@AndroidEntryPoint
class CMWeatherProviderService : WeatherProviderService(), OnRequestWeatherListener {
    private var mRequest: ServiceRequest? = null
    var mLocationHelper: LocationHelper? = null
        @Inject set
    var mWeatherHelper: WeatherHelper? = null
        @Inject set

    private val locationListener: LocationHelper.OnRequestLocationListener =
        object : LocationHelper.OnRequestLocationListener {
            override fun requestLocationSuccess(requestLocation: Location) {
                if (mRequest != null) {
                    mWeatherHelper!!.requestWeather(
                        this@CMWeatherProviderService,
                        requestLocation,
                        this@CMWeatherProviderService
                    )
                }
            }

            override fun requestLocationFailed(requestLocation: Location, requestErrorType: RequestErrorType) {
                mRequest?.fail()
            }
        }
    private val weatherLocationListener: WeatherHelper.OnRequestLocationListener =
        object : WeatherHelper.OnRequestLocationListener {
            override fun requestLocationSuccess(query: String, locationList: List<Location>) {
                if (mRequest != null) {
                    if (locationList.isNotEmpty()) {
                        mWeatherHelper!!.requestWeather(
                            this@CMWeatherProviderService,
                            locationList[0],
                            this@CMWeatherProviderService
                        )
                    } else {
                        requestLocationFailed(query, RequestErrorType.WEATHER_REQ_FAILED)
                    }
                }
            }

            override fun requestLocationFailed(query: String, requestErrorType: RequestErrorType) {
                mRequest?.fail()
            }
        }

    override fun onCreate() {
        super.onCreate()
        mRequest = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest()
    }

    override fun onRequestSubmitted(serviceRequest: ServiceRequest) {
        cancelRequest()
        mRequest = serviceRequest
        val info = serviceRequest.requestInfo
        when (info.requestType) {
            RequestInfo.TYPE_WEATHER_BY_WEATHER_LOCATION_REQ -> requestWeather(info.weatherLocation.city)
            RequestInfo.TYPE_WEATHER_BY_GEO_LOCATION_REQ -> requestLocation()
            RequestInfo.TYPE_LOOKUP_CITY_NAME_REQ -> requestWeather(info.cityName)
            else -> serviceRequest.fail()
        }
    }

    override fun onRequestCancelled(serviceRequest: ServiceRequest) {
        cancelRequest()
    }

    // control.
    private fun requestLocation() {
        mLocationHelper!!.requestLocation(
            this,
            Location.buildLocal(this),
            true,
            locationListener
        )
    }

    private fun requestWeather(cityName: String) {
        if (cityName.isNotEmpty()) {
            val weatherSource = SettingsManager.getInstance(this).weatherSource
            mWeatherHelper!!.requestSearchLocations(this, cityName, weatherSource, weatherLocationListener)
        } else if (mRequest != null) {
            mRequest!!.fail()
        }
    }

    private fun cancelRequest() {
        mRequest = null
        mLocationHelper!!.cancel()
        mWeatherHelper!!.cancel()
    }

    // interface.
    // on request weather listener.
    override fun requestWeatherSuccess(requestLocation: Location) {
        try {
            val weather = requestLocation.weather
            if (mRequest != null && weather?.current?.temperature?.temperature != null) {
                val forecastList: MutableList<DayForecast> = ArrayList()
                weather.dailyForecast.forEach { daily ->
                    forecastList.add(
                        DayForecast.Builder(
                            WeatherConditionConvertHelper.getConditionCode(daily.day?.weatherCode, true)
                        ).apply {
                            daily.day?.temperature?.temperature?.toDouble()?.let { setHigh(it) }
                            daily.night?.temperature?.temperature?.toDouble()?.let { setLow(it) }
                        }.build()
                    )
                }
                val builder = WeatherInfo.Builder(
                    requestLocation.getCityName(applicationContext),
                    weather.current.temperature.temperature.toDouble(),
                    WeatherContract.WeatherColumns.TempUnit.CELSIUS
                ).apply {
                    setWeatherCondition(
                        WeatherConditionConvertHelper.getConditionCode(
                            weather.current.weatherCode,
                            requestLocation.isDaylight
                        )
                    )
                    weather.dailyForecast.getOrNull(0)?.day?.temperature?.temperature?.toDouble()?.let { setTodaysHigh(it) }
                    weather.dailyForecast.getOrNull(0)?.night?.temperature?.temperature?.toDouble()?.let { setTodaysLow(it) }
                    setTimestamp(weather.base.updateDate.time)
                    weather.current.relativeHumidity?.toDouble()?.let { setHumidity(it) }
                    if (weather.current.wind?.speed != null && weather.current.wind.degree?.degree != null) {
                        setWind(
                            weather.current.wind.speed.toDouble(),
                            weather.current.wind.degree.degree.toDouble(),
                            WeatherContract.WeatherColumns.WindSpeedUnit.KPH
                        )
                    }
                    setForecast(forecastList)
                }
                mRequest!!.complete(ServiceRequestResult.Builder(builder.build()).build())
            }
        } catch (ignore: Exception) {
            requestWeatherFailed(requestLocation, RequestErrorType.WEATHER_REQ_FAILED)
        }
    }

    override fun requestWeatherFailed(requestLocation: Location, requestErrorType: RequestErrorType) {
        mRequest?.fail()
    }
}

internal object WeatherConditionConvertHelper {
    fun getConditionCode(code: WeatherCode?, dayTime: Boolean): Int {
        return when (code) {
            WeatherCode.CLEAR -> return if (dayTime) {
                WeatherContract.WeatherColumns.WeatherCode.SUNNY
            } else WeatherContract.WeatherColumns.WeatherCode.CLEAR_NIGHT
            WeatherCode.PARTLY_CLOUDY -> return if (dayTime) {
                WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_DAY
            } else WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_NIGHT
            WeatherCode.CLOUDY -> return WeatherContract.WeatherColumns.WeatherCode.CLOUDY
            WeatherCode.RAIN -> return WeatherContract.WeatherColumns.WeatherCode.SHOWERS
            WeatherCode.SNOW -> return WeatherContract.WeatherColumns.WeatherCode.SNOW
            WeatherCode.WIND -> return WeatherContract.WeatherColumns.WeatherCode.WINDY
            WeatherCode.FOG -> return WeatherContract.WeatherColumns.WeatherCode.FOGGY
            WeatherCode.HAZE -> return WeatherContract.WeatherColumns.WeatherCode.HAZE
            WeatherCode.SLEET -> return WeatherContract.WeatherColumns.WeatherCode.SLEET
            WeatherCode.HAIL -> return WeatherContract.WeatherColumns.WeatherCode.HAIL
            WeatherCode.THUNDER -> return WeatherContract.WeatherColumns.WeatherCode.THUNDERSTORMS
            WeatherCode.THUNDERSTORM -> return WeatherContract.WeatherColumns.WeatherCode.THUNDERSHOWER
            else -> WeatherContract.WeatherColumns.WeatherCode.NOT_AVAILABLE
        }
    }
}
