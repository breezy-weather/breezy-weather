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

package org.breezyweather.sources.jma

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCalendarMonth
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.jma.json.JmaAlertResult
import org.breezyweather.sources.jma.json.JmaAmedasResult
import org.breezyweather.sources.jma.json.JmaAreasResult
import org.breezyweather.sources.jma.json.JmaBulletinResult
import org.breezyweather.sources.jma.json.JmaCurrentResult
import org.breezyweather.sources.jma.json.JmaDailyResult
import org.breezyweather.sources.jma.json.JmaForecastAreaResult
import org.breezyweather.sources.jma.json.JmaHourlyResult
import org.breezyweather.sources.jma.json.JmaWeekAreaResult
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.json.JSONObject
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.floor
import kotlin.math.max
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class JmaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
    private val okHttpClient: OkHttpClient,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "jma"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "気象庁"
        } else {
            "JMA (${context.currentLocale.getCountryName("JP")})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "https://www.jma.go.jp/jma/kishou/info/coment.html"
        } else {
            "https://www.jma.go.jp/jma/en/copyright.html"
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(JMA_BASE_URL)
            .build()
            .create(JmaApi::class.java)
    }

    private val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "気象庁"
        } else {
            "Japan Meteorological Agency"
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
        weatherAttribution to "https://www.jma.go.jp/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("JP", ignoreCase = true)
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
        val parameters = location.parameters.getOrElse(id) { null }
        val class20s = parameters?.getOrElse("class20s") { null }
        val class10s = parameters?.getOrElse("class10s") { null }
        val prefArea = parameters?.getOrElse("prefArea") { null }
        val weekArea05 = parameters?.getOrElse("weekArea05") { null }
        val weekAreaAmedas = parameters?.getOrElse("weekAreaAmedas") { null }
        val forecastAmedas = parameters?.getOrElse("forecastAmedas") { null }
        val currentAmedas = parameters?.getOrElse("currentAmedas") { null }

        if (class20s.isNullOrEmpty() ||
            class10s.isNullOrEmpty() ||
            prefArea.isNullOrEmpty() ||
            weekArea05.isNullOrEmpty() ||
            weekAreaAmedas.isNullOrEmpty() ||
            forecastAmedas.isNullOrEmpty() ||
            currentAmedas.isNullOrEmpty()
        ) {
            return Observable.error(InvalidLocationException())
        }

        // Special case for Amami, Kagoshima Prefecture
        val forecastPrefArea = if (prefArea == "460040") {
            "460100"
        } else {
            prefArea
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily(forecastPrefArea).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly(class10s).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(JmaHourlyResult())
            }
        } else {
            Observable.just(JmaHourlyResult())
        }

        // CURRENT
        // Need to first get the correct timestamp for latest observation data.
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            val request = Request.Builder().url(JMA_BASE_URL + "bosai/amedas/data/latest_time.txt").build()
            val incomingFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
            val outgoingFormatter = SimpleDateFormat("yyyyMMdd_HH", Locale.ENGLISH)
            incomingFormatter.timeZone = TimeZone.getTimeZone("Asia/Tokyo")
            outgoingFormatter.timeZone = TimeZone.getTimeZone("Asia/Tokyo")

            okHttpClient.newCall(request).execute().use { call ->
                if (call.isSuccessful) {
                    val latestTime = incomingFormatter.parse(call.body.string())!!.time

                    // Observation data is recorded in 3-hourly files.
                    val timestamp = (
                        floor(latestTime.toDouble() / 3.hours.inWholeMilliseconds) *
                            3.hours.inWholeMilliseconds
                        ).toLong()
                    mApi.getCurrent(
                        amedas = currentAmedas,
                        timestamp = outgoingFormatter.format(timestamp)
                    ).onErrorResumeNext {
                        failedFeatures[SourceFeature.CURRENT] = it
                        Observable.just(emptyMap())
                    }
                } else {
                    failedFeatures[SourceFeature.CURRENT] = WeatherException()
                    Observable.just(emptyMap())
                }
            }
        } else {
            Observable.just(emptyMap())
        }

        val bulletin = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getBulletin(forecastPrefArea).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(JmaBulletinResult())
            }
        } else {
            Observable.just(JmaBulletinResult())
        }

        // ALERT
        val alert = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlert(prefArea).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(JmaAlertResult())
            }
        } else {
            Observable.just(JmaAlertResult())
        }

        return Observable.zip(current, bulletin, daily, hourly, alert) {
                currentResult: Map<String, JmaCurrentResult>,
                bulletinResult: JmaBulletinResult,
                dailyResult: List<JmaDailyResult>,
                hourlyResult: JmaHourlyResult,
                alertResult: JmaAlertResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyResult, class10s, weekArea05, weekAreaAmedas, forecastAmedas)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult, bulletinResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, alertResult, class20s)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(dailyResult, weekAreaAmedas)?.let { normals ->
                        mapOf(
                            Date().getCalendarMonth(location) to normals
                        )
                    }
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    private fun getCurrent(
        context: Context,
        currentResult: Map<String, JmaCurrentResult>,
        bulletinResult: JmaBulletinResult,
    ): CurrentWrapper? {
        val lastKey = currentResult.keys.sortedDescending()[0]
        val lastHourKey = lastKey.substring(0, 10) + "0000"
        var dailyForecast = bulletinResult.text?.trim()
        if ((dailyForecast ?: "").startsWith("【")) {
            dailyForecast = dailyForecast?.substringAfter("】")?.trim()
        }
        dailyForecast = dailyForecast?.substringBefore("\n")?.trim()
        return currentResult[lastKey]?.let {
            val weather = currentResult.getOrElse(lastHourKey) { null }?.weather?.getOrNull(0)
            CurrentWrapper(
                weatherText = getCurrentWeatherText(context, weather),
                weatherCode = getCurrentWeatherCode(weather),
                temperature = TemperatureWrapper(
                    temperature = it.temp?.getOrNull(0)
                ),
                wind = Wind(
                    degree = getWindDirection(it.windDirection?.getOrNull(0)),
                    speed = it.wind?.getOrNull(0)
                ),
                relativeHumidity = it.humidity?.getOrNull(0),
                pressure = it.normalPressure?.getOrNull(0)?.hectopascals,
                visibility = it.visibility?.getOrNull(0)?.meters,
                dailyForecast = dailyForecast
            )
        }
    }

    private fun getDailyForecast(
        context: Context,
        dailyResult: List<JmaDailyResult>,
        class10s: String,
        weekArea05: String,
        weekAreaAmedas: String,
        forecastAmedas: String,
    ): List<DailyWrapper> {
        val formatter = SimpleDateFormat("HH", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Tokyo")

        val wxMap = mutableMapOf<Long, String?>()
        val maxTMap = mutableMapOf<Long, Double?>()
        val minTMap = mutableMapOf<Long, Double?>()
        val popMap = mutableMapOf<Long, Double>()

        // 7-day weather conditions and daily probabilities of precipitation
        dailyResult.getOrNull(1)?.timeSeries?.getOrNull(0)?.let { series ->
            series.areas
                ?.filter { it.area.code == weekArea05 }
                ?.forEach { area ->
                    series.timeDefines?.forEachIndexed { i, timeDefines ->
                        timeDefines.time.let {
                            wxMap[it] = area.weatherCodes?.getOrNull(i)
                            val pop = area.pops?.getOrNull(i)?.toDoubleOrNull()
                            // fill up popMap from 6am local time rather than midnight,
                            // otherwise the midnight-to-6am PoP will be duplicated in daily charts
                            popMap[it + 6.hours.inWholeMilliseconds] = pop ?: 0.0
                            popMap[it + 12.hours.inWholeMilliseconds] = pop ?: 0.0
                            popMap[it + 18.hours.inWholeMilliseconds] = pop ?: 0.0
                            popMap[it + 24.hours.inWholeMilliseconds] = pop ?: 0.0
                        }
                    }
                }
        }

        // 7-day max and min temperatures
        dailyResult.getOrNull(1)?.timeSeries?.getOrNull(1)?.let { series ->
            series.areas
                ?.filter { it.area.code == weekAreaAmedas }
                ?.forEach { area ->
                    series.timeDefines?.forEachIndexed { i, timeDefines ->
                        timeDefines.time.let {
                            minTMap[it] = area.tempsMin?.getOrNull(i)?.toDoubleOrNull()
                            maxTMap[it] = area.tempsMax?.getOrNull(i)?.toDoubleOrNull()
                        }
                    }
                }
        }

        // 3-day weather conditions
        dailyResult.getOrNull(0)?.timeSeries?.getOrNull(0)?.let { series ->
            series.areas
                ?.filter { it.area.code == class10s }
                ?.forEach { area ->
                    series.timeDefines?.forEachIndexed { i, timeDefines ->
                        timeDefines.time.let {
                            // normalize timestamp to midnight local time
                            val midnight = (
                                floor((it.toDouble() + 9.hours.inWholeMilliseconds) / 1.days.inWholeMilliseconds) *
                                    1.days.inWholeMilliseconds -
                                    9.hours.inWholeMilliseconds
                                ).toLong()
                            wxMap[midnight] = area.weatherCodes?.getOrNull(i)
                        }
                    }
                }
        }

        // 3-day 6-hourly probabilities of precipitation
        dailyResult.getOrNull(0)?.timeSeries?.getOrNull(1)?.let { series ->
            series.areas
                ?.filter { it.area.code == class10s }
                ?.forEach { area ->
                    series.timeDefines?.forEachIndexed { i, timeDefines ->
                        timeDefines.time.let {
                            popMap[it] = area.pops?.getOrNull(i)?.toDoubleOrNull() ?: 0.0
                        }
                    }
                }
        }

        // 3-day max and min temperatures
        val forecastAmedasCodes = forecastAmedas.split(",")
        dailyResult.getOrNull(0)?.timeSeries?.getOrNull(2)?.let { series ->
            series.areas?.forEach { area ->
                forecastAmedasCodes
                    .filter { area.area.code == it }
                    .forEach { code ->
                        series.timeDefines?.forEachIndexed { i, timeDefines ->
                            timeDefines.time.let {
                                val hour = formatter.format(Date(it))
                                if (hour == "00") {
                                    minTMap[it] = area.temps?.getOrNull(i)?.toDoubleOrNull()
                                }
                                if (hour == "09") {
                                    maxTMap[it - 9.hours.inWholeMilliseconds] =
                                        area.temps?.getOrNull(i)?.toDoubleOrNull()
                                }
                            }
                        }
                    }
            }
        }

        return minTMap.keys.sorted().map { key ->
            DailyWrapper(
                date = Date(key),
                day = HalfDayWrapper(
                    weatherText = getDailyWeatherText(
                        context = context,
                        weather = wxMap.getOrElse(key) { null },
                        night = false
                    ),
                    weatherCode = getDailyWeatherCode(
                        weather = wxMap.getOrElse(key) { null },
                        night = false
                    ),
                    temperature = TemperatureWrapper(
                        temperature = maxTMap.getOrElse(key) { null }
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = max(
                            popMap.getOrElse(key + 6.hours.inWholeMilliseconds) { 0.0 },
                            popMap.getOrElse(key + 12.hours.inWholeMilliseconds) { 0.0 }
                        )
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = getDailyWeatherText(
                        context = context,
                        weather = wxMap.getOrElse(key) { null },
                        night = true
                    ),
                    weatherCode = getDailyWeatherCode(
                        weather = wxMap.getOrElse(key) { null },
                        night = true
                    ),
                    temperature = TemperatureWrapper(
                        temperature = minTMap.getOrElse(key + 1.days.inWholeMilliseconds) { null }
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = max(
                            popMap.getOrElse(key + 18.hours.inWholeMilliseconds) { 0.0 },
                            popMap.getOrElse(key + 24.hours.inWholeMilliseconds) { 0.0 }
                        )
                    )
                )
            )
        }
    }

    private fun getHourlyForecast(
        context: Context,
        hourlyResult: JmaHourlyResult,
    ): List<HourlyWrapper> {
        val wxTextMap = mutableMapOf<Long, String?>()
        val wxCodeMap = mutableMapOf<Long, WeatherCode?>()
        val tMap = mutableMapOf<Long, Double?>()
        val wdMap = mutableMapOf<Long, Double?>()
        val wsMap = mutableMapOf<Long, Double?>()
        hourlyResult.areaTimeSeries?.let { timeSeries ->
            timeSeries.timeDefines?.forEachIndexed { i, timeDefines ->
                timeDefines?.dateTime?.time?.let {
                    wxTextMap[it] = getHourlyWeatherText(context, timeSeries.weather?.getOrNull(i))
                    wxCodeMap[it] = getHourlyWeatherCode(timeSeries.weather?.getOrNull(i))
                    wdMap[it] = getWindDirection(timeSeries.wind?.getOrNull(i)?.direction)
                    wsMap[it] = timeSeries.wind?.getOrNull(i)?.range?.substringAfterLast(" ")?.toDoubleOrNull()
                }
            }
        }
        hourlyResult.pointTimeSeries?.let { timeSeries ->
            timeSeries.timeDefines?.forEachIndexed { i, timeDefines ->
                timeDefines?.dateTime?.time?.let {
                    tMap[it] = timeSeries.temperature?.getOrNull(i)
                }
            }
        }

        return wxTextMap.keys.sorted().map { key ->
            HourlyWrapper(
                date = Date(key),
                weatherText = wxTextMap.getOrElse(key) { null },
                weatherCode = wxCodeMap.getOrElse(key) { null },
                temperature = TemperatureWrapper(
                    temperature = tMap.getOrElse(key) { null }
                ),
                wind = Wind(
                    degree = wdMap.getOrElse(key) { null },
                    speed = wsMap.getOrElse(key) { null }
                )
            )
        }
    }

    private fun getNormals(
        dailyResult: List<JmaDailyResult>,
        weekAreaAmedas: String,
    ): Normals? {
        dailyResult.getOrNull(1)?.tempAverage?.areas?.forEach { area ->
            if (area.area.code == weekAreaAmedas) {
                return Normals(
                    daytimeTemperature = area.max?.toDoubleOrNull(),
                    nighttimeTemperature = area.min?.toDoubleOrNull()
                )
            }
        }
        return null
    }

    private fun getAlertList(
        context: Context,
        alertResult: JmaAlertResult,
        class20s: String,
    ): List<Alert> {
        val alertList = mutableListOf<Alert>()
        var severity: AlertSeverity
        alertResult.areaTypes?.getOrNull(1)?.areas?.forEach { area ->
            if (area.code == class20s) {
                area.warnings?.forEach { warning ->
                    if (warning.status != "発表警報・注意報はなし" && warning.status != "解除") {
                        severity = getAlertSeverity(warning.code)
                        alertList.add(
                            Alert(
                                alertId = "${warning.code} ${alertResult.reportDatetime?.time}",
                                startDate = alertResult.reportDatetime,
                                headline = getAlertHeadline(context, warning.code),
                                description = alertResult.headlineText?.trim(),
                                source = alertResult.publishingOffice?.trim(),
                                severity = severity,
                                color = getAlertColor(severity)
                            )
                        )
                    }
                }
            }
        }
        return alertList
    }

    private fun getWindDirection(
        direction: Any?,
    ): Double? {
        return when (direction) {
            0 -> -1.0
            1 -> 22.5
            2, "北東" -> 45.0
            3 -> 67.5
            4, "東" -> 90.0
            5 -> 112.5
            6, "南東" -> 135.0
            7 -> 157.5
            8, "南" -> 180.0
            9 -> 202.5
            10, "南西" -> 225.0
            11 -> 247.5
            12, "西" -> 270.0
            13 -> 292.5
            14, "北西" -> 315.0
            15 -> 337.5
            16, "北" -> 0.0
            else -> null
        }
    }

    private fun getHourlyWeatherText(
        context: Context,
        weather: String?,
    ): String? {
        return when (weather) {
            "晴れ" -> context.getString(R.string.common_weather_text_clear_sky)
            "くもり" -> context.getString(R.string.common_weather_text_cloudy)
            "雨" -> context.getString(R.string.common_weather_text_rain)
            "雪" -> context.getString(R.string.common_weather_text_snow)
            "雨または雪" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
            else -> null
        }
    }

    private fun getHourlyWeatherCode(
        weather: String?,
    ): WeatherCode? {
        return when (weather) {
            "晴れ" -> WeatherCode.CLEAR
            "くもり" -> WeatherCode.CLOUDY
            "雨" -> WeatherCode.RAIN
            "雪" -> WeatherCode.SNOW
            "雨または雪" -> WeatherCode.SLEET
            else -> null
        }
    }

    // There are 118 daily weather codes used by JMA.
// They have been listed in its own file at JmaConstants.kt.
    private fun getDailyWeatherText(
        context: Context,
        weather: String?,
        night: Boolean = false,
    ): String? {
        val i = if (night) {
            1
        } else {
            0
        }
        val stringId = if (JMA_DAILY_WEATHER_TEXTS.containsKey(weather)) {
            JMA_DAILY_WEATHER_TEXTS[weather]!!.getOrNull(i)
        } else {
            null
        }
        return stringId?.let { context.getString(it) }
    }

    // There are 118 daily weather codes used by JMA.
// They have been listed in its own file at JmaConstants.kt.
    private fun getDailyWeatherCode(
        weather: String?,
        night: Boolean = false,
    ): WeatherCode? {
        val i = if (night) {
            1
        } else {
            0
        }
        return if (JMA_DAILY_WEATHER_CODES.containsKey(weather)) {
            JMA_DAILY_WEATHER_CODES[weather]!!.getOrNull(i)
        } else {
            null
        }
    }

    private fun getCurrentWeatherText(
        context: Context,
        weather: Int?,
    ): String? {
        return when (weather) {
            0 -> context.getString(R.string.common_weather_text_clear_sky) // 晴
            1 -> context.getString(R.string.common_weather_text_cloudy) // 曇
            2 -> context.getString(R.string.weather_kind_haze) // 煙霧
            3 -> context.getString(R.string.common_weather_text_fog) // 霧
            4 -> context.getString(R.string.common_weather_text_rain) // 降水またはしゅう雨性の降水
            5 -> context.getString(R.string.common_weather_text_drizzle) // 霧雨
            6 -> context.getString(R.string.common_weather_text_drizzle_freezing) // 着氷性の霧雨
            7 -> context.getString(R.string.common_weather_text_rain) // 雨
            8 -> context.getString(R.string.common_weather_text_rain_freezing) // 着氷性の雨
            9 -> context.getString(R.string.common_weather_text_rain_snow_mixed) // みぞれ
            10 -> context.getString(R.string.common_weather_text_snow) // 雪
            11 -> context.getString(R.string.common_weather_text_rain_freezing) // 凍雨
            12 -> context.getString(R.string.common_weather_text_snow_grains) // 霧雪
            13 -> context.getString(R.string.common_weather_text_rain_showers) // しゅう雨または止み間のある雨
            14 -> context.getString(R.string.common_weather_text_snow_showers) // しゅう雪または止み間のある雪
            15 -> context.getString(R.string.weather_kind_hail) // ひょう
            16 -> context.getString(R.string.weather_kind_thunderstorm) // 雷
            else -> null
        }
    }

    private fun getCurrentWeatherCode(
        weather: Int?,
    ): WeatherCode? {
        return when (weather) {
            0 -> WeatherCode.CLEAR
            1 -> WeatherCode.CLOUDY
            2 -> WeatherCode.HAZE
            3 -> WeatherCode.FOG
            4, 5, 7, 13 -> WeatherCode.RAIN
            6, 8, 9, 11 -> WeatherCode.SLEET
            10, 12, 14 -> WeatherCode.SNOW
            15 -> WeatherCode.HAIL
            16 -> WeatherCode.THUNDERSTORM
            else -> null
        }
    }

    private fun getAlertHeadline(
        context: Context,
        code: String?,
    ): String? {
        return when (code) {
            "33" -> context.getString(R.string.jma_warning_text_heavy_rain_emergency) // 大雨特別警報
            "03" -> context.getString(R.string.jma_warning_text_heavy_rain_warning) // 大雨警報
            "10" -> context.getString(R.string.jma_warning_text_heavy_rain_advisory) // 大雨注意報
            "04" -> context.getString(R.string.jma_warning_text_flood_warning) // 洪水警報
            "18" -> context.getString(R.string.jma_warning_text_flood_advisory) // 洪水注意報
            "35" -> context.getString(R.string.jma_warning_text_storm_emergency) // 暴風特別警報
            "05" -> context.getString(R.string.jma_warning_text_storm_warning) // 暴風警報
            "15" -> context.getString(R.string.jma_warning_text_gale_advisory) // 強風注意報
            "32" -> context.getString(R.string.jma_warning_text_snowstorm_emergency) // 暴風雪特別警報
            "02" -> context.getString(R.string.jma_warning_text_snowstorm_warning) // 暴風雪警報
            "13" -> context.getString(R.string.jma_warning_text_gale_and_snow_advisory) // 風雪注意報
            "36" -> context.getString(R.string.jma_warning_text_heavy_snow_emergency) // 大雪特別警報
            "06" -> context.getString(R.string.jma_warning_text_heavy_snow_warning) // 大雪警報
            "12" -> context.getString(R.string.jma_warning_text_heavy_snow_advisory) // 大雪注意報
            "37" -> context.getString(R.string.jma_warning_text_high_wave_emergency) // 波浪特別警報
            "07" -> context.getString(R.string.jma_warning_text_high_wave_warning) // 波浪警報
            "16" -> context.getString(R.string.jma_warning_text_high_wave_advisory) // 波浪注意報
            "38" -> context.getString(R.string.jma_warning_text_storm_surge_emergency) // 高潮特別警報
            "08" -> context.getString(R.string.jma_warning_text_storm_surge_warning) // 高潮警報
            "19+" -> context.getString(R.string.jma_warning_text_storm_surge_advisory) // 高潮注意報
            "19" -> context.getString(R.string.jma_warning_text_storm_surge_advisory) // 高潮注意報
            "14" -> context.getString(R.string.jma_warning_text_thunderstorm_advisory) // 雷注意報
            "17" -> context.getString(R.string.jma_warning_text_snow_melting_advisory) // 融雪注意報
            "20" -> context.getString(R.string.jma_warning_text_dense_fog_advisory) // 濃霧注意報
            "21" -> context.getString(R.string.jma_warning_text_dry_air_advisory) // 乾燥注意報
            "22" -> context.getString(R.string.jma_warning_text_avalanche_advisory) // なだれ注意報
            "23" -> context.getString(R.string.jma_warning_text_low_temperature_advisory) // 低温注意報
            "24" -> context.getString(R.string.jma_warning_text_frost_advisory) // 霜注意報
            "25" -> context.getString(R.string.jma_warning_text_ice_accretion_advisory) // 着氷注意報
            "26" -> context.getString(R.string.jma_warning_text_snow_accretion_advisory) // 着雪注意報
            else -> null
        }
    }

    private fun getAlertSeverity(
        code: String?,
    ): AlertSeverity {
        return when (code) {
            "33" -> AlertSeverity.EXTREME
            "35", "32", "36", "37", "38", "08" -> AlertSeverity.SEVERE
            "03", "04", "05", "02", "06", "07", "19+" -> AlertSeverity.MODERATE
            "10", "18", "15", "13", "12", "16", "19", "14", "17",
            "20", "21", "22", "23", "24", "25", "26",
            -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
    }

    private fun getAlertColor(
        severity: AlertSeverity,
    ): Int {
        return when (severity) {
            AlertSeverity.EXTREME -> Color.rgb(12, 0, 12)
            AlertSeverity.SEVERE -> Color.rgb(160, 0, 160)
            AlertSeverity.MODERATE -> Color.rgb(255, 40, 0)
            AlertSeverity.MINOR -> Color.rgb(242, 231, 0)
            else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
        }
    }

    // Reverse geocoding
    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val areas = mApi.getAreas()
        val class20s = mApi.getRelm().map { relm ->
            val features = mutableListOf<Any?>()
            relm.forEachIndexed { i, it ->
                if (latitude <= it.ne[0] &&
                    longitude <= it.ne[1] &&
                    latitude >= it.sw[0] &&
                    longitude >= it.sw[1]
                ) {
                    features.addAll(mApi.getClass20s(i).blockingFirst().features)
                }
            }
            features
        }

        return Observable.zip(areas, class20s) {
                areasResult: JmaAreasResult,
                class20sFeatures: List<Any?>,
            ->
            convertLocation(context, latitude, longitude, areasResult, class20sFeatures)
        }
    }

    // Reverse geocoding
    private fun convertLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        areasResult: JmaAreasResult,
        class20sFeatures: List<Any?>,
    ): List<LocationAddressInfo> {
        val matchingLocations = getMatchingLocations(latitude, longitude, class20sFeatures)
        if (matchingLocations.isEmpty()) {
            throw InvalidLocationException()
        }
        val locationList = mutableListOf<LocationAddressInfo>()
        matchingLocations[0].let {
            val code = it.getProperty("code")
            if (code == null) {
                throw InvalidLocationException()
            }
            var city = if (context.currentLocale.code.startsWith("ja")) {
                areasResult.class20s?.get(code)?.name ?: ""
            } else {
                areasResult.class20s?.get(code)?.enName ?: ""
            }
            var district: String? = null

            // Split the city and district strings if necessary
            if (Regex("""^\d{6}[^0]$""").matches(code)) {
                if (context.currentLocale.code.startsWith("ja")) {
                    val matchResult = Regex("""^(.+[市町村])（?([^（^）]*)）?$""").find(city)!!
                    city = matchResult.groups[1]!!.value
                    district = matchResult.groups[2]!!.value
                    if (Regex("""を除く$""").matches(district)) {
                        district = null
                    }
                } else {
                    if (city.contains(",")) {
                        district = city.substringBefore(",").trim()
                        city = city.substringAfter(",").trim()
                    } else if (Regex("""^(Northern|Southern|Eastern|Western) """).matches(city)) {
                        district = city.substringBefore("ern ").trim() + "ern"
                        city = city.substringAfter("ern ").trim()
                    } else if (city.contains(" (")) {
                        district = city.substringAfter(" (").substringBefore(")").trim()
                        city = city.substringBefore(" (")
                    }
                }
            }
            locationList.add(
                LocationAddressInfo(
                    timeZoneId = "Asia/Tokyo",
                    countryCode = "JP",
                    admin1 = getPrefecture(context, code),
                    admin1Code = code.substring(0, 2),
                    city = city,
                    district = district
                )
            )
        }
        return locationList
    }

    private fun getPrefecture(
        context: Context,
        code: String,
    ): String? {
        if (context.currentLocale.code.startsWith("ja")) {
            return with(code) {
                when {
                    startsWith("01") -> "北海道" // Hokkaido
                    startsWith("02") -> "青森県" // Aomori
                    startsWith("03") -> "岩手県" // Iwate
                    startsWith("04") -> "宮城県" // Miyagi
                    startsWith("05") -> "秋田県" // Akita
                    startsWith("06") -> "山形県" // Yamagata
                    startsWith("07") -> "福島県" // Fukushima
                    startsWith("08") -> "茨城県" // Ibaraki
                    startsWith("09") -> "栃木県" // Tochigi
                    startsWith("10") -> "群馬県" // Gunma
                    startsWith("11") -> "埼玉県" // Saitama
                    startsWith("12") -> "千葉県" // Chiba
                    startsWith("13") -> "東京都" // Tōkyō
                    startsWith("14") -> "神奈川県" // Kanagawa
                    startsWith("15") -> "新潟県" // Niigata
                    startsWith("16") -> "富山県" // Toyama
                    startsWith("17") -> "石川県" // Ishikawa
                    startsWith("18") -> "福井県" // Fukui
                    startsWith("19") -> "山梨県" // Yamanashi
                    startsWith("20") -> "長野県" // Nagano
                    startsWith("21") -> "岐阜県" // Gifu
                    startsWith("22") -> "静岡県" // Shizuoka
                    startsWith("23") -> "愛知県" // Aichi
                    startsWith("24") -> "三重県" // Mie
                    startsWith("25") -> "滋賀県" // Shiga
                    startsWith("26") -> "京都府" // Kyōto
                    startsWith("27") -> "大阪府" // Ōsaka
                    startsWith("28") -> "兵庫県" // Hyōgo
                    startsWith("29") -> "奈良県" // Nara
                    startsWith("30") -> "和歌山県" // Wakayama
                    startsWith("31") -> "鳥取県" // Tottori
                    startsWith("32") -> "島根県" // Shimane
                    startsWith("33") -> "岡山県" // Okayama
                    startsWith("34") -> "広島県" // Hiroshima
                    startsWith("35") -> "山口県" // Yamaguchi
                    startsWith("36") -> "徳島県" // Tokushima
                    startsWith("37") -> "香川県" // Kagawa
                    startsWith("38") -> "愛媛県" // Ehime
                    startsWith("39") -> "高知県" // Kōchi
                    startsWith("40") -> "福岡県" // Fukuoka
                    startsWith("41") -> "佐賀県" // Saga
                    startsWith("42") -> "長崎県" // Nagasaki
                    startsWith("43") -> "熊本県" // Kumamoto
                    startsWith("44") -> "大分県" // Ōita
                    startsWith("45") -> "宮崎県" // Miyazaki
                    startsWith("46") -> "鹿児島県" // Kagoshima
                    startsWith("47") -> "沖縄県" // Okinawa
                    else -> null
                }
            }
        } else {
            return with(code) {
                when {
                    startsWith("01") -> "Hokkaido" // 北海道
                    startsWith("02") -> "Aomori" // 青森県
                    startsWith("03") -> "Iwate" // 岩手県
                    startsWith("04") -> "Miyagi" // 宮城県
                    startsWith("05") -> "Akita" // 秋田県
                    startsWith("06") -> "Yamagata" // 山形県
                    startsWith("07") -> "Fukushima" // 福島県
                    startsWith("08") -> "Ibaraki" // 茨城県
                    startsWith("09") -> "Tochigi" // 栃木県
                    startsWith("10") -> "Gunma" // 群馬県
                    startsWith("11") -> "Saitama" // 埼玉県
                    startsWith("12") -> "Chiba" // 千葉県
                    startsWith("13") -> "Tōkyō" // 東京都
                    startsWith("14") -> "Kanagawa" // 神奈川県
                    startsWith("15") -> "Niigata" // 新潟県
                    startsWith("16") -> "Toyama" // 富山県
                    startsWith("17") -> "Ishikawa" // 石川県
                    startsWith("18") -> "Fukui" // 福井県
                    startsWith("19") -> "Yamanashi" // 山梨県
                    startsWith("20") -> "Nagano" // 長野県
                    startsWith("21") -> "Gifu" // 岐阜県
                    startsWith("22") -> "Shizuoka" // 静岡県
                    startsWith("23") -> "Aichi" // 愛知県
                    startsWith("24") -> "Mie" // 三重県
                    startsWith("25") -> "Shiga" // 滋賀県
                    startsWith("26") -> "Kyōto" // 京都府
                    startsWith("27") -> "Ōsaka" // 大阪府
                    startsWith("28") -> "Hyōgo" // 兵庫県
                    startsWith("29") -> "Nara" // 奈良県
                    startsWith("30") -> "Wakayama" // 和歌山県
                    startsWith("31") -> "Tottori" // 鳥取県
                    startsWith("32") -> "Shimane" // 島根県
                    startsWith("33") -> "Okayama" // 岡山県
                    startsWith("34") -> "Hiroshima" // 広島県
                    startsWith("35") -> "Yamaguchi" // 山口県
                    startsWith("36") -> "Tokushima" // 徳島県
                    startsWith("37") -> "Kagawa" // 香川県
                    startsWith("38") -> "Ehime" // 愛媛県
                    startsWith("39") -> "Kōchi" // 高知県
                    startsWith("40") -> "Fukuoka" // 福岡県
                    startsWith("41") -> "Saga" // 佐賀県
                    startsWith("42") -> "Nagasaki" // 長崎県
                    startsWith("43") -> "Kumamoto" // 熊本県
                    startsWith("44") -> "Ōita" // 大分県
                    startsWith("45") -> "Miyazaki" // 宮崎県
                    startsWith("46") -> "Kagoshima" // 鹿児島県
                    startsWith("47") -> "Okinawa" // 沖縄県
                    else -> null
                }
            }
        }
    }

    private fun getMatchingLocations(
        latitude: Double,
        longitude: Double,
        class20sFeatures: List<Any?>,
    ): List<GeoJsonFeature> {
        val json = """{"type":"FeatureCollection","features":[${class20sFeatures.joinToString(",")}]}"""
        val geoJsonParser = GeoJsonParser(JSONObject(json))
        return geoJsonParser.features.filter { feature ->
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
    }

    // Location parameters
    internal fun convertLocationParameters(
        location: Location,
        areasResult: JmaAreasResult,
        class20sFeatures: List<Any?>,
        weekAreaResult: Map<String, List<JmaWeekAreaResult>>,
        weekArea05Result: Map<String, List<String>>,
        forecastAreaResult: Map<String, List<JmaForecastAreaResult>>,
        amedasResult: Map<String, JmaAmedasResult>,
    ): Map<String, String> {
        val class20s: String
        val class15s: String
        val class10s: String
        val prefArea: String
        var weekArea05 = ""
        var weekAreaAmedas = ""
        var forecastAmedas = ""
        var currentAmedas = ""

        val matchingLocations = getMatchingLocations(location.latitude, location.longitude, class20sFeatures)
        if (matchingLocations.isEmpty()) {
            throw InvalidLocationException()
        }
        matchingLocations[0].let {
            class20s = it.getProperty("code") ?: ""
            class15s = areasResult.class20s?.get(class20s)?.parent ?: ""
            class10s = areasResult.class15s?.get(class15s)?.parent ?: ""
            prefArea = areasResult.class10s?.get(class10s)?.parent ?: ""
        }

        weekArea05Result.getOrElse(class10s) { null }?.forEach { wa5 ->
            weekAreaResult.getOrElse(prefArea) { null }?.forEach { wa ->
                if (wa.week == wa5) {
                    weekArea05 = wa5
                    weekAreaAmedas = wa.amedas
                }
            }
        }

        forecastAreaResult.getOrElse(prefArea) { null }?.forEach { fa ->
            if (fa.class10 == class10s) {
                forecastAmedas = fa.amedas.joinToString(",")
            }
        }

        var nearestDistance = Double.POSITIVE_INFINITY
        var distance: Double
        amedasResult.keys.forEach { key ->
            amedasResult[key]?.let {
                distance = SphericalUtil.computeDistanceBetween(
                    LatLng(location.latitude, location.longitude),
                    LatLng(
                        it.lat.getOrElse(0) { 0.0 } + it.lat.getOrElse(1) { 0.0 } / 60.0,
                        it.lon.getOrElse(0) { 0.0 } + it.lon.getOrElse(1) { 0.0 } / 60.0
                    )
                )
                if (distance < nearestDistance) {
                    if (it.elems.substring(0, 1) == "1") {
                        nearestDistance = distance
                        currentAmedas = key
                    }
                }
            }
        }

        return mapOf(
            "class20s" to class20s,
            "class10s" to class10s,
            "prefArea" to prefArea,
            "weekArea05" to weekArea05,
            "weekAreaAmedas" to weekAreaAmedas,
            "forecastAmedas" to forecastAmedas,
            "currentAmedas" to currentAmedas
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val parameters = location.parameters.getOrElse(id) { null }

        val class20s = parameters?.getOrElse("class20s") { null }
        val class10s = parameters?.getOrElse("class10s") { null }
        val prefArea = parameters?.getOrElse("prefArea") { null }
        val weekArea05 = parameters?.getOrElse("weekArea05") { null }
        val weekAreaAmedas = parameters?.getOrElse("weekAreaAmedas") { null }
        val forecastAmedas = parameters?.getOrElse("forecastAmedas") { null }
        val currentAmedas = parameters?.getOrElse("currentAmedas") { null }

        return class20s.isNullOrEmpty() ||
            class10s.isNullOrEmpty() ||
            prefArea.isNullOrEmpty() ||
            weekArea05.isNullOrEmpty() ||
            weekAreaAmedas.isNullOrEmpty() ||
            forecastAmedas.isNullOrEmpty() ||
            currentAmedas.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val areas = mApi.getAreas()
        val class20s = mApi.getRelm().map { relm ->
            val features = mutableListOf<Any?>()
            relm.forEachIndexed { i, it ->
                if (location.latitude <= it.ne[0] &&
                    location.longitude <= it.ne[1] &&
                    location.latitude >= it.sw[0] &&
                    location.longitude >= it.sw[1]
                ) {
                    features.addAll(mApi.getClass20s(i).blockingFirst().features)
                }
            }
            features
        }
        val weekArea = mApi.getWeekArea()
        val weekArea05 = mApi.getWeekArea05()
        val forecastArea = mApi.getForecastArea()
        val amedas = mApi.getAmedas()

        return Observable.zip(areas, class20s, weekArea, weekArea05, forecastArea, amedas) {
                areasResult: JmaAreasResult,
                class20sFeatures: List<Any?>,
                weekAreaResult: Map<String, List<JmaWeekAreaResult>>,
                weekArea05Result: Map<String, List<String>>,
                forecastAreaResult: Map<String, List<JmaForecastAreaResult>>,
                amedasResult: Map<String, JmaAmedasResult>,
            ->
            convertLocationParameters(
                location = location,
                areasResult = areasResult,
                class20sFeatures = class20sFeatures,
                weekAreaResult = weekAreaResult,
                weekArea05Result = weekArea05Result,
                forecastAreaResult = forecastAreaResult,
                amedasResult = amedasResult
            )
        }
    }

    override val testingLocations: List<Location> = emptyList()

    // Only supports its own country
    override val knownAmbiguousCountryCodes: Array<String>? = null

    companion object {
        private const val JMA_BASE_URL = "https://www.jma.go.jp/"
    }
}
