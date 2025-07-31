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

package org.breezyweather.sources.ilmateenistus

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.ilmateenistus.json.IlmateenistusForecastResult
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun convert(
    context: Context,
    location: Location,
    forecastResult: IlmateenistusForecastResult,
): List<Location> {
    val locationList = mutableListOf<Location>()
    if (forecastResult.location.isNullOrEmpty()) {
        throw InvalidLocationException()
    }
    val locationParts = forecastResult.location.split(',')
    locationList.add(
        location.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            timeZone = "Europe/Tallinn",
            country = Locale(context.currentLocale.code, "EE").displayCountry,
            countryCode = "EE",
            admin1 = locationParts.getOrNull(0)?.trim(),
            city = locationParts.getOrNull(1)?.trim() ?: "",
            district = locationParts.getOrNull(2)?.trim()
        )
    )
    return locationList
}

internal fun getDailyForecast(
    hourlyList: List<HourlyWrapper>,
): List<DailyWrapper> {
    // CommonConverter.kt does not compute daily for this source
    // without providing at least a empty list filled with dates.
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Tallinn")
    val hourlyListDates = hourlyList.groupBy { formatter.format(it.date) }.keys
    val dailyList = mutableListOf<DailyWrapper>()
    hourlyListDates.forEach {
        dailyList.add(
            DailyWrapper(
                date = formatter.parse(it)!!
            )
        )
    }
    return dailyList
}

internal fun getHourlyForecast(
    context: Context,
    forecastResult: IlmateenistusForecastResult,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Tallinn")
    forecastResult.forecast?.tabular?.time?.forEach {
        if (it.attributes.from != null) {
            hourlyList.add(
                HourlyWrapper(
                    date = formatter.parse(it.attributes.from)!!,
                    weatherText = getWeatherText(context, it.phenomen?.attributes?.className),
                    weatherCode = getWeatherCode(it.phenomen?.attributes?.className),
                    temperature = TemperatureWrapper(
                        temperature = it.temperature?.attributes?.value?.toDoubleOrNull()
                    ),
                    precipitation = Precipitation(
                        total = it.precipitation?.attributes?.value?.toDoubleOrNull()
                    ),
                    wind = Wind(
                        degree = it.windDirection?.attributes?.deg?.toDoubleOrNull(),
                        speed = it.windSpeed?.attributes?.mps?.toDoubleOrNull()
                    ),
                    pressure = it.pressure?.attributes?.value?.toDoubleOrNull()
                )
            )
        }
    }
    return hourlyList
}

private fun getWeatherText(
    context: Context,
    phenomenon: String?,
): String? {
    return when (phenomenon) {
        "clear" -> context.getString(R.string.common_weather_text_clear_sky)
        "few_clouds" -> context.getString(R.string.common_weather_text_mainly_clear)
        "cloudy" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "cloudy_with_clear_spells" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "overcast" -> context.getString(R.string.common_weather_text_cloudy)
        "light_snow_shower" -> context.getString(R.string.common_weather_text_snow_showers_light)
        "moderate_snow_shower" -> context.getString(R.string.common_weather_text_snow_showers)
        "heavy_snow_shower" -> context.getString(R.string.openmeteo_weather_text_snow_showers_heavy)
        "light_shower" -> context.getString(R.string.common_weather_text_rain_showers_light)
        "moderate_shower" -> context.getString(R.string.common_weather_text_rain_showers_moderate)
        "heavy_shower" -> context.getString(R.string.common_weather_text_rain_showers_heavy)
        "light_rain" -> context.getString(R.string.common_weather_text_rain_light)
        "moderate_rain" -> context.getString(R.string.common_weather_text_rain_moderate)
        "heavy_rain" -> context.getString(R.string.common_weather_text_rain_heavy)
        "risk_of_glaze" -> context.getString(R.string.common_weather_text_glaze)
        "light_sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
        "moderate_sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "light_snowfall" -> context.getString(R.string.common_weather_text_snow_light)
        "moderate_snowfall" -> context.getString(R.string.common_weather_text_snow_moderate)
        "heavy_snowfall" -> context.getString(R.string.common_weather_text_snow_heavy)
        "snowstorm" -> context.getString(R.string.common_weather_text_blowing_snow)
        "drifting_snow" -> context.getString(R.string.common_weather_text_drifting_snow)
        "hail" -> context.getString(R.string.weather_kind_hail)
        "mist" -> context.getString(R.string.common_weather_text_mist)
        "fog" -> context.getString(R.string.common_weather_text_fog)
        "thunder" -> context.getString(R.string.weather_kind_thunder)
        "thunderstorm" -> context.getString(R.string.weather_kind_thunderstorm)
        else -> null
    }
}

private fun getWeatherCode(
    phenomenon: String?,
): WeatherCode? {
    return when (phenomenon) {
        "clear" -> WeatherCode.CLEAR
        "few_clouds" -> WeatherCode.CLEAR
        "cloudy" -> WeatherCode.PARTLY_CLOUDY
        "cloudy_with_clear_spells" -> WeatherCode.PARTLY_CLOUDY
        "overcast" -> WeatherCode.CLOUDY
        "light_snow_shower" -> WeatherCode.SNOW
        "moderate_snow_shower" -> WeatherCode.SNOW
        "heavy_snow_shower" -> WeatherCode.SNOW
        "light_shower" -> WeatherCode.RAIN
        "moderate_shower" -> WeatherCode.RAIN
        "heavy_shower" -> WeatherCode.RAIN
        "light_rain" -> WeatherCode.RAIN
        "moderate_rain" -> WeatherCode.RAIN
        "heavy_rain" -> WeatherCode.RAIN
        "risk_of_glaze" -> WeatherCode.SLEET
        "light_sleet" -> WeatherCode.SLEET
        "moderate_sleet" -> WeatherCode.SLEET
        "light_snowfall" -> WeatherCode.SNOW
        "moderate_snowfall" -> WeatherCode.SNOW
        "heavy_snowfall" -> WeatherCode.SNOW
        "snowstorm" -> WeatherCode.SNOW
        "drifting_snow" -> WeatherCode.SNOW
        "hail" -> WeatherCode.HAIL
        "mist" -> WeatherCode.FOG
        "fog" -> WeatherCode.FOG
        "thunder" -> WeatherCode.THUNDER
        "thunderstorm" -> WeatherCode.THUNDERSTORM
        else -> null
    }
}
