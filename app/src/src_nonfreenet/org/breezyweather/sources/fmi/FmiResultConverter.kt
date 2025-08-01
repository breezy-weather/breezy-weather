package org.breezyweather.sources.fmi

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.CLOUD_COVER_BKN
import org.breezyweather.common.basic.models.options.unit.CLOUD_COVER_FEW
import org.breezyweather.common.basic.models.options.unit.CLOUD_COVER_OVC
import org.breezyweather.common.basic.models.options.unit.CLOUD_COVER_SCT
import org.breezyweather.common.basic.models.options.unit.CLOUD_COVER_SKC
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.sources.common.xml.CapAlert
import org.breezyweather.sources.fmi.xml.FmiSimpleResult
import org.breezyweather.sources.fmi.xml.FmiStationsResult
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Objects
import java.util.TimeZone

fun convert(
    location: Location,
    members: List<FmiStationsResult.Member>,
): String? {
    val stationList = members.associate { member ->
        member.environmentalMonitoringFacility.let {
            val coords = it!!.representativePoint!!.point!!.pos!!.value!!.split(" ")
            it.identifier!!.value!! to LatLng(
                coords[0].toDouble(),
                coords[1].toDouble()
            )
        }
    }
    return LatLng(location.latitude, location.longitude).getNearestLocation(stationList, 50000.0)
}

fun getDailyForecast(
    location: Location,
    forecastResult: FmiSimpleResult,
): List<DailyWrapper>? {
    val dates = forecastResult.members?.filter {
        it.bsWfsElement?.parameterValue?.value != "NaN"
    }?.groupBy {
        it.bsWfsElement?.time?.value?.getIsoFormattedDate(location)
    }?.keys?.sortedBy { it }
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(location.timeZone)
    return dates?.map {
        DailyWrapper(
            date = formatter.parse(it!!)!!
        )
    }
}

fun getHourlyForecast(
    context: Context,
    forecastResult: FmiSimpleResult,
): List<HourlyWrapper>? {
    val hours = forecastResult.members?.groupBy { member ->
        member.bsWfsElement?.time?.value
    }?.keys?.sortedBy { it }

    return hours?.map { hour ->
        val members = forecastResult.members.filter {
            it.bsWfsElement?.time?.value == hour
        }
        HourlyWrapper(
            date = hour!!,
            weatherText = getForecastWeatherText(context, (extract(members, "WeatherSymbol3") ?: 0).toInt()),
            weatherCode = getForecastWeatherCode((extract(members, "WeatherSymbol3") ?: 0).toInt()),
            temperature = TemperatureWrapper(
                temperature = extract(members, "Temperature")
            ),
            precipitation = Precipitation(
                total = extract(members, "Precipitation1h")
            ),
            precipitationProbability = PrecipitationProbability(
                total = extract(members, "PoP"),
                thunderstorm = extract(members, "ProbabilityThunderstorm")
            ),
            wind = Wind(
                degree = extract(members, "WindDirection"),
                speed = extract(members, "WindSpeedMS"),
                gusts = extract(members, "HourlyMaximumGust")
            ),
            relativeHumidity = extract(members, "Humidity"),
            dewPoint = extract(members, "DewPoint"),
            pressure = extract(members, "Pressure"),
            cloudCover = extract(members, "TotalCloudCover")?.toInt()
        )
    }
}

fun getCurrent(
    context: Context,
    currentResult: FmiSimpleResult,
): CurrentWrapper? {
    return currentResult.members?.let {
        val cloudCover = extractLatest(it, "n_man")?.times(12.5)
        CurrentWrapper(
            weatherText = getCurrentWeatherText(context, (extractLatest(it, "wawa") ?: 0).toInt(), cloudCover),
            weatherCode = getCurrentWeatherCode((extractLatest(it, "wawa") ?: 0).toInt(), cloudCover),
            temperature = TemperatureWrapper(
                temperature = extractLatest(it, "t2m")
            ),
            wind = Wind(
                degree = extractLatest(it, "wd_10min"),
                speed = extractLatest(it, "ws_10min"),
                gusts = extractLatest(it, "wg_10min")
            ),
            uV = UV(),
            relativeHumidity = extractLatest(it, "rh"),
            dewPoint = extractLatest(it, "td"),
            pressure = extractLatest(it, "p_sea"),
            cloudCover = cloudCover?.toInt(),
            visibility = extractLatest(it, "vis"),
            ceiling = null
        )
    }
}

