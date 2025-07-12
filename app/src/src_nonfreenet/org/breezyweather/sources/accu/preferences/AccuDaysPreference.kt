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

package org.breezyweather.sources.accu.preferences

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.BaseEnum
import org.breezyweather.common.basic.models.options.basic.UnitUtils

enum class AccuDaysPreference(
    override val id: String,
) : BaseEnum {

    ONE("1"),
    FIVE("5"),
    TEN("10"),
    FIFTEEN("15"),
    ;

    companion object {

        fun getInstance(
            value: String,
        ) = AccuDaysPreference.entries.firstOrNull {
            it.id == value
        } ?: FIFTEEN
    }

    override val valueArrayId = R.array.accu_preference_day_values
    override val nameArrayId = R.array.accu_preference_days

    override fun getName(context: Context) = UnitUtils.getName(context, this)
}
