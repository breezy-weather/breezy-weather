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

enum class CardDisplay(
    override val id: String,
    @StringRes private val nameId: Int,
) : BaseEnum {

    CARD_PRECIPITATION_NOWCAST("precipitation_nowcast", R.string.precipitation_nowcasting),
    CARD_DAILY_OVERVIEW("daily_overview", R.string.daily_forecast),
    CARD_HOURLY_OVERVIEW("hourly_overview", R.string.hourly_forecast),
    CARD_AIR_QUALITY("air_quality", R.string.air_quality),
    CARD_POLLEN("pollen", R.string.pollen),
    CARD_SUNRISE_SUNSET("sunrise_sunset", R.string.ephemeris),
    CARD_LIVE("live", R.string.details),
    ;

    companion object {

        fun toCardDisplayList(
            value: String?,
        ) = if (value.isNullOrEmpty()) {
            mutableListOf()
        } else {
            try {
                val cards = value.split("&").toTypedArray()
                val list = mutableListOf<CardDisplay>()
                for (card in cards) {
                    when (card) {
                        "precipitation_nowcast" -> list.add(CARD_PRECIPITATION_NOWCAST)
                        "daily_overview" -> list.add(CARD_DAILY_OVERVIEW)
                        "hourly_overview" -> list.add(CARD_HOURLY_OVERVIEW)
                        "air_quality" -> list.add(CARD_AIR_QUALITY)
                        "allergen", "pollen" -> list.add(CARD_POLLEN)
                        "sunrise_sunset" -> list.add(CARD_SUNRISE_SUNSET)
                        "live" -> list.add(CARD_LIVE)
                    }
                }

                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun toValue(list: List<CardDisplay>): String {
            val builder = StringBuilder()
            for (v in list) {
                builder.append("&").append(v.id)
            }
            if (builder.isNotEmpty() && builder[0] == '&') {
                builder.deleteCharAt(0)
            }
            return builder.toString()
        }

        fun getSummary(context: Context, list: List<CardDisplay>): String {
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
