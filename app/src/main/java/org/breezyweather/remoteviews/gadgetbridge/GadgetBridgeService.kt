/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.remoteviews.gadgetbridge

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.index.PollutantIndex
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.Hourly
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.remoteviews.gadgetbridge.json.GadgetBridgeAirQuality
import org.breezyweather.remoteviews.gadgetbridge.json.GadgetBridgeDailyForecast
import org.breezyweather.remoteviews.gadgetbridge.json.GadgetBridgeData
import org.breezyweather.remoteviews.gadgetbridge.json.GadgetBridgeHourlyForecast
import org.breezyweather.settings.SettingsManager
import kotlin.math.roundToInt


object GadgetBridgeService {
    private const val GB_INTENT_EXTRA = "WeatherJson"
    private const val GB_INTENT_PACKAGE = "nodomain.freeyourgadget.gadgetbridge"
    private const val GB_INTENT_PACKAGE_NIGHTLY = "$GB_INTENT_PACKAGE.nightly"
    private const val GB_INTENT_PACKAGE_NIGHTLY_NOPEBBLE = "$GB_INTENT_PACKAGE.nightly_nopebble"
    private const val GB_INTENT_PACKAGE_BANGLEJS = "com.espruino.gadgetbridge.banglejs"
    private const val GB_INTENT_PACKAGE_BANGLEJS_NIGHTLY = "$GB_INTENT_PACKAGE_BANGLEJS.nightly"
    private const val GB_INTENT_ACTION = "$GB_INTENT_PACKAGE.ACTION_GENERIC_WEATHER"

    fun sendWeatherBroadcast(context: Context, location: Location) {
        if (location.weather?.current == null) {
            LogHelper.log(msg = "Not sending GadgetBridge data, current weather is null")
            return
        }

        val weatherData = getWeatherData(context, location)
        val encoded = Json.encodeToString(weatherData)

        val intent = Intent(GB_INTENT_ACTION)
            .putExtra(GB_INTENT_EXTRA, encoded)
            .setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)

