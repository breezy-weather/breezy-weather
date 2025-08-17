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

package org.breezyweather.sources.hko

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.getWindDegree
import org.breezyweather.sources.hko.json.HkoCurrentRegionalWeather
import org.breezyweather.sources.hko.json.HkoCurrentResult
import org.breezyweather.sources.hko.json.HkoDailyForecast
import org.breezyweather.sources.hko.json.HkoForecastResult
import org.breezyweather.sources.hko.json.HkoHourlyWeatherForecast
import org.breezyweather.sources.hko.json.HkoNormalsResult
import org.breezyweather.sources.hko.json.HkoOneJsonResult
import org.breezyweather.sources.hko.json.HkoWarningResult
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.breezyweather.unit.ratio.Ratio
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.speed.Speed.Companion.kilometersPerHour
import org.breezyweather.unit.temperature.Temperature.Companion.celsius
import org.json.JSONObject
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.round

class HkoService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "hko"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("zh")) {
            "香港天文台"
        } else {
            "HKO (${context.currentLocale.getCountryName("HK")})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                equals("zh-tw") || equals("zh-hk") || equals("zh-mo") -> "https://www.hko.gov.hk/tc/privacy/policy.htm"
                startsWith("zh") -> "https://www.hko.gov.hk/sc/privacy/policy.htm"
                else -> "https://www.hko.gov.hk/en/privacy/policy.htm"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(HKO_BASE_URL)
            .build()
            .create(HkoApi::class.java)
    }

    private val mMapsApi by lazy {
        client
            .baseUrl(HKO_MAPS_BASE_URL)
            .build()
            .create(HkoMapsApi::class.java)
    }

    private val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("zh")) {
            "香港天文台"
        } else {
            "Hong Kong Observatory"
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.hko.gov.hk/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("HK", ignoreCase = true)
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
        val languageCode = context.currentLocale.code

        // Make sure we have current grid ID, forecast grid ID, and default weather station.
        // We need these to call the APIs.
        val currentGrid = location.parameters.getOrElse(id) { null }?.getOrElse("currentGrid") { null }
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val forecastGrid = location.parameters.getOrElse(id) { null }?.getOrElse("forecastGrid") { null }
        if (currentGrid.isNullOrEmpty() || currentStation.isNullOrEmpty() || forecastGrid.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        // Several of the API endpoints output text in English and Traditional Chinese,
        // while text in Simplified Chinese are at an endpoint under different path.
        val path = if (languageCode.startsWith("zh") &&
            languageCode != "zh-tw" &&
            languageCode != "zh-hk" &&
            languageCode != "zh-mo"
        ) {
            HKO_SIMPLIFIED_CHINESE_PATH
        } else {
            ""
        }

        val warnings = mutableMapOf<String, HkoWarningResult>()
        var warningKey: String
        var endPoint: String

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mMapsApi.getForecast(
                grid = forecastGrid,
                v = System.currentTimeMillis()
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(HkoForecastResult())
            }
        } else {
            Observable.just(HkoForecastResult())
        }

        // CURRENT
        // Full current observation takes two API calls: getCurrentWeather and getOneJson
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrentWeather(
                grid = currentGrid
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(HkoCurrentResult())
            }
        } else {
            Observable.just(HkoCurrentResult())
        }

        val oneJson = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getOneJson(
                path = path,
                suffix = if (languageCode.startsWith("zh")) {
                    "_uc"
                } else {
                    ""
                }
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(HkoOneJsonResult())
            }
        } else {
            Observable.just(HkoOneJsonResult())
        }

        // Keys in Warning Summary endpoint end in different suffixes depending on the language
        val suffix = if (languageCode.startsWith("zh")) "_C" else "_E"

        // ALERTS
        // First read the warning summary file.
        // Loop through each warning type in the summary; check which ones are current.
        // Then only load the detailed files of the current warning types.
        val warningDetails = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getWarningSummary(
                path = path
            ).map { warningSummary ->
                warningSummary.DYN_DAT_WARNSUM?.forEach {
                    if (it.key.endsWith(suffix)) {
                        warningKey = it.key.substring(0, it.key.length - 2)
                        if (HKO_WARNING_ENDPOINTS.containsKey(warningKey)) {
                            endPoint = HKO_WARNING_ENDPOINTS[warningKey]!!
                            if (it.value.Warning_Action != null &&
                                it.value.Warning_Action != "" &&
                                it.value.Warning_Action != "CANCEL"
                            ) {
                                warnings[endPoint] = mApi.getWarningText(path, endPoint).onErrorResumeNext {
                                    failedFeatures[SourceFeature.ALERT] = it
                                    Observable.just(HkoWarningResult())
                                }.blockingFirst()
                            }
                        }
                    }
                }
                warnings
            }.onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(mutableMapOf())
            }
        } else {
            Observable.just(mutableMapOf())
        }

        // NORMALS
        // HKO has its own normals endpoint from all the other stations.
        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            when (currentStation) {
                "HKO" -> mApi.getHkoNormals().onErrorResumeNext {
                    failedFeatures[SourceFeature.NORMALS] = it
                    Observable.just(HkoNormalsResult())
                }
                else -> mApi.getNormals(currentStation).onErrorResumeNext {
                    failedFeatures[SourceFeature.NORMALS] = it
                    Observable.just(HkoNormalsResult())
                }
            }
        } else {
            Observable.just(HkoNormalsResult())
        }

        return Observable.zip(current, forecast, normals, oneJson, warningDetails) {
                currentResult: HkoCurrentResult,
                forecastResult: HkoForecastResult,
                normalsResult: HkoNormalsResult,
                oneJsonResult: HkoOneJsonResult,
                warningDetailsResult: MutableMap<String, HkoWarningResult>,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(
                        context,
                        forecastResult.DailyForecast,
                        oneJsonResult
                    )
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, forecastResult.HourlyWeatherForecast, oneJsonResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult.RegionalWeather, oneJsonResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, warningDetailsResult)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(normalsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(
        context: Context,
        regionalWeather: HkoCurrentRegionalWeather?,
        oneJson: HkoOneJsonResult,
    ): CurrentWrapper {
        return CurrentWrapper(
            weatherText = getWeatherText(context, oneJson.FLW?.Icon1?.toIntOrNull()),
            weatherCode = getWeatherCode(oneJson.FLW?.Icon1?.toIntOrNull()),
            temperature = TemperatureWrapper(
                temperature = regionalWeather?.Temp?.Value?.toDoubleOrNull()?.celsius
            ),
            wind = Wind(
                degree = getWindDegree(regionalWeather?.Wind?.WindDirectionCode),
                speed = regionalWeather?.Wind?.WindSpeed?.toDoubleOrNull()?.kilometersPerHour,
                gusts = regionalWeather?.Wind?.Gust?.toDoubleOrNull()?.kilometersPerHour
            ),
            uV = UV(
                index = oneJson.RHRREAD?.UVIndex?.toDoubleOrNull()
            ),
            relativeHumidity = regionalWeather?.RH?.Value?.toDoubleOrNull()?.percent,
            pressure = regionalWeather?.Pressure?.Value?.toDoubleOrNull()?.hectopascals,
            dailyForecast = oneJson.F9D?.WeatherForecast?.getOrElse(0) { null }?.ForecastWeather
        )
    }

    private fun getNormals(
        normalsResult: HkoNormalsResult,
    ): Map<Month, Normals> {
        val maxTemps = mutableListOf<Double>()
        val minTemps = mutableListOf<Double>()
        var value: Double?

        // TODO: Limit the list to only the most recent 30 years?
        // TODO: Include values like " 25.4#" (incomplete months)? - need to trim space and hash sign
        return Month.entries.associateWith { month ->
            normalsResult.stn?.data?.forEach {
                if (it.code == "MEAN_MAX") {
                    it.monData?.forEach { mon ->
                        value = mon?.getOrElse(month.value) { null }?.toDoubleOrNull()
                        if (value != null) {
                            maxTemps.add(value!!)
                        }
                    }
                }
                if (it.code == "MEAN_MIN") {
                    it.monData?.forEach { mon ->
                        value = mon?.getOrElse(month.value) { null }?.toDoubleOrNull()
                        if (value != null) {
                            minTemps.add(value!!)
                        }
                    }
                }
            }
            Normals(
                daytimeTemperature = maxTemps.takeIf { it.isNotEmpty() }?.average()?.celsius,
                nighttimeTemperature = minTemps.takeIf { it.isNotEmpty() }?.average()?.celsius
            )
        }
    }

    private fun getDailyForecast(
        context: Context,
        dailyForecast: List<HkoDailyForecast>?,
        oneJson: HkoOneJsonResult,
    ): List<DailyWrapper> {
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Hong_Kong")

        // City-wide forecast in case the grid forecast fails to return forecast weather conditions
        val iconRegex = Regex("""^pic\d{2}\.png$""")
        val oneJsonDailyWeather = mutableMapOf<String, Int>()
        oneJson.F9D?.WeatherForecast?.forEach {
            if (it.ForecastDate !== null && it.ForecastIcon !== null && iconRegex.matches(it.ForecastIcon)) {
                oneJsonDailyWeather[it.ForecastDate] = it.ForecastIcon.substring(3, 5).toInt()
            }
        }

        val dailyList = mutableListOf<DailyWrapper>()
        var daytimeWeather: Int?
        var nightTimeWeather: Int?

        dailyForecast?.forEach {
            daytimeWeather = it.ForecastDailyWeather

            // If the grid forecast does not return forecast daily weather,
            // fall back to citywide daily forecast weather conditions,
            // which is preferable to 9 days of "partly cloudy"
            if (daytimeWeather == null) {
                daytimeWeather = oneJsonDailyWeather.getOrElse(it.ForecastDate) { null }
            }

            // Replace a few full day weather codes with night time equivalents for better fitting descriptions
            nightTimeWeather = when (daytimeWeather) {
                50 -> 70 // Replace "Sunny" with "Fine"
                51 -> 77 // Replace "Sunny Periods" with "Mainly Fine"
                52 -> 76 // Replace "Sunny Intervals" with "Mainly Cloudy"
                else -> daytimeWeather
            }
            dailyList.add(
                DailyWrapper(
                    date = formatter.parse(it.ForecastDate)!!,
                    day = HalfDayWrapper(
                        weatherText = getWeatherText(context, daytimeWeather),
                        weatherCode = getWeatherCode(daytimeWeather),
                        precipitationProbability = PrecipitationProbability(
                            total = getPrecipitationProbability(it.ForecastChanceOfRain)
                        )
                    ),
                    night = HalfDayWrapper(
                        weatherText = getWeatherText(context, nightTimeWeather),
                        weatherCode = getWeatherCode(nightTimeWeather),
                        precipitationProbability = PrecipitationProbability(
                            total = getPrecipitationProbability(it.ForecastChanceOfRain)
                        )
                    )
                )
            )
        }
        return dailyList
    }

    private fun getHourlyForecast(
        context: Context,
        hourlyWeatherForecast: List<HkoHourlyWeatherForecast>?,
        oneJson: HkoOneJsonResult,
    ): List<HourlyWrapper> {
        val formatter = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Hong_Kong")

        val hourlyList = mutableListOf<HourlyWrapper>()
        var currentHourWeather: Int?
        var index: Int?
        val iconRegex = Regex("""^pic\d{2}\.png$""")
        val oneJsonDailyWeather = mutableMapOf<String, Int>()

        // City-wide forecast in case the grid forecast fails to return forecast weather conditions
        oneJson.F9D?.WeatherForecast?.forEach {
            if (it.ForecastDate !== null && it.ForecastIcon !== null && iconRegex.matches(it.ForecastIcon)) {
                oneJsonDailyWeather[it.ForecastDate] = it.ForecastIcon.substring(3, 5).toInt()
            }
        }

        if (hourlyWeatherForecast != null) {
            for ((i, value) in hourlyWeatherForecast.withIndex()) {
                // For some reason, ForecastWeather in the output refers to the condition in the previous 3 hours.
                // Therefore we must back fill 3 hours of weather with the next known weather condition.
                currentHourWeather = null
                index = i + 1
                while (currentHourWeather == null && index < hourlyWeatherForecast.size) {
                    if (hourlyWeatherForecast[index].ForecastWeather != null) {
                        currentHourWeather = hourlyWeatherForecast[index].ForecastWeather
                    }
                    ++index
                }

                // If the grid forecast does not return forecast hourly weather,
                // fall back to citywide daily forecast weather conditions,
                // which is preferable to 216 hours of "partly cloudy"
                if (currentHourWeather == null) {
                    currentHourWeather = oneJsonDailyWeather.getOrElse(value.ForecastHour.substring(0, 8)) { null }
                }

                // The last hour in the output is just the condition for the previous 3 hours. Don't add to the list.
                // Occasionally HKO's numerical weather forecast models will produce nonsensical output (e.g. 9999°C).
                // Reject them.
                if (i < hourlyWeatherForecast.size - 1) {
                    hourlyList.add(
                        HourlyWrapper(
                            date = formatter.parse(value.ForecastHour)!!,
                            weatherText = getWeatherText(context, currentHourWeather),
                            weatherCode = getWeatherCode(currentHourWeather),
                            temperature = TemperatureWrapper(
                                temperature = value.ForecastTemperature?.celsius
                            ),
                            wind = Wind(
                                degree = value.ForecastWindDirection,
                                speed = value.ForecastWindSpeed?.kilometersPerHour
                            ),
                            relativeHumidity = value.ForecastRelativeHumidity?.percent
                        )
                    )
                }
            }
        }

        return hourlyList
    }

    private fun getAlertList(
        context: Context,
        warningMap: MutableMap<String, HkoWarningResult>,
    ): List<Alert> {
        val languageKey: String
        val source: String
        val formatter = SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Hong_Kong")

        context.currentLocale.code.let {
            languageKey = when {
                it.startsWith("zh") -> "Val_Chi"
                else -> "Val_Eng"
            }
            source = when {
                it.startsWith("zh") -> "香港天文台"
                else -> "Hong Kong Observatory"
            }
        }
        val alertList = mutableListOf<Alert>()
        var alertDate: String
        var alertId: String
        var startDate: Date?
        var warningCode: String?
        var severity: AlertSeverity
        var headline: String?
        var color: Int?

        // Each warning type has a combination of strings.
        // The combination for each warning type are defined here:
        // https://www.hko.gov.hk/en//files/detail.js
        // We assign the combination in descriptionKeys for each type,
        // and then format accordingly.
        var warning: Map<String, Map<String, String>>?
        var descriptionKeys: List<String>
        var instructionKeys: List<String>
        var descriptionText: String?
        var instructionText: String?

        for ((key, value) in warningMap) {
            warning = when (key) {
                "WTCPRE8" -> value.DYN_DAT_MINDS_WTCPRE8
                "WTCB" -> value.DYN_DAT_MINDS_WTCB
                "WRAINSA" -> value.DYN_DAT_MINDS_WRAINSA
                "WTMW" -> value.DYN_DAT_MINDS_WTMW
                "WFNTSA" -> value.DYN_DAT_MINDS_WFNTSA
                "WMNB" -> value.DYN_DAT_MINDS_WMNB
                "WLSA" -> value.DYN_DAT_MINDS_WLSA
                "WTS" -> value.DYN_DAT_MINDS_WTS
                "WFIRE" -> value.DYN_DAT_MINDS_WFIRE
                "WHOT" -> value.DYN_DAT_MINDS_WHOT
                "WCOLD" -> value.DYN_DAT_MINDS_WCOLD
                "WFROST" -> value.DYN_DAT_MINDS_WFROST
                else -> null
            }

            alertDate = warning?.getOrElse("BulletinDate") { null }?.getOrElse(languageKey) { null } +
                warning?.getOrElse("BulletinTime") { null }?.getOrElse(languageKey) { null }
            alertId = "$key $alertDate"
            startDate = formatter.parse(alertDate)
            headline = null
            descriptionText = null
            instructionText = null
            severity = AlertSeverity.UNKNOWN
            color = Alert.colorFromSeverity(severity)

            if (key == "WTCPRE8") { // Pre-8 Tropical Cyclone Special Announcement
                severity = AlertSeverity.SEVERE
                color = Alert.colorFromSeverity(severity)
                headline = context.getString(R.string.hko_warning_text_tropical_cyclone_pre_8)
                descriptionKeys = listOf(
                    "WTCPRE8_WxAnnouncementSpecialAnnouncementContent1",
                    "WTCPRE8_WxAnnouncementSpecialAnnouncementContent2",
                    "WTCPRE8_WxAnnouncementSpecialAnnouncementContent3",
                    "WTCPRE8_WxAnnouncementSpecialAnnouncementContent4"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }
            if (key == "WTCB") { // Tropical Cyclone Warning Signal
                warningCode = warning?.getOrElse("WTCSGNL_WxWarningCode") { null }?.getOrElse(languageKey) { null }
                severity = when (warningCode) {
                    "TC1" -> AlertSeverity.MINOR
                    "TC3" -> AlertSeverity.MODERATE
                    "TC8NW", "TC8SW", "TC8NE", "TC8SE" -> AlertSeverity.SEVERE
                    "TC9", "TC10" -> AlertSeverity.EXTREME
                    else -> AlertSeverity.UNKNOWN
                }
                color = Alert.colorFromSeverity(severity)
                headline = when (warningCode) {
                    "TC1" -> context.getString(R.string.hko_warning_text_tropical_cyclone_1)
                    "TC3" -> context.getString(R.string.hko_warning_text_tropical_cyclone_3)
                    "TC8NW" -> context.getString(R.string.hko_warning_text_tropical_cyclone_8_northwest)
                    "TC8SW" -> context.getString(R.string.hko_warning_text_tropical_cyclone_8_southwest)
                    "TC8NE" -> context.getString(R.string.hko_warning_text_tropical_cyclone_8_northeast)
                    "TC8SE" -> context.getString(R.string.hko_warning_text_tropical_cyclone_8_southeast)
                    "TC9" -> context.getString(R.string.hko_warning_text_tropical_cyclone_9)
                    "TC10" -> context.getString(R.string.hko_warning_text_tropical_cyclone_10)
                    else -> null
                }
                descriptionKeys = listOf(
                    "WTCB1_WxAnnouncementContent",
                    "WTCB1_WxAnnouncementContent1",
                    "WTCB1_WxAnnouncementContent2",
                    "WTCB1_WxAnnouncementContent3",
                    "WTCB1_WxAnnouncementContent4",
                    "WTCB1_WxAnnouncementContent5",
                    "WTCB1_WxAnnouncementContent6",
                    "WTCB2_WxAnnouncementContent",
                    "WTCB2_WxAnnouncementContent1",
                    "WTCB2_WxAnnouncementContent2",
                    "WTCB2_WxAnnouncementContent3",
                    "WTCB2_WxAnnouncementContent4",
                    "WTCB2_WxAnnouncementContent5",
                    "WTCB2_WxAnnouncementContent6",
                    "WTCB2_WxAnnouncementContent7",
                    "WTCB2_WxAnnouncementContent8",
                    "WTCB2_WxAnnouncementContent9",
                    "WTCB2_WxAnnouncementContent10",
                    "WTCB2_WxAnnouncementContent11",
                    "WTCB2_WxAnnouncementContent12",
                    "WTCB2_WxAnnouncementContent13",
                    "WTCB2_WxAnnouncementContent14",
                    "WTCB2_WxAnnouncementContent15",
                    "WTCB2_WxAnnouncementContent16",
                    "WTCB2_WxAnnouncementContent17",
                    "WTCB2_WxAnnouncementContent18",
                    "WTCB2_WxAnnouncementContent19",
                    "WTCB2_WxAnnouncementContent20",
                    "WTCB2_WxAnnouncementContent21",
                    "WTCB2_WxAnnouncementContent22",
                    "WTCB2_WxAnnouncementContent23",
                    "WTCB2_WxAnnouncementContent24"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
                instructionKeys = listOf(
                    "WTCB3_WxAnnouncementContent",
                    "WTCB3_WxAnnouncementContent1",
                    "WTCB3_WxAnnouncementContent2",
                    "WTCB3_WxAnnouncementContent3",
                    "WTCB3_WxAnnouncementContent4",
                    "WTCB3_WxAnnouncementContent5",
                    "WTCB3_WxAnnouncementContent6",
                    "WTCB3_WxAnnouncementContent7",
                    "WTCB3_WxAnnouncementContent8",
                    "WTCB3_WxAnnouncementContent9",
                    "WTCB3_WxAnnouncementContent10",
                    "WTCB3_WxAnnouncementContent11",
                    "WTCB3_WxAnnouncementContent12",
                    "WTCB3_WxAnnouncementContent13",
                    "WTCB3_WxAnnouncementContent14"
                )
                instructionText = formatWarningText(context, warning, instructionKeys)
            }
            if (key == "WRAINSA") { // Rainstorm Warning Signal
                warningCode = warning?.getOrElse("WRAIN_WxWarningCode") { null }?.getOrElse(languageKey) { null }
                severity = when (warningCode) {
                    "WRAINA" -> AlertSeverity.MODERATE
                    "WRAINR" -> AlertSeverity.SEVERE
                    "WRAINB" -> AlertSeverity.EXTREME
                    else -> AlertSeverity.UNKNOWN
                }
                color = getAlertColor(warningCode)
                headline = when (warningCode) {
                    "WRAINA" -> context.getString(R.string.hko_warning_text_rainstorm_amber)
                    "WRAINR" -> context.getString(R.string.hko_warning_text_rainstorm_red)
                    "WRAINB" -> context.getString(R.string.hko_warning_text_rainstorm_black)
                    else -> null
                }
                descriptionKeys = listOf(
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent1",
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent2",
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent3",
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent4",
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent5",
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent6",
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent7",
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent8",
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent9",
                    "WRAINSA_WxAnnouncementSpecialAnnouncementContent10"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }
            if (key == "WTMW") { // Tsunami Warning
                severity = AlertSeverity.SEVERE
                color = getAlertColor(key)
                headline = context.getString(R.string.hko_warning_text_tsunami)
                descriptionKeys = listOf(
                    "WTMW_WxAnnouncementSpecialAnnouncementContent",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent1",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent2",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent3",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent4",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent5",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent6",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent7",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent8",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent9",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent10",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent11",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent12",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent13",
                    "WTMW_WxAnnouncementSpecialAnnouncementContent14"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }
            if (key == "WFNTSA") { // Special Announcement on Flooding in Northern New Territories
                severity = AlertSeverity.SEVERE
                color = getAlertColor(key)
                headline = context.getString(R.string.hko_warning_text_flooding_northern_nt)
                descriptionKeys = listOf(
                    "WFNTSA_WxWarningActionDesc",
                    "WFNTSA_WxAnnouncementSpecialAnnouncementContent1",
                    "WFNTSA_WxAnnouncementSpecialAnnouncementContent2",
                    "WFNTSA_WxAnnouncementSpecialAnnouncementContent3",
                    "WFNTSA_WxAnnouncementSpecialAnnouncementContent4",
                    "WFNTSA_WxAnnouncementSpecialAnnouncementContent5"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }
            if (key == "WMNB") { // Strong Monsoon Signal
                severity = AlertSeverity.MODERATE
                color = getAlertColor(key)
                headline = context.getString(R.string.hko_warning_text_strong_monsoon)
                descriptionKeys = listOf(
                    "SpecialAnnouncementContent",
                    "SpecialAnnouncementContent1",
                    "SpecialAnnouncementContent2",
                    "SpecialAnnouncementContent3",
                    "SpecialAnnouncementContent4",
                    "SpecialAnnouncementContent5",
                    "SpecialAnnouncementContent6"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }
            if (key == "WLSA") { // Landslip Warning
                severity = AlertSeverity.MODERATE
                color = getAlertColor(key)
                headline = context.getString(R.string.hko_warning_text_landslip)
                descriptionKeys = listOf(
                    "WLSA_WxAnnouncementSpecialAnnouncementContent1",
                    "WLSA_WxAnnouncementSpecialAnnouncementContent2",
                    "WLSA_WxAnnouncementSpecialAnnouncementContent3",
                    "WLSA_WxAnnouncementSpecialAnnouncementContent4",
                    "WLSA_WxAnnouncementSpecialAnnouncementContent5",
                    "WLSA_WxAnnouncementSpecialAnnouncementContent6",
                    "WLSA_WxAnnouncementSpecialAnnouncementContent7"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }
            if (key == "WTS") { // Thunderstorm Warning
                severity = AlertSeverity.MODERATE
                color = getAlertColor(key)
                headline = context.getString(R.string.hko_warning_text_thunderstorm)
                descriptionKeys = listOf(
                    "WTS_WxWarningActionDesc",
                    "WTS_WxAnnouncementSpecialAnnouncementContent",
                    "WTS_WxAnnouncementSpecialAnnouncementContent1",
                    "WTS_WxAnnouncementSpecialAnnouncementContent2",
                    "WTS_WxAnnouncementSpecialAnnouncementContent3",
                    "WTS_WxAnnouncementSpecialAnnouncementContent4",
                    "WTS_WxAnnouncementSpecialAnnouncementContent5",
                    "WTS_WxAnnouncementSpecialAnnouncementContent6",
                    "WTS_WxAnnouncementSpecialAnnouncementContent7",
                    "WTS_WxAnnouncementSpecialAnnouncementContent8",
                    "WTS_WxAnnouncementSpecialAnnouncementContent9",
                    "WTS_WxAnnouncementSpecialAnnouncementContent10",
                    "WTS_WxAnnouncementSpecialAnnouncementContent11"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }
            if (key == "WFIRE") { // Fire Danger Warning
                warningCode = value.DYN_DAT_MINDS_WFIRE?.getOrElse("WFIRE_WxWarningCode") { null }
                    ?.getOrElse(languageKey) { null }
                headline = when (warningCode) {
                    "WFIREY" -> context.getString(R.string.hko_warning_text_fire_yellow)
                    "WFIRER" -> context.getString(R.string.hko_warning_text_fire_red)
                    else -> null
                }
                severity = when (warningCode) {
                    "WFIREY" -> AlertSeverity.MODERATE
                    "WFIRER" -> AlertSeverity.SEVERE
                    else -> AlertSeverity.UNKNOWN
                }
                color = getAlertColor(warningCode)
                descriptionText = when (warningCode) {
                    "WFIREY" -> context.getString(R.string.hko_warning_text_fire_yellow_description)
                    "WFIRER" -> context.getString(R.string.hko_warning_text_fire_red_description)
                    else -> null
                }
            }
            if (key == "WHOT") { // Very Hot Weather Warning
                severity = AlertSeverity.MODERATE
                color = getAlertColor(key)
                headline = context.getString(R.string.hko_warning_text_very_hot)
                descriptionKeys = listOf(
                    "WHOT_WxWarningActionDesc",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent1",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent2",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent3",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent4",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent5",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent6",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent7",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent8",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent9",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent10",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent11",
                    "WHOT_WxAnnouncementSpecialAnnouncementContent12"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }
            if (key == "WCOLD") { // Cold Weather Warning
                severity = AlertSeverity.MODERATE
                color = getAlertColor(key)
                headline = context.getString(R.string.hko_warning_text_cold)
                descriptionKeys = listOf(
                    "WCOLD_WxWarningActionDesc",
                    "WCOLD_WxAnnouncementContent",
                    "WCOLD_WxAnnouncementContent1",
                    "WCOLD_WxAnnouncementContent2",
                    "WCOLD_WxAnnouncementContent3",
                    "WCOLD_WxAnnouncementContent4",
                    "WCOLD_WxAnnouncementContent5",
                    "WCOLD_WxAnnouncementContent6",
                    "WCOLD_WxAnnouncementContent7"
                )
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }
            if (key == "WFROST") { // Frost Warning
                severity = AlertSeverity.SEVERE
                color = getAlertColor(key)
                headline = context.getString(R.string.hko_warning_text_frost)
                descriptionKeys = listOf("WFROST_WxAnnouncementContent")
                descriptionText = formatWarningText(context, warning, descriptionKeys)
            }

            alertList.add(
                Alert(
                    alertId = alertId,
                    startDate = startDate,
                    headline = headline,
                    description = descriptionText,
                    instruction = instructionText,
                    source = source,
                    severity = severity,
                    color = color
                )
            )
        }
        return alertList
    }

    private fun formatWarningText(
        context: Context,
        warning: Map<String, Map<String, String>>?,
        stringKeys: List<String>,
    ): String {
        val languageKey = if (context.currentLocale.code.startsWith("zh")) "Val_Chi" else "Val_Eng"
        val multipleLineFeeds = Regex("\n{3,}")
        val strings = mutableListOf<String>()
        stringKeys.forEach {
            strings.add(warning?.getOrElse(it) { null }?.getOrElse(languageKey) { null } ?: "")
        }
        return multipleLineFeeds.replace(strings.joinToString("\n"), "\n\n").trim()
    }

    // Source: https://www.hko.gov.hk/textonly/v2/explain/wxicon_e.htm
    private fun getWeatherText(
        context: Context,
        icon: Int?,
    ): String? {
        return when (icon) {
            50 -> context.getString(R.string.hko_weather_text_sunny) // Sunny
            51 -> context.getString(R.string.hko_weather_text_sunny_periods)
            52 -> context.getString(R.string.hko_weather_text_sunny_intervals)
            53 -> context.getString(R.string.hko_weather_text_sunny_periods_with_a_few_showers)
            54 -> context.getString(R.string.hko_weather_text_sunny_intervals_with_showers)
            60 -> context.getString(R.string.hko_weather_text_cloudy)
            61 -> context.getString(R.string.hko_weather_text_overcast)
            62 -> context.getString(R.string.hko_weather_text_light_rain)
            63 -> context.getString(R.string.hko_weather_text_rain)
            64 -> context.getString(R.string.hko_weather_text_heavy_rain)
            65 -> context.getString(R.string.hko_weather_text_thunderstorms)
            70, 71, 72, 73, 74, 75 -> context.getString(R.string.hko_weather_text_fine) // Fine
            76, 701, 711, 721, 741, 751 -> context.getString(R.string.hko_weather_text_mainly_cloudy) // Mainly Cloudy
            77, 702, 712, 722, 742, 752 -> context.getString(R.string.hko_weather_text_mainly_fine) // Mainly Fine
            80 -> context.getString(R.string.hko_weather_text_windy) // Windy
            81 -> context.getString(R.string.hko_weather_text_dry)
            82 -> context.getString(R.string.hko_weather_text_humid)
            83 -> context.getString(R.string.hko_weather_text_fog)
            84 -> context.getString(R.string.hko_weather_text_mist)
            85 -> context.getString(R.string.hko_weather_text_haze)
            90 -> context.getString(R.string.hko_weather_text_hot)
            91 -> context.getString(R.string.hko_weather_text_warm)
            92 -> context.getString(R.string.hko_weather_text_cool)
            93 -> context.getString(R.string.hko_weather_text_cold)
            else -> null
        }
    }

    // Source: https://www.hko.gov.hk/textonly/v2/explain/wxicon_e.htm
    private fun getWeatherCode(
        icon: Int?,
    ): WeatherCode? {
        return when (icon) {
            50, 51, 70, 71, 72, 73, 74, 75, 77, 702, 712, 722, 742, 752 -> WeatherCode.CLEAR
            52, 76, 701, 711, 721, 741, 751 -> WeatherCode.PARTLY_CLOUDY
            53, 54, 62, 63, 64 -> WeatherCode.RAIN
            60, 61 -> WeatherCode.CLOUDY
            65 -> WeatherCode.THUNDERSTORM
            // The codes below are only used in Current Observation
            // and never in hourly/daily forecasts.
            80 -> WeatherCode.WIND
            83, 84 -> WeatherCode.FOG
            85 -> WeatherCode.HAZE
            // TODO: In getCurrent, defer to hourly forecast data for the following codes?
            // 81 -> "Dry"
            // 82 -> "Humid"
            // 90 -> "Hot"
            // 91 -> "Warm"
            // 92 -> "Cool"
            // 93 -> "Cold"
            else -> null
        }
    }

    private fun getPrecipitationProbability(
        probability: String?,
    ): Ratio? {
        return when (probability) {
            "<10%" -> 10.0
            "20%" -> 20.0
            "40%" -> 40.0
            "60%" -> 60.0
            "80%" -> 80.0
            ">90%" -> 90.0
            else -> null
        }?.percent
    }

    private fun getAlertColor(
        warningCode: String?,
        severity: AlertSeverity = AlertSeverity.UNKNOWN,
    ): Int {
        return when (warningCode) {
            "WRAINY" -> Color.rgb(255, 204, 0)
            "WRAINR", "WMNB", "WFROST", "WFIRER", "WHOT" -> Color.rgb(255, 0, 0)
            "WRAINB" -> Color.rgb(0, 0, 0)
            "WTS", "WFIREY" -> Color.rgb(255, 186, 0)
            "WFNTSA" -> Color.rgb(0, 216, 89)
            "WLSA" -> Color.rgb(127, 102, 51)
            "WCOLD" -> Color.rgb(0, 0, 255)
            "WTMW" -> Color.rgb(0, 124, 188)
            else -> Alert.colorFromSeverity(severity)
        }
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val languageCode = context.currentLocale.code
        val currentGrid = getCurrentGrid(latitude, longitude)
        return mApi.getLocations(currentGrid).map { locationResult ->
            val json = "{\"type\":\"FeatureCollection\",\"features\":${locationResult.features}}"
            val geoJsonParser = GeoJsonParser(JSONObject(json))
            val matchingLocations = geoJsonParser.features.filter { feature ->
                when (feature.geometry) {
                    is GeoJsonPolygon -> (feature.geometry as GeoJsonPolygon).coordinates.any { polygon ->
                        PolyUtil.containsLocation(latitude, longitude, polygon, true)
                    }
                    is GeoJsonMultiPolygon -> (feature.geometry as GeoJsonMultiPolygon).polygons.any {
                        it.coordinates.any { polygon ->
                            PolyUtil.containsLocation(latitude, longitude, polygon, true)
                        }
                    }
                    else -> false
                }
            }
            matchingLocations.map {
                LocationAddressInfo(
                    timeZoneId = "Asia/Hong_Kong",
                    countryCode = "HK",
                    city = with(languageCode) {
                        when {
                            equals("zh-tw") || equals("zh-hk") || equals("zh-mo") -> {
                                it.getProperty("tc") ?: it.getProperty("sc") ?: it.getProperty("en")
                            }
                            startsWith("zh") -> {
                                it.getProperty("sc") ?: it.getProperty("tc") ?: it.getProperty("en")
                            }
                            else -> {
                                it.getProperty("en") ?: it.getProperty("sc") ?: it.getProperty("tc")
                            }
                        }
                    }
                )
            }
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        // IDs for current observation grid and forecast grid are different.
        // Also need to make sure we have a valid station for normals.
        val currentGrid = location.parameters.getOrElse(id) { null }?.getOrElse("currentGrid") { null }
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val forecastGrid = location.parameters.getOrElse(id) { null }?.getOrElse("forecastGrid") { null }

        return currentGrid.isNullOrEmpty() || currentStation.isNullOrEmpty() || forecastGrid.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        // Forecast grid is valid between (21.75°N–23.25°N, 113.35°E–114.95°E).
        // Current grid is valid between (22.13°N–22.57°N, 113.82°E–114.54°E).
        // If the location is within the boundaries of the current grid,
        // it is definitely within the boundaries of the forecast grid.
        if (location.latitude < HKO_CURRENT_GRID_LATITUDES.first() ||
            location.latitude >= HKO_CURRENT_GRID_LATITUDES.last() ||
            location.longitude < HKO_CURRENT_GRID_LONGITUDES.first() ||
            location.longitude >= HKO_CURRENT_GRID_LONGITUDES.last()
        ) {
            throw InvalidLocationException()
        }
        val currentGrid = getCurrentGrid(location.latitude, location.longitude)

        // Obtain the default weather station for temperature normals
        val station = mApi.getCurrentWeather(currentGrid).onErrorResumeNext {
            // TODO: Log warning
            Observable.just(HkoCurrentResult())
        }.blockingFirst().RegionalWeather?.Temp?.DefaultStation
        if (station == null) {
            throw InvalidLocationException()
        }

        // Identify grid ID for Forecast.
        // It is in a 16 E-W × 15-N-S configuration, numbered from 1 to 240.
        // Each block is 0.1° latitude and 0.1° longitude in size.
        val row = (232 - round(location.latitude * 10)).toInt()
        val column = (round(location.longitude * 10) - 1133).toInt()
        val forecastGrid = "G" + (row * 16 + column).toString()

        return Observable.just(
            mapOf(
                "currentGrid" to currentGrid,
                "currentStation" to station,
                "forecastGrid" to forecastGrid
            )
        )
    }

    private fun getCurrentGrid(
        latitude: Double,
        longitude: Double,
    ): String {
        // Make sure the location is within the boundaries of the current grid.
        if (latitude < HKO_CURRENT_GRID_LATITUDES.first() ||
            latitude >= HKO_CURRENT_GRID_LATITUDES.last() ||
            longitude < HKO_CURRENT_GRID_LONGITUDES.first() ||
            longitude >= HKO_CURRENT_GRID_LONGITUDES.last()
        ) {
            throw InvalidLocationException()
        }
        var row: Int? = null
        var column: Int? = null

        // Identify grid ID for Current Observation and Reverse Geolocation.
        // Current observations are in a 18 E-W × 18-N-S configuration, numbered from 0101 to 1818.
        // The grid spacing is uneven, hence we iterate through the lists of the grid boundaries.
        var i = 1
        while (i < HKO_CURRENT_GRID_LATITUDES.size && row == null) {
            if (latitude >= HKO_CURRENT_GRID_LATITUDES[i - 1] &&
                latitude < HKO_CURRENT_GRID_LATITUDES[i]
            ) {
                row = i
            }
            i++
        }

        i = 1
        while (i < HKO_CURRENT_GRID_LONGITUDES.size && column == null) {
            if (longitude >= HKO_CURRENT_GRID_LONGITUDES[i - 1] &&
                longitude < HKO_CURRENT_GRID_LONGITUDES[i]
            ) {
                column = i
            }
            i++
        }

        // Just double check that we got a valid grid ID
        if (row == null || column == null) {
            throw InvalidLocationException()
        }
        return column.toString().padStart(2, '0') + row.toString().padStart(2, '0')
    }

    override val testingLocations: List<Location> = emptyList()

    // Only supports its own country
    override val knownAmbiguousCountryCodes: Array<String>? = null

    companion object {
        private const val HKO_BASE_URL = "https://www.hko.gov.hk/"
        private const val HKO_MAPS_BASE_URL = "https://maps.weather.gov.hk/"
        private const val HKO_SIMPLIFIED_CHINESE_PATH = "dps/sc/"

        // Some Warning Summary records have different keys from the corresponding end points
        private val HKO_WARNING_ENDPOINTS = mapOf<String, String>(
            "WTCPRE8" to "WTCPRE8", // Pre-8 Tropical Cyclone Special Announcement
            "WTCSGNL" to "WTCB", // Tropical Cyclone Warning Signal
            "WRAIN" to "WRAINSA", // Rainstorm Warning Signal
            "WTMW" to "WTMW", // Tsunami Warning
            "WFNTSA" to "WFNTSA", // Special Announcement on Flooding in Northern New Territories
            "WMSGNL" to "WMNB", // Strong Monsoon Signal
            "WL" to "WLSA", // Landslip Warning
            "WTS" to "WTS", // Thunderstorm Warning
            "WFIRE" to "WFIRE", // Fire Danger Warning
            "WHOT" to "WHOT", // Very Hot Weather Warning
            "WCOLD" to "WCOLD", // Cold Weather Warning
            "WFROST" to "WFROST" // Frost Warning
        )

        // Current grid boundaries are taken from
        // https://my.weather.gov.hk/hiking/geojson/grid.geojson
        // We're not having Breeze Weather download the file itself each time
        // because it's static and unnecessarily big.
        private val HKO_CURRENT_GRID_LONGITUDES = listOf(
            113.816666666666663,
            113.88666666666667,
            113.956666666666663,
            113.996666666666655,
            114.036666666666662,
            114.076666666666654,
            114.11666666666666,
            114.136666666666656,
            114.156666666666666,
            114.176666666666662,
            114.196666666666658,
            114.216666666666669,
            114.236666666666665,
            114.276666666666671,
            114.316666666666663,
            114.356666666666669,
            114.396666666666661,
            114.466666666666654,
            114.536666666666662
        )
        private val HKO_CURRENT_GRID_LATITUDES = listOf(
            22.133333333333333,
            22.173333333333336,
            22.213333333333335,
            22.253333333333334,
            22.273333333333333,
            22.293333333333333,
            22.30833333333333,
            22.323333333333331,
            22.338333333333331,
            22.353333333333332,
            22.368333333333332,
            22.383333333333333,
            22.398333333333333,
            22.41333333333333,
            22.43333333333333,
            22.45333333333333,
            22.493333333333329,
            22.533333333333328,
            22.573333333333331
        )
    }
}
