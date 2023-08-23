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
import android.os.Build
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.remoteviews.gadgetbridge.json.GadgetBridgeDailyForecast
import org.breezyweather.remoteviews.gadgetbridge.json.GadgetBridgeData
import org.breezyweather.settings.SettingsManager
import kotlin.math.roundToInt


object GadgetBridgeService {
    private const val GB_INTENT_EXTRA = "WeatherJson"
    private const val GB_INTENT_PACKAGE = "nodomain.freeyourgadget.gadgetbridge"
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
        return try {
            val flag = PackageManager.MATCH_DEFAULT_ONLY
            val intent = Intent(GB_INTENT_ACTION).setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(flag.toLong())
                )
            } else {
                context.packageManager.queryIntentActivities(
                    intent,
                    flag
                )
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
            precipProbability = today?.day?.precipitationProbability?.total?.roundToInt(),

            forecasts = getDailyForecasts(location.weather?.dailyForecastStartingToday)
        )
    }

    private fun getDailyForecasts(dailyForecast: List<Daily>?): List<GadgetBridgeDailyForecast>? {
        if (dailyForecast.isNullOrEmpty() || dailyForecast.size < 2) return null

        return dailyForecast.slice(1 until dailyForecast.size).map { day ->
            GadgetBridgeDailyForecast(
                conditionCode = getWeatherCode(day.day?.weatherCode),
                maxTemp = getTemp(day.day?.temperature),
                minTemp = getTemp(day.night?.temperature)
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