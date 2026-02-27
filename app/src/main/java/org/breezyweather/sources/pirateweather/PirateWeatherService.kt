/*
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
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.pirateweather.json.PirateWeatherAlert
import org.breezyweather.sources.pirateweather.json.PirateWeatherCurrently
import org.breezyweather.sources.pirateweather.json.PirateWeatherDaily
import org.breezyweather.sources.pirateweather.json.PirateWeatherDayNight
import org.breezyweather.sources.pirateweather.json.PirateWeatherHourly
import org.breezyweather.sources.pirateweather.json.PirateWeatherMinutely
import org.breezyweather.unit.distance.Distance.Companion.kilometers
import org.breezyweather.unit.distance.Distance.Companion.miles
import org.breezyweather.unit.precipitation.Precipitation.Companion.centimeters
import org.breezyweather.unit.precipitation.Precipitation.Companion.inches
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.fraction
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.speed.Speed.Companion.milesPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import org.breezyweather.unit.temperature.Temperature.Companion.fahrenheit
import org.breezyweather.unit.temperature.TemperatureUnit
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

        // Using temperature unit setting as a proxy of whether to use metric or imperial units.
        // This is because Pirate Weather's daily summary text reports temperature rather than precipitation amounts.
        // NB: Its hourly summary text reports precipitation amounts.
        val metric = SettingsManager.getInstance(context).getTemperatureUnit(context) != TemperatureUnit.FAHRENHEIT

        // Make sure user language is supported by Pirate Weather. Fall back to English otherwise.
        var lang = context.currentLocale.code.lowercase()
        if (lang in PIRATE_WEATHER_LANGUAGE_REPLACEMENTS.keys) lang = PIRATE_WEATHER_LANGUAGE_REPLACEMENTS[lang]!!
        if (lang !in PIRATE_WEATHER_LANGUAGES) lang = "en"

        return mApi.getForecast(
            apikey = apiKey,
            lat = location.latitude,
            lon = location.longitude,
            units = if (metric) "si" else "us",
            lang = lang,
            include = "day_night_forecast",
            exclude = null,
            extend = "hourly",
            icon = "pirate",
            version = "2"
        ).map {
            WeatherWrapper(
                /*base = Base(
                    publishDate = it.currently?.time?.seconds?.inWholeMilliseconds?.toDate() ?: Date()
                ),*/
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(it.daily?.data, it.dayNight?.data, metric, context)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(it.hourly?.data, metric)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(it.currently, it.daily?.summary, it.hourly?.summary, metric)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyForecast(it.minutely?.data, metric)
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
        metric: Boolean = true,
    ): CurrentWrapper? {
        if (result == null) return null
        return CurrentWrapper(
            weatherText = result.summary,
            weatherCode = getWeatherCode(result.icon),
            temperature = if (metric) {
                TemperatureWrapper(
                    temperature = result.temperature?.celsius,
                    feelsLike = result.apparentTemperature?.celsius
                )
            } else {
                TemperatureWrapper(
                    temperature = result.temperature?.fahrenheit,
                    feelsLike = result.apparentTemperature?.fahrenheit
                )
            },
            wind = if (metric) {
                Wind(
                    degree = result.windBearing,
                    speed = result.windSpeed?.metersPerSecond,
                    gusts = result.windGust?.metersPerSecond
                )
            } else {
                Wind(
                    degree = result.windBearing,
                    speed = result.windSpeed?.milesPerHour,
                    gusts = result.windGust?.milesPerHour
                )
            },
            uV = UV(index = result.uvIndex),
            relativeHumidity = result.humidity?.fraction,
            dewPoint = if (metric) result.dewPoint?.celsius else result.dewPoint?.fahrenheit,
            pressure = result.pressure?.hectopascals,
            cloudCover = result.cloudCover?.fraction,
            visibility = if (metric) result.visibility?.kilometers else result.visibility?.miles,
            dailyForecast = dailySummary,
            hourlyForecast = hourlySummary
        )
    }

    private fun getDailyForecast(
        dailyResult: List<PirateWeatherDaily>?,
        dayNightResult: List<PirateWeatherDayNight>?,
        metric: Boolean = true,
        context: Context,
    ): List<DailyWrapper>? {
        return dailyResult?.map { result ->
            val dayPart = dayNightResult?.filter { it.time > result.time }?.minByOrNull { it.time }
            val nightPart = dayNightResult?.filter {
                it.time > (dayPart?.time ?: Long.MAX_VALUE)
            }?.minByOrNull { it.time }
            DailyWrapper(
                date = result.time.seconds.inWholeMilliseconds.toDate(),
                day = HalfDayWrapper(
                    weatherText = getWeatherText(dayPart?.icon, context),
                    weatherSummary = dayPart?.summary,
                    weatherCode = getWeatherCode(dayPart?.icon),
                    temperature = if (metric) {
                        TemperatureWrapper(
                            temperature = dayPart?.temperature?.celsius,
                            feelsLike = dayPart?.apparentTemperature?.celsius
                        )
                    } else {
                        TemperatureWrapper(
                            temperature = dayPart?.temperature?.fahrenheit,
                            feelsLike = dayPart?.temperature?.fahrenheit
                        )
                    },
                    precipitation = if (metric) {
                        Precipitation(
                            rain = dayPart?.liquidAccumulation?.centimeters,
                            snow = dayPart?.snowAccumulation?.centimeters,
                            ice = dayPart?.iceAccumulation?.centimeters
                        )
                    } else {
                        Precipitation(
                            rain = dayPart?.liquidAccumulation?.inches,
                            snow = dayPart?.snowAccumulation?.inches,
                            ice = dayPart?.iceAccumulation?.inches
                        )
                    },
                    precipitationProbability = PrecipitationProbability(
                        total = dayPart?.precipProbability?.fraction
                    ),
                    wind = if (metric) {
                        Wind(
                            degree = dayPart?.windBearing,
                            speed = dayPart?.windSpeed?.metersPerSecond,
                            gusts = dayPart?.windGust?.metersPerSecond
                        )
                    } else {
                        Wind(
                            degree = dayPart?.windBearing,
                            speed = dayPart?.windSpeed?.milesPerHour,
                            gusts = dayPart?.windSpeed?.milesPerHour
                        )
                    }
                ),
                night = HalfDayWrapper(
                    weatherText = getWeatherText(nightPart?.icon, context),
                    weatherSummary = nightPart?.summary,
                    weatherCode = getWeatherCode(nightPart?.icon),
                    temperature = if (metric) {
                        TemperatureWrapper(
                            temperature = nightPart?.temperature?.celsius,
                            feelsLike = nightPart?.apparentTemperature?.celsius
                        )
                    } else {
                        TemperatureWrapper(
                            temperature = nightPart?.temperature?.fahrenheit,
                            feelsLike = nightPart?.temperature?.fahrenheit
                        )
                    },
                    precipitation = if (metric) {
                        Precipitation(
                            rain = nightPart?.liquidAccumulation?.centimeters,
                            snow = nightPart?.snowAccumulation?.centimeters,
                            ice = nightPart?.iceAccumulation?.centimeters
                        )
                    } else {
                        Precipitation(
                            rain = nightPart?.liquidAccumulation?.inches,
                            snow = nightPart?.snowAccumulation?.inches,
                            ice = nightPart?.iceAccumulation?.inches
                        )
                    },
                    precipitationProbability = PrecipitationProbability(
                        total = nightPart?.precipProbability?.fraction
                    ),
                    wind = if (metric) {
                        Wind(
                            degree = nightPart?.windBearing,
                            speed = nightPart?.windSpeed?.metersPerSecond,
                            gusts = nightPart?.windGust?.metersPerSecond
                        )
                    } else {
                        Wind(
                            degree = nightPart?.windBearing,
                            speed = nightPart?.windSpeed?.milesPerHour,
                            gusts = nightPart?.windSpeed?.milesPerHour
                        )
                    }
                ),
                uV = UV(index = result.uvIndex),
                relativeHumidity = DailyRelativeHumidity(average = result.humidity?.fraction),
                dewPoint = DailyDewPoint(
                    average = if (metric) result.dewPoint?.celsius else result.dewPoint?.fahrenheit
                ),
                pressure = DailyPressure(average = result.pressure?.hectopascals),
                cloudCover = DailyCloudCover(average = result.cloudCover?.fraction),
                visibility = DailyVisibility(
                    average = if (metric) result.visibility?.kilometers else result.visibility?.miles
                )
            )
        }
    }

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        hourlyResult: List<PirateWeatherHourly>?,
        metric: Boolean = true,
    ): List<HourlyWrapper>? {
        return hourlyResult?.map { result ->
            HourlyWrapper(
                date = result.time.seconds.inWholeMilliseconds.toDate(),
                weatherText = result.summary,
                weatherCode = getWeatherCode(result.icon),
                temperature = if (metric) {
                    TemperatureWrapper(
                        temperature = result.temperature?.celsius,
                        feelsLike = result.apparentTemperature?.celsius
                    )
                } else {
                    TemperatureWrapper(
                        temperature = result.temperature?.fahrenheit,
                        feelsLike = result.apparentTemperature?.fahrenheit
                    )
                },
                // see https://docs.pirateweather.net/en/latest/API/#precipaccumulation
                precipitation = if (metric) {
                    Precipitation(
                        rain = result.liquidAccumulation?.centimeters,
                        snow = result.snowAccumulation?.centimeters,
                        ice = result.iceAccumulation?.centimeters
                    )
                } else {
                    Precipitation(
                        rain = result.liquidAccumulation?.inches,
                        snow = result.snowAccumulation?.inches,
                        ice = result.iceAccumulation?.inches
                    )
                },
                precipitationProbability = PrecipitationProbability(
                    total = result.precipProbability?.fraction
                ),
                wind = if (metric) {
                    Wind(
                        degree = result.windBearing,
                        speed = result.windSpeed?.metersPerSecond,
                        gusts = result.windGust?.metersPerSecond
                    )
                } else {
                    Wind(
                        degree = result.windBearing,
                        speed = result.windSpeed?.milesPerHour,
                        gusts = result.windGust?.milesPerHour
                    )
                },
                uV = UV(
                    index = result.uvIndex
                ),
                relativeHumidity = result.humidity?.fraction,
                dewPoint = if (metric) result.dewPoint?.celsius else result.dewPoint?.fahrenheit,
                pressure = result.pressure?.hectopascals,
                cloudCover = result.cloudCover?.fraction,
                visibility = if (metric) result.visibility?.kilometers else result.visibility?.miles
            )
        }
    }

    /**
     * Returns minutely forecast
     * Copied from openweather implementation
     */
    private fun getMinutelyForecast(
        minutelyResult: List<PirateWeatherMinutely>?,
        metric: Boolean = true,
    ): List<Minutely>? {
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
                    precipitationIntensity = if (metric) {
                        minutelyForecast.precipIntensity?.millimeters
                    } else {
                        minutelyForecast.precipIntensity?.inches
                    }
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

    private fun getWeatherText(icon: String?, context: Context): String? {
        return when (icon) {
            "clear-day" -> context.getString(R.string.common_weather_text_clear_sky)
            "clear-night" -> context.getString(R.string.common_weather_text_clear_sky)
            "thunderstorm" -> context.getString(R.string.weather_kind_thunderstorm)
            "rain" -> context.getString(R.string.common_weather_text_rain)
            "snow" -> context.getString(R.string.common_weather_text_snow)
            "sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            "wind" -> context.getString(R.string.weather_kind_wind)
            "fog" -> context.getString(R.string.common_weather_text_fog)
            "cloudy" -> context.getString(R.string.common_weather_text_cloudy)
            "partly-cloudy-day" -> context.getString(R.string.common_weather_text_partly_cloudy)
            "partly-cloudy-night" -> context.getString(R.string.common_weather_text_partly_cloudy)
            "hail" -> context.getString(R.string.weather_kind_hail)
            "mostly-clear-day" -> context.getString(R.string.common_weather_text_mostly_clear)
            "mostly-clear-night" -> context.getString(R.string.common_weather_text_mostly_clear)
            "mostly-cloudy-day" -> context.getString(R.string.common_weather_text_mostly_cloudy)
            "mostly-cloudy-night" -> context.getString(R.string.common_weather_text_mostly_cloudy)
            // PW translations for below: "Possible rain/snow/sleet/precipitation/thunderstorm"
            "possible-rain-day" -> context.getString(R.string.common_weather_text_rain)
            "possible-rain-night" -> context.getString(R.string.common_weather_text_rain)
            "possible-snow-day" -> context.getString(R.string.common_weather_text_snow)
            "possible-snow-night" -> context.getString(R.string.common_weather_text_snow)
            "possible-sleet-day" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            "possible-sleet-night" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            "possible-precipitation-day" -> context.getString(R.string.precipitation)
            "possible-precipitation-night" -> context.getString(R.string.precipitation)
            "possible-thunderstorm-day" -> context.getString(R.string.weather_kind_thunderstorm)
            "possible-thunderstorm-night" -> context.getString(R.string.weather_kind_thunderstorm)
            "precipitation" -> context.getString(R.string.precipitation)
            "drizzle" -> context.getString(R.string.common_weather_text_drizzle)
            "light-rain" -> context.getString(R.string.common_weather_text_rain_light)
            "heavy-rain" -> context.getString(R.string.common_weather_text_rain_heavy)
            "flurries" -> context.getString(R.string.common_weather_text_snow_light) // PW translation: "Flurries"
            "light-snow" -> context.getString(R.string.common_weather_text_snow_light)
            "heavy-snow" -> context.getString(R.string.common_weather_text_snow_heavy)
            "very-slight-sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
            "light-sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
            "heavy-sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed_heavy)
            "breezy" -> context.getString(R.string.weather_kind_wind) // PW translation: "Breezy"
            "dangerous-wind" -> context.getString(R.string.weather_kind_wind) // PW translation: "Dangerously windy"
            "mist" -> context.getString(R.string.common_weather_text_mist)
            "haze" -> context.getString(R.string.weather_kind_haze)
            "smoke" -> context.getString(R.string.common_weather_text_smoke)
            "mixed" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            else -> null
        }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        return when (icon) {
            "precipitation", "possible-precipitation-day", "possible-precipitation-night",
            "rain", "possible-rain-day", "possible-rain-night",
            "drizzle", "light-rain", "heavy-rain",
            -> WeatherCode.RAIN
            "sleet", "possible-sleet-day", "possible-sleet-night", "mixed",
            "very-light-sleet", "light-sleet", "heavy-sleet",
            -> WeatherCode.SLEET
            "snow", "possible-snow-day", "possible-snow-night",
            "flurries", "light-snow", "heavy-snow",
            -> WeatherCode.SNOW
            "hail" -> WeatherCode.HAIL
            "fog", "mist" -> WeatherCode.FOG
            "haze", "smoke" -> WeatherCode.HAZE
            "wind", "breezy", "dangerous-wind" -> WeatherCode.WIND
            "clear-day", "clear-night", "mostly-clear-day", "mostly-clear-night" -> WeatherCode.CLEAR
            "partly-cloudy-day", "partly-cloudy-night" -> WeatherCode.PARTLY_CLOUDY
            "cloudy", "mostly-cloudy-day", "mostly-cloudy-night" -> WeatherCode.CLOUDY
            "thunderstorm", "possible-thunderstorm-day", "possible-thunderstorm-night" -> WeatherCode.THUNDERSTORM
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

    companion object {
        private const val PIRATE_WEATHER_BASE_URL = "https://api.pirateweather.net/"
        private const val PIRATE_WEATHER_DEV_BASE_URL = "https://dev.pirateweather.net/"
        private val PIRATE_WEATHER_LANGUAGES = arrayOf(
            "en", "ar", "az", "be", "bg", "bn", "bs", "ca", "cs", "cy", "da", "de", "el", "eo", "es", "et", "fa", "fi",
            "fr", "ga", "gd", "he", "hi", "hr", "hu", "id", "is", "it", "ja", "ka", "kn", "ko", "kw", "lv", "ml", "mr",
            "nl", "no", "pa", "pl", "pt", "ro", "ru", "sk", "sl", "sr", "sv", "ta", "te", "tet", "tr", "uk", "ur", "vi",
            "zh", "zh-tw"
        )
        private val PIRATE_WEATHER_LANGUAGE_REPLACEMENTS = mapOf(
            "en-au" to "en",
            "en-ca" to "en",
            "en-gb" to "en",
            "en-us" to "en",
            "in" to "id",
            "iw" to "he",
            "nb" to "no",
            "nb-no" to "no",
            "pt-br" to "pt",
            "sl-si" to "sl",
            "zh-cn" to "zh",
            "zh-hk" to "zh-tw",
            "zh-mo" to "zh-tw",
            "zh-my" to "zh",
            "zh-sg" to "zh"
        )
    }
}
