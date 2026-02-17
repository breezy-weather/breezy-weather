/*
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

package org.breezyweather.common.options.appearance

import android.content.Context
import androidx.annotation.StringRes
import org.breezyweather.R
import org.breezyweather.common.options.BaseEnum

enum class DailyTrendDisplay(
    override val id: String,
    @StringRes val nameId: Int,
) : BaseEnum {

    TAG_TEMPERATURE("temperature", R.string.conditions),
    TAG_AIR_QUALITY("air_quality", R.string.air_quality),
    TAG_WIND("wind", R.string.wind),
    TAG_UV_INDEX("uv_index", R.string.uv_index),
    TAG_PRECIPITATION("precipitation", R.string.precipitation),
    TAG_SUNSHINE("sunshine", R.string.sunshine),
    TAG_FEELS_LIKE("feels_like", R.string.temperature_feels_like),
    ;

    companion object {

        fun toDailyTrendDisplayList(
            value: String?,
        ) = if (value.isNullOrEmpty()) {
            entries.toMutableList()
        } else {
            try {
                value.split("&").toTypedArray().mapNotNull { cardId ->
                    entries.firstOrNull { it.id == cardId }
                }
            } catch (e: Exception) {
                entries.toMutableList()
            }
        }

        fun toValue(list: List<DailyTrendDisplay>): String {
            return list.joinToString("&") { item ->
                item.id
            }
        }

        fun getSummary(context: Context, list: List<DailyTrendDisplay>): String {
            return list.joinToString(context.getString(org.breezyweather.unit.R.string.locale_separator)) { item ->
                item.getName(context)
            }
        }
    }

    override val valueArrayId = 0
    override val nameArrayId = 0

    override fun getName(context: Context) = context.getString(nameId)
}
