package org.breezyweather.common.basic.models.weather

import android.content.Context
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.settings.SettingsManager
import java.io.Serializable

/**
 * Temperature.
 * default unit : [TemperatureUnit.C]
 */
class Temperature(
    val temperature: Int? = null,
    val realFeelTemperature: Int? = null,
    val realFeelShaderTemperature: Int? = null,
    val apparentTemperature: Int? = null,
    val windChillTemperature: Int? = null,
    val wetBulbTemperature: Int? = null,
    val degreeDayTemperature: Int? = null
) : Serializable {

    companion object {
        fun getTemperature(context: Context, temperature: Int?, unit: TemperatureUnit): String? {
            return if (temperature == null) null else unit.getValueText(context, temperature)
        }

        @JvmStatic
        fun getShortTemperature(context: Context, temperature: Int?, unit: TemperatureUnit): String? {
            return if (temperature == null) null else unit.getShortValueText(context, temperature)
        }

        @JvmStatic
        fun getTrendTemperature(context: Context, night: Int?, day: Int?, unit: TemperatureUnit): String? {
            return getTrendTemperature(
                context, night, day, unit,
                SettingsManager.getInstance(context).isDayNightTempOrderReversed
            )
        }

        fun getTrendTemperature(context: Context, night: Int?, day: Int?, unit: TemperatureUnit, switchDayNight: Boolean): String? {
            if (night == null || day == null) {
                return null
            }
            return if (switchDayNight) {
                getShortTemperature(context, day, unit) + "/" +
                        getShortTemperature(context, night, unit)
            } else {
                getShortTemperature(context, night, unit) + "/" +
                        getShortTemperature(context, day, unit)
            }
        }
    }

    val feelsLikeTemperature: Int? = realFeelTemperature ?: realFeelShaderTemperature ?: apparentTemperature
        ?: windChillTemperature ?: wetBulbTemperature ?: degreeDayTemperature

    fun getTemperature(context: Context, unit: TemperatureUnit): String? {
        return getTemperature(context, temperature, unit)
    }

    fun getShortTemperature(context: Context, unit: TemperatureUnit): String? {
        return getShortTemperature(context, temperature, unit)
    }

    fun getFeelsLikeTemperature(context: Context, unit: TemperatureUnit): String? {
        return getTemperature(context, feelsLikeTemperature, unit)
    }

    fun getShortFeelsLikeTemperature(context: Context, unit: TemperatureUnit): String? {
        return getShortTemperature(context, feelsLikeTemperature, unit)
    }
}