fun getAirQuality(
    airQualityResult: FmiSimpleResult,
): AirQualityWrapper {
    val hours = airQualityResult.members?.filter { member ->
        member.bsWfsElement?.time?.value != null
    }?.groupBy { member ->
        member.bsWfsElement?.time?.value
    }?.keys?.sortedBy { it }?.mapNotNull { it }

    return AirQualityWrapper(
        hourlyForecast = hours?.associateWith { hour ->
            val members = airQualityResult.members.filter {
                it.bsWfsElement?.time?.value == hour
            }
            AirQuality(
                pM25 = extract(members, "PM25Concentration"),
                pM10 = extract(members, "PM10Concentration"),
                sO2 = extract(members, "SO2Concentration"),
                nO2 = extract(members, "NO2Concentration"),
                o3 = extract(members, "O3Concentration"),
                cO = extract(members, "COConcentration")?.div(1000.0)
            )
        }
    )
}

fun getNormals(
    normalsResult: FmiSimpleResult,
): Map<Month, Normals> {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Etc/UTC")
    return Month.entries.associateWith { month ->
        val datePattern = Regex("""^\d{4}-${"%02d".format(month.value)}-01T00:00:00Z$""")
        val monthlyRecords = normalsResult.members?.filter {
            datePattern.matches(formatter.format(it.bsWfsElement?.time?.value!!))
        }
        if (!monthlyRecords.isNullOrEmpty()) {
            Normals(
                daytimeTemperature = extract(monthlyRecords, "TAMAXP1M"),
                nighttimeTemperature = extract(monthlyRecords, "TAMINP1M")
            )
        } else {
            Normals()
        }
    }
}

fun getAlertList(
    context: Context,
    location: Location,
    alerts: List<CapAlert>,
): List<Alert> {
    val regex = Regex("""^(Landskapet )?(.*)( Region)?$""")
    return alerts.mapNotNull { capAlert ->
        if (!capAlert.msgType?.value.equals("Cancel", ignoreCase = true)) {
            capAlert.getInfoForContext(context)?.let { info ->
                // Try matching by region name first. It's much faster than calling containsPoint().
                // First clean up region name, as Open-Meteo adds "Landskapet" or "Region" to some regions.
                // Then compare the region name against areas listed on the alert.
                // This check assumes the user did not change language between adding the location and
                // refreshing the location data. Otherwise, the region names won't match and it will
                // fall back to calling containsPoint().
                val regionName = regex.replace(location.admin1 ?: "", "$2").trim()
                val matchingRegion = (
                    info.areas?.any { area -> area.areaDesc?.value.equals(regionName, ignoreCase = true) } ?: false
                    ) || (
                    location.countryCode.equals("AX", ignoreCase = true) && info.areas?.any { area ->
                        arrayOf("Åland", "Ahvenanmaa").any { area.areaDesc?.value.equals(it, ignoreCase = true) }
                    } ?: false
                    )

                if (info.category?.value.equals("Met", ignoreCase = true) &&
                    !info.urgency?.value.equals("Past", ignoreCase = true) &&
                    (matchingRegion || info.containsPoint(LatLng(location.latitude, location.longitude)))
                ) {
                    val severity = when (info.severity?.value) {
                        "Extreme" -> AlertSeverity.EXTREME
                        "Severe" -> AlertSeverity.SEVERE
                        "Moderate" -> AlertSeverity.MODERATE
                        "Minor" -> AlertSeverity.MINOR
                        else -> AlertSeverity.UNKNOWN
                    }
                    val title = info.event?.value ?: info.headline?.value
                    val start = info.onset?.value ?: info.effective?.value ?: capAlert.sent?.value
                    Alert(
                        alertId = capAlert.identifier?.value ?: Objects.hash(title, severity, start).toString(),
                        startDate = start,
                        endDate = info.expires?.value,
                        headline = title,
                        description = info.description?.value,
                        instruction = info.instruction?.value,
                        source = info.senderName?.value,
                        severity = severity,
                        color = Alert.colorFromSeverity(severity)
                    )
                } else {
                    null
                }
            }
        } else {
            null
        }
    }
}

