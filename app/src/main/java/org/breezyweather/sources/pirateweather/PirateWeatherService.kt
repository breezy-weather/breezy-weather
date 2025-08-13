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

package org.breezyweather.sources.pirateweather

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.DailyCloudCover
import breezyweather.domain.weather.model.DailyDewPoint
import breezyweather.domain.weather.model.DailyPressure
import breezyweather.domain.weather.model.DailyRelativeHumidity
import breezyweather.domain.weather.model.DailyVisibility
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.pirateweather.json.PirateWeatherAlert
import org.breezyweather.sources.pirateweather.json.PirateWeatherCurrently
import org.breezyweather.sources.pirateweather.json.PirateWeatherDaily
import org.breezyweather.sources.pirateweather.json.PirateWeatherHourly
import org.breezyweather.sources.pirateweather.json.PirateWeatherMinutely
import org.breezyweather.unit.distance.Distance.Companion.kilometers
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import retrofit2.Retrofit
import java.util.Objects
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

class PirateWeatherService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ConfigurableSource {

    override val id = "pirateweather"
    override val name = "PirateWeather"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://pirate-weather.apiable.io/privacy"

    private val mApi by lazy {
        client
            .baseUrl(instance!!)
            .build()
            .create(PirateWeatherApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to name,
        SourceFeature.CURRENT to name,
        SourceFeature.MINUTELY to name,
        SourceFeature.ALERT to name
    )
    override val attributionLinks = mapOf(
        name to "https://pirateweather.net/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()

        return mApi.getForecast(
            apiKey,
            location.latitude,
            location.longitude,
            "si", // represents metric
            context.currentLocale.code,
            null,
            "hourly"
        ).map {
            WeatherWrapper(
                /*base = Base(
                    publishDate = it.currently?.time?.seconds?.inWholeMilliseconds?.toDate() ?: Date()
                ),*/
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(it.daily?.data)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(it.hourly?.data)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(it.currently, it.daily?.summary, it.hourly?.summary)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyForecast(it.minutely?.data)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(it.alerts)
                } else {
                    null
                }
            )
        }
    }

    /**
     * Returns current weather
     */
    private fun getCurrent(
        result: PirateWeatherCurrently?,
        dailySummary: String?,
        hourlySummary: String?,
    ): CurrentWrapper? {
        if (result == null) return null
        return CurrentWrapper(
            weatherText = result.summary,
            weatherCode = getWeatherCode(result.icon),
            temperature = TemperatureWrapper(
                temperature = result.temperature,
                feelsLike = result.apparentTemperature
            ),
            wind = Wind(
                degree = result.windBearing,
                speed = result.windSpeed,
                gusts = result.windGust
            ),
            uV = UV(index = result.uvIndex),
            relativeHumidity = result.humidity?.times(100),
            dewPoint = result.dewPoint,
            pressure = result.pressure?.hectopascals,
            cloudCover = result.cloudCover?.times(100)?.roundToInt(),
            visibility = result.visibility?.kilometers,
            dailyForecast = dailySummary,
            hourlyForecast = hourlySummary
        )
    }

