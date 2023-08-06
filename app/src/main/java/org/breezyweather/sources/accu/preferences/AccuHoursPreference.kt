/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.accu.preferences

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options._basic.Utils

enum class AccuHoursPreference(
    override val id: String
): BaseEnum {

    ONE("1"),
    TWELVE("12"),
    TWENTY_FOUR("24"),
    SEVENTY_TWO("72"),
    HUNDRED_TWENTY("120"),
    TWO_HUNDRED_FORTY("240");

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "1" -> ONE
            "12" -> TWELVE
            "24" -> TWENTY_FOUR
            "72" -> SEVENTY_TWO
            "120" -> HUNDRED_TWENTY
            else -> TWO_HUNDRED_FORTY
        }
    }

    override val valueArrayId = R.array.accu_preference_hour_values
    override val nameArrayId = R.array.accu_preference_hours

    override fun getName(context: Context) = Utils.getName(context, this)
}