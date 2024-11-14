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

package org.breezyweather.sources.mgm

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.sources.mgm.json.MgmAlertResult
import org.breezyweather.sources.mgm.json.MgmCurrentResult
import org.breezyweather.sources.mgm.json.MgmDailyForecastResult
import org.breezyweather.sources.mgm.json.MgmHourlyForecast
import org.breezyweather.sources.mgm.json.MgmHourlyForecastResult
import org.breezyweather.sources.mgm.json.MgmLocationResult
import org.breezyweather.sources.mgm.json.MgmNormalsResult
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun convert(
    location: Location,
    result: MgmLocationResult,
): Location {
    // Make sure location is within 50km of a known location in Türkiye
    val distance = SphericalUtil.computeDistanceBetween(
        LatLng(location.latitude, location.longitude),
        LatLng(result.latitude, result.longitude)
    )
    if (distance > 50000) {
        throw InvalidLocationException()
    }
    return location.copy(
        latitude = location.latitude,
        longitude = location.longitude,
        timeZone = "Europe/Istanbul",
        country = "Türkiye",
        countryCode = "TR",
        admin1 = result.province,
        city = result.province,
        district = result.district
    )
}

fun convert(
    context: Context,
    townCode: Int,
    currentResult: MgmCurrentResult?,
    dailyResult: MgmDailyForecastResult?,
    hourlyForecastResult: MgmHourlyForecastResult?,
    todayAlertResult: List<MgmAlertResult>?,
    tomorrowAlertResult: List<MgmAlertResult>?,
    normalsResult: MgmNormalsResult?,
): WeatherWrapper {
    return WeatherWrapper(
        current = getCurrent(context, currentResult),
        dailyForecast = getDailyForecast(context, dailyResult),
        hourlyForecast = getHourlyForecast(context, hourlyForecastResult?.forecast),
        alertList = getAlertList(townCode, todayAlertResult, tomorrowAlertResult),
        normals = getNormals(normalsResult)
    )
}

fun convertSecondary(
    context: Context,
    townCode: Int,
    currentResult: MgmCurrentResult?,
    todayAlertResult: List<MgmAlertResult>?,
    tomorrowAlertResult: List<MgmAlertResult>?,
    normalsResult: MgmNormalsResult?,
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        current = if (currentResult !== null) {
            getCurrent(context, currentResult)
        } else {
            null
        },
        alertList = if (todayAlertResult !== null && tomorrowAlertResult !== null) {
            getAlertList(townCode, todayAlertResult, tomorrowAlertResult)
        } else {
            null
        },
        normals = if (normalsResult !== null) {
            getNormals(normalsResult)
        } else {
            null
        }
    )
}

private fun getCurrent(
    context: Context,
    currentResult: MgmCurrentResult?,
): Current {
    return Current(
        weatherText = getWeatherText(context, currentResult?.condition),
        weatherCode = getWeatherCode(currentResult?.condition),
        temperature = Temperature(
            temperature = getValid(currentResult?.temperature)
        ),
        wind = Wind(
            degree = getValid(currentResult?.windDirection),
            speed = getValid(currentResult?.windSpeed)?.div(3.6)
        ),
        relativeHumidity = getValid(currentResult?.humidity),
        pressure = getValid(currentResult?.pressure)
    )
}

