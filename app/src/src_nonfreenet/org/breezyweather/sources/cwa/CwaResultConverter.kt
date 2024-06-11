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

import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.cwa.json.CwaAlertResult
import org.breezyweather.sources.cwa.json.CwaAstroResult
import org.breezyweather.sources.cwa.json.CwaLocationTown
import org.breezyweather.sources.cwa.json.CwaNormalsResult
import org.breezyweather.sources.cwa.json.CwaWeatherForecast
import org.breezyweather.sources.cwa.json.CwaWeatherResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun convert(
    location: Location,
    town: CwaLocationTown
): Location {
    return location.copy(
        timeZone = "Asia/Taipei",
        country = "臺灣",
        province = town.ctyName,
        city = town.townName!!,
        district = town.villageName
    )
}

fun convert(
    weatherResult: CwaWeatherResult,
    normalsResult: CwaNormalsResult,
    alertResult: CwaAlertResult,
    sunResult: CwaAstroResult,
    moonResult: CwaAstroResult,
    location: Location,
    id: String,
    ignoreFeatures: List<SecondaryWeatherSourceFeature>
): WeatherWrapper {
    if (weatherResult.data?.aqi?.getOrNull(0)?.town?.daily == null || weatherResult.data.aqi[0].town?.hourly == null) {
        throw InvalidOrIncompleteDataException()
    }
    return WeatherWrapper(
        current = getCurrent(weatherResult, ignoreFeatures),
        normals = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) getNormals(normalsResult) else null,
        dailyForecast = getDailyForecast(weatherResult.data.aqi[0].town!!.daily, sunResult, moonResult),
        hourlyForecast = getHourlyForecast(weatherResult.data.aqi[0].town!!.hourly),
        alertList = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) getAlertList(alertResult, location, id) else null
    )
}

fun convertSecondary(
    airQualityResult: CwaWeatherResult?,
    alertResult: CwaAlertResult?,
    normalsResult: CwaNormalsResult?,
    location: Location,
    id: String
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        airQuality = if (airQualityResult != null) AirQualityWrapper(current = getAirQuality(airQualityResult, null, null)) else null,
        alertList = if (alertResult != null) getAlertList(alertResult, location, id) else null,
        normals = if (normalsResult != null) getNormals(normalsResult) else null
    )
}

