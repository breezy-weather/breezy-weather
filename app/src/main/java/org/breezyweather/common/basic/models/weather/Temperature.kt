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

package org.breezyweather.common.basic.models.weather

import android.content.Context
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import java.io.Serializable

/**
 * Temperature.
 * default unit : [TemperatureUnit.C]
 */
data class Temperature(
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
            if (night == null || day == null) {
                return null
            }
            return getShortTemperature(context, day, unit) + "/" + getShortTemperature(context, night, unit)
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