private fun getDailyForecast(
    context: Context,
    dailyForecast: MgmDailyForecastResult?,
): List<Daily> {
    val dailyList = mutableListOf<Daily>()
    dailyForecast?.let {
        dailyList.add(
            getDaily(
                context,
                it.dateDay1,
                it.conditionDay1,
                it.maxTempDay1,
                it.minTempDay1,
                it.windDirectionDay1,
                it.windSpeedDay1
            )
        )
        dailyList.add(
            getDaily(
                context,
                it.dateDay2,
                it.conditionDay2,
                it.maxTempDay2,
                it.minTempDay2,
                it.windDirectionDay2,
                it.windSpeedDay2
            )
        )
        dailyList.add(
            getDaily(
                context,
                it.dateDay3,
                it.conditionDay3,
                it.maxTempDay3,
                it.minTempDay3,
                it.windDirectionDay3,
                it.windSpeedDay3
            )
        )
        dailyList.add(
            getDaily(
                context,
                it.dateDay4,
                it.conditionDay4,
                it.maxTempDay4,
                it.minTempDay4,
                it.windDirectionDay4,
                it.windSpeedDay4
            )
        )
        dailyList.add(
            getDaily(
                context,
                it.dateDay5,
                it.conditionDay5,
                it.maxTempDay5,
                it.minTempDay5,
                it.windDirectionDay5,
                it.windSpeedDay5
            )
        )
    }
    return dailyList
}

private fun getHourlyForecast(
    context: Context,
    hourlyForecast: List<MgmHourlyForecast>?,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    // The 'Z' in the timestamp is misused. It is actually in Europe/Istanbul rather than Etc/UTC
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
    hourlyForecast?.forEach {
        hourlyList.add(
            HourlyWrapper(
                date = formatter.parse(it.time)!!,
                weatherText = getWeatherText(context, it.condition),
                weatherCode = getWeatherCode(it.condition),
                temperature = Temperature(
                    temperature = it.temperature
                ),
                wind = Wind(
                    degree = it.windDirection,
                    speed = it.windSpeed?.div(3.6),
                    gusts = it.gust?.div(3.6)
                ),
                relativeHumidity = it.humidity
            )
        )
    }
    return hourlyList
}

private fun getAlertList(
    townCode: Int,
    todayAlertResult: List<MgmAlertResult>?,
    tomorrowAlertResult: List<MgmAlertResult>?,
): List<Alert> {
    val alertList = mutableListOf<Alert>()
    val source = "Meteoroloji Genel Müdürlüğü"
    listOf(todayAlertResult, tomorrowAlertResult).forEach { alerts ->
        alerts?.forEach {
            if (it.towns?.red?.contains(townCode)!!) {
                alertList.add(
                    Alert(
                        alertId = it.id,
                        startDate = it.begin,
                        endDate = it.end,
                        headline = getAlertHeadline(it.weather?.red),
                        description = it.text?.red,
                        source = source,
                        severity = AlertSeverity.EXTREME,
                        color = getAlertColor(AlertSeverity.EXTREME)
                    )
                )
            } else if (it.towns.orange?.contains(townCode)!!) {
                alertList.add(
                    Alert(
                        alertId = it.id,
                        startDate = it.begin,
                        endDate = it.end,
                        headline = getAlertHeadline(it.weather?.orange),
                        description = it.text?.orange,
                        source = source,
                        severity = AlertSeverity.SEVERE,
                        color = getAlertColor(AlertSeverity.SEVERE)
                    )
                )
            } else if (it.towns.yellow?.contains(townCode)!!) {
                alertList.add(
                    Alert(
                        alertId = it.id,
                        startDate = it.begin,
                        endDate = it.end,
                        headline = getAlertHeadline(it.weather?.yellow),
                        description = it.text?.yellow,
                        source = source,
                        severity = AlertSeverity.MODERATE,
                        color = getAlertColor(AlertSeverity.MODERATE)
                    )
                )
            }
        }
    }
    return alertList
}

private fun getNormals(
    normalsResult: MgmNormalsResult?,
): Normals {
    return Normals(
        month = normalsResult?.month,
        daytimeTemperature = normalsResult?.meanMax,
        nighttimeTemperature = normalsResult?.meanMin
    )
}

