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

package org.breezyweather.sources.openmeteo

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.mf.getFrenchDepartmentCode
import org.breezyweather.sources.openmeteo.json.OpenMeteoAirQualityHourly
import org.breezyweather.sources.openmeteo.json.OpenMeteoAirQualityResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoLocationResult
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherDaily
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherHourly
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherMinutely
import org.breezyweather.sources.openmeteo.json.OpenMeteoWeatherResult
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

fun convert(
    result: OpenMeteoLocationResult
): Location {
    return Location(
        cityId = result.id.toString(),
        latitude = result.latitude,
        longitude = result.longitude,
        timeZone = result.timezone,
        country = if (!result.country.isNullOrEmpty()) result.country else result.countryCode ?: "",
        countryCode = result.countryCode,
        province = if (result.admin2.isNullOrEmpty()) {
            if (result.admin1.isNullOrEmpty()) {
                if (result.admin3.isNullOrEmpty()) {
                    result.admin4
                } else result.admin3
            } else result.admin1
        } else result.admin2,
        // Province code is mandatory for MF source to have alerts/air quality, and MF source uses Open-Meteo search
        provinceCode = if (result.countryCode.equals("FR", ignoreCase = true)) {
            getFrenchDepartmentCode(result.admin2 ?: "")
        } else null,
        city = result.name,
        weatherSource = "openmeteo"
    )
}

fun convert(
    context: Context,
    location: Location,
    weatherResult: OpenMeteoWeatherResult,
    airQualityResult: OpenMeteoAirQualityResult
): WeatherWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (weatherResult.hourly == null || weatherResult.daily == null) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        current = Current(
            weatherText = getWeatherText(context, weatherResult.current?.weatherCode),
            weatherCode = getWeatherCode(weatherResult.current?.weatherCode),
            temperature = Temperature(
                temperature = weatherResult.current?.temperature,
                apparentTemperature = weatherResult.current?.apparentTemperature
            ),
            wind = Wind(
                degree = weatherResult.current?.windDirection,
                speed = weatherResult.current?.windSpeed,
                gusts = weatherResult.current?.windGusts
            ),
            uV = UV(index = weatherResult.current?.uvIndex),
            relativeHumidity = weatherResult.current?.relativeHumidity?.toDouble(),
            dewPoint = weatherResult.current?.dewPoint,
            pressure = weatherResult.current?.pressureMsl,
            cloudCover = weatherResult.current?.cloudCover,
            visibility = weatherResult.current?.visibility
        ),
        dailyForecast = getDailyList(weatherResult.daily, location),
        hourlyForecast = getHourlyList(context, weatherResult.hourly, airQualityResult),
        minutelyForecast = getMinutelyList(weatherResult.minutelyFifteen)
    )
}

private fun getDailyList(
    dailyResult: OpenMeteoWeatherDaily,
    location: Location
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList(dailyResult.time.size - 1)
    for (i in 0 until dailyResult.time.size - 1) {
        val theDayWithDstFixed = dailyResult.time[i].times(1000).toDate()
            .toCalendarWithTimeZone(location.javaTimeZone)
            .apply {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.HOUR_OF_DAY, 0)
            }.time
        val daily = Daily(
            date = theDayWithDstFixed,
            day = HalfDay(
                temperature = Temperature(
                    temperature = dailyResult.temperatureMax?.getOrNull(i),
                    apparentTemperature = dailyResult.apparentTemperatureMax?.getOrNull(i)
                ),
            ),
            night = HalfDay(
                temperature = Temperature(
                    // For night temperature, we take the minTemperature from the following day
                    temperature = dailyResult.temperatureMin?.getOrNull(i + 1),
                    apparentTemperature = dailyResult.apparentTemperatureMin?.getOrNull(i + 1)
                )
            ),
            sun = Astro(
                riseDate = dailyResult.sunrise?.getOrNull(i)?.times(1000)?.toDate(),
                setDate = dailyResult.sunset?.getOrNull(i)?.times(1000)?.toDate()
            ),
            uV = UV(index = dailyResult.uvIndexMax?.getOrNull(i)),
            sunshineDuration = dailyResult.sunshineDuration?.getOrNull(i)?.div(3600)
        )
        dailyList.add(daily)
    }
    return dailyList
}

