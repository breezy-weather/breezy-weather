/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.gadgetbridge

import android.content.Context
import android.os.Bundle
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.reference.WeatherCode
import kotlinx.serialization.json.Json
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.source.BroadcastSource
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.domain.location.model.getPlace
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.sources.gadgetbridge.json.GadgetbridgeAirQuality
import org.breezyweather.sources.gadgetbridge.json.GadgetbridgeDailyForecast
import org.breezyweather.sources.gadgetbridge.json.GadgetbridgeData
import org.breezyweather.sources.gadgetbridge.json.GadgetbridgeHourlyForecast
import javax.inject.Inject
import kotlin.math.roundToInt

class GadgetbridgeService @Inject constructor() : BroadcastSource {

    override val id = "gadgetbridge"
    override val name = "Gadgetbridge"

    override val intentAction = "nodomain.freeyourgadget.gadgetbridge.ACTION_GENERIC_WEATHER"

    override fun getExtras(
        context: Context,
        allLocations: List<Location>,
        updatedLocationIds: Array<String>?,
    ): Bundle? {
        if (!allLocations.any { it.weather?.current != null }) {
            LogHelper.log(msg = "Not sending GadgetBridge data, current weather is null")
            return null
        }

        return Bundle().apply {
            putString(
                "WeatherJson",
                Json.encodeToString(getWeatherData(context, allLocations[0]))
            )
            putString(
                "WeatherSecondaryJson",
                Json.encodeToString(
                    allLocations.drop(1).mapNotNull {
                        if (it.weather?.current != null) getWeatherData(context, it) else null
                    }
                )
            )
        }
    }

    private fun getWeatherData(
        context: Context,
        location: Location,
    ): GadgetbridgeData {
        val current = location.weather?.current
        val today = location.weather?.today

        return GadgetbridgeData(
            timestamp = location.weather?.base?.forecastUpdateTime?.time?.div(1000)?.toInt(),
            location = location.getPlace(context),
            currentTemp = current?.temperature?.temperature?.roundCelsiusToKelvin(),
            currentConditionCode = getWeatherCode(current?.weatherCode),
            currentCondition = current?.weatherText,
            currentHumidity = current?.relativeHumidity?.roundToInt(),
            windSpeed = current?.wind?.speed?.let {
                SpeedUnit.KILOMETER_PER_HOUR.convertUnit(it)
            }?.toFloat(),
            windDirection = current?.wind?.degree?.roundToInt(),
            uvIndex = current?.uV?.index?.toFloat(),

            todayMaxTemp = today?.day?.temperature?.temperature?.roundCelsiusToKelvin(),
            todayMinTemp = today?.night?.temperature?.temperature?.roundCelsiusToKelvin(),
            feelsLikeTemp = current?.temperature?.feelsLikeTemperature?.roundCelsiusToKelvin(),
            precipProbability = maxOfNullable(
                today?.day?.precipitationProbability?.total,
                today?.night?.precipitationProbability?.total
            )?.roundToInt(),

            dewPoint = current?.dewPoint?.roundCelsiusToKelvin(),
            pressure = current?.pressure?.inHectopascals?.toFloat(),
            cloudCover = current?.cloudCover,
            visibility = current?.visibility?.inMeters?.toFloat(),

            sunRise = today?.sun?.riseDate?.time?.div(1000)?.toInt(),
            sunSet = today?.sun?.setDate?.time?.div(1000)?.toInt(),
            moonRise = today?.moon?.riseDate?.time?.div(1000)?.toInt(),
            moonSet = today?.moon?.setDate?.time?.div(1000)?.toInt(),
            moonPhase = today?.moonPhase?.angle,

            airQuality = getAirQuality(current?.airQuality),

            forecasts = getDailyForecasts(location.weather?.dailyForecastStartingToday),
            hourly = getHourlyForecasts(location.weather?.fullNextHourlyForecast)
        )
    }

