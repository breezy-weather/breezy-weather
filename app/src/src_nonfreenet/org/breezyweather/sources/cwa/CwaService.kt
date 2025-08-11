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

package org.breezyweather.sources.cwa

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.PrecipitationProbability
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
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGH
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.sources.computeMeanSeaLevelPressure
import org.breezyweather.sources.computePollutantInUgm3FromPpb
import org.breezyweather.sources.cwa.json.CwaAirQualityResult
import org.breezyweather.sources.cwa.json.CwaAlertResult
import org.breezyweather.sources.cwa.json.CwaAssistantResult
import org.breezyweather.sources.cwa.json.CwaCurrentResult
import org.breezyweather.sources.cwa.json.CwaForecastResult
import org.breezyweather.sources.cwa.json.CwaLocationTown
import org.breezyweather.sources.cwa.json.CwaNormalsResult
import org.breezyweather.sources.nlsc.NlscService.Companion.KINMEN_BBOX
import org.breezyweather.sources.nlsc.NlscService.Companion.MATSU_BBOX
import org.breezyweather.sources.nlsc.NlscService.Companion.PENGHU_BBOX
import org.breezyweather.sources.nlsc.NlscService.Companion.TAIWAN_BBOX
import org.breezyweather.sources.nlsc.NlscService.Companion.WUQIU_BBOX
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.hours

class CwaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource, ConfigurableSource {