private fun getHourlyList(
    context: Context,
    hourlyResult: OpenMeteoWeatherHourly,
    airQualityResult: OpenMeteoAirQualityResult
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    for (i in hourlyResult.time.indices) {
        val airQualityIndex = airQualityResult.hourly?.time?.indexOfFirst { it == hourlyResult.time[i] }

        hourlyList.add(
            HourlyWrapper(
                date = hourlyResult.time[i].times(1000).toDate(),
                isDaylight = if (hourlyResult.isDay?.getOrNull(i) != null) hourlyResult.isDay[i] > 0 else null,
                weatherText = getWeatherText(context, hourlyResult.weatherCode?.getOrNull(i)),
                weatherCode = getWeatherCode(hourlyResult.weatherCode?.getOrNull(i)),
                temperature = Temperature(
                    temperature = hourlyResult.temperature?.getOrNull(i),
                    apparentTemperature = hourlyResult.apparentTemperature?.getOrNull(i)
                ),
                precipitation = Precipitation(
                    total = hourlyResult.precipitation?.getOrNull(i),
                    rain = hourlyResult.rain?.getOrNull(i) + hourlyResult.showers?.getOrNull(i),
                    snow = hourlyResult.snowfall?.getOrNull(i)
                ),
                precipitationProbability = PrecipitationProbability(
                    total = hourlyResult.precipitationProbability?.getOrNull(i)?.toDouble()
                ),
                wind = Wind(
                    degree = hourlyResult.windDirection?.getOrNull(i)?.toDouble(),
                    speed = hourlyResult.windSpeed?.getOrNull(i),
                    gusts = hourlyResult.windGusts?.getOrNull(i)
                ),
                airQuality = if (airQualityIndex != null && airQualityIndex != -1) AirQuality(
                    pM25 = airQualityResult.hourly.pm25?.getOrNull(airQualityIndex),
                    pM10 = airQualityResult.hourly.pm10?.getOrNull(airQualityIndex),
                    sO2 = airQualityResult.hourly.sulphurDioxide?.getOrNull(airQualityIndex),
                    nO2 = airQualityResult.hourly.nitrogenDioxide?.getOrNull(airQualityIndex),
                    o3 = airQualityResult.hourly.ozone?.getOrNull(airQualityIndex),
                    cO = airQualityResult.hourly.carbonMonoxide?.getOrNull(airQualityIndex)?.div(1000.0)
                ) else null,
                pollen = if (airQualityIndex != null && airQualityIndex != -1) Pollen(
                    alder = airQualityResult.hourly.alderPollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    birch = airQualityResult.hourly.birchPollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    grass = airQualityResult.hourly.grassPollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    mugwort = airQualityResult.hourly.mugwortPollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    olive = airQualityResult.hourly.olivePollen?.getOrNull(airQualityIndex)?.roundToInt(),
                    ragweed = airQualityResult.hourly.ragweedPollen?.getOrNull(airQualityIndex)?.roundToInt()
                ) else null,
                uV = UV(index = hourlyResult.uvIndex?.getOrNull(i)),
                relativeHumidity = hourlyResult.relativeHumidity?.getOrNull(i)?.toDouble(),
                dewPoint = hourlyResult.dewPoint?.getOrNull(i),
                pressure = hourlyResult.pressureMsl?.getOrNull(i),
                cloudCover = hourlyResult.cloudCover?.getOrNull(i),
                visibility = hourlyResult.visibility?.getOrNull(i)?.toDouble()
            )
        )
    }
    return hourlyList
}

