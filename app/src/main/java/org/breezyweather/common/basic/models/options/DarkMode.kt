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

package org.breezyweather.common.basic.models.options

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options._basic.Utils

enum class DarkMode(
    override val id: String
): BaseEnum {

    AUTO("auto"),
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "system" -> SYSTEM
            "light" -> LIGHT
            "dark" -> DARK
            else -> AUTO
        }
    }

    override val valueArrayId = R.array.dark_mode_values
    override val nameArrayId = R.array.dark_modes

    override fun getName(context: Context) = Utils.getName(context, this)
}