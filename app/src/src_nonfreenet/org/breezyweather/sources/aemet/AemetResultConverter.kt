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

package org.breezyweather.sources.aemet

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.Month
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.sources.aemet.json.AemetCurrentResult
import org.breezyweather.sources.aemet.json.AemetDailyResult
import org.breezyweather.sources.aemet.json.AemetHourlyResult
import org.breezyweather.sources.aemet.json.AemetNormalsResult
import org.breezyweather.sources.aemet.json.AemetStationsResult
import org.breezyweather.sources.getWindDegree
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal fun convert(
    location: Location,
    stationList: List<AemetStationsResult>,
): String {
    var distance: Double
    var nearestDistance = Double.POSITIVE_INFINITY
    var nearestStation = ""
    var stationLatitude: Double?
    var stationLongitude: Double?

    stationList.forEach {
        if (it.latitud != null && it.longitud != null) {
            stationLatitude = getDecimalDegrees(it.latitud)
            stationLongitude = getDecimalDegrees(it.longitud)
            if (stationLatitude != null && stationLongitude != null) {
                distance = SphericalUtil.computeDistanceBetween(
                    LatLng(location.latitude, location.longitude),
                    LatLng(stationLatitude!!, stationLongitude!!)
                )
                if (distance < nearestDistance && it.indicativo != null) {
                    nearestDistance = distance
                    nearestStation = it.indicativo
                }
            }
        }
    }
    return nearestStation
}

private fun getDecimalDegrees(
    dms: String,
): Double? {
    if (!Regex("""^\d{6}[NESW]$""").matches(dms)) return null
    return (
        dms.substring(0, 2).toDouble() +
            dms.substring(2, 4).toDouble().div(60.0) +
            dms.substring(4, 6).toDouble().div(3600.0)
        ) *
        if (dms.substring(6, 7) == "S" || dms.substring(6, 7) == "W") {
            -1.0
        } else {
            1.0
        }
}

internal fun getCurrent(
    currentResult: List<AemetCurrentResult>,
): CurrentWrapper? {
    return currentResult.lastOrNull()?.let {
        CurrentWrapper(
            temperature = TemperatureWrapper(
                temperature = it.ta
            ),
            wind = Wind(
                degree = it.dv,
                speed = it.vv,
                gusts = it.vmax
            ),
            relativeHumidity = it.hr,
            dewPoint = it.tpr,
            pressure = it.pres,
            visibility = it.vis
        )
    }
}

internal fun getNormals(
    normalsResult: List<AemetNormalsResult>,
): Map<Month, Normals> {
    return normalsResult
        .filter { it.mes?.toIntOrNull() != null && it.mes.toInt() in 1..12 }
        .associate {
            Month.of(it.mes!!.toInt()) to Normals(
                daytimeTemperature = it.max?.toDoubleOrNull(),
                nighttimeTemperature = it.min?.toDoubleOrNull()
            )
        }
}