    override val id = "cwa"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "中央氣象署"
                else -> "CWA"
            }
        } +
            " (${context.currentLocale.getCountryName("TW")})"
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "https://www.cwa.gov.tw/V8/C/private.html"
                else -> "https://www.cwa.gov.tw/V8/E/private.html"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(CWA_BASE_URL)
            .build()
            .create(CwaApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "中央氣象署"
                else -> "Central Weather Administration"
            }
        }
    }
    private val airQualityAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "環境部"
                else -> "Ministry of Environment"
            }
        }
    }
    private val reverseGeocodingAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "內政部國土測繪中心"
                else -> "National Land Survey and Mapping Center"
            }
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to airQualityAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.cwa.gov.tw/",
        airQualityAttribution to "https://airtw.moenv.gov.tw/",
        reverseGeocodingAttribution to "https://www.nlsc.gov.tw/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        val latLng = LatLng(location.latitude, location.longitude)
        return location.countryCode.equals("TW", ignoreCase = true) ||
            TAIWAN_BBOX.contains(latLng) ||
            PENGHU_BBOX.contains(latLng) ||
            KINMEN_BBOX.contains(latLng) ||
            WUQIU_BBOX.contains(latLng) ||
            MATSU_BBOX.contains(latLng)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> if (feature == SourceFeature.ALERT) {
                PRIORITY_HIGH // This makes NCDR being used in priority
            } else {
                PRIORITY_HIGHEST
            }
            else -> PRIORITY_NONE
        }
    }

    @SuppressLint("CheckResult")
    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()

        // County Name and Township Code are retrieved upon reverse geocoding,
        // but not for user-selected locations. Since a few API calls require these,
        // we will make sure these parameters are available before proceeding.
        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }
        val countyName = location.parameters.getOrElse(id) { null }?.getOrElse("countyName") { null }
        val townshipName = location.parameters.getOrElse(id) { null }?.getOrElse("townshipName") { null }
        val townshipCode = location.parameters.getOrElse(id) { null }?.getOrElse("townshipCode") { null }
        if (stationId.isNullOrEmpty() ||
            countyName.isNullOrEmpty() ||
            townshipName.isNullOrEmpty() ||
            townshipCode.isNullOrEmpty() ||
            !CWA_HOURLY_ENDPOINTS.containsKey(countyName) ||
            !CWA_DAILY_ENDPOINTS.containsKey(countyName) ||
            !CWA_ASSISTANT_ENDPOINTS.containsKey(countyName)
        ) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                apiKey = apiKey,
                endpoint = CWA_HOURLY_ENDPOINTS[countyName]!!,
                townshipName = townshipName
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(CwaForecastResult())
            }
        } else {
            Observable.just(CwaForecastResult())
        }
        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                apiKey = apiKey,
                endpoint = CWA_DAILY_ENDPOINTS[countyName]!!,
                townshipName = townshipName
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(CwaForecastResult())
            }
        } else {
            Observable.just(CwaForecastResult())
        }

        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                apiKey = apiKey,
                stationId = stationId
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(CwaCurrentResult())
            }
        } else {
            Observable.just(CwaCurrentResult())
        }

        // "Weather Assistant" provides human-written forecast summary on a county level.
        val assistant = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getAssistant(
                endpoint = CWA_ASSISTANT_ENDPOINTS[countyName]!!,
                apiKey = apiKey
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(CwaAssistantResult())
            }
        } else {
            Observable.just(CwaAssistantResult())
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            val body = LINE_FEED_SPACES.replace(
                """
            {
                "query":"query aqi{
                    aqi(
                        longitude:${location.longitude},
                        latitude:${location.latitude}
                    ){
                        pm2_5,
                        pm10,
                        o3,
                        no2,
                        so2,
                        co
                    }
                }",
                "variables":null
            }
            """,
                ""
            )
            mApi.getAirQuality(
                apiKey = apiKey,
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(CwaAirQualityResult())
            }
        } else {
            Observable.just(CwaAirQualityResult())
        }

        // Temperature normals are only available at 27 stations (out of 700+),
        // and not available in the main weather API call.
        // Therefore we will call a different endpoint,
        // but we must specify the station ID rather than using lat/lon.
        val currentMonth = Date().toCalendarWithTimeZone(location.timeZone)[Calendar.MONTH] + 1
        val station = LatLng(location.latitude, location.longitude).getNearestLocation(CWA_NORMALS_STATIONS)
        val normals = if (SourceFeature.NORMALS in requestedFeatures && station != null) {
            mApi.getNormals(
                apiKey = apiKey,
                stationId = station,
                month = currentMonth.toString()
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(CwaNormalsResult())
            }
        } else {
            Observable.just(CwaNormalsResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts(
                apiKey = apiKey
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(CwaAlertResult())
            }
        } else {
            Observable.just(CwaAlertResult())
        }

        return Observable.zip(current, airQuality, daily, hourly, normals, alerts, assistant) {
                currentResult: CwaCurrentResult,
                airQualityResult: CwaAirQualityResult,
                dailyResult: CwaForecastResult,
                hourlyResult: CwaForecastResult,
                normalsResult: CwaNormalsResult,
                alertResult: CwaAlertResult,
                assistantResult: CwaAssistantResult,
            ->
            val currentWrapper = if (SourceFeature.CURRENT in requestedFeatures) {
                getCurrent(currentResult, assistantResult)
            } else {
                null
            }

            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(dailyResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(hourlyResult)
                } else {
                    null
                },
                current = currentWrapper,
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(
                        current = getAirQuality(
                            airQualityResult,
                            currentWrapper?.temperature?.temperature,
                            getValid(currentResult.records?.station?.getOrNull(0)?.weatherElement?.airPressure)
                                as Double?
                        )
                    )
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(alertResult, location)
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
        currentResult: CwaCurrentResult,
        assistantResult: CwaAssistantResult,
    ): CurrentWrapper {
        var latitude: Double? = null
        currentResult.records?.station?.getOrNull(0)?.geoInfo?.coordinates?.forEach {
            if (it.coordinateName == "WGS84") {
                latitude = it.stationLatitude
            }
        }
        val altitude = currentResult.records?.station?.getOrNull(0)?.geoInfo?.stationAltitude?.toDoubleOrNull()
        val current = currentResult.records?.station?.getOrNull(0)?.weatherElement
        val temperature = getValid(current?.airTemperature) as Double?
        val relativeHumidity = getValid(current?.relativeHumidity) as Double?
        val barometricPressure = getValid(current?.airPressure) as Double?
        val windDirection = getValid(current?.windDirection) as Double?
        val windSpeed = getValid(current?.windSpeed) as Double?
        val windGusts = getValid(current?.gustInfo?.peakGustSpeed) as Double?
        val weatherText = getValid(current?.weather) as String?
        var weatherCode: WeatherCode? = null

        // The current observation result does not come with a "code".
        // We need to decipher the best code to use based on the text.
        // First we check for precipitation, thunder, and fog conditions.
        weatherText?.let {
            weatherCode = when {
                it.endsWith("有雷") -> WeatherCode.THUNDER
                it.endsWith("大雷雹") -> WeatherCode.HAIL
                it.endsWith("大雷雨") -> WeatherCode.THUNDERSTORM
                it.endsWith("有雷雹") -> WeatherCode.HAIL
                it.endsWith("有雷雪") -> WeatherCode.SNOW
                it.endsWith("有雷雨") -> WeatherCode.THUNDERSTORM
                it.endsWith("有雹") -> WeatherCode.HAIL
                it.endsWith("陣雨雪") -> WeatherCode.SLEET
                it.endsWith("有陣雨") -> WeatherCode.RAIN
                it.endsWith("有大雪") || it.endsWith("有雪珠") || it.endsWith("有冰珠") -> WeatherCode.SNOW
                it.endsWith("有雨雪") -> WeatherCode.SLEET
                it.endsWith("有雨") -> WeatherCode.RAIN
                it.endsWith("有霧") -> WeatherCode.FOG
                it.endsWith("有閃電") || it.endsWith("有雷聲") -> WeatherCode.THUNDER
                it.endsWith("有靄") -> WeatherCode.FOG
                it.endsWith("有霾") -> WeatherCode.HAZE

                // If there is no precipitation, thunder, or fog, we check for strong winds.
                // CWA's thresholds for "Strong Wind Advisory" are
                // sustained winds of Bft 6 (10.8m/s), or gusts Bft 8 (17.2m/s).
                (windSpeed ?: 0.0) >= 10.8 || (windGusts ?: 0.0) >= 17.2 -> WeatherCode.WIND

                // If there is no precipitation, thunder, fog, or wind,
                // we determine the code from cloud cover.
                it.startsWith("晴") -> WeatherCode.CLEAR
                it.startsWith("多雲") -> WeatherCode.PARTLY_CLOUDY
                it.startsWith("陰") -> WeatherCode.CLOUDY

                else -> null
            }
        }

        // "Weather Assistant" returns a few paragraphs of human-written forecast summary.
        // We only want the first paragraph to keep it concise.
        val dailyForecast: String? = if (assistantResult.cwaopendata != null) {
            assistantResult.cwaopendata.dataset?.parameterSet?.parameter?.getOrNull(0)?.parameterValue
        } else {
            // Just in case the Assistant feed regresses to "cwbopendata" as the root property.
            assistantResult.cwbopendata?.dataset?.parameterSet?.parameter?.getOrNull(0)?.parameterValue
        }

        return CurrentWrapper(
            weatherText = weatherText,
            weatherCode = weatherCode,
            temperature = TemperatureWrapper(
                temperature = temperature
            ),
            wind = Wind(
                degree = windDirection,
                speed = windSpeed,
                gusts = windGusts
            ),
            relativeHumidity = relativeHumidity,
            pressure = computeMeanSeaLevelPressure(
                barometricPressure = barometricPressure,
                altitude = altitude,
                temperature = temperature,
                humidity = relativeHumidity,
                latitude = latitude
            )?.hectopascals,
            dailyForecast = dailyForecast
        )
    }

    private fun getNormals(
        normalsResult: CwaNormalsResult,
    ): Map<Month, Normals>? {
        return normalsResult.records?.data?.surfaceObs?.location?.getOrNull(0)
            ?.stationObsStatistics?.AirTemperature?.monthly
            ?.filter { it.Month?.toIntOrNull() != null && it.Month.toInt() in 1..12 }
            ?.associate {
                Month.of(it.Month!!.toInt()) to Normals(
                    daytimeTemperature = it.Maximum?.toDoubleOrNull(),
                    nighttimeTemperature = it.Minimum?.toDoubleOrNull()
                )
            }
    }

    // Concentrations of SO₂, NO₂, O₃ are given in ppb (and in ppm for CO).
    // We need to convert these figures to µg/m³ (and mg/m³ for CO).
    private fun getAirQuality(
        airQualityResult: CwaAirQualityResult?,
        temperature: Double?,
        pressure: Double?,
    ): AirQuality? {
        return airQualityResult?.data?.aqi?.getOrNull(0)?.let {
            AirQuality(
                pM25 = it.pm25?.toDoubleOrNull(),
                pM10 = it.pm10?.toDoubleOrNull(),
                sO2 = computePollutantInUgm3FromPpb(
                    PollutantIndex.SO2,
                    it.so2?.toDoubleOrNull(),
                    temperature,
                    pressure
                ),
                nO2 = computePollutantInUgm3FromPpb(
                    PollutantIndex.NO2,
                    it.no2?.toDoubleOrNull(),
                    temperature,
                    pressure
                ),
                o3 = computePollutantInUgm3FromPpb(
                    PollutantIndex.O3,
                    it.o3?.toDoubleOrNull(),
                    temperature,
                    pressure
                ),
                cO = computePollutantInUgm3FromPpb(
                    PollutantIndex.CO,
                    it.co?.toDoubleOrNull(),
                    temperature,
                    pressure
                )
            )
        }
    }

    // Forecast data from the main weather API call are unsorted.
// We need to first store the numbers into maps, then sort the keys,
// and retrieve the relevant numbers using the sorted keys.
    private fun getDailyForecast(
        dailyResult: CwaForecastResult,
    ): List<DailyWrapper> {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")

        val dailyList = mutableListOf<DailyWrapper>()
        val popMap = mutableMapOf<Long, Double?>()
        val wsMap = mutableMapOf<Long, Double?>()
        val maxAtMap = mutableMapOf<Long, Double?>()
        val wxTextMap = mutableMapOf<Long, String?>()
        val wxCodeMap = mutableMapOf<Long, WeatherCode?>()
        val minTMap = mutableMapOf<Long, Double?>()
        val uviMap = mutableMapOf<Long, Double?>()
        val minAtMap = mutableMapOf<Long, Double?>()
        val maxTMap = mutableMapOf<Long, Double?>()
        val wdMap = mutableMapOf<Long, Double?>()

        var key: Long
        var extraMilliSeconds: Long

        // New schema from 2024-12-10
        dailyResult.records?.locations?.getOrNull(0)?.location?.getOrNull(0)?.weatherElement?.forEach { element ->
            element.time?.forEach { item ->
                if (item.startTime != null) {
                    // We calculate delta from the previous 06:00 and 18:00 local time (22:00 and 10:00 UTC).
                    // So that we can normalize quarter-day start times (12:00 and 00:00) to half-day start times.
                    extraMilliSeconds =
                        (item.startTime.time - 10.hours.inWholeMilliseconds).mod(12.hours.inWholeMilliseconds)
                    key = item.startTime.time - extraMilliSeconds

                    item.elementValue?.getOrNull(0)?.let {
                        // We have to assign the map values within individual if statements,
                        // otherwise the null values from later elements will overwrite actual values from earlier ones.
                        if (it.maxTemperature != null) {
                            maxTMap[key] = getValid(it.maxTemperature.toDoubleOrNull()) as Double?
                        }
                        if (it.minTemperature != null) {
                            minTMap[key] = getValid(it.minTemperature.toDoubleOrNull()) as Double?
                        }
                        if (it.maxApparentTemperature != null) {
                            maxAtMap[key] = getValid(it.maxApparentTemperature.toDoubleOrNull()) as Double?
                        }
                        if (it.minApparentTemperature != null) {
                            minAtMap[key] = getValid(it.minApparentTemperature.toDoubleOrNull()) as Double?
                        }
                        if (it.windDirection != null) {
                            wdMap[key] = getWindDirection(getValid(it.windDirection) as String?)
                        }
                        if (it.windSpeed != null) {
                            wsMap[key] = if (it.windSpeed == ">= 11") {
                                11.0
                            } else {
                                getValid(it.windSpeed.toDoubleOrNull()) as Double?
                            }
                        }
                        if (it.probabilityOfPrecipitation != null) {
                            popMap[key] = getValid(it.probabilityOfPrecipitation.toDoubleOrNull()) as Double?
                        }
                        if (it.weather != null) {
                            wxTextMap[key] = getValid(it.weather) as String?
                        }
                        if (it.weatherCode != null) {
                            wxCodeMap[key] = getWeatherCode(getValid(it.weatherCode) as String?)
                        }
                        if (it.uvIndex != null) {
                            uviMap[key] = getValid(it.uvIndex.toDoubleOrNull()) as Double?
                        }
                    }
                }
            }
        }

        val dates = wxTextMap.keys.groupBy { formatter.format(it).substring(0, 10) }.keys
        var dayTime: Long
        var nightTime: Long
        dates.forEachIndexed { i, date ->
            dayTime = formatter.parse("$date 06:00:00")!!.time
            nightTime = formatter.parse("$date 18:00:00")!!.time
            dailyList.add(
                DailyWrapper(
                    date = formatter.parse("$date 00:00:00")!!,
                    day = HalfDayWrapper(
                        weatherText = wxTextMap.getOrElse(dayTime) { null },
                        weatherCode = wxCodeMap.getOrElse(dayTime) { null },
                        temperature = TemperatureWrapper(
                            temperature = maxTMap.getOrElse(dayTime) { null },
                            feelsLike = maxAtMap.getOrElse(dayTime) { null }
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = popMap.getOrElse(dayTime) { null }
                        ),
                        wind = Wind(
                            degree = wdMap.getOrElse(dayTime) { null },
                            speed = wsMap.getOrElse(dayTime) { null }
                        )
                    ),
                    night = HalfDayWrapper(
                        weatherText = wxTextMap.getOrElse(nightTime) { null },
                        weatherCode = wxCodeMap.getOrElse(nightTime) { null },
                        temperature = TemperatureWrapper(
                            temperature = minTMap.getOrElse(nightTime) { null },
                            feelsLike = minAtMap.getOrElse(nightTime) { null }
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = popMap.getOrElse(nightTime) { null }
                        ),
                        wind = Wind(
                            degree = wdMap.getOrElse(nightTime) { null },
                            speed = wsMap.getOrElse(nightTime) { null }
                        )
                    ),
                    uV = UV(
                        index = uviMap.getOrElse(dayTime) { null }
                    )
                )
            )
        }
        return dailyList
    }

    // Forecast data from the main weather API call are unsorted.
// We need to first store the numbers into maps, then sort the keys,
// and retrieve the relevant numbers using the sorted keys.
    private fun getHourlyForecast(
        hourlyResult: CwaForecastResult,
    ): List<HourlyWrapper> {
        val hourlyList = mutableListOf<HourlyWrapper>()
        val wxTextMap = mutableMapOf<Long, String?>()
        val wxCodeMap = mutableMapOf<Long, WeatherCode?>()
        val atMap = mutableMapOf<Long, Double?>()
        val tMap = mutableMapOf<Long, Double?>()
        val rhMap = mutableMapOf<Long, Double?>()
        val popMap = mutableMapOf<Long, Double?>()
        val wsMap = mutableMapOf<Long, Double?>()
        val wdMap = mutableMapOf<Long, Double?>()
        val tdMap = mutableMapOf<Long, Double?>()
        var key: Long

        // New schema from 2024-12-10
        hourlyResult.records?.locations?.getOrNull(0)?.location?.getOrNull(0)?.weatherElement?.forEach { element ->
            element.time?.forEach { item ->
                if (item.dataTime != null || item.startTime != null) {
                    key = (item.dataTime ?: item.startTime!!).time
                    item.elementValue?.getOrNull(0)?.let {
                        // We have to assign the map values within individual if statements,
                        // otherwise the null values from later elements will overwrite actual values from earlier ones.
                        if (it.temperature != null) {
                            tMap[key] = getValid(it.temperature.toDoubleOrNull()) as Double?
                        }
                        if (it.dewPoint != null) {
                            tdMap[key] = getValid(it.dewPoint.toDoubleOrNull()) as Double?
                        }
                        if (it.apparentTemperature != null) {
                            atMap[key] = getValid(it.apparentTemperature.toDoubleOrNull()) as Double?
                        }
                        if (it.relativeHumidity != null) {
                            rhMap[key] = getValid(it.relativeHumidity.toDoubleOrNull()) as Double?
                        }
                        if (it.windDirection != null) {
                            wdMap[key] = getWindDirection(getValid(it.windDirection) as String?)
                        }
                        if (it.windSpeed != null) {
                            wsMap[key] = if (it.windSpeed == ">= 11") {
                                11.0
                            } else {
                                getValid(it.windSpeed.toDoubleOrNull()) as Double?
                            }
                        }
                        if (it.probabilityOfPrecipitation != null) {
                            popMap[key] = getValid(it.probabilityOfPrecipitation.toDoubleOrNull()) as Double?
                        }
                        if (it.weather != null) {
                            wxTextMap[key] = getValid(it.weather) as String?
                        }
                        if (it.weatherCode != null) {
                            wxCodeMap[key] = getWeatherCode(getValid(it.weatherCode) as String?)
                        }
                    }
                }
            }
        }

        var lastWd: Double? = null
        var lastWs: Double? = null
        var lastPop: Double? = null
        var lastWxText: String? = null
        var lastWxCode: WeatherCode? = null
        tMap.keys.sorted().forEach { key ->
            // Not all elements are forecast for each hour.
            // Fill the missing elements with the last known values.
            if (wdMap.containsKey(key)) {
                lastWd = wdMap[key]
            } else {
                wdMap[key] = lastWd
            }
            if (wsMap.containsKey(key)) {
                lastWs = wsMap[key]
            } else {
                wsMap[key] = lastWs
            }
            if (popMap.containsKey(key)) {
                lastPop = popMap[key]
            } else {
                popMap[key] = lastPop
            }
            if (wxTextMap.containsKey(key)) {
                lastWxText = wxTextMap[key]
            } else {
                wxTextMap[key] = lastWxText
            }
            if (wxCodeMap.containsKey(key)) {
                lastWxCode = wxCodeMap[key]
            } else {
                wxCodeMap[key] = lastWxCode
            }

            hourlyList.add(
                HourlyWrapper(
                    date = Date(key),
                    weatherText = wxTextMap.getOrElse(key) { null },
                    weatherCode = wxCodeMap.getOrElse(key) { null },
                    temperature = TemperatureWrapper(
                        temperature = tMap.getOrElse(key) { null },
                        feelsLike = atMap.getOrElse(key) { null }
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = popMap.getOrElse(key) { null }
                    ),
                    wind = Wind(
                        degree = wdMap.getOrElse(key) { null },
                        speed = wsMap.getOrElse(key) { null }
                    ),
                    relativeHumidity = rhMap.getOrElse(key) { null },
                    dewPoint = tdMap.getOrElse(key) { null }
                )
            )
        }
        return hourlyList
    }

    // CWA issues warnings primarily for counties,
// but also for specific areas in each county:
//  • 山區 Mountain ("M"): 59 townships
//  • 基隆北海岸 Keelung North Coast ("K"): 15 townships
//  • 恆春半島 Hengchun Peninsula ("H"): 6 townships
//  • 蘭嶼綠島 Lanyu and Ludao ("L"): 2 townships
// These specifications are stored in CWA_TOWNSHIP_WARNING_AREAS.
    private fun getAlertList(
        alertResult: CwaAlertResult,
        location: Location,
    ): List<Alert> {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")
        val alertList = mutableListOf<Alert>()
        var headline: String
        var severity: AlertSeverity
        var alert: Alert
        var applicable: Boolean
        val id = "cwa"

        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }
        val countyName = location.parameters.getOrElse(id) { null }?.getOrElse("countyName") { null }
        val townshipName = location.parameters.getOrElse(id) { null }?.getOrElse("townshipName") { null }
        val townshipCode = location.parameters.getOrElse(id) { null }?.getOrElse("townshipCode") { null }
        if (stationId.isNullOrEmpty() ||
            countyName.isNullOrEmpty() ||
            townshipName.isNullOrEmpty() ||
            townshipCode.isNullOrEmpty()
        ) {
            throw InvalidLocationException()
        }

        val warningArea = CWA_TOWNSHIP_WARNING_AREAS.getOrElse(townshipCode) { "G" }

        alertResult.records?.record?.forEach { record ->
            applicable = false
            record.hazardConditions?.hazards?.hazard?.forEach { hazard ->
                hazard.info?.affectedAreas?.location?.forEach { location ->
                    if (
                        location.locationName == countyName ||
                        (location.locationName == countyName + "山區" && warningArea == "M") ||
                        (location.locationName == "基隆北海岸" && warningArea == "K") ||
                        (location.locationName == "恆春半島" && warningArea == "H") ||
                        (location.locationName == "蘭嶼綠島" && warningArea == "L")
                    ) {
                        // so we don't cover up a more severe level with a less severe one
                        // TODO: Why? There can be multiple same-type alerts at different times
                        if (!applicable) {
                            applicable = true
                            headline = hazard.info.phenomena + hazard.info.significance
                            severity = getAlertSeverity(headline)
                            alert = Alert(
                                // TODO: Unsafe
                                alertId = headline + "-" + record.datasetInfo!!.validTime.startTime,
                                startDate = formatter.parse(record.datasetInfo.validTime.startTime)!!,
                                endDate = formatter.parse(record.datasetInfo.validTime.endTime)!!,
                                headline = headline,
                                description = record.contents?.content?.contentText?.trim(),
                                source = "中央氣象署",
                                severity = severity,
                                color = getAlertColor(headline, severity)
                            )
                            alertList.add(alert)
                        }
                    }
                }
            }
        }

        return alertList
    }

    private fun getWindDirection(direction: String?): Double? {
        return if (direction == null) {
            null
        } else {
            when (direction) {
                "偏北風" -> 0.0
                "東北風" -> 45.0
                "偏東風" -> 90.0
                "東南風" -> 135.0
                "偏南風" -> 180.0
                "西南風" -> 225.0
                "偏西風" -> 270.0
                "西北風" -> 315.0
                else -> null
            }
        }
    }

    // Weather icon source:
