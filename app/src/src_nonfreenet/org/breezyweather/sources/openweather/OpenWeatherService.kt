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

package org.breezyweather.sources.openweather

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollution
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.sources.openweather.json.OpenWeatherForecast
import org.breezyweather.sources.openweather.json.OpenWeatherForecastResult
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.seconds

/**
 * OpenWeatherMap
 * No longer based on OneCall API as it is now billing-only
 */
class OpenWeatherService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ConfigurableSource {

    override val id = "openweather"
    override val name = "OpenWeather"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://openweather.co.uk/privacy-policy"

    private val mApi by lazy {
        client
            .baseUrl(OPEN_WEATHER_BASE_URL)
            .build()
            .create(OpenWeatherApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to name,
        SourceFeature.CURRENT to name,
        SourceFeature.AIR_QUALITY to name
    )
    override val attributionLinks = mapOf(
        name to "https://openweather.co.uk/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = context.currentLocale.code
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                apiKey,
                location.latitude,
                location.longitude,
                "metric",
                languageCode
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(OpenWeatherForecastResult())
            }
        } else {
            Observable.just(OpenWeatherForecastResult())
        }
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                apiKey,
                location.latitude,
                location.longitude,
                "metric",
                languageCode
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(OpenWeatherForecast())
            }
        } else {
            Observable.just(OpenWeatherForecast())
        }
        val airPollution = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mApi.getAirPollution(
                apiKey,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(OpenWeatherAirPollutionResult())
            }
        } else {
            Observable.just(OpenWeatherAirPollutionResult())
        }
        return Observable.zip(forecast, current, airPollution) {
                forecastResult: OpenWeatherForecastResult,
                currentResult: OpenWeatherForecast,
                airPollutionResult: OpenWeatherAirPollutionResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(forecastResult.list, location)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(forecastResult.list)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(currentResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(hourlyForecast = getHourlyAirQuality(airPollutionResult.list))
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(currentResult: OpenWeatherForecast): CurrentWrapper? {
        if (currentResult.dt == null) return null

        return CurrentWrapper(
            weatherText = currentResult.weather?.getOrNull(0)?.main?.capitalize(),
            weatherCode = getWeatherCode(currentResult.weather?.getOrNull(0)?.id),
            temperature = TemperatureWrapper(
                temperature = currentResult.main?.temp,
                feelsLike = currentResult.main?.feelsLike
            ),
            wind = Wind(
                degree = currentResult.wind?.deg?.toDouble(),
                speed = currentResult.wind?.speed,
                gusts = currentResult.wind?.gust
            ),
            relativeHumidity = currentResult.main?.humidity?.toDouble(),
            pressure = currentResult.main?.pressure?.hectopascals,
            cloudCover = currentResult.clouds?.all,
            visibility = currentResult.visibility?.toDouble()
        )
    }

    private fun getDailyList(
        hourlyResult: List<OpenWeatherForecast>?,
        location: Location,
    ): List<DailyWrapper> {
        if (hourlyResult.isNullOrEmpty()) return emptyList()
        val dailyList = mutableListOf<DailyWrapper>()
        val hourlyListByDay = hourlyResult.groupBy {
            it.dt!!.seconds.inWholeMilliseconds.toDate().getIsoFormattedDate(location)
        }
        for (i in 0 until hourlyListByDay.entries.size - 1) {
            val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.timeZone)
            if (dayDate != null) {
                dailyList.add(
                    DailyWrapper(
                        date = dayDate
                    )
                )
            }
        }
        return dailyList
    }

    private fun getHourlyList(
        hourlyResult: List<OpenWeatherForecast>?,
    ): List<HourlyWrapper>? {
        return hourlyResult?.map { result ->
            val theDate = result.dt!!.seconds.inWholeMilliseconds.toDate()
            HourlyWrapper(
                date = theDate,
                weatherText = result.weather?.getOrNull(0)?.main?.capitalize(),
                weatherCode = getWeatherCode(result.weather?.getOrNull(0)?.id),
                temperature = TemperatureWrapper(
                    temperature = result.main?.temp,
                    feelsLike = result.main?.feelsLike
                ),
                precipitation = Precipitation(
                    total = getTotalPrecipitation(result.rain?.cumul3h, result.snow?.cumul3h),
                    rain = result.rain?.cumul3h,
                    snow = result.snow?.cumul3h
                ),
                precipitationProbability = PrecipitationProbability(total = result.pop?.times(100.0)),
                wind = Wind(
                    degree = result.wind?.deg?.toDouble(),
                    speed = result.wind?.speed,
                    gusts = result.wind?.gust
                ),
                relativeHumidity = result.main?.humidity?.toDouble(),
                pressure = result.main?.pressure?.hectopascals,
                cloudCover = result.clouds?.all,
                visibility = result.visibility?.toDouble()
            )
        }
    }

    // Function that checks for null before sum up
    private fun getTotalPrecipitation(rain: Double?, snow: Double?): Double? {
        if (rain == null) {
            return snow
        }
        return if (snow == null) {
            rain
        } else {
            rain + snow
        }
    }

    private fun getWeatherCode(icon: Int?): WeatherCode? {
        return when (icon) {
            null -> null
            200, 201, 202 -> WeatherCode.THUNDERSTORM
            210, 211, 212 -> WeatherCode.THUNDER
            221, 230, 231, 232 -> WeatherCode.THUNDERSTORM
            300, 301, 302, 310, 311, 312, 313, 314, 321 -> WeatherCode.RAIN
            500, 501, 502, 503, 504 -> WeatherCode.RAIN
            511 -> WeatherCode.SLEET
            600, 601, 602 -> WeatherCode.SNOW
            611, 612, 613, 614, 615, 616 -> WeatherCode.SLEET
            620, 621, 622 -> WeatherCode.SNOW
            701, 711, 721, 731 -> WeatherCode.HAZE
            741 -> WeatherCode.FOG
            751, 761, 762 -> WeatherCode.HAZE
            771, 781 -> WeatherCode.WIND
            800 -> WeatherCode.CLEAR
            801, 802 -> WeatherCode.PARTLY_CLOUDY
            803, 804 -> WeatherCode.CLOUDY
            else -> null
        }
    }

    private fun getHourlyAirQuality(
        airPollutionResultList: List<OpenWeatherAirPollution>?,
    ): MutableMap<Date, AirQuality> {
        val airQualityHourly = mutableMapOf<Date, AirQuality>()
        airPollutionResultList?.forEach {
            airQualityHourly[it.dt.seconds.inWholeMilliseconds.toDate()] = AirQuality(
                pM25 = it.components?.pm25,
                pM10 = it.components?.pm10,
                sO2 = it.components?.so2,
                nO2 = it.components?.no2,
                o3 = it.components?.o3,
                cO = it.components?.co?.div(1000.0)
            )
        }
        return airQualityHourly
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.OPEN_WEATHER_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_open_weather_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            )
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/"
    }
}
