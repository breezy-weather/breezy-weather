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

package org.breezyweather.unit.pollen

import android.content.Context
import android.icu.util.MeasureUnit
import org.breezyweather.unit.R
import org.breezyweather.unit.WeatherUnit
import org.breezyweather.unit.formatting.UnitDecimals
import org.breezyweather.unit.formatting.UnitTranslation
import org.breezyweather.unit.formatting.UnitWidth
import java.util.Locale

enum class PollenConcentrationUnit(
    override val id: String,
    override val displayName: UnitTranslation,
    override val nominative: UnitTranslation,
    override val per: UnitTranslation? = null,
    override val measureUnit: MeasureUnit? = null,
    override val perMeasureUnit: MeasureUnit? = null,
    override val decimals: UnitDecimals,
) : WeatherUnit {

    PER_CUBIC_METER(
        "pcum",
        displayName = UnitTranslation(
            short = R.string.length_m3_per_short,
            long = R.string.length_m3_per_long
        ),
        nominative = UnitTranslation(
            short = R.string.length_m3_per_short,
            long = R.string.length_m3_per_long
        ),
        decimals = UnitDecimals(0)
    ),
    ;

    override fun getDisplayName(
        context: Context,
        locale: Locale,
        width: UnitWidth,
        useMeasureFormat: Boolean,
    ): String {
        return context.getString(
            when (width) {
                UnitWidth.SHORT -> displayName.short
                UnitWidth.LONG -> displayName.long
                UnitWidth.NARROW -> displayName.narrow
            },
            ""
        ).trim()
    }

    companion object {

        fun getUnit(id: String): PollenConcentrationUnit? {
            return entries.firstOrNull { it.id == id }
        }
    }
}