fun getMinutelyList(minutelyFifteen: OpenMeteoWeatherMinutely?): List<Minutely>? {
    if (minutelyFifteen?.time == null || minutelyFifteen.time.isEmpty()) return null

    val currentMinutelyIndex = minutelyFifteen.time.indexOfFirst { it.times(1000) >= (Date().time - 15 * 60 * 1000) }
    val maxMinutelyIndex = minOf(currentMinutelyIndex + 8, minutelyFifteen.time.size - 1)
    val precipitationMinutely = minutelyFifteen.precipitation?.slice(currentMinutelyIndex until maxMinutelyIndex)
    //val precipitationProbabilityMinutely = minutelyFifteen.precipitationProbability?.slice(currentMinutelyIndex until maxMinutelyIndex)
    return minutelyFifteen.time.slice(currentMinutelyIndex until maxMinutelyIndex)
        // 2 hours
        .mapIndexed { i, time ->
            Minutely(
                date = time.times(1000).toDate(),
                minuteInterval = 15,
                precipitationIntensity = /*if (precipitationProbabilityMinutely?.getOrNull(i) != null &&
                    precipitationProbabilityMinutely[i]!! > 30) {*/
                    precipitationMinutely?.getOrNull(i)?.toDouble()
                //} else null
            )
        }
}

private fun getWeatherText(context: Context, icon: Int?): String? {
    return when (icon) {
        null -> null
        0 -> context.getString(R.string.openmeteo_weather_text_clear_sky)
        1 -> context.getString(R.string.openmeteo_weather_text_mainly_clear)
        2 -> context.getString(R.string.openmeteo_weather_text_partly_cloudy)
        3 -> context.getString(R.string.openmeteo_weather_text_overcast)
        45 -> context.getString(R.string.openmeteo_weather_text_fog)
        48 -> context.getString(R.string.openmeteo_weather_text_depositing_rime_fog)
        51 -> context.getString(R.string.openmeteo_weather_text_drizzle_light_intensity)
        53 -> context.getString(R.string.openmeteo_weather_text_drizzle_moderate_intensity)
        55 -> context.getString(R.string.openmeteo_weather_text_drizzle_dense_intensity)
        56 -> context.getString(R.string.openmeteo_weather_text_freezing_drizzle_light_intensity)
        57 -> context.getString(R.string.openmeteo_weather_text_freezing_drizzle_dense_intensity)
        61 -> context.getString(R.string.openmeteo_weather_text_rain_slight_intensity)
        63 -> context.getString(R.string.openmeteo_weather_text_rain_moderate_intensity)
        65 -> context.getString(R.string.openmeteo_weather_text_rain_heavy_intensity)
        66 -> context.getString(R.string.openmeteo_weather_text_freezing_rain_light_intensity)
        67 -> context.getString(R.string.openmeteo_weather_text_freezing_rain_heavy_intensity)
        71 -> context.getString(R.string.openmeteo_weather_text_snow_slight_intensity)
        73 -> context.getString(R.string.openmeteo_weather_text_snow_moderate_intensity)
        75 -> context.getString(R.string.openmeteo_weather_text_snow_heavy_intensity)
        77 -> context.getString(R.string.openmeteo_weather_text_snow_grains)
        80 -> context.getString(R.string.openmeteo_weather_text_rain_showers_slight)
        81 -> context.getString(R.string.openmeteo_weather_text_rain_showers_moderate)
        82 -> context.getString(R.string.openmeteo_weather_text_rain_showers_violent)
        85 -> context.getString(R.string.openmeteo_weather_text_snow_showers_slight)
        86 -> context.getString(R.string.openmeteo_weather_text_snow_showers_heavy)
        95 -> context.getString(R.string.openmeteo_weather_text_thunderstorm_slight_or_moderate)
        96 -> context.getString(R.string.openmeteo_weather_text_thunderstorm_with_slight_hail)
        99 -> context.getString(R.string.openmeteo_weather_text_thunderstorm_with_heavy_hail)
        else -> null
    }
}

private fun getWeatherCode(icon: Int?): WeatherCode? {
    return when (icon) {
        null -> null
        0, 1 -> WeatherCode.CLEAR // Clear sky or Mainly clear
        2 -> WeatherCode.PARTLY_CLOUDY // Partly cloudy
        3 -> WeatherCode.CLOUDY // Overcast
        45, 48 -> WeatherCode.FOG // Fog and depositing rime fog
        51, 53, 55, // Drizzle: Light, moderate, and dense intensity
        56, 57, // Freezing Drizzle: Light and dense intensity
        61, 63, 65, // Rain: Slight, moderate and heavy intensity
        66, 67, // Freezing Rain: Light and heavy intensity
        80, 81, 82 -> WeatherCode.RAIN // Rain showers: Slight, moderate, and violent
        71, 73, 75, // Snow fall: Slight, moderate, and heavy intensity
        85, 86 -> WeatherCode.SNOW // Snow showers slight and heavy
        77 -> WeatherCode.SLEET // Snow grains
        95, 96, 99 -> WeatherCode.THUNDERSTORM // Thunderstorm with slight and heavy hail
        else -> null
    }
}

