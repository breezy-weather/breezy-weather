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

package org.breezyweather.sources.smg

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.smg.json.SmgAirQualityResult
import org.breezyweather.sources.smg.json.SmgBulletinResult
import org.breezyweather.sources.smg.json.SmgCurrentResult
import org.breezyweather.sources.smg.json.SmgForecastResult
import org.breezyweather.sources.smg.json.SmgUvResult
import org.breezyweather.sources.smg.json.SmgWarningResult
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.milligramsPerCubicMeter
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

class SmgService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : SmgServiceStub(context) {

    override val id = "smg"

    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "https://www.smg.gov.mo/zh/subpage/21/page/21"
                startsWith("pt") -> "https://www.smg.gov.mo/pt/subpage/21/page/21"
                else -> "https://www.smg.gov.mo/en/subpage/21/page/21"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(SMG_BASE_URL)
            .build()
            .create(SmgApi::class.java)
    }

    private val mCmsApi by lazy {
        client
            .baseUrl(SMG_CMS_URL)
            .build()
            .create(SmgCmsApi::class.java)
    }

    override val attributionLinks
        get() = mapOf(
            weatherAttribution to "https://www.smg.gov.mo/"
        )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily().onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(SmgForecastResult())
            }
        } else {
            Observable.just(SmgForecastResult())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly().onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(SmgForecastResult())
            }
        } else {
            Observable.just(SmgForecastResult())
        }

        // CURRENT
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent().onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(SmgCurrentResult())
            }
        } else {
            Observable.just(SmgCurrentResult())
        }

        val lang = with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "c"
                startsWith("pt") -> "p"
                else -> "e"
            }
        }
        val bulletin = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getBulletin(
                lang = lang
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(SmgBulletinResult())
            }
        } else {
            Observable.just(SmgBulletinResult())
        }
        val uv = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getUVIndex().onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(SmgUvResult())
            }
        } else {
            Observable.just(SmgUvResult())
        }

        // ALERT
        val alerts = MutableList(SMG_ALERT_TYPES.size) {
            if (SourceFeature.ALERT in requestedFeatures) {
                mApi.getWarning(SMG_ALERT_TYPES[it], lang).onErrorResumeNext {
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(SmgWarningResult())
                }
            } else {
                Observable.just(SmgWarningResult())
            }
        }

        val warnings = if (SourceFeature.ALERT in requestedFeatures) {
            Observable.zip(alerts[0], alerts[1], alerts[2], alerts[3], alerts[4], alerts[5]) {
                    typhoonResult: SmgWarningResult,
                    rainstormResult: SmgWarningResult,
                    monsoonResult: SmgWarningResult,
                    thunderstormResult: SmgWarningResult,
                    stormsurgeResult: SmgWarningResult,
                    tsunamiResult: SmgWarningResult,
                ->
                listOf(
                    typhoonResult,
                    rainstormResult,
                    monsoonResult,
                    thunderstormResult,
                    stormsurgeResult,
                    tsunamiResult
                )
            }
        } else {
            Observable.just(emptyList())
        }

        // AIR QUALITY
        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mCmsApi.getAirQuality(Calendar.getInstance().timeInMillis).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(SmgAirQualityResult())
            }
        } else {
            Observable.just(SmgAirQualityResult())
        }

        return Observable.zip(current, daily, hourly, bulletin, uv, warnings, airQuality) {
                currentResult: SmgCurrentResult,
                dailyResult: SmgForecastResult,
                hourlyResult: SmgForecastResult,
                bulletinResult: SmgBulletinResult,
                uvResult: SmgUvResult,
                warningsResult: List<SmgWarningResult>,
                airQualityResult: SmgAirQualityResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(currentResult, bulletinResult, uvResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(
                        current = getAirQuality(airQualityResult)
                    )
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, warningsResult)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals()
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(
        currentResult: SmgCurrentResult,
        bulletinResult: SmgBulletinResult,
        uvResult: SmgUvResult,
    ): CurrentWrapper {
        var current = CurrentWrapper()
        currentResult.Weather?.Custom?.getOrNull(0)?.WeatherReport?.forEach { report ->
            // SMG has not released the coordinates of its various monitoring stations.
            // Therefore we default to "Taipa Grande" which is SMG's office location.
            // This is fine because Macao has a land area of just 32.9km²,
            // which is one-third the size of the forecast grid of most other countries (c. 100km²).
            // TODO: Once we have more intel on the coordinates of the stations, we can locate the nearest one.
            report.station?.getOrNull(0)?.takeIf { it.stationname?.getOrNull(0) == "TAIPA GRANDE" }?.let {
                current = CurrentWrapper(
                    temperature = TemperatureWrapper(
                        temperature = it.Temperature?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull()?.celsius
                    ),
                    wind = Wind(
                        degree = it.WindDirection?.getOrNull(0)?.Degree?.getOrNull(0)?.toDoubleOrNull(),
                        speed = it.WindSpeed?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull()?.kilometersPerHour,
                        gusts = it.WindGust?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull()?.kilometersPerHour
                    ),
                    uV = UV(
                        index = uvResult.UV?.Custom?.getOrNull(0)?.ActualUVBReport?.getOrNull(0)?.index
                            ?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull()
                    ),
                    relativeHumidity = it.Humidity?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull()?.percent,
                    dewPoint = it.DewPoint?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull()?.celsius,
                    pressure = it.MeanSeaLevelPressure?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull()
                        ?.hectopascals,
                    dailyForecast = bulletinResult.Forecast?.Custom?.getOrNull(0)?.TodaySituation?.getOrNull(0)
                )
            }
        }
        return current
    }

    private fun getDailyForecast(
        context: Context,
        dailyResult: SmgForecastResult,
    ): List<DailyWrapper> {
        val dailyList = mutableListOf<DailyWrapper>()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Macau")
        val dateTimeFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH)
        dateTimeFormatter.timeZone = TimeZone.getTimeZone("Asia/Macau")

        var maxTemp: Double?
        var minTemp: Double?

        dailyResult.forecast?.Custom?.getOrNull(0)?.WeatherForecast?.forEach {
            if (it.ValidFor?.getOrNull(0) !== null) {
                maxTemp = null
                minTemp = null
                it.Temperature?.forEach { t ->
                    when (t.Type?.getOrNull(0)) {
                        "1" -> maxTemp = t.Value?.getOrNull(0)?.toDoubleOrNull()
                        "2" -> minTemp = t.Value?.getOrNull(0)?.toDoubleOrNull()
                    }
                }
                dailyList.add(
                    DailyWrapper(
                        date = formatter.parse(it.ValidFor[0])!!,
                        day = HalfDayWrapper(
                            weatherText = getWeatherText(context, it.dailyWeatherStatus?.getOrNull(0)),
                            weatherCode = getWeatherCode(it.dailyWeatherStatus?.getOrNull(0)),
                            temperature = TemperatureWrapper(
                                temperature = maxTemp?.celsius
                            )
                        ),
                        night = HalfDayWrapper(
                            weatherText = getWeatherText(context, it.dailyWeatherStatus?.getOrNull(0)),
                            weatherCode = getWeatherCode(it.dailyWeatherStatus?.getOrNull(0)),
                            temperature = TemperatureWrapper(
                                temperature = minTemp?.celsius
                            )
                        )
                    )
                )
            }
        }
        return dailyList
    }

    private fun getHourlyForecast(
        context: Context,
        hourlyResult: SmgForecastResult,
    ): List<HourlyWrapper> {
        val hourlyList = mutableListOf<HourlyWrapper>()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Macau")
        hourlyResult.forecast?.Custom?.getOrNull(0)?.WeatherForecast?.forEach {
            if (it.ValidFor?.getOrNull(0) !== null && it.f_time?.getOrNull(0) !== null) {
                hourlyList.add(
                    HourlyWrapper(
                        date = formatter.parse(it.ValidFor[0] + " " + it.f_time[0])!!,
                        weatherText = getWeatherText(
                            context,
                            it.hourlyWeatherStatus?.getOrNull(0)?.Value?.getOrNull(0)
                        ),
                        weatherCode = getWeatherCode(it.hourlyWeatherStatus?.getOrNull(0)?.Value?.getOrNull(0)),
                        temperature = TemperatureWrapper(
                            temperature = it.Temperature?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull()?.celsius
                        ),
                        wind = Wind(
                            degree = it.Winddiv?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull(),
                            speed = it.Windspd?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull()?.kilometersPerHour
                        ),
                        relativeHumidity = it.Humidity?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull()?.percent
                    )
                )
            }
        }
        return hourlyList
    }

    private fun getAlertList(
        context: Context,
        warningsResult: List<SmgWarningResult>,
    ): List<Alert> {
        val alertList = mutableListOf<Alert>()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Macau")
        var active: Boolean
        var alertId: String
        var startDate: Date?
        var headline: String?
        var description: String?
        var severity: AlertSeverity
        var color: Int
        val source by lazy {
            with(context.currentLocale.code) {
                when {
                    startsWith("zh") -> "地球物理氣象局"
                    startsWith("pt") -> "Direcção dos Serviços Meteorológicos e Geofísicos"
                    else -> "Macao Meteorological and Geophysical Bureau"
                }
            }
        }
        warningsResult.forEach {
            active = false
            alertId = ""
            startDate = null
            headline = null
            description = null
            severity = AlertSeverity.UNKNOWN
            color = Alert.colorFromSeverity(severity)
            it.TyphoonWarning?.Custom?.getOrNull(0)?.TropicalCyclone?.getOrNull(0)?.let { warning ->
                if (warning.Action?.getOrNull(0) != "NIL") {
                    active = true
                    if (warning.IssuedAt?.getOrNull(0) !== null) {
                        alertId = "typhoon $startDate"
                        startDate = formatter.parse(warning.IssuedAt[0])
                    } else {
                        alertId = "typhoon"
                    }
                    headline = when (warning.Warncode?.getOrNull(0)?.uppercase()) {
                        "1" -> context.getString(R.string.smg_warning_text_tropical_cyclone_1)
                        "3" -> context.getString(R.string.smg_warning_text_tropical_cyclone_3)
                        "8NE" -> context.getString(R.string.smg_warning_text_tropical_cyclone_8_northeast)
                        "8NW" -> context.getString(R.string.smg_warning_text_tropical_cyclone_8_northwest)
                        "8SE" -> context.getString(R.string.smg_warning_text_tropical_cyclone_8_southeast)
                        "8SW" -> context.getString(R.string.smg_warning_text_tropical_cyclone_8_southwest)
                        "9" -> context.getString(R.string.smg_warning_text_tropical_cyclone_9)
                        "10" -> context.getString(R.string.smg_warning_text_tropical_cyclone_10)
                        else -> null
                    }
                    description = warning.Misc?.getOrNull(0) ?: warning.Description?.getOrNull(0)
                    severity = when (warning.Warncode?.getOrNull(0)?.uppercase()) {
                        "1" -> AlertSeverity.MINOR
                        "3" -> AlertSeverity.MODERATE
                        "8NE", "8NW", "8SE", "8SW" -> AlertSeverity.SEVERE
                        "9", "10" -> AlertSeverity.EXTREME
                        else -> AlertSeverity.UNKNOWN
                    }
                    color = Alert.colorFromSeverity(severity)
                }
            }
            it.RainstormWarning?.Custom?.getOrNull(0)?.Rainstorm?.getOrNull(0)?.let { warning ->
                if (warning.Action?.getOrNull(0) != "NIL") {
                    active = true
                    // TODO: Confirm this section when we find out the actual Warncode
                    if (warning.IssuedAt?.getOrNull(0) !== null) {
                        alertId = "rainstorm $startDate"
                        startDate = formatter.parse(warning.IssuedAt[0])
                    } else {
                        alertId = "rainstorm"
                    }
                    headline = when (warning.Warncode?.getOrNull(0)?.uppercase()) {
                        "YELLOW" -> context.getString(R.string.smg_warning_text_rainstorm_yellow)
                        "RED" -> context.getString(R.string.smg_warning_text_rainstorm_red)
                        "BLACK" -> context.getString(R.string.smg_warning_text_rainstorm_black)
                        else -> null
                    }
                    description = warning.Description?.getOrNull(0)
                    severity = when (warning.Warncode?.getOrNull(0)?.uppercase()) {
                        "YELLOW" -> AlertSeverity.MODERATE
                        "RED" -> AlertSeverity.SEVERE
                        "BLACK" -> AlertSeverity.EXTREME
                        else -> AlertSeverity.UNKNOWN
                    }
                    color = when (warning.Warncode?.getOrNull(0)?.uppercase()) {
                        "YELLOW" -> Color.rgb(244, 216, 100)
                        "ORANGE" -> Color.rgb(225, 49, 38)
                        "BLACK" -> Color.rgb(15, 14, 15)
                        else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                    }
                }
            }
            it.MonsoonWarning?.Custom?.getOrNull(0)?.Monsoon?.getOrNull(0)?.let { warning ->
                if (warning.Action?.getOrNull(0) != "NIL") {
                    active = true
                    if (warning.IssuedAt?.getOrNull(0) !== null) {
                        alertId = "monsoon $startDate"
                        startDate = formatter.parse(warning.IssuedAt[0])
                    } else {
                        alertId = "monsoon"
                    }
                    headline = context.getString(R.string.smg_warning_text_strong_monsoon)
                    description = warning.Description?.getOrNull(0)
                    severity = AlertSeverity.MODERATE
                    color = Alert.colorFromSeverity(severity)
                }
            }
            it.ThunderstormWarning?.Custom?.getOrNull(0)?.Thunderstorm?.getOrNull(0)?.let { warning ->
                if (warning.Action?.getOrNull(0) != "NIL") {
                    active = true
                    if (warning.IssuedAt?.getOrNull(0) !== null) {
                        alertId = "thunderstorm $startDate"
                        startDate = formatter.parse(warning.IssuedAt[0])
                    } else {
                        alertId = "thunderstorm"
                    }
                    headline = context.getString(R.string.smg_warning_text_thunderstorm)
                    description = warning.Description?.getOrNull(0)
                    severity = AlertSeverity.MODERATE
                    color = Alert.colorFromSeverity(severity)
                }
            }
            it.StormsurgeWarning?.Custom?.getOrNull(0)?.Stormsurge?.getOrNull(0)?.let { warning ->
                if (warning.Action?.getOrNull(0) != "NIL") {
                    active = true
                    // TODO: Confirm this section when we find out the actual Warncode
                    if (warning.IssuedAt?.getOrNull(0) !== null) {
                        alertId = "stormsurge $startDate"
                        startDate = formatter.parse(warning.IssuedAt[0])
                    } else {
                        alertId = "stormsurge"
                    }
                    headline = when (warning.Warncode?.getOrNull(0)?.uppercase()) {
                        "BLUE" -> context.getString(R.string.smg_warning_text_storm_surge_blue)
                        "YELLOW" -> context.getString(R.string.smg_warning_text_storm_surge_yellow)
                        "ORANGE" -> context.getString(R.string.smg_warning_text_storm_surge_orange)
                        "RED" -> context.getString(R.string.smg_warning_text_storm_surge_red)
                        "BLACK" -> context.getString(R.string.smg_warning_text_storm_surge_black)
                        else -> ""
                    }
                    description = warning.Misc?.getOrNull(0) ?: warning.Description?.getOrNull(0)
                    severity = when (warning.Warncode?.getOrNull(0)?.uppercase()) {
                        "BLUE" -> AlertSeverity.MINOR
                        "YELLOW" -> AlertSeverity.MODERATE
                        "ORANGE" -> AlertSeverity.SEVERE
                        "RED" -> AlertSeverity.EXTREME
                        "BLACK" -> AlertSeverity.EXTREME
                        else -> AlertSeverity.UNKNOWN
                    }
                    color = when (warning.Warncode?.getOrNull(0)?.uppercase()) {
                        "BLUE" -> Color.rgb(113, 152, 200)
                        "YELLOW" -> Color.rgb(249, 218, 133)
                        "ORANGE" -> Color.rgb(241, 158, 75)
                        "RED" -> Color.rgb(220, 54, 49)
                        "BLACK" -> Color.rgb(32, 29, 28)
                        else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                    }
                }
            }
            it.TsunamiWarning?.Custom?.getOrNull(0)?.Tsunami?.getOrNull(0)?.let { warning ->
                if (warning.Action?.getOrNull(0) != "NIL") {
                    active = true
                    if (warning.IssuedAt?.getOrNull(0) !== null) {
                        alertId = "thunderstorm $startDate"
                        startDate = formatter.parse(warning.IssuedAt[0])
                    } else {
                        alertId = "thunderstorm"
                    }
                    headline = context.getString(R.string.smg_warning_text_tsunami)
                    description = warning.Description?.getOrNull(0)
                    severity = AlertSeverity.MODERATE
                    color = Alert.colorFromSeverity(severity)
                }
            }
            if (active) {
                alertList.add(
                    Alert(
                        alertId = alertId,
                        startDate = startDate,
                        headline = headline,
                        description = description,
                        source = source,
                        severity = severity,
                        color = color
                    )
                )
            }
        }
        return alertList
    }

    // Mean max and mean min temperatures for each month from 1991 to 2020.
    // Hard-coded since Macao has a land area of just 32.9km²,
    // and there are only records for SMG's main office in Taipa Grande.
    // Source: https://www.smg.gov.mo/en/subpage/348/page/252
    private fun getNormals(): Map<Month, Normals> {
        return mapOf(
            Month.JANUARY to Normals(18.6.celsius, 12.7.celsius),
            Month.FEBRUARY to Normals(19.2.celsius, 13.9.celsius),
            Month.MARCH to Normals(21.4.celsius, 16.5.celsius),
            Month.APRIL to Normals(25.1.celsius, 20.3.celsius),
            Month.MAY to Normals(28.7.celsius, 23.7.celsius),
            Month.JUNE to Normals(30.5.celsius, 25.7.celsius),
            Month.JULY to Normals(31.4.celsius, 26.1.celsius),
            Month.AUGUST to Normals(31.5.celsius, 25.9.celsius),
            Month.SEPTEMBER to Normals(30.8.celsius, 25.1.celsius),
            Month.OCTOBER to Normals(28.5.celsius, 22.7.celsius),
            Month.NOVEMBER to Normals(24.7.celsius, 18.8.celsius),
            Month.DECEMBER to Normals(20.3.celsius, 14.3.celsius)
        )
    }

    // Source: mostly from https://www.smg.gov.mo/en/subpage/232/page/268
    // Can check with https://cms.smg.gov.mo/uploads/image/introduction/ww-e01.png (replace 01 with the code below)
    private fun getWeatherText(
        context: Context,
        weather: String?,
    ): String? {
        return when (weather) {
            "01", "a1" -> context.getString(R.string.common_weather_text_clear_sky) // Fine
            "02", "a2" -> context.getString(R.string.common_weather_text_partly_cloudy) // Sunny periods
            "03" -> context.getString(R.string.common_weather_text_cloudy) // Cloudy
            "04" -> context.getString(R.string.common_weather_text_overcast) // Overcast
            "05" -> context.getString(R.string.common_weather_text_dust) // Dust
            "06" -> context.getString(R.string.common_weather_text_fog) // Fog
            "07" -> context.getString(R.string.weather_kind_haze) // Haze
            "08" -> context.getString(R.string.common_weather_text_mist) // Mist
            "09" -> context.getString(R.string.common_weather_text_smoke) // Smoke
            "10" -> context.getString(R.string.common_weather_text_drizzle) // Drizzle
            "11" -> context.getString(R.string.common_weather_text_squall) // Squall
            "12" -> context.getString(R.string.common_weather_text_rain) // Rain
            "13" -> context.getString(R.string.common_weather_text_rain_heavy)
            "14", "15" -> context.getString(R.string.common_weather_text_snow) // Snow
            "16" -> context.getString(R.string.common_weather_text_rain_showers) // Showers
            "17" -> context.getString(R.string.common_weather_text_rain_showers_heavy)
            "18" -> context.getString(R.string.weather_kind_thunderstorm) // Thundershowers
            "19", "20" -> context.getString(R.string.common_weather_text_dust_storm) // Dust storm
            "21" -> context.getString(R.string.common_weather_text_dry) // Dry
            "22", "23" -> context.getString(R.string.common_weather_text_sand_storm) // Sand storm
            "24" -> context.getString(R.string.weather_kind_thunderstorm) // Squally thunderstorm
            "25" -> context.getString(R.string.weather_kind_thunder) // Thunder
            "26" -> context.getString(R.string.common_weather_text_humid) // Wet
            "27" -> context.getString(R.string.weather_kind_wind) // Windy
            "28", "c8" -> context.getString(R.string.common_weather_text_rain) // Rainy with sunny intervals
            "29", "c9" -> context.getString(R.string.common_weather_text_rain_showers) // Showery with sunny intervals
            "30" -> context.getString(R.string.meteoam_weather_text_tornado_watersprout) // Tornado
            // TODO: Migrate tornado string from MeteoAM?
            "31" -> context.getString(R.string.common_weather_text_hot) // Hot
            "32" -> context.getString(R.string.common_weather_text_cold) // Cold
            "33" -> context.getString(R.string.common_weather_text_cold) // Cold
            else -> null
        }
    }

    private fun getWeatherCode(
        weather: String?,
    ): WeatherCode? {
        return when (weather) {
            "01", "a1" -> WeatherCode.CLEAR
            "02", "a2" -> WeatherCode.PARTLY_CLOUDY
            "03", "04" -> WeatherCode.CLOUDY
            "06", "08" -> WeatherCode.FOG
            "05", "07", "09" -> WeatherCode.HAZE
            "10", "12", "13", "16", "17", "28", "c8", "29", "c9" -> WeatherCode.RAIN
            "18", "24" -> WeatherCode.THUNDERSTORM
            "14", "15" -> WeatherCode.SNOW
            "25" -> WeatherCode.THUNDER
            "11", "19", "20", "22", "23", "27", "30" -> WeatherCode.WIND
            else -> null
        }
    }

    private fun getAirQuality(
        airQualityResult: SmgAirQualityResult?,
    ): AirQuality? {
        if (airQualityResult == null) return null
        // "tghopolu" refers to Taipa Grande (SMG's office location).
        // TODO: Once we have more intel on the coordinates of the stations, we can locate the nearest one.
        return AirQuality(
            pM25 = airQualityResult.tghopolu?.HE_PM2_5?.toDoubleOrNull()?.microgramsPerCubicMeter,
            pM10 = airQualityResult.tghopolu?.HE_PM10?.toDoubleOrNull()?.microgramsPerCubicMeter,
            sO2 = airQualityResult.tghopolu?.HE_SO2?.toDoubleOrNull()?.microgramsPerCubicMeter,
            nO2 = airQualityResult.tghopolu?.HE_NO2?.toDoubleOrNull()?.microgramsPerCubicMeter,
            o3 = airQualityResult.tghopolu?.HE_O3?.toDoubleOrNull()?.microgramsPerCubicMeter,
            cO = airQualityResult.tghopolu?.HE_CO?.toDoubleOrNull()?.milligramsPerCubicMeter
        )
    }

    companion object {
        private const val SMG_BASE_URL = "https://new-api.smg.gov.mo/"
        private const val SMG_CMS_URL = "https://cms.smg.gov.mo/"
        private val SMG_ALERT_TYPES = listOf("typhoon", "rainstorm", "monsoon", "thunderstorm", "stormsurge", "tsunami")
    }
}