private fun getCurrent(
    weatherResult: CwaWeatherResult,
    ignoreFeatures: List<SecondaryWeatherSourceFeature>
): Current {
    var weatherText: String? = null
    var weatherCode: WeatherCode? = null
    var temperature: Temperature? = null
    var wind: Wind? = null
    var windDegree: Double? = null
    var windSpeed: Double? = null
    var windGusts: Double? = null
    var airQuality: AirQuality? = null
    var relativeHumidity: Double? = null
    var pressure: Double? = null

    weatherResult.data!!.aqi[0].station!!.weatherElement.forEach {
        if (it.elementValue != "-99" && it.elementValue != "-99.0") {
            when (it.elementName) {
                "Weather" -> weatherText = it.elementValue
                "TEMP" -> temperature = Temperature( temperature = it.elementValue.toDouble() )
                "WDIR" -> windDegree = it.elementValue.toDouble()
                "WDSD" -> windSpeed = it.elementValue.toDouble()
                "H_FX" -> windGusts = it.elementValue.toDouble()
                "HUMD" -> relativeHumidity = it.elementValue.toDouble()
                "PRES" -> pressure = it.elementValue.toDouble()
            }
        }
    }

    // The current observation result does not come with a "code".
    // We need to decipher the best code to use based on the text.
    // First we check for precipitation, thunder, and fog conditions.
    if (weatherText != null) {
        if (weatherText!!.endsWith("有霾")) weatherCode = WeatherCode.HAZE
        if (weatherText!!.endsWith("有靄")) weatherCode = WeatherCode.FOG
        if (weatherText!!.endsWith("有閃電")) weatherCode = WeatherCode.THUNDER
        if (weatherText!!.endsWith("有雷聲")) weatherCode = WeatherCode.THUNDER
        if (weatherText!!.endsWith("有霧")) weatherCode = WeatherCode.FOG
        if (weatherText!!.endsWith("有雨")) weatherCode = WeatherCode.RAIN
        if (weatherText!!.endsWith("有雨雪")) weatherCode = WeatherCode.SLEET
        if (weatherText!!.endsWith("有大雪")) weatherCode = WeatherCode.SNOW
        if (weatherText!!.endsWith("有雪珠")) weatherCode = WeatherCode.SNOW
        if (weatherText!!.endsWith("有冰珠")) weatherCode = WeatherCode.SNOW
        if (weatherText!!.endsWith("有陣雨")) weatherCode = WeatherCode.RAIN
        if (weatherText!!.endsWith("陣雨雪")) weatherCode = WeatherCode.SLEET
        if (weatherText!!.endsWith("有雹")) weatherCode = WeatherCode.HAIL
        if (weatherText!!.endsWith("有雷雨")) weatherCode = WeatherCode.THUNDERSTORM
        if (weatherText!!.endsWith("有雷雪")) weatherCode = WeatherCode.SNOW
        if (weatherText!!.endsWith("有雷雹")) weatherCode = WeatherCode.HAIL
        if (weatherText!!.endsWith("大雷雨")) weatherCode = WeatherCode.THUNDERSTORM
        if (weatherText!!.endsWith("大雷雹")) weatherCode = WeatherCode.HAIL
        if (weatherText!!.endsWith("有雷")) weatherCode = WeatherCode.THUNDER

        // If there is no precipitation, thunder, or fog, we check for strong winds.
        // CWA's thresholds for "Strong Wind Advisory" are
        // sustained winds of Bft 6 (10.8m/s), or gusts Bft 8 (17.2m/s).
        if (weatherCode == null) {
            if (windSpeed != null && windSpeed!! >= 10.8) weatherCode = WeatherCode.WIND
            if (windGusts != null && windGusts!! >= 17.2) weatherCode = WeatherCode.WIND
        }

        // If there is no precipitation, thunder, fog, or wind,
        // we determine the code from cloud cover.
        if (weatherCode == null) {
            if (weatherText!!.startsWith("晴")) weatherCode = WeatherCode.CLEAR
            if (weatherText!!.startsWith("多雲")) weatherCode = WeatherCode.PARTLY_CLOUDY
            if (weatherText!!.startsWith("陰")) weatherCode = WeatherCode.CLOUDY
        }
    }

    wind = Wind(
        degree = windDegree,
        speed = windSpeed,
        gusts = windGusts
    )

    airQuality = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) getAirQuality(weatherResult, temperature?.temperature, pressure) else null

    return Current(
        weatherText = weatherText,
        weatherCode = weatherCode,
        temperature = temperature,
        wind = wind,
        airQuality = airQuality,
        relativeHumidity = relativeHumidity,
        pressure = pressure
    )
}

private fun getNormals(
    normalsResult: CwaNormalsResult
): Normals {
    return if (normalsResult.records != null) Normals(
        month = normalsResult.records.data.surfaceObs.location[0].stationObsStatistics.AirTemperature.monthly[0].Month.toInt(),
        daytimeTemperature = normalsResult.records.data.surfaceObs.location[0].stationObsStatistics.AirTemperature.monthly[0].Maximum.toDouble(),
        nighttimeTemperature = normalsResult.records.data.surfaceObs.location[0].stationObsStatistics.AirTemperature.monthly[0].Minimum.toDouble()
    ) else Normals()
}

// Concentrations of SO₂, NO₂, O₃ are given in ppb (and in ppm for CO).
// We need to convert these figures to µg/m³ (and mg/m³ for CO).
private fun getAirQuality(
    result: CwaWeatherResult,
    temperature: Double?,
    pressure: Double?
): AirQuality {
    return if (result.data != null) AirQuality(
        pM25 = result.data.aqi[0].pm2_5.toDoubleOrNull(),
        pM10 = result.data.aqi[0].pm10.toDoubleOrNull(),
        sO2 = ppbToUgm3("SO2", result.data.aqi[0].so2.toDoubleOrNull(), temperature, pressure),
        nO2 = ppbToUgm3("NO2", result.data.aqi[0].no2.toDoubleOrNull(), temperature, pressure),
        o3 = ppbToUgm3("O3", result.data.aqi[0].o3.toDoubleOrNull(), temperature, pressure),
        cO = ppbToUgm3("CO", result.data.aqi[0].co.toDoubleOrNull(), temperature, pressure)
    ) else AirQuality()
}

