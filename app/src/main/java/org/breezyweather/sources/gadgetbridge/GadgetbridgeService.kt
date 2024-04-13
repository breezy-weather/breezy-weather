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
import breezyweather.domain.weather.model.WeatherCode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.extensions.roundDecimals
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

    override fun getExtras(context: Context, locations: List<Location>): Bundle? {
        if (!locations.any { it.weather?.current != null }) {
            LogHelper.log(msg = "Not sending GadgetBridge data, current weather is null")
            return null
        }

        return Bundle().apply {
            putString(
                "WeatherJson",
                Json.encodeToString(getWeatherData(context, locations[0]))
            )
            putString(
                "WeatherSecondaryJson",
                Json.encodeToString(
                    locations.drop(1).mapNotNull {
                        if (it.weather?.current != null) {
                            getWeatherData(context, it)
                        } else null
                    }
                )
            )
        }
    }

    private fun getWeatherData(
        context: Context,
        location: Location
    ): GadgetbridgeData {
        val current = location.weather?.current
        val today = location.weather?.today

        return GadgetbridgeData(
            timestamp = location.weather?.base?.mainUpdateTime?.time?.div(1000)?.toInt(),
            location = location.getPlace(context),
            currentTemp = current?.temperature?.temperature?.roundCelsiusToKelvin(),
            currentConditionCode = getWeatherCode(current?.weatherCode),
            currentCondition = current?.weatherText,
            currentHumidity = current?.relativeHumidity?.roundToInt(),
            windSpeed = current?.wind?.speed?.let {
                SpeedUnit.KPH.convertUnit(it)
            }?.roundDecimals(1)?.toFloat(),
            windDirection = current?.wind?.degree?.roundToInt(),
            uvIndex = current?.uV?.index?.roundDecimals(1)?.toFloat(),

            todayMaxTemp = today?.day?.temperature?.temperature?.roundCelsiusToKelvin(),
            todayMinTemp = today?.night?.temperature?.temperature?.roundCelsiusToKelvin(),
            feelsLikeTemp = current?.temperature?.feelsLikeTemperature?.roundCelsiusToKelvin(),
            precipProbability = today?.day?.precipitationProbability?.total?.roundToInt(),

            dewPoint = current?.dewPoint?.roundCelsiusToKelvin(),
            pressure = current?.pressure?.roundDecimals(1)?.toFloat(),
            cloudCover = current?.cloudCover,
            visibility = current?.visibility?.roundDecimals(1)?.toFloat(),

            sunRise = today?.sun?.riseDate?.time?.div(1000)?.toInt(),
            sunSet = today?.sun?.setDate?.time?.div(1000)?.toInt(),
            moonRise = today?.moon?.riseDate?.time?.div(1000)?.toInt(),
            moonSet = today?.moon?.setDate?.time?.div(1000)?.toInt(),
            moonPhase = today?.moonPhase?.angle,

            airQuality = getAirQuality(current?.airQuality),

            forecasts = getDailyForecasts(location.weather?.dailyForecastStartingToday),
            hourly = getHourlyForecasts(location.weather?.nextHourlyForecast),
        )
    }

    private fun getDailyForecasts(dailyForecast: List<Daily>?): List<GadgetbridgeDailyForecast>? {
        if (dailyForecast.isNullOrEmpty() || dailyForecast.size < 2) return null

        return dailyForecast.slice(1 until dailyForecast.size).map { day ->
            GadgetbridgeDailyForecast(
                conditionCode = getWeatherCode(day.day?.weatherCode),
                maxTemp = day.day?.temperature?.temperature?.roundCelsiusToKelvin(),
                minTemp = day.night?.temperature?.temperature?.roundCelsiusToKelvin(),

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
            co = airQuality.cO?.roundDecimals(2)?.toFloat(),
            no2 = airQuality.nO2?.roundDecimals(2)?.toFloat(),
            o3 = airQuality.o3?.roundDecimals(2)?.toFloat(),
            pm10 = airQuality.pM10?.roundDecimals(2)?.toFloat(),
            pm25 = airQuality.pM25?.roundDecimals(2)?.toFloat(),
            so2 = airQuality.sO2?.roundDecimals(2)?.toFloat(),
            coAqi = airQuality.getIndex(PollutantIndex.CO),
            no2Aqi = airQuality.getIndex(PollutantIndex.NO2),
            o3Aqi = airQuality.getIndex(PollutantIndex.O3),
            pm10Aqi = airQuality.getIndex(PollutantIndex.PM10),
            pm25Aqi = airQuality.getIndex(PollutantIndex.PM25),
            so2Aqi = airQuality.getIndex(PollutantIndex.SO2),
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
                    SpeedUnit.KPH.convertUnit(it)
                }?.roundDecimals(1)?.toFloat(),
                windDirection = hour.wind?.degree?.roundToInt(),
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

    companion object {
        fun Double.roundCelsiusToKelvin(): Int {
            return TemperatureUnit.K.convertUnit(this).roundToInt()
        }
    }
}
