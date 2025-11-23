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

package org.breezyweather.sources.metie

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.metie.json.MetIeForecastHourly
import org.breezyweather.sources.metie.json.MetIeForecastResult
import org.breezyweather.sources.metie.json.MetIeLocationResult
import org.breezyweather.sources.metie.json.MetIeWarning
import org.breezyweather.sources.metie.json.MetIeWarningResult
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named

/**
 * MET Éireann service
 */
class MetIeService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : MetIeServiceStub(context) {

    override val privacyPolicyUrl = "https://www.met.ie/about-us/privacy"

    private val mApi by lazy {
        client
            .baseUrl(MET_IE_BASE_URL)
            .build()
            .create(MetIeApi::class.java)
    }

    override val attributionLinks = mapOf(
        "Met Éireann" to "https://www.met.ie/",
        "met.ie" to "https://www.met.ie/",
        "Creative Commons Attribution 4.0 International (CC BY 4.0)" to "https://creativecommons.org/licenses/by/4.0/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            val calendar = Date().toCalendarWithTimeZone(location.timeZone)
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            formatter.timeZone = location.timeZone
            List(6) { day ->
                mApi.getForecast(
                    token = apiKey,
                    lat = location.latitude,
                    lon = location.longitude,
                    date = formatter.format(calendar.time),
                    src = MET_IE_SRC,
                    version = MET_IE_VERSION,
                    env = MET_IE_ENV
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.FORECAST] = it
                    Observable.just(MetIeForecastResult())
                }.also {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        } else {
            List(5) {
                Observable.just(MetIeForecastResult())
            }
        }
        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getWarnings(
                token = apiKey,
                src = MET_IE_SRC,
                version = MET_IE_VERSION,
                env = MET_IE_ENV
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(MetIeWarningResult())
            }
        } else {
            Observable.just(MetIeWarningResult())
        }

        return Observable.zip(
            forecast[0],
            forecast[1],
            forecast[2],
            forecast[3],
            forecast[4],
            forecast[5],
            alerts
        ) {
                forecastResultJ0: MetIeForecastResult,
                forecastResultJ1: MetIeForecastResult,
                forecastResultJ2: MetIeForecastResult,
                forecastResultJ3: MetIeForecastResult,
                forecastResultJ4: MetIeForecastResult,
                forecastResultJ5: MetIeForecastResult,
                alertsResult: MetIeWarningResult,
            ->
            val hourlyForecastResultMerged = forecastResultJ0.mergedForecast.orEmpty() +
                forecastResultJ1.mergedForecast.orEmpty() +
                forecastResultJ2.mergedForecast.orEmpty() +
                forecastResultJ3.mergedForecast.orEmpty() +
                forecastResultJ4.mergedForecast.orEmpty() +
                forecastResultJ5.mergedForecast.orEmpty()

            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, hourlyForecastResultMerged)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(hourlyForecastResultMerged)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(location, alertsResult.warnings?.national)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getDailyForecast(
        location: Location,
        hourlyResult: List<MetIeForecastHourly>?,
    ): List<DailyWrapper>? {
        if (hourlyResult == null) return null

        val dailyList = mutableListOf<DailyWrapper>()
        val hourlyListByDay = hourlyResult.groupBy { it.date }
        for (i in 0 until hourlyListByDay.entries.size) {
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
        hourlyResult: List<MetIeForecastHourly>?,
    ): List<HourlyWrapper>? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HHmm", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Europe/Dublin")