// Source for WeatherSymbol3 definitions:
// https://www.ilmatieteenlaitos.fi/latauspalvelun-pikaohje
private fun getForecastWeatherText(context: Context, symbol: Int): String? {
    return when (symbol) {
        1 -> context.getString(R.string.common_weather_text_clear_sky)
        2 -> context.getString(R.string.common_weather_text_partly_cloudy)
        3 -> context.getString(R.string.common_weather_text_cloudy)
        21 -> context.getString(R.string.common_weather_text_rain_showers_light)
        22 -> context.getString(R.string.common_weather_text_rain_showers)
        23 -> context.getString(R.string.common_weather_text_rain_showers_heavy)
        31 -> context.getString(R.string.common_weather_text_rain_light)
        32 -> context.getString(R.string.common_weather_text_rain)
        33 -> context.getString(R.string.common_weather_text_rain_heavy)
        41 -> context.getString(R.string.common_weather_text_snow_showers_light)
        42 -> context.getString(R.string.common_weather_text_snow_showers)
        43 -> context.getString(R.string.common_weather_text_snow_showers_heavy)
        51 -> context.getString(R.string.common_weather_text_snow_light)
        52 -> context.getString(R.string.common_weather_text_snow)
        53 -> context.getString(R.string.common_weather_text_snow_heavy)
        // originally: Ukkoskuuroja (Thundershower)
        61 -> context.getString(R.string.weather_kind_thunderstorm)
        // originally: Voimakkaita ukkoskuuroja (Heavy thundershower)
        62 -> context.getString(R.string.weather_kind_thunderstorm)
        // originally: Ukkosta (Thunder)
        63 -> context.getString(R.string.weather_kind_thunder)
        // originally: Voimakasta ukkosta (Heavy thunder -- what does that even mean?)
        64 -> context.getString(R.string.weather_kind_thunder)
        71 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers_light)
        72 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers)
        73 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers_heavy)
        81 -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
        82 -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        83 -> context.getString(R.string.common_weather_text_rain_snow_mixed_heavy)
        91 -> context.getString(R.string.common_weather_text_mist)
        92 -> context.getString(R.string.common_weather_text_fog)
        else -> null
    }
}

private fun getForecastWeatherCode(symbol: Int): WeatherCode? {
    return when (symbol) {
        1 -> WeatherCode.CLEAR
        2 -> WeatherCode.PARTLY_CLOUDY
        3 -> WeatherCode.CLOUDY
        21, 22, 23, 31, 32, 33 -> WeatherCode.RAIN
        41, 42, 43, 51, 52, 53 -> WeatherCode.SNOW
        61, 62 -> WeatherCode.THUNDERSTORM
        63, 64 -> WeatherCode.THUNDER
        71, 72, 73, 81, 82, 83 -> WeatherCode.SLEET
        91, 92 -> WeatherCode.FOG
        else -> null
    }
}