    private fun getDailyForecast(
        dailyResult: List<PirateWeatherDaily>?,
    ): List<DailyWrapper>? {
        return dailyResult?.map { result ->
            DailyWrapper(
                date = result.time.seconds.inWholeMilliseconds.toDate(),
                day = HalfDayWrapper(
                    weatherSummary = result.summary,
                    weatherCode = getWeatherCode(result.icon),
                    temperature = TemperatureWrapper(
                        temperature = result.temperatureHigh,
                        feelsLike = result.apparentTemperatureHigh
                    )
                ),
                night = HalfDayWrapper(
                    weatherSummary = result.summary,
                    weatherCode = getWeatherCode(result.icon),
                    // temperatureLow/High are always forward-looking
                    // See https://docs.pirateweather.net/en/latest/API/#temperaturelow
                    temperature = TemperatureWrapper(
                        temperature = result.temperatureLow,
                        feelsLike = result.apparentTemperatureLow
                    )
                ),
                uV = UV(index = result.uvIndex),
                relativeHumidity = DailyRelativeHumidity(average = result.humidity?.times(100)),
                dewPoint = DailyDewPoint(average = result.dewPoint),
                pressure = DailyPressure(average = result.pressure?.hectopascals),
                cloudCover = DailyCloudCover(average = result.cloudCover?.times(100)?.roundToInt()),
                visibility = DailyVisibility(average = result.visibility?.kilometers)
            )
        }
    }

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        hourlyResult: List<PirateWeatherHourly>?,
    ): List<HourlyWrapper>? {
        return hourlyResult?.map { result ->
            HourlyWrapper(
                date = result.time.seconds.inWholeMilliseconds.toDate(),
                weatherText = result.summary,
                weatherCode = getWeatherCode(result.icon),
                temperature = TemperatureWrapper(
                    temperature = result.temperature,
                    feelsLike = result.apparentTemperature
                ),
                // see https://docs.pirateweather.net/en/latest/API/#precipaccumulation
                precipitation = Precipitation(
                    total = result.precipAccumulation?.times(10),
                    rain = result.liquidAccumulation?.times(10),
                    snow = result.snowAccumulation?.times(10),
                    ice = result.iceAccumulation?.times(10)
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.precipProbability?.times(100)
                ),
                wind = Wind(
                    degree = result.windBearing,
                    speed = result.windSpeed,
                    gusts = result.windGust
                ),
                uV = UV(
                    index = result.uvIndex
                ),
                relativeHumidity = result.humidity?.times(100),
                dewPoint = result.dewPoint,
                pressure = result.pressure?.hectopascals,
                cloudCover = result.cloudCover?.times(100)?.roundToInt(),
                visibility = result.visibility?.kilometers
            )
        }
    }

    /**
     * Returns minutely forecast
     * Copied from openweather implementation
     */
    private fun getMinutelyForecast(minutelyResult: List<PirateWeatherMinutely>?): List<Minutely>? {
        if (minutelyResult.isNullOrEmpty()) return null
        val minutelyList = mutableListOf<Minutely>()
        minutelyResult.forEachIndexed { i, minutelyForecast ->
            minutelyList.add(
                Minutely(
                    date = minutelyForecast.time.seconds.inWholeMilliseconds.toDate(),
                    minuteInterval = if (i < minutelyResult.size - 1) {
                        ((minutelyResult[i + 1].time - minutelyForecast.time) / 60).toDouble().roundToInt()
                    } else {
                        ((minutelyForecast.time - minutelyResult[i - 1].time) / 60).toDouble().roundToInt()
                    },
                    precipitationIntensity = minutelyForecast.precipIntensity
                )
            )
        }
        return minutelyList
    }

    /**
     * Returns alerts
     */
    private fun getAlertList(alertList: List<PirateWeatherAlert>?): List<Alert>? {
        if (alertList.isNullOrEmpty()) return null
        return alertList.map { alert ->
            val severity = when (alert.severity?.lowercase()) {
                "extreme" -> AlertSeverity.EXTREME
                "severe" -> AlertSeverity.SEVERE
                "moderate" -> AlertSeverity.MODERATE
                "minor" -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            }
            Alert(
                // Create unique ID from: title, severity, start time
                alertId = Objects.hash(alert.title, alert.severity, alert.start).toString(),
                startDate = alert.start.seconds.inWholeMilliseconds.toDate(),
                endDate = alert.end.seconds.inWholeMilliseconds.toDate(),
                headline = alert.title,
                description = alert.description,
                source = alert.uri,
                severity = severity,
                color = Alert.colorFromSeverity(severity)
            )
        }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        return when (icon) {
            "rain" -> WeatherCode.RAIN
            "sleet" -> WeatherCode.SLEET
            "snow" -> WeatherCode.SNOW
            "fog" -> WeatherCode.FOG
            "wind" -> WeatherCode.WIND
            "clear-day", "clear-night" -> WeatherCode.CLEAR
            "partly-cloudy-day", "partly-cloudy-night" -> WeatherCode.PARTLY_CLOUDY
            "cloudy" -> WeatherCode.CLOUDY
            else -> null
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var instance: String?
        set(value) {
            value?.let {
                config.edit().putString("instance", it).apply()
            } ?: config.edit().remove("instance").apply()
        }
        get() = config.getString("instance", null)
            ?: if (BreezyWeather.instance.debugMode) PIRATE_WEATHER_DEV_BASE_URL else PIRATE_WEATHER_BASE_URL
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.PIRATE_WEATHER_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_pirate_weather_instance,
                summary = { _, content ->
                    content.ifEmpty {
                        if (BreezyWeather.instance.debugMode) PIRATE_WEATHER_DEV_BASE_URL else PIRATE_WEATHER_BASE_URL
                    }
                },
                content = if (instance != if (BreezyWeather.instance.debugMode) {
                        PIRATE_WEATHER_DEV_BASE_URL
                    } else {
                        PIRATE_WEATHER_BASE_URL
                    }
                ) {
                    instance
                } else {
                    null
                },
                placeholder = if (BreezyWeather.instance.debugMode) {
                    PIRATE_WEATHER_DEV_BASE_URL
                } else {
                    PIRATE_WEATHER_BASE_URL
                },
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    instance = if (it == if (BreezyWeather.instance.debugMode) {
                            PIRATE_WEATHER_DEV_BASE_URL
                        } else {
                            PIRATE_WEATHER_BASE_URL
                        }
                    ) {
                        null
                    } else {
                        it.ifEmpty { null }
                    }
                }
            ),
            EditTextPreference(
                titleId = R.string.settings_weather_source_pirate_weather_api_key,
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
        private const val PIRATE_WEATHER_BASE_URL = "https://api.pirateweather.net/"
        private const val PIRATE_WEATHER_DEV_BASE_URL = "https://dev.pirateweather.net/"
    }
}
