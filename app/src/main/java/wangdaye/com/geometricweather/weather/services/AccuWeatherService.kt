package wangdaye.com.geometricweather.weather.services

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.weather.apis.AccuWeatherApi
import wangdaye.com.geometricweather.weather.converters.AccuResultConverter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Accu weather service.
 */
class AccuWeatherService @Inject constructor(
    private val api: AccuWeatherApi,
) : WeatherService() {

    override suspend fun getWeather(context: Context, location: Location): Weather? = coroutineScope {
        val languageCode = SettingsManager.getInstance(context).getLanguage().code

        val realtime = async {
            api.callCurrent(
                    location.cityId,
                    SettingsManager.getInstance(context).getProviderAccuCurrentKey(true),
                    languageCode,
                    true
            )
        }
        val daily = async {
            api.callDaily(
                    location.cityId,
                    SettingsManager.getInstance(context).getProviderAccuWeatherKey(true),
                    languageCode,
                    metric = true,
                    details = true
            )
        }
        val hourly = async {
            api.callHourly(
                    location.cityId,
                    SettingsManager.getInstance(context).getProviderAccuWeatherKey(true),
                    languageCode,
                    true
            )
        }
        val minutely = async {
            try {
                api.callMinutely(
                        SettingsManager.getInstance(context).getProviderAccuWeatherKey(true),
                        languageCode,
                        true,
                        location.latitude.toString() + "," + location.longitude
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        val alert = async {
            try {
                api.callAlert(
                        location.cityId,
                        SettingsManager.getInstance(context).getProviderAccuWeatherKey(true),
                        languageCode,
                        true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                ArrayList()
            }
        }
        val aqi = async {
            try {
                api.callAirQuality(
                        location.cityId,
                        SettingsManager.getInstance(context).getProviderAccuAqiKey(true)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        return@coroutineScope AccuResultConverter.convert(
                context,
                location,
                realtime.await()[0],
                daily.await(),
                hourly.await(),
                minutely.await(),
                aqi.await(),
                alert.await()
        ).result
    }

    override suspend fun getLocation(context: Context, query: String): List<Location> = coroutineScope {
        val languageCode = SettingsManager.getInstance(context).getLanguage().code
        val resultList = api.callWeatherLocation(
                "Always",
                SettingsManager.getInstance(context).getProviderAccuWeatherKey(true),
                query,
                languageCode
        )

        val zipCode = if (query.matches(Regex("[a-zA-Z0-9]*"))) {
            query
        } else {
            null
        }

        val locationList = ArrayList<Location>()
        for (r in resultList) {
            locationList.add(AccuResultConverter.convert(null, r, zipCode))
        }

        return@coroutineScope locationList
    }

    override suspend fun getLocation(context: Context,
                                     location: Location): List<Location> = coroutineScope {
        val languageCode = SettingsManager.getInstance(context).getLanguage().code

        val result = api.callWeatherLocationByGeoPosition(
                "Always",
                SettingsManager.getInstance(context).getProviderAccuWeatherKey(true),
                location.latitude.toString() + "," + location.longitude,
                languageCode
        )

        val locationList = ArrayList<Location>()
        locationList.add(AccuResultConverter.convert(location, result, null))

        return@coroutineScope locationList
    }
}