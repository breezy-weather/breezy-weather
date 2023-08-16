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
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl
import kotlin.math.roundToInt

enum class CloudCoverUnit(
    override val id: String,
    override val convertUnit: (Int) -> Float
): UnitEnum<Int> {

    PERCENT("%", { valueInDefaultUnit -> valueInDefaultUnit * 1f });

    override val valueArrayId = 0
    override val nameArrayId = 0
    override val voiceArrayId = 0

    override fun getName(context: Context) = "%"

    override fun getVoice(context: Context) = "%"

    override fun getValueWithoutUnit(
        valueInDefaultUnit: Int
    ) = convertUnit(valueInDefaultUnit).roundToInt()

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
    ) = Utils.formatInt(valueInDefaultUnit) + "\u202f" + id

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Int
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Int,
        rtl: Boolean
    ) = Utils.formatInt(valueInDefaultUnit) + "\u202f" + id
}