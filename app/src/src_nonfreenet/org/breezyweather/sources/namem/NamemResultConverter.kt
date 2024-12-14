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

package org.breezyweather.sources.namem

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.sources.namem.json.NamemAirQualityResult
import org.breezyweather.sources.namem.json.NamemCurrentResult
import org.breezyweather.sources.namem.json.NamemDailyResult
import org.breezyweather.sources.namem.json.NamemHourlyResult
import org.breezyweather.sources.namem.json.NamemNormalsResult
import org.breezyweather.sources.namem.json.NamemStation
import java.util.Calendar
import java.util.Date

// Reverse geocoding
internal fun convert(
    location: Location,
    stations: List<NamemStation>?,
): List<Location> {
    val locationList = mutableListOf<Location>()
    val station = getNearestStation(location, stations, 200000.0)

    if (station == null || station.lat == null || station.lon == null) {
        throw InvalidLocationException()
    }
    locationList.add(
        location.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            timeZone = when (station.provinceName) {
                "Баян-Өлгий", // Bayan-Ölgii
                "Говь-Алтай", // Govi-Altai
                "Ховд", // Khovd
                "Увс", // Uvs
                "Завхан", // Zavkhan
                -> "Asia/Hovd"
                else -> "Asia/Ulaanbaatar"
            },
            country = "Монгол",
            countryCode = "MN",
            admin1 = station.provinceName,
            admin2 = station.districtName,
            city = station.districtName ?: "",
            district = if (station.stationName != station.districtName) {
                station.stationName
            } else {
                null
            }
        )
    )
    return locationList
}

// Location parameters
internal fun getLocationParameters(
    location: Location,
    stations: List<NamemStation>?,
): Map<String, String> {
    val weatherStation = getNearestStation(location, stations, 200000.0)
    if (weatherStation?.sid == null) {
        throw InvalidLocationException()
    }
    return mapOf(
        "stationId" to weatherStation.sid.toString()
    )
}

private fun getNearestStation(
    location: Location,
    stations: List<NamemStation>?,
    limit: Double? = null,
): NamemStation? {
    var distance: Double
    var nearestDistance: Double = Double.POSITIVE_INFINITY
    var nearestStation: NamemStation? = null
    stations?.forEach {
        if (it.lat != null && it.lon != null && (it.sid != null || it.id != null)) {
            distance = SphericalUtil.computeDistanceBetween(
                LatLng(location.latitude, location.longitude),
                LatLng(it.lat, it.lon)
            )
            if (distance < nearestDistance) {
                if (limit == null || distance <= limit) {
                    nearestDistance = distance
                    nearestStation = it
                }
            }
        }
    }
    return nearestStation
}

internal fun getCurrent(
    context: Context,
    currentResult: NamemCurrentResult,
): CurrentWrapper {
    val current = currentResult.aws?.getOrNull(0)
    return CurrentWrapper(
        weatherText = getWeatherText(context, current?.nh),
        weatherCode = getWeatherCode(current?.nh),
        temperature = Temperature(
            temperature = current?.ttt,
            apparentTemperature = current?.tttFeels
        ),
        wind = Wind(
            degree = current?.windDir,
            speed = current?.windSpeed
        ),
        relativeHumidity = current?.ff,
        pressure = current?.pslp
    )
}

internal fun getAirQuality(
    location: Location,
    airQualityResult: NamemAirQualityResult,
): AirQuality {
    val station = getNearestStation(location, airQualityResult.data, 50000.0)
    var pM25: Double? = null
    var pM10: Double? = null
    var sO2: Double? = null
    var nO2: Double? = null
    var o3: Double? = null
    var cO: Double? = null
    station?.elementList?.forEach {
        if (it.unit == "АЧИ") {
            when (it.id) {
                "pm25" -> pM25 = convertAqi(PollutantIndex.PM25, it.current)
                "pm10" -> pM10 = convertAqi(PollutantIndex.PM10, it.current)
                "so2" -> sO2 = convertAqi(PollutantIndex.SO2, it.current)
                "no2" -> nO2 = convertAqi(PollutantIndex.NO2, it.current)
                "o3" -> o3 = convertAqi(PollutantIndex.O3, it.current)
                "co" -> cO = convertAqi(PollutantIndex.CO, it.current)
            }
        }
    }
    return AirQuality(
        pM25 = pM25,
        pM10 = pM10,
        sO2 = sO2,
        nO2 = nO2,
        o3 = o3,
        cO = cO
    )
}

