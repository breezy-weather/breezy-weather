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
import androidx.annotation.StringRes
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.BaseEnum

enum class HourlyTrendDisplay(
    override val id: String,
    @StringRes val nameId: Int,
) : BaseEnum {

    TAG_TEMPERATURE("temperature", R.string.temperature),
    TAG_AIR_QUALITY("air_quality", R.string.air_quality),
    TAG_WIND("wind", R.string.wind),
    TAG_UV_INDEX("uv_index", R.string.uv_index),
    TAG_PRECIPITATION("precipitation", R.string.precipitation),
    TAG_FEELS_LIKE("feels_like", R.string.temperature_feels_like),
    TAG_HUMIDITY("humidity", R.string.humidity_dew_point),
    TAG_PRESSURE("pressure", R.string.pressure),
    TAG_CLOUD_COVER("cloud_cover", R.string.cloud_cover),
    TAG_VISIBILITY("visibility", R.string.visibility),
    ;

    companion object {

        fun toHourlyTrendDisplayList(
            value: String?,
        ) = if (value.isNullOrEmpty()) {
            mutableListOf()
        } else {
            try {
                val cards = value.split("&").toTypedArray()
                val list = mutableListOf<HourlyTrendDisplay>()
                for (card in cards) {
                    when (card) {
                        "temperature" -> list.add(TAG_TEMPERATURE)
                        "air_quality" -> list.add(TAG_AIR_QUALITY)
                        "wind" -> list.add(TAG_WIND)
                        "uv_index" -> list.add(TAG_UV_INDEX)
                        "precipitation" -> list.add(TAG_PRECIPITATION)
                        "feels_like" -> list.add(TAG_FEELS_LIKE)
                        "humidity" -> list.add(TAG_HUMIDITY)
                        "pressure" -> list.add(TAG_PRESSURE)
                        "cloud_cover" -> list.add(TAG_CLOUD_COVER)
                        "visibility" -> list.add(TAG_VISIBILITY)
                    }
                }
                list
            } catch (e: Exception) {
                mutableListOf()
            }
        }

        fun toValue(list: List<HourlyTrendDisplay>): String {
            val builder = StringBuilder()
            for (v in list) {
                builder.append("&").append(v.id)
            }
            if (builder.isNotEmpty() && builder[0] == '&') {
                builder.deleteCharAt(0)
            }
            return builder.toString()
        }

        fun getSummary(context: Context, list: List<HourlyTrendDisplay>): String {
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