internal fun getDailyForecast(
    context: Context,
    location: Location,
    dailyResult: List<AemetDailyResult>,
): List<DailyWrapper> {
    val dailyList = mutableListOf<DailyWrapper>()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(location.timeZone)
    var date: String
    var time: Long
    val wxMap = mutableMapOf<Long, String?>()
    val ppMap = mutableMapOf<Long, Double?>()
    val maxTMap = mutableMapOf<Long, Double?>()
    val minTMap = mutableMapOf<Long, Double?>()
    val maxAtMap = mutableMapOf<Long, Double?>()
    val minAtMap = mutableMapOf<Long, Double?>()
    val wdMap = mutableMapOf<Long, Double?>()
    val wsMap = mutableMapOf<Long, Double?>()
    val wgMap = mutableMapOf<Long, Double?>()
    val uviMap = mutableMapOf<Long, Double?>()

    dailyResult.forEach { result ->
        result.prediccion?.dia?.forEach { day ->
            date = day.fecha.substringBefore("T")
            time = formatter.parse(date)!!.time
            day.probPrecipitacion?.forEach {
                if (it.periodo == null || it.periodo == "00-24") {
                    ppMap[time] = it.value
                }
            }
            day.estadoCielo?.forEach {
                if (it.periodo == null || it.periodo == "00-24") {
                    wxMap[time] = it.value
                }
            }
            day.viento?.forEach {
                if (it.periodo == null || it.periodo == "00-24") {
                    wdMap[time] = getWindDegree(it.direccion)
                    wsMap[time] = it.velocidad?.div(3.6)
                }
            }
            day.rachaMax?.forEach {
                if (it.periodo == null || it.periodo == "00-24") {
                    wgMap[time] = it.value?.toDoubleOrNull()?.div(3.6)
                }
            }
            maxTMap[time] = day.temperatura?.maxima
            minTMap[time] = day.temperatura?.minima
            maxAtMap[time] = day.sensTermica?.maxima
            minAtMap[time] = day.sensTermica?.minima
            uviMap[time] = day.uvMax
        }
    }

    wxMap.keys.sorted().forEach { key ->
        dailyList.add(
            DailyWrapper(
                date = Date(key),
                day = HalfDayWrapper(
                    weatherText = getWeatherText(context, wxMap.getOrElse(key) { null }),
                    weatherCode = getWeatherCode(wxMap.getOrElse(key) { null }),
                    temperature = TemperatureWrapper(
                        temperature = maxTMap.getOrElse(key) { null },
                        feelsLike = maxAtMap.getOrElse(key) { null }
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = ppMap.getOrElse(key) { null }
                    ),
                    wind = Wind(
                        degree = wdMap.getOrElse(key) { null },
                        speed = wsMap.getOrElse(key) { null },
                        gusts = wgMap.getOrElse(key) { null }
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = getWeatherText(context, wxMap.getOrElse(key) { null }),
                    weatherCode = getWeatherCode(wxMap.getOrElse(key) { null }),
                    temperature = TemperatureWrapper(
                        temperature = minTMap.getOrElse(key) { null },
                        feelsLike = minAtMap.getOrElse(key) { null }
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = ppMap.getOrElse(key) { null }
                    ),
                    wind = Wind(
                        degree = wdMap.getOrElse(key) { null },
                        speed = wsMap.getOrElse(key) { null },
                        gusts = wgMap.getOrElse(key) { null }
                    )
                ),
                uV = UV(
                    index = uviMap.getOrElse(key) { null }
                )
            )
        )
    }

    return dailyList
}

internal fun getHourlyForecast(
    context: Context,
    location: Location,
    hourlyResult: List<AemetHourlyResult>,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(location.timeZone)
    var date: String
    var time: Long
    val wxMap = mutableMapOf<Long, String?>()
    val prMap = mutableMapOf<Long, Double?>()
    val ppMap = mutableMapOf<Long, Double?>()
    val ptMap = mutableMapOf<Long, Double?>()
    val snMap = mutableMapOf<Long, Double?>()
    val psMap = mutableMapOf<Long, Double?>()
    val tMap = mutableMapOf<Long, Double?>()
    val atMap = mutableMapOf<Long, Double?>()
    val rhMap = mutableMapOf<Long, Double?>()
    val wdMap = mutableMapOf<Long, Double?>()
    val wsMap = mutableMapOf<Long, Double?>()
    val wgMap = mutableMapOf<Long, Double?>()

    hourlyResult.forEach { result ->
        result.prediccion?.dia?.forEach { day ->
            date = day.fecha.substringBefore("T")
            day.estadoCielo?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                wxMap[time] = it.value
            }
            day.precipitacion?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                prMap[time] = it.value?.toDoubleOrNull()
            }
            day.probPrecipitacion?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                ppMap[time] = it.value?.toDoubleOrNull()
            }
            day.probTormenta?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                ptMap[time] = it.value?.toDoubleOrNull()
            }
            day.nieve?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                snMap[time] = it.value?.toDoubleOrNull()
            }
            day.probNieve?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                psMap[time] = it.value?.toDoubleOrNull()
            }
            day.temperatura?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                tMap[time] = it.value?.toDoubleOrNull()
            }
            day.sensTermica?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                atMap[time] = it.value?.toDoubleOrNull()
            }
            day.humedadRelativa?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                rhMap[time] = it.value?.toDoubleOrNull()
            }
            day.vientoAndRachaMax?.forEach {
                time = formatter.parse("${date}T${it.periodo?.substring(0, 2)}")!!.time
                it.direccion?.first()?.let { direction ->
                    wdMap[time] = getWindDegree(direction)
                }
                it.velocidad?.first()?.let { speed ->
                    wsMap[time] = speed.toDoubleOrNull()?.div(3.6)
                }
                it.value?.let { gusts ->
                    wgMap[time] = gusts.toDoubleOrNull()?.div(3.6)
                }
            }
        }
    }

    // Precipitation probabilities are forecast once every 6 hours.
    // Fill in the gaps.
    var lastPp: Double? = null
    var lastPt: Double? = null
    var lastPs: Double? = null
    wxMap.keys.sorted().forEach { key ->
        if (ppMap.containsKey(key)) {
            lastPp = ppMap[key]
        } else {
            ppMap[key] = lastPp
        }
        if (ptMap.containsKey(key)) {
            lastPt = ptMap[key]
        } else {
            ptMap[key] = lastPt
        }
        if (psMap.containsKey(key)) {
            lastPs = psMap[key]
        } else {
            psMap[key] = lastPs
        }
    }

    wxMap.keys.sorted().forEach { key ->
        hourlyList.add(
            HourlyWrapper(
                date = Date(key),
                weatherText = getWeatherText(context, wxMap.getOrElse(key) { null }),
                weatherCode = getWeatherCode(wxMap.getOrElse(key) { null }),
                temperature = TemperatureWrapper(
                    temperature = tMap.getOrElse(key) { null },
                    feelsLike = atMap.getOrElse(key) { null }
                ),
                precipitation = Precipitation(
                    total = prMap.getOrElse(key) { null },
                    snow = snMap.getOrElse(key) { null }
                ),
                precipitationProbability = PrecipitationProbability(
                    total = ppMap.getOrElse(key) { null },
                    thunderstorm = ptMap.getOrElse(key) { null },
                    snow = psMap.getOrElse(key) { null }
                ),
                wind = Wind(
                    degree = wdMap.getOrElse(key) { null },
                    speed = wsMap.getOrElse(key) { null },
                    gusts = wgMap.getOrElse(key) { null }
                ),
                relativeHumidity = rhMap.getOrElse(key) { null }
            )
        )
    }
    return hourlyList
}