internal fun getNormals(
    normalsResult: NamemNormalsResult,
): Normals {
    var index = 0
    val now = Calendar.getInstance().timeInMillis
    normalsResult.foreMonthly?.forEachIndexed { i, normals ->
        if (normals.obsDate != null) {
            if (normals.obsDate.time < now) {
                index = i
            }
        }
    }
    return Normals(
        month = Calendar.getInstance().get(Calendar.MONTH) + 1,
        daytimeTemperature = normalsResult.foreMonthly?.getOrNull(index)?.ttMaxAve,
        nighttimeTemperature = normalsResult.foreMonthly?.getOrNull(index)?.ttMinAve
    )
}

internal fun getDailyForecast(
    context: Context,
    dailyResult: NamemDailyResult,
): List<DailyWrapper> {
    val dailyList = mutableListOf<DailyWrapper>()
    var date: Date
    dailyResult.fore5Day?.forEachIndexed { i, forecast ->
        // account for western provinces which are 1 hour behind
        date = Date(forecast.foreDate!!.time.plus(3600000))
        if (i == 0) {
            dailyList.add(
                DailyWrapper(
                    date = Date(date.time.minus(86400000)),
                    night = HalfDay(
                        weatherText = getWeatherText(context, forecast.wwN),
                        weatherCode = getWeatherCode(forecast.wwN),
                        temperature = Temperature(
                            temperature = forecast.temN,
                            apparentTemperature = forecast.temNFeel
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = forecast.wwNPer
                        ),
                        wind = Wind(
                            speed = forecast.wndN
                        )
                    )
                )
            )
        }
        dailyList.add(
            DailyWrapper(
                date = date,
                day = HalfDay(
                    weatherText = getWeatherText(context, forecast.wwD),
                    weatherCode = getWeatherCode(forecast.wwD),
                    temperature = Temperature(
                        temperature = forecast.temD,
                        apparentTemperature = forecast.temDFeel
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = forecast.wwDPer
                    ),
                    wind = Wind(
                        speed = forecast.wndD
                    )
                ),
                night = dailyResult.fore5Day.getOrNull(i + 1)?.let {
                    HalfDay(
                        weatherText = getWeatherText(context, it.wwN),
                        weatherCode = getWeatherCode(it.wwN),
                        temperature = Temperature(
                            temperature = it.temN,
                            apparentTemperature = it.temNFeel
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = it.wwNPer
                        ),
                        wind = Wind(
                            speed = it.wndN
                        )
                    )
                }
            )
        )
    }
    return dailyList
}

internal fun getHourlyForecast(
    hourlyResult: NamemHourlyResult,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    hourlyResult.fore3Hours?.forEach {
        if (it.fdate !== null) {
            hourlyList.add(
                HourlyWrapper(
                    date = it.fdate,
                    temperature = Temperature(
                        temperature = it.tem?.toDoubleOrNull()
                    ),
                    precipitation = Precipitation(
                        total = it.pre?.toDoubleOrNull()
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = it.preProb?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        speed = it.wnd?.toDoubleOrNull()
                    )
                )
            )
        }
    }
    return hourlyList
}

private fun getWeatherText(
    context: Context,
    weather: Int?,
): String? {
    // TODO: Check with a Mongolian speaker (???) for correct interpretation of the text
    return when (weather) {
        2 -> context.getString(R.string.common_weather_text_clear_sky) // "Цэлмэг"
        3 -> context.getString(R.string.common_weather_text_overcast) // "Үүлэрхэг"
        5, 7 -> context.getString(R.string.common_weather_text_mainly_clear) // "Багавтар үүлтэй"
        9, 10 -> context.getString(R.string.common_weather_text_cloudy) // "Үүлшинэ"
        20 -> context.getString(R.string.common_weather_text_mainly_clear) // "Үүл багасна"
        23, 24 -> context.getString(R.string.common_weather_text_snow_light) // "Ялимгүй цас"
        27, 28 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers_light) // "Ялимгүй хур тунадас"
        60 -> context.getString(R.string.common_weather_text_rain_light) // "Бага зэргийн бороо"
        61 -> context.getString(R.string.common_weather_text_rain) // "Бороо"
        63 -> context.getString(R.string.common_weather_text_rain_heavy) // "Их бороо"
        65 -> context.getString(R.string.common_weather_text_rain_snow_mixed) // "Хур тунадас"
        66 -> context.getString(R.string.common_weather_text_rain_snow_mixed_heavy) // "Их хур тунадас"
        67 -> context.getString(R.string.common_weather_text_rain_snow_mixed_heavy) // "Аадар их хур тунадас"
        68 -> context.getString(R.string.common_weather_text_rain_heavy) // "Их усархаг бороо"
        71 -> context.getString(R.string.common_weather_text_snow) // "Цас"
        73 -> context.getString(R.string.common_weather_text_snow_heavy) // "Их цас"
        75 -> context.getString(R.string.common_weather_text_snow_heavy) // "Аадар их цас"
        80, 81 -> context.getString(R.string.common_weather_text_rain_showers_light) // "Бага зэргийн аадар"
        82, 83 -> context.getString(R.string.common_weather_text_rain_showers) // "Аадар бороо"
        84, 85 -> context.getString(R.string.common_weather_text_rain_showers_heavy) // "Усархаг аадар бороо"
        90, 91 -> context.getString(R.string.weather_kind_thunderstorm) // "Дуу.Цах бага зэргийн аадар бороо"
        92, 93 -> context.getString(R.string.weather_kind_thunderstorm) // "Дуу.Цах аадар бороо"
        94, 95 -> context.getString(R.string.weather_kind_thunderstorm) // "Дуу.Цах усархаг аадар бороо"
        else -> null
    }
}