        return hourlyResult?.map { result ->
            HourlyWrapper(
                date = formatter.parse("${result.date} ${result.localTime}")!!,
                weatherCode = getWeatherCode(result.symbol?.number),
                weatherText = result.symbol?.description,
                temperature = TemperatureWrapper(
                    temperature = result.temperature?.value?.toDoubleOrNull()?.celsius,
                    feelsLike = result.feelsLike?.celsius
                ),
                precipitation = Precipitation(
                    total = result.precipitation?.value?.toDoubleOrNull()?.millimeters
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.precipitation?.probability?.toDoubleOrNull()?.percent
                ),
                wind = Wind(
                    degree = result.windDirection?.deg?.toDoubleOrNull(),
                    speed = result.windSpeed?.kph?.kilometersPerHour
                ),
                relativeHumidity = result.humidity?.value?.toDoubleOrNull()?.percent,
                pressure = result.pressure?.value?.toDoubleOrNull()?.hectopascals,
                cloudCover = result.cloudiness?.percent?.toDoubleOrNull()?.percent
            )
        }
    }

    private fun getAlertList(location: Location, warnings: List<MetIeWarning>?): List<Alert>? {
        if (warnings == null) return null
        if (warnings.isEmpty()) return emptyList()

        val region = if (regionsMapping.containsKey(location.admin2)) {
            location.admin2
        } else {
            location.parameters.getOrElse("metie") { null }?.getOrElse("region") { null }
        }
        val eiRegion = region?.let { regionsMapping.getOrElse(region) { null } }

        return warnings
            .filter {
                // National
                it.regions.contains("EI0") ||
                    it.regions.contains(eiRegion)
            }
            .map { alert ->
                val severity = when (alert.severity?.lowercase()) {
                    "extreme" -> AlertSeverity.EXTREME
                    "severe" -> AlertSeverity.SEVERE
                    "moderate" -> AlertSeverity.MODERATE
                    "minor" -> AlertSeverity.MINOR
                    else -> AlertSeverity.UNKNOWN
                }
                Alert(
                    alertId = alert.id,
                    startDate = alert.onset,
                    endDate = alert.expiry,
                    headline = alert.headline,
                    description = alert.description,
                    source = "MET Éireann",
                    severity = severity,
                    color = when (alert.level?.lowercase()) {
                        "red" -> Color.rgb(224, 0, 0)
                        "orange" -> Color.rgb(255, 140, 0)
                        "yellow" -> Color.rgb(255, 255, 0)
                        else -> Alert.colorFromSeverity(severity)
                    }
                )
            }
    }

    private fun getWeatherCode(icon: String?): WeatherCode? {
        if (icon == null) return null
        return with(icon) {
            when {
                startsWith("01") ||
                    startsWith("02") -> WeatherCode.CLEAR
                startsWith("03") -> WeatherCode.PARTLY_CLOUDY
                startsWith("04") -> WeatherCode.CLOUDY
                startsWith("05") ||
                    startsWith("09") ||
                    startsWith("10") ||
                    startsWith("40") ||
                    startsWith("41") ||
                    startsWith("46") -> WeatherCode.RAIN
                startsWith("06") ||
                    startsWith("11") ||
                    startsWith("14") ||
                    startsWith("2") ||
                    startsWith("30") ||
                    startsWith("31") ||
                    startsWith("32") ||
                    startsWith("33") ||
                    startsWith("34") -> WeatherCode.THUNDERSTORM
                startsWith("07") ||
                    startsWith("12") ||
                    startsWith("42") ||
                    startsWith("43") ||
                    startsWith("47") ||
                    startsWith("48") -> WeatherCode.SLEET
                startsWith("08") ||
                    startsWith("13") ||
                    startsWith("44") ||
                    startsWith("45") ||
                    startsWith("49") ||
                    startsWith("50") -> WeatherCode.SNOW
                startsWith("15") -> WeatherCode.FOG
                startsWith("51") ||
                    startsWith("52") -> WeatherCode.HAIL
                else -> null
            }
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getReverseLocation(
            token = getApiKeyOrDefault(),
            lat = latitude,
            lon = longitude,
            src = MET_IE_SRC,
            version = MET_IE_VERSION,
            env = MET_IE_ENV
        ).map {
            val locationList = mutableListOf<LocationAddressInfo>()
            if (it.city != "NO LOCATION SELECTED") {
                locationList.add(convertLocation(it))
            }
            locationList
        }
    }

    private fun convertLocation(
        result: MetIeLocationResult,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            timeZoneId = "Europe/Dublin",
            countryCode = "IE",
            admin2 = result.county,
            city = result.city
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (regionsMapping.containsKey(location.admin2)) return false

        val currentRegion = location.parameters
            .getOrElse(id) { null }?.getOrElse("region") { null }

        return currentRegion.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getReverseLocation(
            token = getApiKeyOrDefault(),
            lat = location.latitude,
            lon = location.longitude,
            src = MET_IE_SRC,
            version = MET_IE_VERSION,
            env = MET_IE_ENV
        ).map {
            if (it.city != "NO LOCATION SELECTED" &&
                !it.county.isNullOrEmpty() &&
                regionsMapping.containsKey(it.county)
            ) {
                mapOf("region" to it.county)
            } else {
                throw InvalidLocationException()
            }
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.MET_IE_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_met_ie_api_key,
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
        private const val MET_IE_BASE_URL = "https://prodapi.metweb.ie/"
        private const val MET_IE_SRC = "android"
        private const val MET_IE_VERSION = "20513" // Last checked: 2025-10-29
        private const val MET_IE_ENV = "prod"

        // Last checked: 2024-03-02 https://prodapi.metweb.ie/v2/warnings/regions
        val regionsMapping = mapOf(
            "All Counties" to "EI0",
            "All Sea Areas" to "EI8",
            "Carlow" to "EI01",
            "Cavan" to "EI02",
            "Clare" to "EI03",
            "Cork" to "EI04",
            "Donegal" to "EI06",
            "Dublin" to "EI07",
            "Galway" to "EI10",
            "Kerry" to "EI11",
            "Kildare" to "EI12",
            "Kilkenny" to "EI13",
            "Laois" to "EI15",
            "Leitrim" to "EI14",
            "Limerick" to "EI16",
            "Longford" to "EI18",
            "Louth" to "EI19",
            "Mayo" to "EI20",
            "Meath" to "EI21",
            "Monaghan" to "EI22",
            "Offaly" to "EI23",
            "Roscommon" to "EI24",
            "Sligo" to "EI25",
            "Tipperary" to "EI26",
            "Waterford" to "EI27",
            "Westmeath" to "EI29",
            "Wexford" to "EI30",
            "Wicklow" to "EI31"
        )
    }
}
