package wangdaye.com.geometricweather.weather.services

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.settings.SettingsManager
import wangdaye.com.geometricweather.weather.apis.AtmoAuraIqaApi
import wangdaye.com.geometricweather.weather.apis.MfWeatherApi
import wangdaye.com.geometricweather.weather.converters.MfResultConverter
import wangdaye.com.geometricweather.weather.json.mf.*
import java.util.*
import javax.inject.Inject

/**
 * Mf weather service.
 */
class MfWeatherService @Inject constructor(
    private val mfApi: MfWeatherApi,
    private val atmoAuraApi: AtmoAuraIqaApi
) : WeatherService() {

    override suspend fun getWeather(context: Context, location: Location): Weather? = coroutineScope {
        val languageCode = SettingsManager.getInstance(context).getLanguage().code

        val current = async {
            mfApi.getCurrent(
                    location.latitude.toDouble(),
                    location.longitude.toDouble(),
                    languageCode,
                    SettingsManager.getInstance(context).getProviderMfWsftKey(true)
            )
        }
        val forecast = async {
            mfApi.getForecast(
                    location.latitude.toDouble(),
                    location.longitude.toDouble(),
                    languageCode,
                    SettingsManager.getInstance(context).getProviderMfWsftKey(true)
            )
        }

        // TODO: Will allow us to display forecast for day and night in daily
        //Observable<MfForecastResult> dayNightForecast = api.getForecastInstants(
        //        location.getLatitude(), location.getLongitude(), languageCode, "afternoon,night", SettingsManager.getInstance(context).getProviderMfWsftKey(true));
        val ephemeris = async {
            mfApi.getEphemeris(
                    location.latitude.toDouble(),
                    location.longitude.toDouble(),
                    "en",
                    SettingsManager.getInstance(context).getProviderMfWsftKey(true)
            )
        }
        // English required to convert moon phase
        val rain = async {
            mfApi.getRain(
                    location.latitude.toDouble(),
                    location.longitude.toDouble(),
                    languageCode,
                    SettingsManager.getInstance(context).getProviderMfWsftKey(true)
            )
        }
        val warnings = async {
            mfApi.getWarnings(
                    location.province,
                    null,
                    SettingsManager.getInstance(context).getProviderMfWsftKey(true)
            )
        }
        val aqiAtmoAura = async {
            try {
                if (location.province == "Auvergne-Rhône-Alpes"
                        || location.province == "01"
                        || location.province == "03"
                        || location.province == "07"
                        || location.province == "15"
                        || location.province == "26"
                        || location.province == "38"
                        || location.province == "42"
                        || location.province == "43"
                        || location.province == "63"
                        || location.province == "69"
                        || location.province == "73"
                        || location.province == "74") {
                    atmoAuraApi.getQAFull(
                            SettingsManager.getInstance(context).getProviderIqaAtmoAuraKey(true),
                            location.latitude.toString(),
                            location.longitude.toString()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }

        return@coroutineScope MfResultConverter.convert(
                context,
                location,
                current.await(),
                forecast.await(),
                ephemeris.await(),
                rain.await(),
                warnings.await(),
                aqiAtmoAura.await()
        ).result
    }

    override suspend fun getLocation(context: Context, query: String): List<Location> = coroutineScope {
        val resultList = mfApi.callWeatherLocation(query, 48.86, 2.34, SettingsManager.getInstance(context).getProviderMfWsftKey(true))

        val locationList: MutableList<Location> = ArrayList()
        for (r in resultList) {
            if (r.postCode != null) {
                locationList.add(MfResultConverter.convert(null, r))
            }
        }
        return@coroutineScope locationList
    }

    override suspend fun getLocation(context: Context, location: Location): List<Location> = coroutineScope {
        val languageCode = SettingsManager.getInstance(context).getLanguage().code

        val result = mfApi.getForecastV2(
                location.latitude.toDouble(),
                location.longitude.toDouble(),
                languageCode,
                SettingsManager.getInstance(context).getProviderMfWsftKey(true)
        )

        val locationList = ArrayList<Location>()
        if (result.properties.insee != null) {
            locationList.add(MfResultConverter.convert(null, result))
        }

        return@coroutineScope locationList
    }
}