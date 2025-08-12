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

package org.breezyweather.unit.duration

import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.RequiresApi
import org.breezyweather.unit.R
import org.breezyweather.unit.formatting.UnitNominative
import org.breezyweather.unit.formatting.UnitWidth
import kotlin.time.DurationUnit

/**
 * Duration already exists in Kotlin.time
 * Extending it with what we need
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public fun DurationUnit.toMeasureUnit(): MeasureUnit = when (this) {
    DurationUnit.NANOSECONDS -> MeasureUnit.NANOSECOND
    DurationUnit.MICROSECONDS -> MeasureUnit.MICROSECOND
    DurationUnit.MILLISECONDS -> MeasureUnit.MILLISECOND
    DurationUnit.SECONDS -> MeasureUnit.SECOND
    DurationUnit.MINUTES -> MeasureUnit.MINUTE
    DurationUnit.HOURS -> MeasureUnit.HOUR
    DurationUnit.DAYS -> MeasureUnit.DAY
}

public fun DurationUnit.getPrecision(width: UnitWidth): Int = when (width) {
    UnitWidth.SHORT -> 1
    UnitWidth.NARROW -> 0
    UnitWidth.LONG -> 2
}

internal fun DurationUnit.getNominative(): UnitNominative = when (this) {
    DurationUnit.NANOSECONDS -> UnitNominative(
        short = R.string.duration_ns_nominative_short,
        long = R.string.duration_ns_nominative_long
    )
    DurationUnit.MICROSECONDS -> UnitNominative(
        short = R.string.duration_micros_nominative_short,
        long = R.string.duration_micros_nominative_long
    )
    DurationUnit.MILLISECONDS -> UnitNominative(
        short = R.string.duration_ms_nominative_short,
        long = R.string.duration_ms_nominative_long
    )
    DurationUnit.SECONDS -> UnitNominative(
        short = R.string.duration_sec_nominative_short,
        long = R.string.duration_sec_nominative_long
    )
    DurationUnit.MINUTES -> UnitNominative(
        short = R.string.duration_min_nominative_short,
        long = R.string.duration_min_nominative_long
    )
    DurationUnit.HOURS -> UnitNominative(
        short = R.string.duration_hr_nominative_short,
        long = R.string.duration_hr_nominative_long
    )
    DurationUnit.DAYS -> UnitNominative(
        short = R.string.duration_day_nominative_short,
        long = R.string.duration_day_nominative_long
    )
}
