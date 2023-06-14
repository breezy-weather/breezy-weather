package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils

// actual pressure = pressure(mb) * factor.
enum class PressureUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Float> {

    MB("mb", 1f),
    KPA("kpa", 0.1f),
    HPA("hpa", 1f),
    ATM("atm", 0.0009869f),
    MMHG("mmhg", 0.75006f),
    INHG("inhg", 0.02953f),
    KGFPSQCM("kgfpsqcm", 0.00102f);

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ) = when (value) {
            "kpa" -> KPA
            "hpa" -> HPA
            "atm" -> ATM
            "mmhg" -> MMHG
            "inhg" -> INHG
            "kgfpsqcm" -> KGFPSQCM
            else -> MB
        }
    }

    override val valueArrayId = R.array.pressure_unit_values
    override val nameArrayId = R.array.pressure_units
    override val voiceArrayId = R.array.pressure_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Float) = valueInDefaultUnit * unitFactor

    override fun getValueInDefaultUnit(valueInCurrentUnit: Float) = valueInCurrentUnit / unitFactor

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Float
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 2)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueText(context, valueInDefaultUnit, org.breezyweather.common.utils.DisplayUtils.isRtl(context))

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 2,
        rtl = rtl
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueVoice(context, valueInDefaultUnit, org.breezyweather.common.utils.DisplayUtils.isRtl(context))

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 2,
        rtl = rtl
    )
}