package org.breezyweather.sources.smg

import android.content.Context
import android.graphics.Color
import breezyweather.domain.feature.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.smg.json.SmgAirQualityResult
import org.breezyweather.sources.smg.json.SmgAstroResult
import org.breezyweather.sources.smg.json.SmgBulletinResult
import org.breezyweather.sources.smg.json.SmgCurrentResult
import org.breezyweather.sources.smg.json.SmgForecastResult
import org.breezyweather.sources.smg.json.SmgUvResult
import org.breezyweather.sources.smg.json.SmgWarningResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun convert(
    context: Context,
    currentResult: SmgCurrentResult,
    dailyResult: SmgForecastResult,
    hourlyResult: SmgForecastResult,
    bulletinResult: SmgBulletinResult,
    uvResult: SmgUvResult,
    warningsResult: List<SmgWarningResult>,
    airQualityResult: SmgAirQualityResult,
    includeNormals: Boolean = true,
    astroResult: SmgAstroResult,
    failedFeatures: List<SourceFeature>,
): WeatherWrapper {
    return WeatherWrapper(
        current = getCurrent(currentResult, bulletinResult, uvResult, airQualityResult),
        dailyForecast = getDailyForecast(context, dailyResult, astroResult),
        hourlyForecast = getHourlyForecast(context, hourlyResult),
        alertList = getAlertList(context, warningsResult),
        normals = if (includeNormals) {
            getNormals()
        } else {
            null
        },
        failedFeatures = failedFeatures
    )
}

fun convertSecondary(
    context: Context,
    currentResult: SmgCurrentResult?,
    bulletinResult: SmgBulletinResult?,
    uvResult: SmgUvResult?,
    warningsResult: List<SmgWarningResult>?,
    airQualityResult: SmgAirQualityResult?,
    includeNormals: Boolean = true,
    failedFeatures: List<SourceFeature>,
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        current = if (currentResult != null && bulletinResult != null && uvResult != null) {
            getCurrent(currentResult, bulletinResult, uvResult)
        } else {
            null
        },
        airQuality = if (airQualityResult != null) {
            AirQualityWrapper(
                current = getAirQuality(airQualityResult)
            )
        } else {
            null
        },
        alertList = if (warningsResult != null) {
            getAlertList(context, warningsResult)
        } else {
            null
        },
        normals = if (includeNormals) {
            getNormals()
        } else {
            null
        },
        failedFeatures = failedFeatures
    )
}

private fun getCurrent(
    currentResult: SmgCurrentResult,
    bulletinResult: SmgBulletinResult,
    uvResult: SmgUvResult,
    airQualityResult: SmgAirQualityResult? = null,
): Current? {
    var current = Current()
    var stationName: String?
    currentResult.Weather?.Custom?.getOrNull(0)?.WeatherReport?.forEach { report ->
        // SMG has not released the coordinates of its various monitoring stations.
        // Therefore we default to "Taipa Grande" which is SMG's office location.
        // This is fine because Macao has a land area of just 32.9km²,
        // which is one-third the size of the forecast grid of most other countries (c. 100km²).
        // TODO: Once we have more intel on the coordinates of the stations, we can locate the nearest one.
        stationName = report.station?.getOrNull(0)?.stationname?.getOrNull(0)
        if (report.station?.getOrNull(0) !== null && stationName == "TAIPA GRANDE") {
            report.station[0].let {
                current = Current(
                    temperature = Temperature(
                        temperature = it.Temperature?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = it.WindDirection?.getOrNull(0)?.Degree?.getOrNull(0)?.toDoubleOrNull(),
                        speed = it.WindSpeed?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull()?.div(3.6),
                        gusts = it.WindGust?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull()?.div(3.6)
                    ),
                    uV = UV(
                        index = uvResult.UV?.Custom?.getOrNull(
                            0
                        )?.ActualUVBReport?.getOrNull(0)?.index?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull()
                    ),
                    airQuality = getAirQuality(airQualityResult),
                    relativeHumidity = it.Humidity?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull(),
                    dewPoint = it.DewPoint?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull(),
                    pressure = it.MeanSeaLevelPressure?.getOrNull(0)?.dValue?.getOrNull(0)?.toDoubleOrNull(),
                    dailyForecast = bulletinResult.Forecast?.Custom?.getOrNull(0)?.TodaySituation?.getOrNull(0)
                )
            }
        }
    }
    return current
}

