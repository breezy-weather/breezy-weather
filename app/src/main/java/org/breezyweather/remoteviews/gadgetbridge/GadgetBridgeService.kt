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
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.Hourly
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.extensions.roundDecimals
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
            currentTemp = current?.temperature?.temperature?.roundCelsiusToKelvin(),
            currentConditionCode = getWeatherCode(current?.weatherCode),
            currentCondition = current?.weatherText,
            currentHumidity = current?.relativeHumidity?.roundToInt(),
            windSpeed = current?.wind?.speed?.let {
                SpeedUnit.KPH.convertUnit(it)
            }?.roundDecimals(1),
            windDirection = current?.wind?.degree?.roundToInt(),
            uvIndex = current?.uV?.index?.roundDecimals(1),

            todayMaxTemp = today?.day?.temperature?.temperature?.roundCelsiusToKelvin(),
            todayMinTemp = today?.night?.temperature?.temperature?.roundCelsiusToKelvin(),
            feelsLikeTemp = current?.temperature?.feelsLikeTemperature?.roundCelsiusToKelvin(),
            precipProbability = today?.day?.precipitationProbability?.total?.roundToInt(),

            dewPoint = current?.dewPoint?.roundCelsiusToKelvin(),
            pressure = current?.pressure?.roundDecimals(1),
            cloudCover = current?.cloudCover,
            visibility = current?.visibility?.roundDecimals(1),

            sunRise = today?.sun?.riseDate?.time?.div(1000)?.toInt(),
            sunSet = today?.sun?.setDate?.time?.div(1000)?.toInt(),
            moonRise = today?.moon?.riseDate?.time?.div(1000)?.toInt(),
            moonSet = today?.moon?.setDate?.time?.div(1000)?.toInt(),
            moonPhase = today?.moonPhase?.angle,

            latitude = location.latitude,
            longitude = location.longitude,
            isCurrentLocation = if (location.isCurrentPosition) 1 else 0,

            airQuality = getAirQuality(current?.airQuality),

            forecasts = getDailyForecasts(location.weather?.dailyForecastStartingToday),
            hourly = getHourlyForecasts(location.weather?.nextHourlyForecast),
        )
    }

    private fun getDailyForecasts(dailyForecast: List<Daily>?): List<GadgetBridgeDailyForecast>? {
        if (dailyForecast.isNullOrEmpty() || dailyForecast.size < 2) return null

        return dailyForecast.slice(1 until dailyForecast.size).map { day ->
            GadgetBridgeDailyForecast(
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

    private fun getAirQuality(airQuality: AirQuality?): GadgetBridgeAirQuality? {
        if (airQuality == null) return null
        val aqi = airQuality.getIndex() ?: return null

        return GadgetBridgeAirQuality(
            aqi = aqi,
            co = airQuality.cO?.roundDecimals(2),
            no2 = airQuality.nO2?.roundDecimals(2),
            o3 = airQuality.o3?.roundDecimals(2),
            pm10 = airQuality.pM10?.roundDecimals(2),
            pm25 = airQuality.pM25?.roundDecimals(2),
            so2 = airQuality.sO2?.roundDecimals(2),
            coAqi = airQuality.getIndex(PollutantIndex.CO),
            no2Aqi = airQuality.getIndex(PollutantIndex.NO2),
            o3Aqi = airQuality.getIndex(PollutantIndex.O3),
            pm10Aqi = airQuality.getIndex(PollutantIndex.PM10),
            pm25Aqi = airQuality.getIndex(PollutantIndex.PM25),
            so2Aqi = airQuality.getIndex(PollutantIndex.SO2),
        )
    }

    private fun getHourlyForecasts(dailyForecast: List<Hourly>?): List<GadgetBridgeHourlyForecast>? {
        if (dailyForecast.isNullOrEmpty()) return null

        return dailyForecast.map { hour ->
            GadgetBridgeHourlyForecast(
                timestamp = hour.date.time.div(1000).toInt(),
                temp = hour.temperature?.temperature?.roundCelsiusToKelvin(),
                conditionCode = getWeatherCode(hour.weatherCode),
                humidity = hour.relativeHumidity?.roundToInt(),
                windSpeed = hour.wind?.speed?.let {
                    SpeedUnit.KPH.convertUnit(it)
                }?.roundDecimals(1),
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
}

internal fun Float.roundCelsiusToKelvin(): Int {
    return TemperatureUnit.K.convertUnit(this).roundToInt()
}