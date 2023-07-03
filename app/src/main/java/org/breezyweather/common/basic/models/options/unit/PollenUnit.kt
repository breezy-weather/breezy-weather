package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl

enum class PollenUnit(
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