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

package org.breezyweather.daily.adapter.model

import breezyweather.domain.weather.model.HalfDay
import org.breezyweather.daily.adapter.DailyWeatherAdapter

class Overview(
    val halfDay: HalfDay,
    val isDaytime: Boolean
) : DailyWeatherAdapter.ViewModel {
    override val code = 1

    companion object {
        fun isCode(code: Int) = (code == 1)
    }
}
