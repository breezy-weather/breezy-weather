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
    val temperature: Float? = null,
    val realFeelTemperature: Float? = null,
    val realFeelShaderTemperature: Float? = null,
    val apparentTemperature: Float? = null,
    val windChillTemperature: Float? = null,
    val wetBulbTemperature: Float? = null
) : Serializable {

    companion object {
        fun getTemperature(context: Context, temperature: Float?, unit: TemperatureUnit, decimals: Int = 1): String? {
            return temperature?.let { unit.getValueText(context, it, decimals) }
        }

        fun getShortTemperature(context: Context, temperature: Float?, unit: TemperatureUnit): String? {
            return temperature?.let { unit.getShortValueText(context, it) }
        }

        fun getTrendTemperature(context: Context, night: Float?, day: Float?, unit: TemperatureUnit): String? {
            return getTrendTemperature(
                context, night, day, unit,
                SettingsManager.getInstance(context).isDayNightTempOrderReversed
            )
        }

        fun getTrendTemperature(context: Context, night: Float?, day: Float?, unit: TemperatureUnit, switchDayNight: Boolean): String? {
            if (night == null || day == null) {
                return null
            }
            return if (switchDayNight) {
                getShortTemperature(context, day, unit) + "/" + getShortTemperature(context, night, unit)
            } else {
                getShortTemperature(context, night, unit) + "/" + getShortTemperature(context, day, unit)
            }
        }
    }

    val feelsLikeTemperature: Float? = realFeelTemperature ?: realFeelShaderTemperature
        ?: apparentTemperature ?: windChillTemperature ?: wetBulbTemperature

    fun getTemperature(context: Context, unit: TemperatureUnit, decimals: Int = 1): String? {
        return getTemperature(context, temperature, unit, decimals)
    }

    fun getShortTemperature(context: Context, unit: TemperatureUnit): String? {
        return getShortTemperature(context, temperature, unit)
    }

    fun getFeelsLikeTemperature(context: Context, unit: TemperatureUnit, decimals: Int = 1): String? {
        return getTemperature(context, feelsLikeTemperature, unit, decimals)
    }

    fun getShortFeelsLikeTemperature(context: Context, unit: TemperatureUnit): String? {
        return getShortTemperature(context, feelsLikeTemperature, unit)
    }
}
