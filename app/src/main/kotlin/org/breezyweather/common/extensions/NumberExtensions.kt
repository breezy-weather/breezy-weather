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

package org.breezyweather.common.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalLayoutDirection
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.ceil
import kotlin.math.floor

operator fun Int?.plus(other: Int?): Int? = if (this != null || other != null) {
    (this ?: 0) + (other ?: 0)
} else {
    null
}

operator fun Double?.plus(other: Double?): Double? = if (this != null || other != null) {
    (this ?: 0.0) + (other ?: 0.0)
} else {
    null
}

operator fun Double?.minus(other: Double?): Double? = if (this != null || other != null) {
    (this ?: 0.0) - (other ?: 0.0)
} else {
    null
}

fun Double.ensurePositive(): Double? = if (this >= 0.0) this else null

fun Double.roundUpToNearestMultiplier(multiplier: Double): Double {
    return ceil(div(multiplier)).times(multiplier)
}

fun Double.roundDownToNearestMultiplier(multiplier: Double): Double {
    return floor(div(multiplier)).times(multiplier)
}

fun Double.roundDecimals(decimals: Int): Double? {
    return if (!isNaN()) {
        BigDecimal(this).setScale(decimals, RoundingMode.HALF_UP).toDouble()
    } else {
        null
    }
}

val Array<Double>.median: Double?
    get() {
        if (isEmpty()) return null

        sort()

        return if (size % 2 != 0) {
            this[size / 2]
        } else {
            (this[(size - 1) / 2] + this[size / 2]) / 2.0
        }
    }

/**
 * Taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/58a0add4f6bd8a5ab1006755035ff1b102355d4a/presentation-core/src/main/java/tachiyomi/presentation/core/util/PaddingValues.kt
 */
@Composable
@ReadOnlyComposable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) +
            other.calculateStartPadding(layoutDirection),
        end = calculateEndPadding(layoutDirection) +
            other.calculateEndPadding(layoutDirection),
        top = calculateTopPadding() + other.calculateTopPadding(),
        bottom = calculateBottomPadding() + other.calculateBottomPadding()
    )
}
