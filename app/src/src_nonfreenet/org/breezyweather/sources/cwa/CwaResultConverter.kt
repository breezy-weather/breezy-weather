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

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
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
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.hours

internal fun convert(
    context: Context,
    location: Location,
    town: CwaLocationTown,
): Location {
    return location.copy(
        timeZone = "Asia/Taipei",
        country = context.currentLocale.getCountryName("TW"),
        countryCode = "TW",
        admin1 = town.ctyName,
        city = town.townName,
        district = town.villageName
    )
}

internal fun getCurrent(
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
        ),
        dailyForecast = dailyForecast
    )
}

internal fun getNormals(
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
internal fun getAirQuality(
    airQualityResult: CwaAirQualityResult?,
    temperature: Double?,
    pressure: Double?,
): AirQuality? {
    return airQualityResult?.data?.aqi?.getOrNull(0)?.let {
        AirQuality(
            pM25 = it.pm25?.toDoubleOrNull(),
            pM10 = it.pm10?.toDoubleOrNull(),
            sO2 = computePollutantInUgm3FromPpb(PollutantIndex.SO2, it.so2?.toDoubleOrNull(), temperature, pressure),
            nO2 = computePollutantInUgm3FromPpb(PollutantIndex.NO2, it.no2?.toDoubleOrNull(), temperature, pressure),
            o3 = computePollutantInUgm3FromPpb(PollutantIndex.O3, it.o3?.toDoubleOrNull(), temperature, pressure),
            cO = computePollutantInUgm3FromPpb(PollutantIndex.CO, it.co?.toDoubleOrNull(), temperature, pressure)
        )
    }
}

// Forecast data from the main weather API call are unsorted.
// We need to first store the numbers into maps, then sort the keys,
// and retrieve the relevant numbers using the sorted keys.
internal fun getDailyForecast(
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
internal fun getHourlyForecast(
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
internal fun getAlertList(
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

internal fun getValid(
    value: Any?,
): Any? {
    return if (value != -99 && value != -99.0 && value != "-99" && value != "-99.0") {
        value
    } else {
        null
    }
}
