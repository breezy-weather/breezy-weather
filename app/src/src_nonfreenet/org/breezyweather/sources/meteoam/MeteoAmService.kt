/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.meteoam

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.sources.meteoam.json.MeteoAmForecastResult
import org.breezyweather.sources.meteoam.json.MeteoAmForecastStats
import org.breezyweather.sources.meteoam.json.MeteoAmObservationResult
import org.breezyweather.sources.meteoam.json.MeteoAmReverseLocation
import org.breezyweather.sources.meteoam.json.MeteoAmReverseLocationResult
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class MeteoAmService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : MeteoAmServiceStub(context) {

    override val privacyPolicyUrl = "https://www.meteoam.it/it/privacy-policy"

    private val mApi by lazy {
        client
            .baseUrl(METEOAM_BASE_URL)
            .build()
            .create(MeteoAmApi::class.java)
    }

    override val attributionLinks = mapOf(
        "Servizio Meteorologico dell’Aeronautica Militare" to "https://www.meteoam.it/",
        "www.meteoam.it" to "https://www.meteoam.it/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(MeteoAmForecastResult())
            }
        } else {
            Observable.just(MeteoAmForecastResult())
        }
        val observation = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(MeteoAmObservationResult())
            }
        } else {
            Observable.just(MeteoAmObservationResult())
        }
        return Observable.zip(forecast, observation) {
                forecastResult: MeteoAmForecastResult,
                observationResult: MeteoAmObservationResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, forecastResult.extrainfo?.stats)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(
                        context,
                        forecastResult.timeseries,
                        forecastResult.paramlist,
                        forecastResult.datasets?.data
                    )
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    val oParams = observationResult.paramlist
                    val observationData = observationResult.datasets?.getOrElse("0") { null }
                    observationData?.let { oParams?.let { getCurrent(context, oParams, observationData) } }
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(
        context: Context,
        params: List<String>,
        currentResult: Map<String, Map<String, Any?>>,
    ): CurrentWrapper {
        val keys = mutableMapOf<String, String>()
        for (i in params.indices) {
            keys[params[i]] = i.toString()
        }

        val icon = currentResult.getOrElse(keys["icon"].toString()) { null }?.getOrElse("0") { null }.toString()
        return CurrentWrapper(
            weatherText = getWeatherText(context, icon),
            weatherCode = getWeatherCode(icon),
            temperature = TemperatureWrapper(
                temperature = (
                    currentResult.getOrElse(keys["2t"].toString()) { null }?.getOrElse("0") { null } as? Double
                    )?.celsius
            ),
            wind = Wind(
                degree = currentResult.getOrElse(keys["wdir"].toString()) { null }
                    ?.getOrElse("0") { null }?.let {
                        it as? Double ?: if (it == "VRB") -1.0 else null
                    },
                speed = (
                    currentResult.getOrElse(keys["wkmh"].toString()) { null }?.getOrElse("0") { null } as? Double
                    )?.kilometersPerHour
            ),
            relativeHumidity = (
                currentResult.getOrElse(keys["r"].toString()) { null }?.getOrElse("0") { null } as? Double
                )?.percent,
            pressure = (currentResult.getOrElse(keys["pmsl"].toString()) { null }?.getOrElse("0") { null } as? Double)
                ?.hectopascals
        )
    }

    private fun getDailyForecast(
        context: Context,
        dailyResult: List<MeteoAmForecastStats>?,
    ): List<DailyWrapper> {
        return dailyResult
            ?.filter { it.icon != "-" } // Safe to skip: no data for this day when no icon
            ?.mapIndexed { i, result ->
                DailyWrapper(
                    date = result.localDate,
                    day = HalfDayWrapper(
                        weatherText = getWeatherText(context, result.icon),
                        weatherCode = getWeatherCode(result.icon)
                    ),
                    night = HalfDayWrapper(
                        weatherText = getWeatherText(context, result.icon),
                        weatherCode = getWeatherCode(result.icon)
                    )
                )
            } ?: emptyList()
    }

    private fun getHourlyForecast(
        context: Context,
        timeseries: List<Date>?,
        params: List<String>?,
        data: Map<String, Map<String, Any?>>?,
    ): List<HourlyWrapper> {
        if (timeseries.isNullOrEmpty() || params.isNullOrEmpty() || data.isNullOrEmpty()) {
            return emptyList()
        }

        val keys = mutableMapOf<String, String>()
        for (i in params.indices) {
            keys[params[i]] = i.toString()
        }
        return timeseries.indices.map { i ->
            val icon = data.getOrElse(keys["icon"].toString()) { null }?.getOrElse(i.toString()) { null }.toString()
            HourlyWrapper(
                date = timeseries[i],
                weatherText = getWeatherText(context, icon),
                weatherCode = getWeatherCode(icon),
                temperature = TemperatureWrapper(
                    temperature = (
                        data.getOrElse(keys["2t"].toString()) { null }?.getOrElse(i.toString()) { null } as? Double
                        )?.celsius
                ),
                precipitationProbability = PrecipitationProbability(
                    total = (
                        data.getOrElse(keys["tpp"].toString()) { null }?.getOrElse(i.toString()) { null } as? Double
                        )?.percent
                ),
                wind = Wind(
                    degree = data.getOrElse(keys["wdir"].toString()) { null }?.getOrElse(i.toString()) { null }?.let {
                        it as? Double ?: if (it == "VRB") -1.0 else null
                    },
                    speed = (
                        data.getOrElse(keys["wkmh"].toString()) { null }?.getOrElse(i.toString()) { null } as? Double
                        )?.kilometersPerHour
                ),
                relativeHumidity = (
                    data.getOrElse(keys["r"].toString()) { null }?.getOrElse(i.toString()) { null } as? Double
                    )?.percent,
                pressure = (
                    data.getOrElse(keys["pmsl"].toString()) { null }?.getOrElse(i.toString()) { null } as? Double
                    )?.hectopascals
            )
        }
    }

    // Icon definitions can be found at https://www.meteoam.it/it/legenda-simboli
    // Icon sources can be found at https://www-static.meteoam.it/maps/img/icon_web_v4/{icon}.svg
    // Icons 06 and 07 are duplicates
    private fun getWeatherCode(icon: String?): WeatherCode? {
        return when (icon) {
            "01", "31" -> WeatherCode.CLEAR
            "02", "03", "04", "32", "33", "34" -> WeatherCode.PARTLY_CLOUDY
            "05", "06", "07", "35" -> WeatherCode.CLOUDY
            "08", "09" -> WeatherCode.RAIN
            "10" -> WeatherCode.THUNDERSTORM
            "11", "12" -> WeatherCode.SLEET
            "13", "18", "36" -> WeatherCode.HAZE
            "14" -> WeatherCode.FOG
            "15" -> WeatherCode.HAIL
            "16" -> WeatherCode.SNOW
            "17", "19" -> WeatherCode.WIND
            else -> null
        }
    }

    // Icon definitions can be found at https://www.meteoam.it/it/legenda-simboli
    // Icon sources can be found at https://www-static.meteoam.it/maps/img/icon_web_v4/{icon}.svg
    // Icons 06 and 07 are duplicates
    private fun getWeatherText(context: Context, icon: String?): String? {
        return when (icon) {
            "01", "31" -> context.getString(R.string.meteoam_weather_text_clear_sky) // "Sereno"
            // "Parzialmente velato" o "Velato":
            "02", "03", "32", "33" -> context.getString(R.string.meteoam_weather_text_mainly_cloudy)
            "04", "34" -> context.getString(R.string.meteoam_weather_text_partly_cloudy) // "Poco nuvoloso"
            "05", "35" -> context.getString(R.string.meteoam_weather_text_cloudy) // "Molto nuvoloso"
            "06", "07" -> context.getString(R.string.meteoam_weather_text_overcast) // "Coperto"
            "08" -> context.getString(R.string.meteoam_weather_text_rain_light) // "Pioggia debole"
            "09" -> context.getString(R.string.meteoam_weather_text_rain_heavy) // "Pioggia forte"
            "10" -> context.getString(R.string.meteoam_weather_text_thunderstorm) // "Temporale"
            "11" -> context.getString(R.string.meteoam_weather_text_rain_snow_mixed) // "Pioggia mista a neve"
            "12" -> context.getString(R.string.meteoam_weather_text_rain_freezing) // "Pioggia che gela"
            "13", "36" -> context.getString(R.string.meteoam_weather_text_haze) // "Foschia"
            "14" -> context.getString(R.string.meteoam_weather_text_fog) // "Nebbia"
            "15" -> context.getString(R.string.meteoam_weather_text_hail) // "Grandine"
            "16" -> context.getString(R.string.meteoam_weather_text_snow) // "Neve"
            "17" -> context.getString(R.string.meteoam_weather_text_tornado_watersprout) // "Tromba d’aria o marina"
            "18" -> context.getString(R.string.meteoam_weather_text_smoke) // "Fumo"
            "19" -> context.getString(R.string.meteoam_weather_text_sand_storm) // "Tempesta di sabbia"
            else -> null
        }
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val reverseLocation = mApi.getReverseLocation(latitude, longitude)
        val forecast = mApi.getForecast(latitude, longitude)
        val locationList = mutableListOf<LocationAddressInfo>()
        return Observable.zip(reverseLocation, forecast) {
                reverseLocationResult: MeteoAmReverseLocationResult,
                forecastResult: MeteoAmForecastResult,
            ->
            val resultsWithCountryCode = reverseLocationResult.results?.filter { it.countryCode != null }
            if (!resultsWithCountryCode.isNullOrEmpty()) {
                locationList.add(
                    convert(resultsWithCountryCode[0], forecastResult.extrainfo?.timezone)
                )
            }
            locationList
        }
    }

    private fun convert(
        reverseLocation: MeteoAmReverseLocation,
        timezone: String?,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            timeZoneId = timezone,
            country = reverseLocation.country,
            countryCode = reverseLocation.countryCode!!,
            admin2 = reverseLocation.county,
            city = reverseLocation.city
        )
    }

    companion object {
        private const val METEOAM_BASE_URL = "https://api.meteoam.it/"
    }
}
