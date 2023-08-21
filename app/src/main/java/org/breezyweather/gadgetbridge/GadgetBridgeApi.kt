package org.breezyweather.gadgetbridge

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.gadgetbridge.json.GadgetBridgeDailyForecast
import org.breezyweather.gadgetbridge.json.GadgetBridgeData
import org.breezyweather.settings.SettingsManager
import kotlin.math.roundToInt


object GadgetBridgeApi {
    private const val GB_INTENT_EXTRA = "WeatherJson"

    private const val GB_INTENT_PACKAGE = "nodomain.freeyourgadget.gadgetbridge"
    private const val GB_INTENT_PACKAGE_NIGHTLY = "$GB_INTENT_PACKAGE.nightly"
    private const val GB_INTENT_ACTION = "$GB_INTENT_PACKAGE.ACTION_GENERIC_WEATHER"

    private var GB_AVAILABLE = false

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
        GB_AVAILABLE = isPackageAvailable(pm, GB_INTENT_PACKAGE) ||
                isPackageAvailable(pm, GB_INTENT_PACKAGE_NIGHTLY)
        return GB_AVAILABLE
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
        val dailyForecasts = location.weather?.dailyForecast?.mapIndexed { index, day ->
            GadgetBridgeDailyForecast(
                conditionCode = getWeatherCode(day.day?.weatherCode),
                maxTemp = getTemp(day.day?.temperature),
                minTemp = getTemp(day.night?.temperature),
                // this is inefficient
                humidity = location.weather.hourlyForecast.firstOrNull { it.date.after(day.date) }
                    ?.relativeHumidity?.roundToInt()
            )
        }

        val current = location.weather?.current
        val today = location.weather?.today

        return GadgetBridgeData(
            timestamp = location.weather?.base?.mainUpdateTime?.time?.div(1000)?.toInt(),
            location = location.getPlace(context),
            currentTemp = getTemp(current?.temperature),
            currentConditionCode = getWeatherCode(current?.weatherCode),
            currentCondition = current?.weatherText,
            currentHumidity = current?.relativeHumidity?.roundToInt(),
            windSpeed = current?.wind?.speed,
            windDirection = current?.wind?.degree?.roundToInt(),
            uvIndex = current?.uV?.index,

            todayMaxTemp = getTemp(today?.day?.temperature),
            todayMinTemp = getTemp(today?.night?.temperature),
            precipProbability = today?.day?.precipitationProbability?.total?.times(100)
                ?.roundToInt(),

            forecasts = dailyForecasts
        )
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