private fun getWeatherCode(
    weather: Int?,
): WeatherCode? {
    return when (weather) {
        2 -> WeatherCode.CLEAR // "Цэлмэг"
        3 -> WeatherCode.CLOUDY // "Үүлэрхэг"
        5, 7 -> WeatherCode.PARTLY_CLOUDY // "Багавтар үүлтэй"
        9, 10 -> WeatherCode.CLOUDY // "Үүлшинэ"
        20 -> WeatherCode.PARTLY_CLOUDY // "Үүл багасна"
        23, 24 -> WeatherCode.SNOW // "Ялимгүй цас"
        27, 28 -> WeatherCode.SLEET // "Ялимгүй хур тунадас"
        60 -> WeatherCode.RAIN // "Бага зэргийн бороо"
        61 -> WeatherCode.RAIN // "Бороо"
        63 -> WeatherCode.RAIN // "Их бороо"
        65 -> WeatherCode.SLEET // "Хур тунадас"
        66 -> WeatherCode.SLEET // "Их хур тунадас"
        67 -> WeatherCode.SLEET // "Аадар их хур тунадас"
        68 -> WeatherCode.RAIN // "Их усархаг бороо"
        71 -> WeatherCode.SNOW // "Цас"
        73 -> WeatherCode.SNOW // "Их цас"
        75 -> WeatherCode.SNOW // "Аадар их цас"
        80, 81 -> WeatherCode.RAIN // "Бага зэргийн аадар"
        82, 83 -> WeatherCode.RAIN // "Аадар бороо"
        84, 85 -> WeatherCode.RAIN // "Усархаг аадар бороо"
        90, 91 -> WeatherCode.THUNDERSTORM // "Дуу.Цах бага зэргийн аадар бороо"
        92, 93 -> WeatherCode.THUNDERSTORM // "Дуу.Цах аадар бороо"
        94, 95 -> WeatherCode.THUNDERSTORM // "Дуу.Цах усархаг аадар бороо"
        else -> null
    }
}

// Convert Mongolian AQI to pollutant concentration
// SO2, NO2, PM10, PM2.5, O3 in µg/m³
// CO in mg/m³
//
// Breakpoint source: http://agaar.mn/article-view/692
private fun convertAqi(
    pollutant: PollutantIndex,
    aqi: Double?,
): Double? {
    if (aqi == null || aqi < 0.0) return null
    if (aqi == 0.0) return 0.0

    val thresholds = listOf(0.0, 50.0, 100.0, 200.0, 300.0, 400.0, 500.0)
    val breakpoints = mapOf<PollutantIndex, List<Double>>(
        PollutantIndex.SO2 to listOf(0.0, 100.0, 300.0, 800.0, 1600.0, 2100.0, 2620.0),
        PollutantIndex.NO2 to listOf(0.0, 100.0, 200.0, 700.0, 2000.0, 3500.0, 3840.0),
        PollutantIndex.PM10 to listOf(0.0, 50.0, 100.0, 300.0, 420.0, 500.0, 600.0),
        PollutantIndex.PM25 to listOf(0.0, 35.0, 50.0, 150.0, 250.0, 350.0, 500.0),
        PollutantIndex.CO to listOf(0.0, 10.0, 30.0, 60.0, 90.0, 120.0, 150.0),
        PollutantIndex.O3 to listOf(0.0, 50.0, 100.0, 265.0, 800.0, 1000.0, 1200.0)
    )
    // AQI less than 500
    for (i in 1..thresholds.size - 1) {
        if (aqi > thresholds[i - 1] && aqi <= thresholds[i]) {
            return (aqi - thresholds[i - 1]) /
                (thresholds[i] - thresholds[i - 1]) *
                (breakpoints[pollutant]!![i] - breakpoints[pollutant]!![i - 1]) +
                (breakpoints[pollutant]!![i - 1])
        }
    }
    // AQI more than 500
    // linear extrapolation from the 400-500 range
    return (aqi - thresholds[5]) /
        (thresholds[6] - thresholds[5]) *
        (breakpoints[pollutant]!![6] - breakpoints[pollutant]!![5]) +
        (breakpoints[pollutant]!![5])
}
