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

package org.breezyweather.sources.metoffice

import android.content.Context
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import org.breezyweather.R
import org.breezyweather.sources.metoffice.json.MetOfficeDaily
import org.breezyweather.sources.metoffice.json.MetOfficeForecast
import org.breezyweather.sources.metoffice.json.MetOfficeHourly

internal fun getDailyForecast(
    dailyResult: MetOfficeForecast<MetOfficeDaily>,
    context: Context,
): List<DailyWrapper> {
    val feature = dailyResult.features[0] // should only be one feature for this kind of API call
    return feature.properties.timeSeries.map { result ->
        val (dayText, dayCode) = convertWeatherCode(result.daySignificantWeatherCode, context)
            ?: Pair(null, null)
        val (nightText, nightCode) = convertWeatherCode(result.nightSignificantWeatherCode, context)
            ?: Pair(null, null)
        DailyWrapper(
            date = result.time,
            day = HalfDay(
                weatherText = dayText,
                weatherCode = dayCode,
                temperature = Temperature(
                    temperature = result.dayMaxScreenTemperature,
                    apparentTemperature = result.dayMaxFeelsLikeTemp
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.dayProbabilityOfPrecipitation?.toDouble(),
                    rain = result.dayProbabilityOfRain?.toDouble(),
                    snow = result.dayProbabilityOfSnow?.toDouble(),
                    thunderstorm = result.dayProbabilityOfSferics?.toDouble()
                )
            ),
            night = HalfDay(
                weatherText = nightText,
                weatherCode = nightCode,
                temperature = Temperature(
                    temperature = result.nightMinScreenTemperature,
                    apparentTemperature = result.nightMinFeelsLikeTemp
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.nightProbabilityOfPrecipitation?.toDouble(),
                    rain = result.nightProbabilityOfRain?.toDouble(),
                    snow = result.nightProbabilityOfSnow?.toDouble(),
                    thunderstorm = result.nightProbabilityOfSferics?.toDouble()
                )
            ),
            uV = UV(index = result.maxUvIndex?.toDouble())
        )
    }
}

/**
 * Returns hourly forecast
 */
internal fun getHourlyForecast(
    hourlyResult: MetOfficeForecast<MetOfficeHourly>,
    context: Context,
): List<HourlyWrapper> {
    val feature = hourlyResult.features[0] // should only be one feature for this kind of API call
    return feature.properties.timeSeries.map { result ->
        val (weatherText, weatherCode) = convertWeatherCode(result.significantWeatherCode, context)
            ?: Pair(null, null)
        HourlyWrapper(
            date = result.time,
            weatherText = weatherText,
            weatherCode = weatherCode,
            temperature = Temperature(
                temperature = result.screenTemperature,
                apparentTemperature = result.feelsLikeTemperature
            ),
            precipitation = Precipitation(
                total = result.totalPrecipAmount,
                snow = result.totalSnowAmount
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.probOfPrecipitation?.toDouble()
            ),
            wind = Wind(
                degree = result.windDirectionFrom10m?.toDouble(),
                speed = result.windSpeed10m,
                gusts = result.windGustSpeed10m
            ),
            uV = UV(
                index = result.uvIndex?.toDouble()
            ),
            relativeHumidity = result.screenRelativeHumidity,
            dewPoint = result.screenDewPointTemperature,
            pressure = result.mslp?.toDouble()?.div(100), // pa -> mb
            visibility = result.visibility?.toDouble()
        )
    }
}

private fun convertWeatherCode(
    significantWeatherCode: Int?,
    context: Context,
): Pair<String, WeatherCode>? {
    return when (significantWeatherCode) {
        -1 -> Pair("Trace rain", WeatherCode.CLOUDY)
        0 -> Pair(context.getString(R.string.common_weather_text_clear_sky), WeatherCode.CLEAR)
        1 -> Pair(context.getString(R.string.common_weather_text_clear_sky), WeatherCode.CLEAR)
        2, 3 -> Pair(context.getString(R.string.common_weather_text_partly_cloudy), WeatherCode.PARTLY_CLOUDY)
        5 -> Pair(context.getString(R.string.common_weather_text_mist), WeatherCode.FOG)
        6 -> Pair(context.getString(R.string.common_weather_text_fog), WeatherCode.FOG)
        7 -> Pair(context.getString(R.string.common_weather_text_cloudy), WeatherCode.CLOUDY)
        8 -> Pair(context.getString(R.string.common_weather_text_overcast), WeatherCode.CLOUDY)
        9, 10 -> Pair(context.getString(R.string.common_weather_text_rain_showers_light), WeatherCode.RAIN)
        11 -> Pair(context.getString(R.string.common_weather_text_drizzle), WeatherCode.RAIN)
        12 -> Pair(context.getString(R.string.common_weather_text_rain_light), WeatherCode.RAIN)
        13, 14 -> Pair(context.getString(R.string.common_weather_text_rain_showers_heavy), WeatherCode.RAIN)
        15 -> Pair(context.getString(R.string.common_weather_text_rain_heavy), WeatherCode.RAIN)
        16, 17 -> Pair(context.getString(R.string.metno_weather_text_sleetshowers), WeatherCode.SLEET)
        18 -> Pair(context.getString(R.string.metno_weather_text_sleet), WeatherCode.SLEET)
        19, 20 -> Pair("Hail shower", WeatherCode.HAIL)
        21 -> Pair(context.getString(R.string.weather_kind_hail), WeatherCode.HAIL)
        22, 23 -> Pair(context.getString(R.string.common_weather_text_snow_showers_light), WeatherCode.SNOW)
        24 -> Pair(context.getString(R.string.common_weather_text_snow_light), WeatherCode.SNOW)
        25, 26 -> Pair(context.getString(R.string.common_weather_text_snow_showers_heavy), WeatherCode.SNOW)
        27 -> Pair(context.getString(R.string.common_weather_text_snow_heavy), WeatherCode.SNOW)
        28, 29 -> Pair(context.getString(R.string.weather_kind_thunderstorm), WeatherCode.THUNDERSTORM)
        30 -> Pair(context.getString(R.string.weather_kind_thunder), WeatherCode.THUNDER)
        else -> null
    }
}