private fun getDaily(
    context: Context,
    date: String,
    condition: String?,
    maxTemp: Double?,
    minTemp: Double?,
    windDirection: Double?,
    windSpeed: Double?,
): Daily {
    // The 'Z' in the timestamp is misused. It is actually in Europe/Istanbul rather than Etc/UTC
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
    return Daily(
        date = formatter.parse(date)!!,
        day = HalfDay(
            weatherText = getWeatherText(context, condition),
            weatherCode = getWeatherCode(condition),
            temperature = Temperature(
                temperature = maxTemp
            ),
            wind = Wind(
                degree = windDirection,
                speed = windSpeed?.div(3.6)
            )
        ),
        night = HalfDay(
            weatherText = getWeatherText(context, condition),
            weatherCode = getWeatherCode(condition),
            temperature = Temperature(
                temperature = minTemp
            ),
            wind = Wind(
                degree = windDirection,
                speed = windSpeed?.div(3.6)
            )
        )
    )
}

// Source: https://www.mgm.gov.tr/Scripts/ziko16_js/angularService/ililceler.js?v=4
// under function convertHadise
private fun getWeatherText(
    context: Context,
    condition: String?,
): String? {
    return when (condition) {
        "A" -> context.getString(R.string.common_weather_text_clear_sky) // Açık
        "AB" -> context.getString(R.string.common_weather_text_mainly_clear) // Az Bulutlu
        "PB" -> context.getString(R.string.common_weather_text_partly_cloudy) // Parçalı Bulutlu
        "CB" -> context.getString(R.string.common_weather_text_cloudy) // Çok Bulutlu
        "HY" -> context.getString(R.string.common_weather_text_rain_light) // Hafif Yağmurlu
        "Y" -> context.getString(R.string.common_weather_text_rain) // Yağmurlu
        "KY" -> context.getString(R.string.common_weather_text_rain_heavy) // Kuvvetli Yağmurlu
        "KKY" -> context.getString(R.string.common_weather_text_rain_snow_mixed) // Karla Karışık Yağmurlu
        "HKY" -> context.getString(R.string.common_weather_text_snow_light) // Hafif Kar Yağışlı
        "K" -> context.getString(R.string.common_weather_text_snow) // Kar Yağışlı
        "KYK", "YKY" -> context.getString(R.string.common_weather_text_snow_heavy) // Yoğun Kar Yağışlı
        "HSY" -> context.getString(R.string.common_weather_text_rain_showers_light) // Hafif Sağanak Yağışlı
        "SY" -> context.getString(R.string.common_weather_text_rain_showers) // Sağanak Yağışlı
        "KSY" -> context.getString(R.string.common_weather_text_rain_showers_heavy) // Kuvvetli Sağanak Yağışlı
        "MSY" -> context.getString(R.string.common_weather_text_rain_showers) // Mevzi Sağanak Yağışlı
        "DY" -> context.getString(R.string.weather_kind_hail) // Dolu
        "GSY" -> context.getString(R.string.weather_kind_thunderstorm) // Gökgürültülü Sağanak Yağışlı
        "KGY" -> context.getString(R.string.weather_kind_thunderstorm) // Kuvvetli Gökgürültülü Sağanak Yağışlı
        "SIS" -> context.getString(R.string.common_weather_text_fog) // Sisli
        "PUS" -> context.getString(R.string.common_weather_text_mist) // Puslu
        "DNM" -> context.getString(R.string.common_weather_text_smoke) // Dumanlı
        "KF" -> context.getString(R.string.common_weather_text_sand_storm) // Toz veya Kum Fırtınası
        "R" -> context.getString(R.string.weather_kind_wind) // Rüzgarlı
        "GKR" -> context.getString(R.string.weather_kind_wind) // Güneyli Kuvvetli Rüzgar
        "KKR" -> context.getString(R.string.weather_kind_wind) // Kuzeyli Kuvvetli Rüzgar
        "SCK" -> context.getString(R.string.common_weather_text_hot) // Sıcak
        "SGK" -> context.getString(R.string.common_weather_text_cold) // Soğuk
        "HHY" -> context.getString(R.string.common_weather_text_rain) // Yağışlı
        else -> null
    }
}

