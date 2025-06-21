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

package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.weather.model.MoonPhase
import org.breezyweather.R
import org.shredzone.commons.suncalc.MoonPhase.Phase

fun MoonPhase.getDescription(context: Context): String? {
    if (angle == null) return null

    return when (Phase.toPhase(angle!!.toDouble())) {
        Phase.NEW_MOON -> context.getString(R.string.ephemeris_moon_phase_new_moon)
        Phase.WAXING_CRESCENT -> context.getString(R.string.ephemeris_moon_phase_waxing_crescent)
        Phase.FIRST_QUARTER -> context.getString(R.string.ephemeris_moon_phase_first_quarter)
        Phase.WAXING_GIBBOUS -> context.getString(R.string.ephemeris_moon_phase_waxing_gibbous)
        Phase.FULL_MOON -> context.getString(R.string.ephemeris_moon_phase_full_moon)
        Phase.WANING_GIBBOUS -> context.getString(R.string.ephemeris_moon_phase_waning_gibbous)
        Phase.LAST_QUARTER -> context.getString(R.string.ephemeris_moon_phase_last_quarter)
        Phase.WANING_CRESCENT -> context.getString(R.string.ephemeris_moon_phase_waning_crescent)
        else -> null
    }
}