// Forecast data from the main weather API call are unsorted.
// We need to first store the numbers into maps, then sort the keys,
// and retrieve the relevant numbers using the sorted keys.
//
// "Half-day" forecasts are generated four times per day:
// at 05:00 for 06:00-18:00, 18:00-06:00, etc.
// at 11:00 for 12:00-18:00, 18:00-06:00, 06:00-18:00, etc.
// at 17:00 for 18:00-06:00, 06:00-18:00, etc.
// at 23:00 for 00:00-06:00, 06:00-18:00, 18:00-06:00, etc.
//
// These ranges are great for fitting into Breezy Weather's schema,
// but the start time for the first segments from the 11:00 and 23:00
// forecasts need to be "normalized" to match the rest of the half days.
private fun getDailyForecast(
    dailyResult: CwaWeatherForecast,
    sunResult: CwaAstroResult,
    moonResult: CwaAstroResult
): List<Daily> {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")

    val dailyList = mutableListOf<Daily>()
    val popMap = mutableMapOf<String, Double?>()
    val wsMap = mutableMapOf<String, Double?>()
    val atMap = mutableMapOf<String, Double?>()
    val wxMap = mutableMapOf<String, String?>()
    val wxIconMap = mutableMapOf<String, String?>()
    val tMap = mutableMapOf<String, Double?>()
    val uviMap = mutableMapOf<String, Double?>()
    val wdMap = mutableMapOf<String, Double?>()
    val srMap = mutableMapOf<String, String?>()
    val ssMap = mutableMapOf<String, String?>()
    val mrMap = mutableMapOf<String, String?>()
    val msMap = mutableMapOf<String, String?>()

    var timeKey: String
    var day = ""
    var dayKey: String
    var nightKey: String
    var dayPart: HalfDay
    var nightPart: HalfDay

    dailyResult.Wx!!.timePeriods.forEach {
        timeKey = normalizeHalfDay(it.startTime!!.replace('T', ' '))
        wxMap[timeKey] = it.weather!!
        wxIconMap[timeKey] = it.weatherIcon!!
    }
    dailyResult.MinT!!.timePeriods.forEach {
        timeKey = normalizeHalfDay(it.startTime!!.replace('T', ' '))
        if (timeKey.substring(11, 13) == "18") {
            tMap[timeKey] = it.temperature!!.toDoubleOrNull()
        }
    }
    dailyResult.MaxT!!.timePeriods.forEach {
        timeKey = normalizeHalfDay(it.startTime!!.replace('T', ' '))
        if (timeKey.substring(11, 13) == "06") {
            tMap[timeKey] = it.temperature!!.toDoubleOrNull()
        }
    }
    dailyResult.MinAT!!.timePeriods.forEach {
        timeKey = normalizeHalfDay(it.startTime!!.replace('T', ' '))
        if (timeKey.substring(11, 13) == "18") {
            atMap[timeKey] = it.apparentTemperature!!.toDoubleOrNull()
        }
    }
    dailyResult.MaxAT!!.timePeriods.forEach {
        timeKey = normalizeHalfDay(it.startTime!!.replace('T', ' '))
        if (timeKey.substring(11, 13) == "06") {
            atMap[timeKey] = it.apparentTemperature!!.toDoubleOrNull()
        }
    }
    dailyResult.WD!!.timePeriods.forEach {
        timeKey = normalizeHalfDay(it.startTime!!.replace('T', ' '))
        wdMap[timeKey] = getWindDirection(it.windDirectionDescription)
    }
    dailyResult.WS!!.timePeriods.forEach {
        timeKey = normalizeHalfDay(it.startTime!!.replace('T', ' '))
        wsMap[timeKey] = it.windSpeed!!.toDoubleOrNull()
    }
    dailyResult.PoP12h!!.timePeriods.forEach {
        timeKey = normalizeHalfDay(it.startTime!!.replace('T', ' '))
        popMap[timeKey] = it.probabilityOfPrecipitation!!.toDoubleOrNull()
    }
    dailyResult.UVI!!.timePeriods.forEach {
        timeKey = normalizeHalfDay(it.startTime!!.replace('T', ' '))
        uviMap[timeKey] = it.UVIndex!!.toDoubleOrNull()
    }

    sunResult.records.locations.location[0].time.forEach { time ->
        srMap[time.date] = time.sunRiseTime
        ssMap[time.date] = time.sunSetTime
    }
    moonResult.records.locations.location[0].time.forEach { time ->
        mrMap[time.date] = time.moonRiseTime
        msMap[time.date] = time.moonSetTime
    }

    val timeKeys = wxMap.keys.sorted()
    timeKeys.forEach {
        if (it.substring(0, 10) != day) {
            day = it.substring(0, 10)
            dayKey = "$day 06:00:00"
            nightKey = "$day 18:00:00"
            if (wxMap.containsKey(dayKey)) {
                dayPart = HalfDay(
                    weatherText = wxMap[dayKey],
                    weatherCode = getWeatherCode(wxIconMap[dayKey]),
                    temperature = Temperature(
                        temperature = tMap[dayKey],
                        apparentTemperature = atMap[dayKey]
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = popMap[dayKey]
                    ),
                    wind = Wind(
                        degree = wdMap[dayKey],
                        speed = wsMap[dayKey]
                    )
                )
            } else {
                dayPart = HalfDay()
            }
            if (wxMap.containsKey(nightKey)) {
                nightPart = HalfDay(
                    weatherText = wxMap[nightKey],
                    weatherCode = getWeatherCode(wxIconMap[nightKey]),
                    temperature = Temperature(
                        temperature = tMap[nightKey],
                        apparentTemperature = atMap[nightKey]
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = popMap[nightKey]
                    ),
                    wind = Wind(
                        degree = wdMap[nightKey],
                        speed = wsMap[nightKey]
                    )
                )
            } else {
                nightPart = HalfDay()
            }
            dailyList.add(
                Daily(
                    date = formatter.parse("$day 00:00:00")!!,
                    day = dayPart,
                    night = nightPart,
                    sun = Astro(
                        riseDate = if (!ssMap[day].isNullOrBlank()) formatter.parse(day + " " + srMap[day] + ":00") else null,
                        setDate = if (!ssMap[day].isNullOrBlank()) formatter.parse(day + " " + ssMap[day] + ":00") else null
                    ),
                    moon = Astro(
                        riseDate = if (!mrMap[day].isNullOrBlank()) formatter.parse(day + " " + mrMap[day] + ":00") else null,
                        setDate = if (!msMap[day].isNullOrBlank()) formatter.parse(day + " " + msMap[day] + ":00") else null
                    ),
                    uV = UV(
                        index = uviMap[dayKey]
                    )
                )
            )
        }
    }

    return dailyList
}

