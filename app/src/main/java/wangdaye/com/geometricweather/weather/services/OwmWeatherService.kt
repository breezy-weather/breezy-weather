package wangdaye.com.geometricweather.weather.services

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.weather.apis.OwmApi
import wangdaye.com.geometricweather.weather.converters.OwmResultConverter
import java.util.*
import javax.inject.Inject

/**
 * Owm weather service.
 */
class OwmWeatherService @Inject constructor(private val api: OwmApi) : WeatherService() {

    override suspend fun getWeather(context: Context, location: Location): Weather? = coroutineScope {
        val languageCode = SettingsManager.getInstance(context).getLanguage().code
        val oneCall = async {
            api.getOneCall(
                    SettingsManager.getInstance(context).getProviderOwmKey(true),
                    location.latitude.toDouble(),
                    location.longitude.toDouble(),
                    "metric",
                    languageCode
            )
        }
        val airPollutionCurrent = async {
            try {
                api.getAirPollutionCurrent(
                        SettingsManager.getInstance(context).getProviderOwmKey(true),
                        location.latitude.toDouble(),
                        location.longitude.toDouble()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        val airPollutionForecast = async {
            try {
                api.getAirPollutionForecast(
                        SettingsManager.getInstance(context).getProviderOwmKey(true),
                        location.latitude.toDouble(),
                        location.longitude.toDouble()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        return@coroutineScope OwmResultConverter.convert(
                context,
                location,
                oneCall.await(),
                airPollutionCurrent.await(),
                airPollutionForecast.await()
        ).result
    }

    override suspend fun getLocation(context: Context, query: String): List<Location> = coroutineScope {
        val resultList = api.callWeatherLocation(SettingsManager.getInstance(context).getProviderOwmKey(true), query)

        val zipCode = if (query.matches(Regex("[a-zA-Z0-9]*"))) {
            query
        } else {
            null
        }

        val locationList: MutableList<Location> = ArrayList()
        for (r in resultList) {
            locationList.add(OwmResultConverter.convert(null, r, zipCode))
        }
        return@coroutineScope locationList
    }

    override suspend fun getLocation(context: Context, location: Location): List<Location> = coroutineScope {
        val result = api.getWeatherLocationByGeoPosition(
                SettingsManager.getInstance(context).getProviderOwmKey(true),
                location.latitude.toDouble(),
                location.longitude.toDouble()
        )

        val locationList = ArrayList<Location>()
        locationList.add(OwmResultConverter.convert(location, result[0], null))

        return@coroutineScope locationList
    }
}