// https://opendata.cwa.gov.tw/opendatadoc/MFC/A0012-001.pdf
    private fun getWeatherCode(icon: String?): WeatherCode? {
        return if (icon == null) {
            null
        } else {
            when (icon) {
                "01", "02" -> WeatherCode.CLEAR
                "03", "04" -> WeatherCode.PARTLY_CLOUDY
                "05", "06", "07" -> WeatherCode.CLOUDY
                "08", "09", "10", "11", "12", "13", "14",
                "19", "20", "29", "30", "31", "32", "38", "39",
                -> WeatherCode.RAIN
                "15", "16", "21", "22", "33", "34", "35", "36" -> WeatherCode.THUNDER
                "17", "18", "41" -> WeatherCode.THUNDERSTORM
                "23", "37", "40" -> WeatherCode.SLEET
                "24", "25", "26", "27", "28" -> WeatherCode.FOG
                "42" -> WeatherCode.SNOW
                else -> null
            }
        }
    }

    private fun getAlertSeverity(headline: String): AlertSeverity {
        return when (headline) {
            // missing severity levels for the following because we are not sure about wording in the API JSON yet
            // 低溫特報 (嚴寒, 非常寒冷, 寒冷), 高溫資訊 (紅燈, 橙燈, 黃燈)
            "超大豪雨特報" -> AlertSeverity.EXTREME
            "大豪雨特報", "海上陸上颱風警報", "陸上颱風警報", "海嘯警報" -> AlertSeverity.SEVERE
            "豪雨特報", "海上颱風警報", "海嘯警訊" -> AlertSeverity.MODERATE
            "熱帶性低氣壓特報", "大雨特報", "海嘯消息", "濃霧特報",
            "長浪即時訊息", "陸上強風特報", "海上強風特報",
            -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
    }

    // Color source: https://www.cwa.gov.tw/V8/assets/css/main.css
    private fun getAlertColor(headline: String, severity: AlertSeverity): Int {
        return when (headline) {
            "陸上強風特報" -> Color.rgb(230, 229, 98)
            "濃霧特報" -> Color.rgb(151, 240, 60)
            else -> when (severity) {
                AlertSeverity.EXTREME -> Color.rgb(214, 0, 204)
                AlertSeverity.SEVERE -> Color.rgb(255, 0, 0)
                AlertSeverity.MODERATE -> Color.rgb(255, 128, 0)
                AlertSeverity.MINOR -> Color.rgb(255, 255, 2)
                else -> Color.rgb(237, 146, 156)
            }
        }
    }

    private fun getValid(
        value: Any?,
    ): Any? {
        return if (value != -99 && value != -99.0 && value != "-99" && value != "-99.0") {
            value
        } else {
            null
        }
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val apiKey = getApiKeyOrDefault()

        // The reverse geocoding API call requires plugging in the location's coordinates
        // (latitude: $latitude, longitude: $longitude) into the body of a PUSH request.
        val body = LINE_FEED_SPACES.replace(
            """
            {
                "query":"query aqi{
                    aqi(
                        longitude:$longitude,
                        latitude:$latitude
                    ){
                        station{
                            StationId
                        },
                        town{
                            ctyName,
                            townCode,
                            townName,
                            villageName
                        }
                    }
                }",
                "variables":null
            }
        """,
            ""
        )
        return mApi.getLocation(
            apiKey,
            body.toRequestBody("application/json".toMediaTypeOrNull())
        ).map {
            if (it.data?.aqi?.getOrNull(0)?.town == null) {
                throw InvalidLocationException()
            }
            listOf(convertLocation(it.data.aqi[0].town!!))
        }
    }

    private fun convertLocation(
        town: CwaLocationTown,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            timeZoneId = "Asia/Taipei",
            countryCode = "TW",
            admin1 = town.ctyName,
            city = town.townName,
            district = town.villageName
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }
        val countyName = location.parameters.getOrElse(id) { null }?.getOrElse("countyName") { null }
        val townshipName = location.parameters.getOrElse(id) { null }?.getOrElse("townshipName") { null }
        val townshipCode = location.parameters.getOrElse(id) { null }?.getOrElse("townshipCode") { null }

        return stationId.isNullOrEmpty() ||
            countyName.isNullOrEmpty() ||
            townshipName.isNullOrEmpty() ||
            townshipCode.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val apiKey = getApiKeyOrDefault()

        // The reverse geocoding API call requires plugging in the location's coordinates
        // (latitude: $latitude, longitude: $longitude) into the body of a PUSH request.
        val body = LINE_FEED_SPACES.replace(
            """
            {
                "query":"query aqi{
                    aqi(
                        longitude:${location.longitude},
                        latitude:${location.latitude}
                    ){
                        station{
                            StationId
                        },
                        town{
                            ctyName,
                            townCode,
                            townName,
                            villageName
                        }
                    }
                }",
                "variables":null
            }
        """,
            ""
        )
        return mApi.getLocation(
            apiKey,
            body.toRequestBody("application/json".toMediaTypeOrNull())
        ).map {
            if (it.data?.aqi?.getOrNull(0) == null ||
                it.data.aqi[0].station?.StationId == null ||
                it.data.aqi[0].town?.ctyName == null ||
                it.data.aqi[0].town?.townName == null ||
                it.data.aqi[0].town?.townCode == null
            ) {
                throw InvalidLocationException()
            }
            mapOf(
                "stationId" to it.data.aqi[0].station!!.StationId!!,
                "countyName" to it.data.aqi[0].town!!.ctyName!!,
                "townshipName" to it.data.aqi[0].town!!.townName,
                "townshipCode" to it.data.aqi[0].town!!.townCode!!
            )
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
        return apikey.ifEmpty { BuildConfig.CWA_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_cwa_api_key,
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
        private const val CWA_BASE_URL = "https://opendata.cwa.gov.tw/"
        private val LINE_FEED_SPACES = Regex("""\n\s*""")
    }
}