        context.sendBroadcast(intent)
    }

    fun isEnabled(context: Context): Boolean {
        return SettingsManager.getInstance(context).isGadgetBridgeSupportEnabled &&
                isAvailable(context)
    }

    fun isAvailable(context: Context): Boolean {
        val pm = context.packageManager
        return isPackageAvailable(pm, GB_INTENT_PACKAGE) ||
                isPackageAvailable(pm, GB_INTENT_PACKAGE_NIGHTLY) ||
                isPackageAvailable(pm, GB_INTENT_PACKAGE_NIGHTLY_NOPEBBLE) ||
                isPackageAvailable(pm, GB_INTENT_PACKAGE_BANGLEJS) ||
                isPackageAvailable(pm, GB_INTENT_PACKAGE_BANGLEJS_NIGHTLY)
    }

    private fun isPackageAvailable(pm: PackageManager, name: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(name, PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                pm.getPackageInfo(name, PackageManager.GET_META_DATA)
            }
            true
        } catch (exc: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getWeatherData(
        context: Context,
        location: Location
    ): GadgetBridgeData {
        val current = location.weather?.current
        val today = location.weather?.today

        return GadgetBridgeData(
            timestamp = location.weather?.base?.mainUpdateTime?.time?.div(1000)?.toInt(),
            location = location.getPlace(context),
            currentTemp = getTemp(current?.temperature),
            currentConditionCode = getWeatherCode(current?.weatherCode),
            currentCondition = current?.weatherText,
            currentHumidity = current?.relativeHumidity?.roundToInt(),
            windSpeed = current?.wind?.speed?.let {
                SpeedUnit.KPH.convertUnit(it)
            },
            windDirection = current?.wind?.degree?.roundToInt(),
            uvIndex = current?.uV?.index,

            todayMaxTemp = getTemp(today?.day?.temperature),
            todayMinTemp = getTemp(today?.night?.temperature),
            feelsLikeTemp = current?.temperature?.feelsLikeTemperature?.let { TemperatureUnit.K.convertUnit(it).roundToInt() },
            precipProbability = today?.day?.precipitationProbability?.total?.roundToInt(),

            dewPoint = (current?.dewPoint?.plus(273.15))?.roundToInt(),
            pressure = current?.pressure,
            cloudCover = current?.cloudCover,
            visibility = current?.visibility,

            sunRise = today?.sun?.riseDate?.time?.div(1000)?.toInt(),
            sunSet = today?.sun?.setDate?.time?.div(1000)?.toInt(),
            moonRise = today?.moon?.riseDate?.time?.div(1000)?.toInt(),
            moonSet = today?.moon?.setDate?.time?.div(1000)?.toInt(),
            moonPhase = today?.moonPhase?.angle,

            latitude = location.latitude,
            longitude = location.longitude,
            isCurrentLocation = if (location.isCurrentPosition) 1 else 0,

            airQuality = GadgetBridgeAirQuality(
                aqi = current?.airQuality?.getIndex(null),
                co = current?.airQuality?.cO,
                no2 = current?.airQuality?.nO2,
                o3 = current?.airQuality?.o3,
                pm10 = current?.airQuality?.pM10,
                pm25 = current?.airQuality?.pM25,
                so2 = current?.airQuality?.sO2,
                coAqi = current?.airQuality?.getIndex(PollutantIndex.CO),
                no2Aqi = current?.airQuality?.getIndex(PollutantIndex.NO2),
                o3Aqi = current?.airQuality?.getIndex(PollutantIndex.O3),
                pm10Aqi = current?.airQuality?.getIndex(PollutantIndex.PM10),
                pm25Aqi = current?.airQuality?.getIndex(PollutantIndex.PM25),
                so2Aqi = current?.airQuality?.getIndex(PollutantIndex.SO2),
            ),

            forecasts = getDailyForecasts(location.weather?.dailyForecastStartingToday),
            hourly = getHourlyForecasts(location.weather?.nextHourlyForecast),
        )
    }

    private fun getDailyForecasts(dailyForecast: List<Daily>?): List<GadgetBridgeDailyForecast>? {
        if (dailyForecast.isNullOrEmpty()) return null

        return dailyForecast.slice(1 until dailyForecast.size).map { day ->
            GadgetBridgeDailyForecast(
                conditionCode = getWeatherCode(day.day?.weatherCode),
                maxTemp = getTemp(day.day?.temperature),
                minTemp = getTemp(day.night?.temperature),

                sunRise = day.sun?.riseDate?.time?.div(1000)?.toInt(),
                sunSet = day.sun?.setDate?.time?.div(1000)?.toInt(),
                moonRise = day.moon?.riseDate?.time?.div(1000)?.toInt(),
                moonSet = day.moon?.setDate?.time?.div(1000)?.toInt(),
                moonPhase = day.moonPhase?.angle,

                airQuality = GadgetBridgeAirQuality(
                    aqi = day.airQuality?.getIndex(null),
                    co = day.airQuality?.cO,
                    no2 = day.airQuality?.nO2,
                    o3 = day.airQuality?.o3,
                    pm10 = day.airQuality?.pM10,
                    pm25 = day.airQuality?.pM25,
                    so2 = day.airQuality?.sO2,
                    coAqi = day.airQuality?.getIndex(PollutantIndex.CO),
                    no2Aqi = day.airQuality?.getIndex(PollutantIndex.NO2),
                    o3Aqi = day.airQuality?.getIndex(PollutantIndex.O3),
                    pm10Aqi = day.airQuality?.getIndex(PollutantIndex.PM10),
                    pm25Aqi = day.airQuality?.getIndex(PollutantIndex.PM25),
                    so2Aqi = day.airQuality?.getIndex(PollutantIndex.SO2),
                ),
            )
        }
    }

    private fun getHourlyForecasts(dailyForecast: List<Hourly>?): List<GadgetBridgeHourlyForecast>? {
        if (dailyForecast.isNullOrEmpty()) return null

        return dailyForecast.map { hour ->
            GadgetBridgeHourlyForecast(
                timestamp = hour.date.time.div(1000).toInt(),
                temp = getTemp(hour.temperature),
                conditionCode = getWeatherCode(hour.weatherCode),
                humidity = hour.relativeHumidity?.roundToInt(),
                windSpeed = hour.wind?.speed,
                windDirection = hour.wind?.degree?.roundToInt(),
            )
        }
    }


    private fun getTemp(temp: Temperature?): Int? {
        return temp?.temperature?.let { TemperatureUnit.K.convertUnit(it).roundToInt() }
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
}