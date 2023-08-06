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

package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl

enum class AllergenUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Int> {

    PPCM("ppcm", 1f);

    override val valueArrayId = R.array.pollen_unit_values
    override val nameArrayId = R.array.pollen_units
    override val voiceArrayId = R.array.pollen_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(
        valueInDefaultUnit: Int
    ) = (valueInDefaultUnit * unitFactor).toInt()

    override fun getValueInDefaultUnit(
        valueInCurrentUnit: Int
    ) = (valueInCurrentUnit / unitFactor).toInt()

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Int
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Int
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Int,
        rtl: Boolean
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        rtl = rtl
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Int
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Int,
        rtl: Boolean
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        rtl = rtl
    )
}