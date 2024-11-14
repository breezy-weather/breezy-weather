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

interface BaseEnum {
    val id: String
    val nameArrayId: Int
    val valueArrayId: Int
    fun getName(context: Context): String
}

interface VoiceEnum : BaseEnum {
    val voiceArrayId: Int
    fun getVoice(context: Context): String
}

interface UnitEnum<T : Number> : VoiceEnum {
    val convertUnit: (T) -> Double
    fun getValueWithoutUnit(valueInDefaultUnit: T): T
    fun getValueTextWithoutUnit(valueInDefaultUnit: T): String
    fun getValueText(context: Context, valueInDefaultUnit: T): String
    fun getValueText(context: Context, valueInDefaultUnit: T, rtl: Boolean): String
    fun getValueVoice(context: Context, valueInDefaultUnit: T): String
    fun getValueVoice(context: Context, valueInDefaultUnit: T, rtl: Boolean): String
}
