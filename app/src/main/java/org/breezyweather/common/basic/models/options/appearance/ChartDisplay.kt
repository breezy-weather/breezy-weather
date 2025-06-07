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
import breezyweather.domain.weather.model.Hourly
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.BaseEnum

enum class ChartDisplay(
    override val id: String,
    @StringRes val nameId: Int,
    @DrawableRes val iconId: Int,
) : BaseEnum {

    TAG_CONDITIONS("conditions", R.string.temperature, R.drawable.ic_device_thermostat),
    TAG_PRECIPITATION("precipitation", R.string.precipitation, R.drawable.ic_precipitation),
    TAG_WIND("wind", R.string.wind, R.drawable.ic_wind),
    TAG_AIR_QUALITY("air_quality", R.string.air_quality, R.drawable.weather_haze_mini_xml),
    TAG_POLLEN("pollen", R.string.pollen, R.drawable.ic_allergy),
    TAG_UV_INDEX("uv_index", R.string.uv_index, R.drawable.ic_uv),
    TAG_HUMIDITY("humidity", R.string.humidity_dew_point, R.drawable.ic_humidity_percentage),
    TAG_PRESSURE("pressure", R.string.pressure, R.drawable.ic_gauge),
    TAG_CLOUD_COVER("cloud_cover", R.string.cloud_cover, R.drawable.ic_cloud),
    TAG_VISIBILITY("visibility", R.string.visibility, R.drawable.ic_eye),
    TAG_SUN_MOON("sun_moon", R.string.ephemeris, R.drawable.weather_clear_night_mini_xml),
    TAG_OTHER_DETAILS("other_details", R.string.other_details, R.drawable.ic_about), // TODO: Temporary, to be removed
    ;

    /**
     * Returns true if the hourly list in parameter has enough data for a chart
     */
    fun isValidForChart(hourlyList: List<Hourly>): Boolean {
        return when (this) {
            TAG_CONDITIONS -> hourlyList.filter { it.temperature?.temperature != null }.size >= 4
            TAG_PRECIPITATION -> hourlyList.filter { it.precipitation?.total != null }.size >= 4
            TAG_WIND -> hourlyList.filter { it.wind?.speed != null }.size >= 4
            TAG_AIR_QUALITY -> hourlyList.filter { it.airQuality?.isIndexValid == true }.size >= 4
            TAG_POLLEN -> true
            TAG_UV_INDEX -> hourlyList.filter { it.uV?.isValid == true }.size >= 4
            TAG_HUMIDITY -> hourlyList.filter { it.relativeHumidity != null }.size >= 4
            TAG_PRESSURE -> hourlyList.filter { it.pressure != null }.size >= 4
            TAG_CLOUD_COVER -> hourlyList.filter { it.cloudCover != null }.size >= 4
            TAG_VISIBILITY -> hourlyList.filter { it.visibility != null }.size >= 4
            TAG_SUN_MOON -> true
            TAG_OTHER_DETAILS -> true // TODO?
        }
    }

    companion object {

        fun toChartDisplayList(
            value: String?,
        ) = if (value.isNullOrEmpty()) {
            mutableListOf()
        } else {
            try {
                val cards = value.split("&").toTypedArray()
                val list = mutableListOf<ChartDisplay>()
                for (card in cards) {
                    when (card) {
                        "conditions", "temperature", "feels_like" -> list.add(TAG_CONDITIONS)
                        "precipitation" -> list.add(TAG_PRECIPITATION)
                        "wind" -> list.add(TAG_WIND)
                        "air_quality" -> list.add(TAG_AIR_QUALITY)
                        "pollen" -> list.add(TAG_POLLEN)
                        "uv_index" -> list.add(TAG_UV_INDEX)
                        "humidity" -> list.add(TAG_HUMIDITY)
                        "pressure" -> list.add(TAG_PRESSURE)
                        "cloud_cover" -> list.add(TAG_CLOUD_COVER)
                        "visibility" -> list.add(TAG_VISIBILITY)
                        "sun_moon" -> list.add(TAG_SUN_MOON)
                        "other_details" -> list.add(TAG_OTHER_DETAILS)
                    }
                }
                list
            } catch (e: Exception) {
                mutableListOf()
            }
        }

        fun toValue(list: List<ChartDisplay>): String {
            val builder = StringBuilder()
            for (v in list) {
                builder.append("&").append(v.id)
            }
            if (builder.isNotEmpty() && builder[0] == '&') {
                builder.deleteCharAt(0)
            }
            return builder.toString()
        }

        fun getSummary(context: Context, list: List<ChartDisplay>): String {
            val builder = StringBuilder()
            for (item in list) {
                builder.append(",").append(item.getName(context))
            }
            if (builder.isNotEmpty() && builder[0] == ',') {
                builder.deleteCharAt(0)
            }
            return builder.toString().replace(",", context.getString(R.string.comma_separator))
        }
    }

    override val valueArrayId = 0
    override val nameArrayId = 0

    override fun getName(context: Context) = context.getString(nameId)
}
