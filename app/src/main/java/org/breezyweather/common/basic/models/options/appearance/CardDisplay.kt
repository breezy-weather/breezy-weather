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

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.BaseEnum
import org.breezyweather.common.utils.helpers.IntentHelper

enum class CardDisplay(
    override val id: String,
    @StringRes private val nameId: Int,
    val configure: ((Activity) -> Unit)? = null,
) : BaseEnum {

    CARD_NOWCAST("nowcast", R.string.precipitation_nowcasting),
    CARD_DAILY_FORECAST(
        "daily_forecast",
        R.string.daily_forecast,
        { activity -> IntentHelper.startDailyTrendDisplayManageActivity(activity) }
    ),
    CARD_HOURLY_FORECAST(
        "hourly_forecast",
        R.string.hourly_forecast,
        { activity -> IntentHelper.startHourlyTrendDisplayManageActivity(activity) }
    ),
    CARD_PRECIPITATION("precipitation", R.string.precipitation),
    CARD_WIND("wind", R.string.wind),
    CARD_AIR_QUALITY("air_quality", R.string.air_quality),
    CARD_POLLEN("pollen", R.string.pollen),
    CARD_HUMIDITY("humidity", R.string.humidity),
    CARD_UV("uv", R.string.uv_index),
    CARD_VISIBILITY("visibility", R.string.visibility),
    CARD_PRESSURE("pressure", R.string.pressure),
    CARD_SUN("sun", R.string.ephemeris_sun),
    CARD_MOON("moon", R.string.ephemeris_moon),
    CARD_CLOCK("clock", R.string.clock),
    ;

    companion object {

        fun toCardDisplayList(
            value: String?,
        ) = if (value.isNullOrEmpty()) {
            mutableListOf()
        } else {
            try {
                value.split("&").toTypedArray().mapNotNull { cardId ->
                    CardDisplay.entries.firstOrNull { it.id == cardId }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun toValue(list: List<CardDisplay>): String {
            return list.joinToString("&") { item ->
                item.id
            }
        }

        fun getSummary(context: Context, list: List<CardDisplay>): String {
            return list.joinToString(context.getString(R.string.comma_separator)) { item ->
                item.getName(context)
            }
        }
    }

    override val valueArrayId = 0
    override val nameArrayId = 0

    override fun getName(context: Context) = context.getString(nameId)
}