private fun getWeatherCode(
    condition: String?,
): WeatherCode? {
    return when (condition) {
        "A" -> WeatherCode.CLEAR // Açık
        "AB" -> WeatherCode.PARTLY_CLOUDY // Az Bulutlu
        "PB" -> WeatherCode.PARTLY_CLOUDY // Parçalı Bulutlu
        "CB" -> WeatherCode.CLOUDY // Çok Bulutlu
        "HY" -> WeatherCode.RAIN // Hafif Yağmurlu
        "Y" -> WeatherCode.RAIN // Yağmurlu
        "KY" -> WeatherCode.RAIN // Kuvvetli Yağmurlu
        "KKY" -> WeatherCode.SLEET // Karla Karışık Yağmurlu
        "HKY" -> WeatherCode.SNOW // Hafif Kar Yağışlı
        "K" -> WeatherCode.SNOW // Kar Yağışlı
        "KYK", "YKY" -> WeatherCode.SNOW // Yoğun Kar Yağışlı
        "HSY" -> WeatherCode.RAIN // Hafif Sağanak Yağışlı
        "SY" -> WeatherCode.RAIN // Sağanak Yağışlı
        "KSY" -> WeatherCode.RAIN // Kuvvetli Sağanak Yağışlı
        "MSY" -> WeatherCode.RAIN // Mevzi Sağanak Yağışlı
        "DY" -> WeatherCode.HAIL // Dolu
        "GSY" -> WeatherCode.THUNDERSTORM // Gökgürültülü Sağanak Yağışlı
        "KGY" -> WeatherCode.THUNDERSTORM // Kuvvetli Gökgürültülü Sağanak Yağışlı
        "SIS" -> WeatherCode.FOG // Sisli
        "PUS" -> WeatherCode.FOG // Puslu
        "DNM" -> WeatherCode.HAZE // Dumanlı
        "KF" -> WeatherCode.WIND // Toz veya Kum Fırtınası
        "R" -> WeatherCode.WIND // Rüzgarlı
        "GKR" -> WeatherCode.WIND // Güneyli Kuvvetli Rüzgar
        "KKR" -> WeatherCode.WIND // Kuzeyli Kuvvetli Rüzgar
        "SCK" -> null // Sıcak
        "SGK" -> null // Soğuk
        "HHY" -> WeatherCode.RAIN // Yağışlı
        else -> null
    }
}

// Simply join names of the active alert types as alert headline.
// Turkish terminology from: https://www.mgm.gov.tr/meteouyari/turkiye.aspx?Gun=1
// under "harita-alti-hadise"
private fun getAlertHeadline(
    weather: List<String>?,
): String? {
    val items = mutableListOf<String>()
    weather?.forEach {
        when (it) {
            "cold" -> items.add("Soğuk")
            "hot" -> items.add("Sıcak")
            "fog" -> items.add("Sis")
            "agricultural" -> items.add("Zirai Don")
            "ice" -> items.add("Buzlanma ve Don")
            "dust" -> items.add("Toz Taşınımı")
            "snowmelt" -> items.add("Kar Erimesi")
            "avalanche" -> items.add("Çığ")
            "snow" -> items.add("Kar")
            "thunderstorm" -> items.add("Gökgürültülü Sağanak Yağış")
            "wind" -> items.add("Rüzgar")
            "rain" -> items.add("Yağmur")
        }
    }
    return items.joinToString(", ")
}

// Color source: https://www.mgm.gov.tr/meteouyari/meteouyari.css
private fun getAlertColor(
    severity: AlertSeverity,
): Int {
    return when (severity) {
        AlertSeverity.EXTREME -> Color.rgb(249, 75, 101) // #btnKirmizi
        AlertSeverity.SEVERE -> Color.rgb(255, 199, 87) // #btnTuruncu
        AlertSeverity.MODERATE -> Color.rgb(255, 244, 49) // #btnSari
        else -> Alert.colorFromSeverity(severity)
    }
}

private fun getValid(
    value: Double?,
): Double? {
    return if (value == -9999.0) null else value
}
