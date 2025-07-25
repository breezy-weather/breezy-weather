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

    TAG_TEMPERATURE("temperature", R.string.conditions),
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
            HourlyTrendDisplay.entries.toMutableList()
        } else {
            try {
                value.split("&").toTypedArray().mapNotNull { cardId ->
                    HourlyTrendDisplay.entries.firstOrNull { it.id == cardId }
                }
            } catch (e: Exception) {
                HourlyTrendDisplay.entries.toMutableList()
            }
        }

        fun toValue(list: List<HourlyTrendDisplay>): String {
            return list.joinToString("&") { item ->
                item.id
            }
        }

        fun getSummary(context: Context, list: List<HourlyTrendDisplay>): String {
            return list.joinToString(context.getString(R.string.comma_separator)) { item ->
                item.getName(context)
            }
        }
    }

    override val valueArrayId = 0
    override val nameArrayId = 0

    override fun getName(context: Context) = context.getString(nameId)
}
