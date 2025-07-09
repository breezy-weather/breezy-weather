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

package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.Utils
import org.breezyweather.common.extensions.isRtl
import kotlin.math.roundToInt

enum class PollenUnit(
    override val id: String,
    override val convertUnit: (Int) -> Double,
) : UnitEnum<Int> {

    PPCM("ppcm", { valueInDefaultUnit -> valueInDefaultUnit * 1.0 }),
    ;

    override val valueArrayId = R.array.pollen_unit_values
    override val nameArrayId = R.array.pollen_units
    override val voiceArrayId = R.array.pollen_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(
        valueInDefaultUnit: Int,
    ) = convertUnit(valueInDefaultUnit).roundToInt()

    override fun getValueTextWithoutUnit(
        context: Context,
        valueInDefaultUnit: Int,
    ) = Utils.getValueTextWithoutUnit(context, this, valueInDefaultUnit)!!

    override fun getValueText(
        context: Context,
        value: Int,
        isValueInDefaultUnit: Boolean,
    ) = getValueText(context, value, context.isRtl, isValueInDefaultUnit)

    override fun getValueText(
        context: Context,
        value: Int,
        rtl: Boolean,
        isValueInDefaultUnit: Boolean,
    ) = Utils.getValueText(
        context = context,
        enum = this,
        value = value,
        rtl = rtl,
        isValueInDefaultUnit = isValueInDefaultUnit
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Int,
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Int,
        rtl: Boolean,
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        rtl = rtl
    )
}
