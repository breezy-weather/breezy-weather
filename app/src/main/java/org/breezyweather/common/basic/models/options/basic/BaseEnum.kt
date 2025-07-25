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

    /**
     * Get the name of the unit
     * @param context
     * @returns formatted unit, such as “km/h”
     */
    fun getName(context: Context): String
}

interface ContentDescriptionEnum : BaseEnum {
    val contentDescriptionArrayId: Int

    /**
     * Get the name of the unit for screen readers
     * @param context
     * @returns formatted unit, such as “kilometers per hour”
     */
    fun getMeasureContentDescription(context: Context): String
}

interface UnitEnum<T : Number> : ContentDescriptionEnum {
    val measureUnit: MeasureUnit?
    val perMeasureUnit: MeasureUnit?
    val convertUnit: (T) -> Double

    /**
     * Converted a value in default unit to the target unit (this)
     * @param valueInDefaultUnit value in default unit
     * @returns Number
     */
    fun getConvertedUnit(valueInDefaultUnit: T): T

    /**
     * Converted a value in default unit to the target unit (this)
     * @param valueInDefaultUnit value in default unit
     * @returns Number
     */
    fun formatValue(context: Context, valueInDefaultUnit: T): String

    /**
     * Format a value and its unit for general use
     * @param context
     * @param value
     * @param isValueInDefaultUnit defaults to true, pass false if the value needs to be converted
     * @returns formatted value with its unit, such as “14,6 km/h”
     */
    fun formatMeasure(context: Context, value: T, isValueInDefaultUnit: Boolean = true): String

    /**
     * Format a value and its unit for screen readers
     * @param context
     * @returns formatted value with its unit, such as “14,6 kilometers per hour”
     */
    fun formatContentDescription(context: Context, value: T, isValueInDefaultUnit: Boolean = true): String
}
