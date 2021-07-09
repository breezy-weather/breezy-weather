package wangdaye.com.geometricweather.background.interfaces

import android.text.TextUtils
import cyanogenmod.providers.WeatherContract
import cyanogenmod.weather.RequestInfo
import cyanogenmod.weather.WeatherInfo
import cyanogenmod.weather.WeatherInfo.DayForecast
import cyanogenmod.weatherservice.ServiceRequest
import cyanogenmod.weatherservice.ServiceRequestResult
import cyanogenmod.weatherservice.WeatherProviderService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import wangdaye.com.geometricweather.GeometricWeather
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode
import wangdaye.com.geometricweather.location.LocationHelper
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.weather.WeatherHelper
import java.util.*
import javax.inject.Inject

/**
 * CM weather provider service.
 */
@AndroidEntryPoint
class CMWeatherProviderService : WeatherProviderService() {

    private var request: ServiceRequest? = null

    @Inject lateinit var locationHelper: LocationHelper
    @Inject lateinit var weatherHelper: WeatherHelper

    private var job: Job? = null

    private object WeatherConditionConvertHelper {
        fun getConditionCode(code: WeatherCode?, dayTime: Boolean): Int {
            when (code) {
                WeatherCode.CLEAR -> return if (dayTime) {
                    WeatherContract.WeatherColumns.WeatherCode.SUNNY
                } else {
                    WeatherContract.WeatherColumns.WeatherCode.CLEAR_NIGHT
                }
                WeatherCode.PARTLY_CLOUDY -> return if (dayTime) {
                    WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_DAY
                } else {
                    WeatherContract.WeatherColumns.WeatherCode.PARTLY_CLOUDY_NIGHT
                }
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
            }
            return WeatherContract.WeatherColumns.WeatherCode.NOT_AVAILABLE
        }
    }

    override fun onCreate() {
        super.onCreate()
        request = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelRequest()
    }

    override fun onRequestSubmitted(serviceRequest: ServiceRequest) {
        cancelRequest()

        request = serviceRequest

        val info = serviceRequest.requestInfo
        when (info.requestType) {
            RequestInfo.TYPE_WEATHER_BY_WEATHER_LOCATION_REQ -> requestUpdate(info.weatherLocation.city)
            RequestInfo.TYPE_WEATHER_BY_GEO_LOCATION_REQ -> requestUpdate()
            RequestInfo.TYPE_LOOKUP_CITY_NAME_REQ -> requestUpdate(info.cityName)
            else -> serviceRequest.fail()
        }
    }

    override fun onRequestCancelled(serviceRequest: ServiceRequest) {
        cancelRequest()
    }

    // control.

    private fun requestUpdate(cityName: String? = null) = GeometricWeather.instance.applicationScope.launch {
        val context = this@CMWeatherProviderService

        val location = withContext(Dispatchers.IO) {

            if (TextUtils.isEmpty(cityName)) {
                val response = locationHelper.getLocation(
                        context, Location.buildLocal(), true)
                response.result
            } else {
                val response = weatherHelper.getLocation(
                        context,
                        cityName!!,
                        listOf(SettingsManager.getInstance(context).getWeatherSource())
                )
                response.result?.let { it[0] }
            }
        }

        location?.let {
            it.weather = withContext(Dispatchers.IO) {
                weatherHelper.getWeather(context, it).result
            }
            completeRequest(it)
            return@launch
        }

        request?.fail()
    }

    private fun completeRequest(requestLocation: Location) {
        try {
            val weather = requestLocation.weather
            if (request != null && weather != null) {
                val forecastList: MutableList<DayForecast> = ArrayList()
                for (i in weather.dailyForecast.indices) {
                    forecastList.add(
                            DayForecast.Builder(
                                    WeatherConditionConvertHelper.getConditionCode(
                                            weather.dailyForecast[i].day().weatherCode,
                                            true
                                    )
                            ).setHigh(weather.dailyForecast[i].day().temperature.temperature.toDouble())
                                    .setHigh(weather.dailyForecast[i].night().temperature.temperature.toDouble())
                                    .build()
                    )
                }
                val builder = WeatherInfo.Builder(
                        requestLocation.getCityName(applicationContext),
                        weather.current.temperature.temperature.toDouble(),
                        WeatherContract.WeatherColumns.TempUnit.CELSIUS
                ).setWeatherCondition(
                        WeatherConditionConvertHelper.getConditionCode(
                                weather.current.weatherCode,
                                requestLocation.isDaylight
                        )
                ).setTodaysHigh(weather.dailyForecast[0].day().temperature.temperature.toDouble())
                        .setTodaysLow(weather.dailyForecast[0].night().temperature.temperature.toDouble())
                        .setTimestamp(weather.base.timeStamp)
                if (weather.current.relativeHumidity != null) {
                    builder.setHumidity(weather.current.relativeHumidity!!.toDouble())
                }
                if (weather.current.wind.speed != null) {
                    builder.setWind(
                            weather.current.wind.speed!!.toDouble(),
                            weather.current.wind.degree.degree.toDouble(),
                            WeatherContract.WeatherColumns.WindSpeedUnit.KPH
                    ).setForecast(forecastList)
                }
                request?.complete(ServiceRequestResult.Builder(builder.build()).build())
            }
        } catch (ignore: Exception) {
            request?.fail()
        }
    }

    private fun cancelRequest() {
        request = null

        job?.cancel()
        job = null
    }
}