// Forecast data from the main weather API call are unsorted.
// We need to first store the numbers into maps, then sort the keys,
// and retrieve the relevant numbers using the sorted keys.
//
// CWA provides forecasts at 3-hour intervals, rather than hourly.
// Precipitation probability figures are at 6-hour intervals,
// so they are duplicated for the next "3-hourly" key.
private fun getHourlyForecast(
    hourlyResult: CwaWeatherForecast
): List<HourlyWrapper> {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")

    val hourlyList = mutableListOf<HourlyWrapper>()
    val wxMap = mutableMapOf<String, String?>()
    val wxIconMap = mutableMapOf<String, String?>()
    val atMap = mutableMapOf<String, Double?>()
    val tMap = mutableMapOf<String, Double?>()
    val rhMap = mutableMapOf<String, Double?>()
    val popMap = mutableMapOf<String, Double?>()
    val wsMap = mutableMapOf<String, Double?>()
    val wdMap = mutableMapOf<String, Double?>()
    val tdMap = mutableMapOf<String, Double?>()

    var timeKey: String
    var nextTimeKey: String
    var hour: Int

    hourlyResult.Wx!!.timePeriods.forEach {
        timeKey = it.startTime!!.replace('T', ' ')
        wxMap[timeKey] = it.weather!!
        wxIconMap[timeKey] = it.weatherIcon!!
    }
    hourlyResult.T!!.timePeriods.forEach {
        timeKey = it.dataTime!!.replace('T', ' ')
        tMap[timeKey] = it.temperature!!.toDoubleOrNull()
    }
    hourlyResult.AT!!.timePeriods.forEach {
        timeKey = it.dataTime!!.replace('T', ' ')
        atMap[timeKey] = it.apparentTemperature!!.toDoubleOrNull()
    }
    hourlyResult.Td!!.timePeriods.forEach {
        timeKey = it.dataTime!!.replace('T', ' ')
        tdMap[timeKey] = it.dewPointTemperature!!.toDoubleOrNull()
    }
    hourlyResult.RH!!.timePeriods.forEach {
        timeKey = it.dataTime!!.replace('T', ' ')
        rhMap[timeKey] = it.relativeHumidity!!.toDoubleOrNull()
    }
    hourlyResult.WD!!.timePeriods.forEach {
        timeKey = it.dataTime!!.replace('T', ' ')
        wdMap[timeKey] = getWindDirection(it.windDirectionDescription)
    }
    hourlyResult.WS!!.timePeriods.forEach {
        timeKey = it.dataTime!!.replace('T', ' ')
        wsMap[timeKey] = it.windSpeed!!.toDoubleOrNull()
    }
    hourlyResult.PoP6h!!.timePeriods.forEach {
        timeKey = it.startTime!!.replace('T', ' ')
        popMap[timeKey] = it.probabilityOfPrecipitation!!.toDoubleOrNull()
        hour = timeKey.substring(11, 13).toInt() + 3
        nextTimeKey = timeKey.substring(0, 11) + hour.toString().padStart(2, '0') + ":00:00"
        popMap[nextTimeKey] = it.probabilityOfPrecipitation!!.toDoubleOrNull()
    }

    val timeKeys = wxMap.keys.sorted()

    timeKeys.forEach {
        hour = it.substring(11, 13).toInt()
        hourlyList.add(
            HourlyWrapper(
                date = formatter.parse(it)!!,
                isDaylight = (hour >= 6) && (hour < 18),
                weatherText = wxMap[it],
                weatherCode = getWeatherCode(wxIconMap[it]),
                temperature = Temperature(
                    temperature = tMap[it],
                    apparentTemperature = atMap[it]
                ),
                precipitationProbability = PrecipitationProbability(
                    total = popMap[it]
                ),
                wind = Wind(
                    degree = wdMap[it],
                    speed = wsMap[it]
                ),
                relativeHumidity = rhMap[it],
                dewPoint = tdMap[it]
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
    id: String
): List<Alert> {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")
    val alertList = mutableListOf<Alert>()
    var headline: String
    var severity: AlertSeverity
    var alert: Alert
    var applicable: Boolean

    val county = location.parameters
        .getOrElse(id) { null }?.getOrElse("county") { null }
    val township = location.parameters
        .getOrElse(id) { null }?.getOrElse("township") { null }

    if (county.isNullOrEmpty() || township.isNullOrEmpty()) {
        throw InvalidLocationException()
    }

    val warningArea = CWA_TOWNSHIP_WARNING_AREAS.getOrDefault(township, "G")

    if (alertResult.records != null) {
        alertResult.records.record?.forEach { record ->
            applicable = false
            record.hazardConditions?.hazards?.hazard?.forEach { hazard ->
                hazard.info.affectedAreas.location.forEach { location ->
                    if (
                        (location.locationName == county)
                        || (location.locationName == county + "山區" && warningArea == "M")
                        || (location.locationName == "基隆北海岸" && warningArea == "K")
                        || (location.locationName == "恆春半島" && warningArea == "H")
                        || (location.locationName == "蘭嶼綠島" && warningArea == "L")
                    ) {
                        if (!applicable) { // so we don't cover up a more severe level with a less severe one
                            applicable = true
                            headline = hazard.info.phenomena + hazard.info.significance
                            severity = getAlertSeverity(headline)
                            alert = Alert(
                                alertId = headline + " " + record.datasetInfo.validTime.startTime,
                                startDate = formatter.parse(record.datasetInfo.validTime.startTime)!!,
                                endDate = formatter.parse(record.datasetInfo.validTime.endTime)!!,
                                headline = headline,
                                description = record.contents.content.contentText.trim(),
                                instruction = null,
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
    }
    return alertList
}

// When a half-day forecast only covers the last 6 hours of a standard half-day
// (06:00-18:00, 18:00-06:00), normalize the time so that it starts 6 hours sooner.
private fun normalizeHalfDay(timestamp: String): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")
    val year = timestamp.substring(0, 4).toInt()
    val month = timestamp.substring(5, 7).toInt()
    val day = timestamp.substring(8, 10).toInt()
    val hour = timestamp.substring(11, 13).toInt()
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"), Locale.ENGLISH)
    calendar.set(year, month-1, day, hour, 0, 0)
    if (hour == 0 || hour == 12) {
        calendar.add(Calendar.HOUR_OF_DAY, -6)
    }
    val result = formatter.format(calendar.time)
    return result
}

private fun getWindDirection(direction: String?): Double? {
    return if (direction == null) {
        null
    } else when (direction) {
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

// Weather icon source:
// https://opendata.cwa.gov.tw/opendatadoc/MFC/A0012-001.pdf
private fun getWeatherCode(icon: String?): WeatherCode? {
    return if (icon == null) {
        null
    } else when (icon) {
        "01", "02" -> WeatherCode.CLEAR
        "03", "04" -> WeatherCode.PARTLY_CLOUDY
        "05", "06", "07" -> WeatherCode.CLOUDY
        "08", "09", "10", "11", "12", "13", "14", "19", "20", "29", "30", "31", "32", "38", "39" -> WeatherCode.RAIN
        "15", "16", "21", "22", "33", "34", "35", "36" -> WeatherCode.THUNDER
        "17", "18", "41" -> WeatherCode.THUNDERSTORM
        "23", "37", "40" -> WeatherCode.SLEET
        "24", "25", "26", "27", "28" -> WeatherCode.FOG
        "42" -> WeatherCode.SNOW
        else -> null
    }
}

private fun getAlertSeverity(headline: String): AlertSeverity {
    return when (headline) {
        // missing severity levels for the following because we are not sure about wording in the API JSON yet
        // 低溫特報 (嚴寒, 非常寒冷, 寒冷), 高溫資訊 (紅燈, 橙燈, 黃燈)
        "超大豪雨特報" -> AlertSeverity.EXTREME
        "大豪雨特報", "海上陸上颱風警報", "陸上颱風警報", "海嘯警報" -> AlertSeverity.SEVERE
        "豪雨特報", "海上颱風警報", "海嘯警訊" -> AlertSeverity.MODERATE
        "熱帶性低氣壓特報", "大雨特報", "海嘯消息", "濃霧特報", "長浪即時訊息", "陸上強風特報", "海上強風特報" -> AlertSeverity.MINOR
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

// Concentrations of SO₂, NO₂, O₃ are given in ppb (and in ppm for CO).
// We need to convert these figures to µg/m³ (and mg/m³ for CO).
private fun ppbToUgm3(pollutant: String, ppb: Double?, temperature: Double?, pressure: Double?): Double? {
    if (ppb == null) return null
    val molecularMass: Double = when (pollutant) {
        "NO2" -> 46.0055
        "O3" -> 48.0
        "SO2" -> 64.066
        "CO" -> 28.01
        else -> return null
    }
    // Source: https://en.wikipedia.org/wiki/Useful_conversions_and_formulas_for_air_dispersion_modeling
    // Molar Gas Constant = 8.31446261815324 m³3⋅Pa⋅K⁻¹⋅mol⁻¹
    // assume 1013.25 hPa (1 atm) if air pressure is not given
    // assume 25°C if temperature is not given
    return ppb / (8.31446261815324 / (pressure ?: 1013.25) * 10) / (273.15 + (temperature ?: 25.0)) * molecularMass
}