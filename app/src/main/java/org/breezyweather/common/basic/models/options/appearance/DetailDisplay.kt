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

package org.breezyweather.common.basic.models.options.appearance

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import breezyweather.domain.weather.model.Current
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.BaseEnum
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getContentDescription
import org.breezyweather.domain.weather.model.getShortDescription

enum class DetailDisplay(
    override val id: String,
    @StringRes private val nameId: Int,
    @StringRes private val nameShortId: Int,
    @DrawableRes val iconId: Int,
) : BaseEnum {

    DETAIL_FEELS_LIKE(
        "feels_like",
        R.string.temperature_feels_like,
        R.string.temperature_feels_like_short,
        R.drawable.ic_device_thermostat
    ),
    DETAIL_WIND("wind", R.string.wind, R.string.wind_short, R.drawable.ic_wind),
    DETAIL_UV_INDEX("uv_index", R.string.uv_index, R.string.uv_index_short, R.drawable.ic_uv),
    DETAIL_HUMIDITY("humidity", R.string.humidity, R.string.humidity_short, R.drawable.ic_humidity_percentage),
    DETAIL_DEW_POINT("dew_point", R.string.dew_point, R.string.dew_point_short, R.drawable.ic_dew_point),
    DETAIL_PRESSURE("pressure", R.string.pressure, R.string.pressure_short, R.drawable.ic_gauge),
    DETAIL_VISIBILITY("visibility", R.string.visibility, R.string.visibility_short, R.drawable.ic_eye),
    DETAIL_CLOUD_COVER("cloud_cover", R.string.cloud_cover, R.string.cloud_cover_short, R.drawable.ic_cloud),
    DETAIL_CEILING("ceiling", R.string.ceiling, R.string.ceiling_short, R.drawable.ic_top),
    ;

    companion object {

        fun toDetailDisplayList(
            value: String?,
        ) = if (value.isNullOrEmpty()) {
            mutableListOf()
        } else {
            try {
                val details = value.split("&").toTypedArray()
                val list = mutableListOf<DetailDisplay>()
                for (detail in details) {
                    when (detail) {
                        "feels_like" -> list.add(DETAIL_FEELS_LIKE)
                        "wind" -> list.add(DETAIL_WIND)
                        "uv_index" -> list.add(DETAIL_UV_INDEX)
                        "humidity" -> list.add(DETAIL_HUMIDITY)
                        "dew_point" -> list.add(DETAIL_DEW_POINT)
                        "pressure" -> list.add(DETAIL_PRESSURE)
                        "visibility" -> list.add(DETAIL_VISIBILITY)
                        "cloud_cover" -> list.add(DETAIL_CLOUD_COVER)
                        "ceiling" -> list.add(DETAIL_CEILING)
                    }
                }

                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun toDetailDisplayUnlisted(
            value: String?,
        ) = if (value.isNullOrEmpty()) {
            DetailDisplay.entries.toMutableList()
        } else {
            try {
                val list = DetailDisplay.entries.toMutableList()
                val details = value.split("&").toTypedArray()
                for (detail in details) {
                    when (detail) {
                        "feels_like" -> list.remove(DETAIL_FEELS_LIKE)
                        "wind" -> list.remove(DETAIL_WIND)
                        "uv_index" -> list.remove(DETAIL_UV_INDEX)
                        "humidity" -> list.remove(DETAIL_HUMIDITY)
                        "dew_point" -> list.remove(DETAIL_DEW_POINT)
                        "pressure" -> list.remove(DETAIL_PRESSURE)
                        "visibility" -> list.remove(DETAIL_VISIBILITY)
                        "cloud_cover" -> list.remove(DETAIL_CLOUD_COVER)
                        "ceiling" -> list.remove(DETAIL_CEILING)
                    }
                }

                list
            } catch (e: Exception) {
                DetailDisplay.entries.toMutableList()
            }
        }

        fun toValue(list: List<DetailDisplay>): String {
            return list.joinToString("&") { item ->
                item.id
            }
        }

        fun getSummary(context: Context, list: List<DetailDisplay>): String {
            return list.joinToString(context.getString(R.string.comma_separator)) { item ->
                item.getName(context)
            }
        }
    }

    override val valueArrayId = 0
    override val nameArrayId = 0

    override fun getName(context: Context) = context.getString(nameId)

    fun getShortName(context: Context) = context.getString(nameShortId)

    fun getCurrentValue(context: Context, current: Current, isDaylight: Boolean = true): String? = when (id) {
        "feels_like" -> current.temperature?.feelsLikeTemperature?.let {
            SettingsManager.getInstance(context).getTemperatureUnit(context).formatMeasure(context, it, 0)
        }
        "wind" -> if (!current.wind?.getShortDescription(
                context,
                SettingsManager.getInstance(context).getSpeedUnit(context)
            ).isNullOrEmpty()
        ) {
            current.wind?.getShortDescription(context, SettingsManager.getInstance(context).getSpeedUnit(context))
        } else {
            null
        }
        "uv_index" -> if (current.uV?.index != null && (isDaylight || current.uV!!.index!! > 0)) {
            current.uV!!.getShortDescription(context)
        } else {
            null
        }
        "humidity" -> current.relativeHumidity?.let {
            UnitUtils.formatPercent(context, it)
        }
        "dew_point" -> current.dewPoint?.let {
            SettingsManager.getInstance(context).getTemperatureUnit(context).formatMeasure(context, it, 0)
        }
        "pressure" -> current.pressure?.let {
            SettingsManager.getInstance(context).getPressureUnit(context).formatMeasure(context, it)
        }
        "visibility" -> current.visibility?.let {
            SettingsManager.getInstance(context).getDistanceUnit(context).formatMeasure(context, it)
        }
        "cloud_cover" -> current.cloudCover?.let {
            UnitUtils.formatPercent(context, it.toDouble())
        }
        "ceiling" -> current.ceiling?.let {
            SettingsManager.getInstance(context).getDistanceUnit(context).formatMeasure(context, it)
        }
        else -> null
    }

    fun getContentDescription(context: Context, current: Current, isDaylight: Boolean = true): String? = when (id) {
        "feels_like" -> current.temperature?.feelsLikeTemperature?.let {
            getName(context) +
                context.getString(R.string.colon_separator) +
                SettingsManager.getInstance(context).getTemperatureUnit(context).formatContentDescription(context, it)
        }
        "wind" -> if (!current.wind?.getContentDescription(
                context,
                SettingsManager.getInstance(context).getSpeedUnit(context)
            ).isNullOrEmpty()
        ) {
            current.wind!!.getContentDescription(context, SettingsManager.getInstance(context).getSpeedUnit(context))
        } else {
            null
        }
        "uv_index" -> if (current.uV?.index != null && (isDaylight || current.uV!!.index!! > 0)) {
            getName(context) +
                context.getString(R.string.colon_separator) +
                current.uV!!.getContentDescription(context)
        } else {
            null
        }
        "humidity" -> current.relativeHumidity?.let {
            getName(context) +
                context.getString(R.string.colon_separator) +
                UnitUtils.formatPercent(context, it)
        }
        "dew_point" -> current.dewPoint?.let {
            getName(context) +
                context.getString(R.string.colon_separator) +
                SettingsManager.getInstance(context).getTemperatureUnit(context).formatContentDescription(context, it)
        }
        "pressure" -> current.pressure?.let {
            getName(context) +
                context.getString(R.string.colon_separator) +
                SettingsManager.getInstance(context).getPressureUnit(context).formatContentDescription(context, it)
        }
        "visibility" -> current.visibility?.let {
            getName(context) +
                context.getString(R.string.colon_separator) +
                SettingsManager.getInstance(context).getDistanceUnit(context).formatContentDescription(context, it)
        }
        "cloud_cover" -> current.cloudCover?.let {
            getName(context) +
                context.getString(R.string.colon_separator) +
                UnitUtils.formatPercent(context, it.toDouble())
        }
        "ceiling" -> current.ceiling?.let {
            getName(context) +
                context.getString(R.string.colon_separator) +
                SettingsManager.getInstance(context).getDistanceUnit(context).formatContentDescription(context, it)
        }
        else -> null
    }
}