// Source for "WaWa" codes (Sääsymbolien selitykset säähavainnoissa):
// https://www.ilmatieteenlaitos.fi/latauspalvelun-pikaohje
private fun getCurrentWeatherText(context: Context, symbol: Int, cloudCover: Double?): String? {
    return when (symbol) {
        0 -> cloudCover?.let {
            when {
                it < 0.0 -> null
                it < CLOUD_COVER_SKC -> context.getString(R.string.common_weather_text_clear_sky)
                it < CLOUD_COVER_FEW -> context.getString(R.string.common_weather_text_mostly_clear)
                it < CLOUD_COVER_SCT -> context.getString(R.string.common_weather_text_partly_cloudy)
                it < CLOUD_COVER_BKN -> context.getString(R.string.common_weather_text_mostly_cloudy)
                it < CLOUD_COVER_OVC -> context.getString(R.string.common_weather_text_cloudy)
                it == CLOUD_COVER_OVC -> context.getString(R.string.common_weather_text_overcast)
                else -> null
            }
        }
        4, 5 -> context.getString(R.string.weather_kind_haze)
        10 -> context.getString(R.string.common_weather_text_mist)
        20, 30, 31, 32, 33, 34 -> context.getString(R.string.common_weather_text_fog)
        21, 23, 40, 41, 60 -> context.getString(R.string.common_weather_text_rain)
        22, 50 -> context.getString(R.string.common_weather_text_drizzle)
        24, 70 -> context.getString(R.string.common_weather_text_snow)
        25 -> context.getString(R.string.common_weather_text_rain_freezing)
        61 -> context.getString(R.string.common_weather_text_rain_light)
        42, 63 -> context.getString(R.string.common_weather_text_rain_heavy)
        51 -> context.getString(R.string.common_weather_text_drizzle_light)
        52 -> context.getString(R.string.common_weather_text_drizzle_moderate)
        53 -> context.getString(R.string.common_weather_text_drizzle_heavy)
        54 -> context.getString(R.string.common_weather_text_drizzle_freezing_light)
        55 -> context.getString(R.string.common_weather_text_drizzle_freezing)
        56 -> context.getString(R.string.common_weather_text_drizzle_freezing_heavy)
        62 -> context.getString(R.string.common_weather_text_rain_moderate)
        64 -> context.getString(R.string.common_weather_text_rain_freezing_light)
        65 -> context.getString(R.string.common_weather_text_rain_freezing)
        66 -> context.getString(R.string.common_weather_text_rain_freezing_heavy)
        67 -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
        68 -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        71 -> context.getString(R.string.common_weather_text_snow_light)
        72 -> context.getString(R.string.common_weather_text_snow_moderate)
        73 -> context.getString(R.string.common_weather_text_snow_heavy)
        74 -> context.getString(R.string.fmi_weather_text_ice_pellets_light)
        75 -> context.getString(R.string.fmi_weather_text_ice_pellets_moderate)
        76 -> context.getString(R.string.fmi_weather_text_ice_pellets_heavy)
        77 -> context.getString(R.string.common_weather_text_snow_grains)
        78 -> context.getString(R.string.fmi_weather_text_ice_crystals)
        80 -> context.getString(R.string.common_weather_text_rain_showers)
        81 -> context.getString(R.string.common_weather_text_rain_showers_light)
        82 -> context.getString(R.string.common_weather_text_rain_showers_moderate)
        83, 84 -> context.getString(R.string.common_weather_text_rain_showers_heavy)
        85 -> context.getString(R.string.common_weather_text_snow_showers_light)
        86 -> context.getString(R.string.common_weather_text_snow_showers)
        87 -> context.getString(R.string.common_weather_text_snow_showers_heavy)
        89 -> context.getString(R.string.weather_kind_hail)
        else -> null
    }
}

private fun getCurrentWeatherCode(symbol: Int, cloudCover: Double?): WeatherCode? {
    return when (symbol) {
        0 -> cloudCover?.let {
            when {
                it < 0.0 -> null
                it < CLOUD_COVER_FEW -> WeatherCode.CLEAR
                it < CLOUD_COVER_SCT -> WeatherCode.PARTLY_CLOUDY
                it <= CLOUD_COVER_OVC -> WeatherCode.CLOUDY
                else -> null
            }
        }
        4, 5 -> WeatherCode.HAZE
        10, 20, 30, 31, 32, 33, 34 -> WeatherCode.FOG
        21, 22, 23, 40, 41, 42, 50, 51, 52, 53, 60, 61, 62, 63, 80, 81, 82, 83, 84 -> WeatherCode.RAIN
        24, 70, 71, 72, 73, 74, 75, 76, 77, 85, 86, 87 -> WeatherCode.SNOW
        25, 54, 55, 56, 64, 65, 66, 67, 68 -> WeatherCode.SLEET
        78 -> WeatherCode.FOG // ice crystals
        89 -> WeatherCode.HAIL
        else -> null
    }
}

private fun extract(
    members: List<FmiSimpleResult.Member>,
    parameter: String,
): Double? {
    return members.firstOrNull {
        it.bsWfsElement?.parameterName?.value == parameter
    }?.bsWfsElement?.parameterValue?.value?.toDoubleOrNull()
}

// used only by getCurrent()
// which will only have access to observation data points in the last hour
// so should not check for data points that are no longer current
private fun extractLatest(
    members: List<FmiSimpleResult.Member>,
    parameter: String,
): Double? {
    val timestamp = members.filter {
        it.bsWfsElement?.parameterName?.value == parameter
    }.map {
        it.bsWfsElement?.time?.value
    }.sortedBy { it }.last()

    return members.first {
        it.bsWfsElement?.time?.value == timestamp &&
            it.bsWfsElement?.parameterName?.value == parameter
    }.bsWfsElement?.parameterValue?.value?.toDoubleOrNull()
}