/**
 * Secondary convert
 */
fun convertSecondary(
    minutelyFifteen: OpenMeteoWeatherMinutely?,
    hourlyAirQualityResult: OpenMeteoAirQualityHourly?,
    requestedFeatures: List<SecondaryWeatherSourceFeature>
): SecondaryWeatherWrapper {
    val airQualityHourly = mutableMapOf<Date, AirQuality>()
    val pollenHourly = mutableMapOf<Date, Pollen>()

    if (hourlyAirQualityResult != null) {
        for (i in hourlyAirQualityResult.time.indices) {
            if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
                airQualityHourly[hourlyAirQualityResult.time[i].times(1000).toDate()] = AirQuality(
                    pM25 = hourlyAirQualityResult.pm25?.getOrNull(i),
                    pM10 = hourlyAirQualityResult.pm10?.getOrNull(i),
                    sO2 = hourlyAirQualityResult.sulphurDioxide?.getOrNull(i),
                    nO2 = hourlyAirQualityResult.nitrogenDioxide?.getOrNull(i),
                    o3 = hourlyAirQualityResult.ozone?.getOrNull(i),
                    cO = hourlyAirQualityResult.carbonMonoxide?.getOrNull(i)?.div(1000.0),
                )
            }
            if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)) {
                pollenHourly[hourlyAirQualityResult.time[i].times(1000).toDate()] = Pollen(
                    alder = hourlyAirQualityResult.alderPollen?.getOrNull(i)?.roundToInt(),
                    birch = hourlyAirQualityResult.birchPollen?.getOrNull(i)?.roundToInt(),
                    grass = hourlyAirQualityResult.grassPollen?.getOrNull(i)?.roundToInt(),
                    mugwort = hourlyAirQualityResult.mugwortPollen?.getOrNull(i)?.roundToInt(),
                    olive = hourlyAirQualityResult.olivePollen?.getOrNull(i)?.roundToInt(),
                    ragweed = hourlyAirQualityResult.ragweedPollen?.getOrNull(i)?.roundToInt(),
                )
            }
        }
    }

    return SecondaryWeatherWrapper(
        airQuality = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            AirQualityWrapper(hourlyForecast = airQualityHourly)
        } else null,
        pollen = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_POLLEN)) {
            PollenWrapper(hourlyForecast = pollenHourly)
        } else null,
        minutelyForecast = getMinutelyList(minutelyFifteen)
    )
}

/**
 * Functions for debugging purposes (tracking NPE)
 */
// Sometimes used in dev to make some null-safety checks
// TODO: Should be moved to its own DebugWeatherService
fun debugConvert(
    context: Context,
    weatherResult: OpenMeteoWeatherResult,
    airQualityResult: OpenMeteoAirQualityResult
): WeatherWrapper {
    val dailyList = mutableListOf<Daily>()
    if (weatherResult.daily != null) {
        for (i in 1 until weatherResult.daily.time.size) {
            val daily = Daily(date = Date(weatherResult.daily.time[i].times(1000)))
            dailyList.add(daily)
        }
    }

    val hourlyList = mutableListOf<HourlyWrapper>()
    if (weatherResult.hourly != null) {
        for (i in weatherResult.hourly.time.indices) {
            // Add to the app only if starts in the current hour
            if (weatherResult.hourly.time[i] >= System.currentTimeMillis() / 1000 - 3600) {
                hourlyList.add(HourlyWrapper(date = Date(weatherResult.hourly.time[i].times(1000))))
            }
        }
    }

    return WeatherWrapper(
        dailyForecast = dailyList,
        hourlyForecast = hourlyList
    )
}
