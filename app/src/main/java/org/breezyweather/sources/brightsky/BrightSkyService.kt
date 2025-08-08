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

package org.breezyweather.sources.brightsky

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.brightsky.json.BrightSkyAlert
import org.breezyweather.sources.brightsky.json.BrightSkyAlertsResult
import org.breezyweather.sources.brightsky.json.BrightSkyCurrentWeather
import org.breezyweather.sources.brightsky.json.BrightSkyCurrentWeatherResult
import org.breezyweather.sources.brightsky.json.BrightSkyWeather
import org.breezyweather.sources.brightsky.json.BrightSkyWeatherResult
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class BrightSkyService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") val client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ConfigurableSource {

    override val id = "brightsky"
    override val name = "Bright Sky (DWD) (${context.currentLocale.getCountryName("DE")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://brightsky.dev/"

    private val weatherAttribution = "Bright Sky, Data basis: Deutscher Wetterdienst. " +
        context.getString(R.string.data_modified, context.getString(R.string.breezy_weather))

    private val mApi: BrightSkyApi
        get() {
            return client
                .baseUrl(instance!!)
                .build()
                .create(BrightSkyApi::class.java)
        }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "Bright Sky" to "https://brightsky.dev/",
        "Deutscher Wetterdienst" to "https://www.dwd.de/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("DE", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val weather = if (SourceFeature.FORECAST in requestedFeatures) {
            val initialDate = Date().toTimezoneNoHour(location.timeZone)
            val date = initialDate.toCalendarWithTimeZone(location.timeZone).apply {
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0)
            }.time
            val lastDate = initialDate.toCalendarWithTimeZone(location.timeZone).apply {
                add(Calendar.DAY_OF_YEAR, 12)
                set(Calendar.HOUR_OF_DAY, 0)
            }.time

            mApi.getWeather(
                location.latitude,
                location.longitude,
                date.getFormattedDate("yyyy-MM-dd'T'HH:mm:ss", location),
                lastDate.getFormattedDate("yyyy-MM-dd'T'HH:mm:ss", location)
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(BrightSkyWeatherResult())
            }
        } else {
            Observable.just(BrightSkyWeatherResult())
        }

        val curWeather = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrentWeather(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(BrightSkyCurrentWeatherResult())
            }
        } else {
            Observable.just(BrightSkyCurrentWeatherResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(BrightSkyAlertsResult())
            }
        } else {
            Observable.just(BrightSkyAlertsResult())
        }

        return Observable.zip(weather, curWeather, alerts) { brightSkyWeather, brightSkyCurWeather, brightSkyAlerts ->
            val languageCode = context.currentLocale.code
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, brightSkyWeather.weather)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(brightSkyWeather.weather)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(brightSkyCurWeather.weather)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(brightSkyAlerts.alerts, languageCode)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    /**
     * Returns current weather
     */
    private fun getCurrent(result: BrightSkyCurrentWeather?): CurrentWrapper? {
        if (result == null) return null
        return CurrentWrapper(
            weatherCode = getWeatherCode(result.icon),
            temperature = TemperatureWrapper(
                temperature = result.temperature
            ),
            wind = Wind(
                degree = result.windDirection?.toDouble(),
                speed = result.windSpeed?.div(3.6),
                gusts = result.windGustSpeed?.div(3.6)
            ),
            relativeHumidity = result.relativeHumidity?.toDouble(),
            dewPoint = result.dewPoint,
            pressure = result.pressure,
            cloudCover = result.cloudCover,
            visibility = result.visibility?.toDouble()
        )
    }

    /**
     * Generate empty daily days from hourly weather since daily doesn't exist in API
     */
    private fun getDailyForecast(
        location: Location,
        weatherResult: List<BrightSkyWeather>?,
    ): List<DailyWrapper>? {
        if (weatherResult == null) return null

        val dailyList = mutableListOf<DailyWrapper>()
        val hourlyListByDay = weatherResult.groupBy {
            it.timestamp.getIsoFormattedDate(location)
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

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        weatherResult: List<BrightSkyWeather>?,
    ): List<HourlyWrapper>? {
        if (weatherResult == null) return null

        return weatherResult.map { result ->
            HourlyWrapper(
                date = result.timestamp,
                weatherCode = getWeatherCode(result.icon),
                temperature = TemperatureWrapper(
                    temperature = result.temperature
                ),
                precipitation = Precipitation(
                    total = result.precipitation
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.precipitationProbability?.toDouble()
                ),
                wind = Wind(
                    degree = result.windDirection?.toDouble(),
                    speed = result.windSpeed?.div(3.6),
                    gusts = result.windGustSpeed?.div(3.6)
                ),
                relativeHumidity = result.relativeHumidity?.toDouble(),
                dewPoint = result.dewPoint,
                pressure = result.pressure,
                cloudCover = result.cloudCover,
                visibility = result.visibility?.toDouble(),
                sunshineDuration = result.sunshine?.div(60)
            )
        }
    }

    /**
     * Returns alerts
     */
    private fun getAlertList(alertList: List<BrightSkyAlert>?, languageCode: String): List<Alert>? {
        if (alertList.isNullOrEmpty()) return null
        return alertList.map { alert ->
            Alert(
                alertId = alert.id.toString(),
                startDate = alert.onset,
                endDate = alert.expires,
                headline = if (languageCode == "de") alert.headlineDe else alert.headlineEn,
                description = if (languageCode == "de") alert.descriptionDe else alert.descriptionEn,
                instruction = if (languageCode == "de") alert.instructionDe else alert.instructionEn,
                source = "Deutscher Wetterdienst",
                severity = when (alert.severity?.lowercase()) {
                    "extreme" -> AlertSeverity.EXTREME
                    "severe" -> AlertSeverity.SEVERE
                    "moderate" -> AlertSeverity.MODERATE
                    "minor" -> AlertSeverity.MINOR
                    else -> AlertSeverity.UNKNOWN
                },
                color = when (alert.severity?.lowercase()) {
                    "extreme" -> Color.rgb(241, 48, 255)
                    "severe" -> Color.rgb(255, 48, 48)
                    "moderate" -> Color.rgb(255, 179, 48)
                    "minor" -> Color.rgb(255, 238, 48)
                    else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                }
            )
        }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        return when (icon) {
            "clear-day", "clear-night" -> WeatherCode.CLEAR
            "partly-cloudy-day", "partly-cloudy-night" -> WeatherCode.PARTLY_CLOUDY
            "cloudy" -> WeatherCode.CLOUDY
            "fog" -> WeatherCode.FOG
            "wind" -> WeatherCode.WIND
            "rain" -> WeatherCode.RAIN
            "sleet" -> WeatherCode.SLEET
            "snow" -> WeatherCode.SNOW
            "hail" -> WeatherCode.HAIL
            "thunderstorm" -> WeatherCode.THUNDERSTORM
            else -> null
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    override val isConfigured = true
    override val isRestricted = false
    private var instance: String?
        set(value) {
            value?.let {
                config.edit().putString("instance", it).apply()
            } ?: config.edit().remove("instance").apply()
        }
        get() = config.getString("instance", null) ?: BRIGHT_SKY_BASE_URL
    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_bright_sky_instance,
                summary = { _, content ->
                    content.ifEmpty {
                        BRIGHT_SKY_BASE_URL
                    }
                },
                content = if (instance != BRIGHT_SKY_BASE_URL) instance else null,
                placeholder = BRIGHT_SKY_BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    instance = if (it == BRIGHT_SKY_BASE_URL) null else it.ifEmpty { null }
                }
            )
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val BRIGHT_SKY_BASE_URL = "https://api.brightsky.dev/"
    }
}