// Source: https://www.aemet.es/es/eltiempo/prediccion/espana/ayuda
private fun getWeatherText(
    context: Context,
    code: String?,
): String? {
    return code?.let {
        with(code) {
            when {
                startsWith("11") -> context.getString(R.string.common_weather_text_clear_sky)
                startsWith("12") -> context.getString(R.string.common_weather_text_mostly_clear)
                startsWith("13") -> context.getString(R.string.common_weather_text_partly_cloudy)
                startsWith("14") -> context.getString(R.string.common_weather_text_cloudy)
                startsWith("15") -> context.getString(R.string.common_weather_text_cloudy)
                startsWith("16") -> context.getString(R.string.common_weather_text_overcast)
                startsWith("17") -> context.getString(R.string.common_weather_text_mostly_clear)
                startsWith("2") -> context.getString(R.string.common_weather_text_rain)
                startsWith("3") -> context.getString(R.string.common_weather_text_snow)
                startsWith("4") -> context.getString(R.string.common_weather_text_rain_light)
                startsWith("5") -> context.getString(R.string.weather_kind_thunderstorm)
                startsWith("6") -> context.getString(R.string.weather_kind_thunderstorm)
                startsWith("7") -> context.getString(R.string.common_weather_text_snow_light)
                startsWith("81") -> context.getString(R.string.common_weather_text_fog)
                startsWith("82") -> context.getString(R.string.common_weather_text_mist)
                startsWith("83") -> context.getString(R.string.weather_kind_haze)
                else -> null
            }
        }
    }
}

private fun getWeatherCode(
    code: String?,
): WeatherCode? {
    return code?.let {
        with(code) {
            when {
                startsWith("11") -> WeatherCode.CLEAR
                startsWith("12") -> WeatherCode.CLEAR
                startsWith("13") -> WeatherCode.PARTLY_CLOUDY
                startsWith("14") -> WeatherCode.CLOUDY
                startsWith("15") -> WeatherCode.CLOUDY
                startsWith("16") -> WeatherCode.CLOUDY
                startsWith("17") -> WeatherCode.CLEAR
                startsWith("2") -> WeatherCode.RAIN
                startsWith("3") -> WeatherCode.SNOW
                startsWith("4") -> WeatherCode.RAIN
                startsWith("5") -> WeatherCode.THUNDERSTORM
                startsWith("6") -> WeatherCode.THUNDERSTORM
                startsWith("7") -> WeatherCode.SNOW
                startsWith("81") -> WeatherCode.FOG
                startsWith("82") -> WeatherCode.FOG
                startsWith("83") -> WeatherCode.HAZE
                else -> null
            }
        }
    }
}