    private fun getDailyForecasts(dailyForecast: List<Daily>?): List<GadgetbridgeDailyForecast>? {
        if (dailyForecast.isNullOrEmpty() || dailyForecast.size < 2) return null

        return dailyForecast.subList(1, dailyForecast.size).map { day ->
            val maxWind = listOf(
                day.day?.wind,
                day.night?.wind
            ).maxByOrNull { it?.speed ?: Double.MIN_VALUE }

            GadgetbridgeDailyForecast(
                conditionCode = getWeatherCode(day.day?.weatherCode),
                maxTemp = day.day?.temperature?.temperature?.roundCelsiusToKelvin(),
                minTemp = day.night?.temperature?.temperature?.roundCelsiusToKelvin(),
                humidity = day.relativeHumidity?.average?.roundToInt(),
                windSpeed = maxWind?.speed?.let {
                    SpeedUnit.KILOMETER_PER_HOUR.convertUnit(it)
                }?.toFloat(),
                windDirection = maxWind?.degree?.roundToInt(),
                uvIndex = day.uV?.index?.toFloat(),
                precipProbability = maxOfNullable(
                    day.day?.precipitationProbability?.total,
                    day.night?.precipitationProbability?.total
                )?.roundToInt(),

                sunRise = day.sun?.riseDate?.time?.div(1000)?.toInt(),
                sunSet = day.sun?.setDate?.time?.div(1000)?.toInt(),
                moonRise = day.moon?.riseDate?.time?.div(1000)?.toInt(),
                moonSet = day.moon?.setDate?.time?.div(1000)?.toInt(),
                moonPhase = day.moonPhase?.angle,

                airQuality = getAirQuality(day.airQuality)
            )
        }
    }

    private fun getAirQuality(airQuality: AirQuality?): GadgetbridgeAirQuality? {
        if (airQuality == null) return null
        val aqi = airQuality.getIndex() ?: return null

        return GadgetbridgeAirQuality(
            aqi = aqi,
            co = airQuality.cO?.inMicrogramsPerCubicMeter?.toFloat(),
            no2 = airQuality.nO2?.inMicrogramsPerCubicMeter?.toFloat(),
            o3 = airQuality.o3?.inMicrogramsPerCubicMeter?.toFloat(),
            pm10 = airQuality.pM10?.inMicrogramsPerCubicMeter?.toFloat(),
            pm25 = airQuality.pM25?.inMicrogramsPerCubicMeter?.toFloat(),
            so2 = airQuality.sO2?.inMicrogramsPerCubicMeter?.toFloat(),
            coAqi = airQuality.getIndex(PollutantIndex.CO),
            no2Aqi = airQuality.getIndex(PollutantIndex.NO2),
            o3Aqi = airQuality.getIndex(PollutantIndex.O3),
            pm10Aqi = airQuality.getIndex(PollutantIndex.PM10),
            pm25Aqi = airQuality.getIndex(PollutantIndex.PM25),
            so2Aqi = airQuality.getIndex(PollutantIndex.SO2)
        )
    }

    private fun getHourlyForecasts(dailyForecast: List<Hourly>?): List<GadgetbridgeHourlyForecast>? {
        if (dailyForecast.isNullOrEmpty()) return null

        return dailyForecast.map { hour ->
            GadgetbridgeHourlyForecast(
                timestamp = hour.date.time.div(1000).toInt(),
                temp = hour.temperature?.temperature?.roundCelsiusToKelvin(),
                conditionCode = getWeatherCode(hour.weatherCode),
                humidity = hour.relativeHumidity?.roundToInt(),
                windSpeed = hour.wind?.speed?.let {
                    SpeedUnit.KILOMETER_PER_HOUR.convertUnit(it)
                }?.toFloat(),
                windDirection = hour.wind?.degree?.roundToInt(),
                uvIndex = hour.uV?.index?.toFloat(),
                precipProbability = hour.precipitationProbability?.total?.roundToInt()
            )
        }
    }

    private fun getWeatherCode(code: WeatherCode?): Int {
        return when (code) {
            WeatherCode.CLEAR -> 800
            WeatherCode.PARTLY_CLOUDY -> 801
            WeatherCode.CLOUDY -> 803
            WeatherCode.RAIN -> 500
            WeatherCode.SNOW -> 600
            WeatherCode.WIND -> 771
            WeatherCode.FOG -> 741
            WeatherCode.HAZE -> 751
            WeatherCode.SLEET -> 611
            WeatherCode.HAIL -> 511
            WeatherCode.THUNDER -> 210
            WeatherCode.THUNDERSTORM -> 211
            else -> 3200
        }
    }

    private fun <T : Comparable<T>> maxOfNullable(a: T?, b: T?): T? {
        return when {
            a == null -> b
            b == null -> a
            else -> maxOf(a, b)
        }
    }

    companion object {
        fun Double.roundCelsiusToKelvin(): Int {
            return TemperatureUnit.KELVIN.convertUnit(this).roundToInt()
        }
    }
}
