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

package org.breezyweather.sources.openweather.preferences

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options._basic.Utils

enum class OpenWeatherOneCallVersion(
    override val id: String
): BaseEnum {

    VERSION_25("2.5"),
    VERSION_30("3.0");

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "2.5" -> VERSION_25
            "3.0" -> VERSION_30
            else -> VERSION_25
        }
    }

    override val valueArrayId = R.array.open_weather_one_call_version_values
    override val nameArrayId = R.array.open_weather_one_call_version

    override fun getName(context: Context) = Utils.getName(context, this)
}