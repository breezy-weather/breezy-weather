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

package org.breezyweather.common.basic.models.options.basic

import android.content.Context
import android.icu.util.MeasureUnit

interface BaseEnum {
    val id: String
    val nameArrayId: Int
    val valueArrayId: Int
    fun getName(context: Context): String
}

interface ContentDescriptionEnum : BaseEnum {
    val contentDescriptionArrayId: Int
    fun getMeasureContentDescription(context: Context): String
}

interface UnitEnum<T : Number> : ContentDescriptionEnum {
    val measureUnit: MeasureUnit?
    val perMeasureUnit: MeasureUnit?
    val convertUnit: (T) -> Double
    fun getConvertedUnit(valueInDefaultUnit: T): T
    fun formatValue(context: Context, valueInDefaultUnit: T): String
    fun formatMeasure(context: Context, value: T, isValueInDefaultUnit: Boolean = true): String
    fun formatContentDescription(context: Context, value: T, isValueInDefaultUnit: Boolean = true): String
}