private fun getDailyForecast(
    context: Context,
    dailyResult: SmgForecastResult,
    astroResult: SmgAstroResult,
): List<Daily> {
    val dailyList = mutableListOf<Daily>()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Macau")
    val dateTimeFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH)
    dateTimeFormatter.timeZone = TimeZone.getTimeZone("Asia/Macau")

    var maxTemp: Double?
    var minTemp: Double?
    var date: Date
    var sunriseMap = mutableMapOf<String, Date>()
    var sunsetMap = mutableMapOf<String, Date>()
    var moonriseMap = mutableMapOf<String, Date>()
    var moonsetMap = mutableMapOf<String, Date>()
    astroResult.let {
        if (it.displayDate?.en != null) {
            if (!it.sunrise.isNullOrBlank()) {
                date = dateTimeFormatter.parse("${it.displayDate.en} ${it.sunrise}")!!
                sunriseMap[formatter.format(date.time)] = date
            }
            if (!it.sunset.isNullOrBlank()) {
                date = dateTimeFormatter.parse("${it.displayDate.en} ${it.sunset}")!!
                sunsetMap[formatter.format(date.time)] = date
            }
            if (!it.moonrise.isNullOrBlank()) {
                date = dateTimeFormatter.parse("${it.displayDate.en} ${it.moonrise}")!!
                moonriseMap[formatter.format(date.time)] = date
            }
            if (!it.moonset.isNullOrBlank()) {
                date = dateTimeFormatter.parse("${it.displayDate.en} ${it.moonset}")!!
                moonsetMap[formatter.format(date.time)] = date
            }
        }
    }

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
                Daily(
                    date = formatter.parse(it.ValidFor[0])!!,
                    day = HalfDay(
                        weatherText = getWeatherText(context, it.dailyWeatherStatus?.getOrNull(0)),
                        weatherCode = getWeatherCode(it.dailyWeatherStatus?.getOrNull(0)),
                        temperature = Temperature(
                            temperature = maxTemp
                        )
                    ),
                    night = HalfDay(
                        weatherText = getWeatherText(context, it.dailyWeatherStatus?.getOrNull(0)),
                        weatherCode = getWeatherCode(it.dailyWeatherStatus?.getOrNull(0)),
                        temperature = Temperature(
                            temperature = minTemp
                        )
                    ),
                    sun = Astro(
                        riseDate = sunriseMap.getOrElse(it.ValidFor[0]) { null },
                        setDate = sunsetMap.getOrElse(it.ValidFor[0]) { null }
                    ),
                    moon = Astro(
                        riseDate = moonriseMap.getOrElse(it.ValidFor[0]) { null },
                        setDate = moonsetMap.getOrElse(it.ValidFor[0]) { null }
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
                    weatherText = getWeatherText(context, it.hourlyWeatherStatus?.getOrNull(0)?.Value?.getOrNull(0)),
                    weatherCode = getWeatherCode(it.hourlyWeatherStatus?.getOrNull(0)?.Value?.getOrNull(0)),
                    temperature = Temperature(
                        temperature = it.Temperature?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = it.Winddiv?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull(),
                        speed = it.Windspd?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull()?.div(3.6)
                    ),
                    relativeHumidity = it.Humidity?.getOrNull(0)?.Value?.getOrNull(0)?.toDoubleOrNull()
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
private fun getNormals(): Normals? {
    val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Macau"))
    val month = now.get(Calendar.MONTH) + 1
    return when (month) {
        1 -> Normals(1, 18.6, 12.7)
        2 -> Normals(2, 19.2, 13.9)
        3 -> Normals(3, 21.4, 16.5)
        4 -> Normals(4, 25.1, 20.3)
        5 -> Normals(5, 28.7, 23.7)
        6 -> Normals(6, 30.5, 25.7)
        7 -> Normals(7, 31.4, 26.1)
        8 -> Normals(8, 31.5, 25.9)
        9 -> Normals(9, 30.8, 25.1)
        10 -> Normals(10, 28.5, 22.7)
        11 -> Normals(11, 24.7, 18.8)
        12 -> Normals(12, 20.3, 14.3)
        else -> null
    }
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
    // "tghopolu" refers to Taipa Grande (SMG's office location).
    // TODO: Once we have more intel on the coordinates of the stations, we can locate the nearest one.
    return AirQuality(
        pM25 = airQualityResult?.tghopolu?.HE_PM2_5?.toDoubleOrNull(),
        pM10 = airQualityResult?.tghopolu?.HE_PM10?.toDoubleOrNull(),
        sO2 = airQualityResult?.tghopolu?.HE_SO2?.toDoubleOrNull(),
        nO2 = airQualityResult?.tghopolu?.HE_NO2?.toDoubleOrNull(),
        o3 = airQualityResult?.tghopolu?.HE_O3?.toDoubleOrNull(),
        cO = airQualityResult?.tghopolu?.HE_CO?.toDoubleOrNull()
    )
}
