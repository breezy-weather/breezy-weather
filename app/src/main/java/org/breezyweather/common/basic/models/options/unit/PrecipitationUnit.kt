package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl

// actual precipitation = precipitation(mm) * factor.
enum class PrecipitationUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Float> {

    MM("mm", 1f),
    CM("cm", 0.1f),
    IN("in", 0.0394f),
    LPSQM("lpsqm", 1f);

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "cm" -> CM
            "in" -> IN
            "lpsqm" -> LPSQM
            else -> MM
        }
    }

    override val valueArrayId = R.array.precipitation_unit_values
    override val nameArrayId = R.array.precipitation_units
    override val voiceArrayId = R.array.precipitation_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Float) = valueInDefaultUnit * unitFactor

    override fun getValueInDefaultUnit(valueInCurrentUnit: Float) = valueInCurrentUnit / unitFactor

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Float
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 1)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )
}

// actual precipitation intensity = precipitation intensity(mm/h) * factor.
enum class PrecipitationIntensityUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Float> {

    MMPH("mmph", 1f),
    CMPH("cmph", 0.1f),
    INPH("inph", 0.0394f),
    LPSQMPH("lpsqmph", 1f);

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "cmph" -> CMPH
            "inph" -> INPH
            "lpsqmph" -> LPSQMPH
            else -> MMPH
        }
    }

    override val valueArrayId = R.array.precipitation_intensity_unit_values
    override val nameArrayId = R.array.precipitation_intensity_units
    override val voiceArrayId = R.array.precipitation_intensity_unit_voices

    override fun getName(context: Context) = Utils.getName(context, this)

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueWithoutUnit(valueInDefaultUnit: Float) = valueInDefaultUnit * unitFactor

    override fun getValueInDefaultUnit(valueInCurrentUnit: Float) = valueInCurrentUnit / unitFactor

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Float
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 1)!!

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float
    ) = getValueVoice(context, valueInDefaultUnit, context.isRtl)

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Float,
        rtl: Boolean
    ) = Utils.getVoiceText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = 1,
        rtl = rtl
    )
}