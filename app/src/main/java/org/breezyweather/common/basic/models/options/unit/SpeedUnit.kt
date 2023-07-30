package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl

// actual speed = speed(km/h) * factor.
enum class SpeedUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Float> {

    MPS("mps", 1f),
    KPH("kph", 3.6f),
    KN("kn", 1.94385f),
    MPH("mph", 2.23694f),
    FTPS("ftps", 3.28084f);

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "kph" -> KPH
            "kn" -> KN
            "mph" -> MPH
            "ftps" -> FTPS
            else -> MPS
        }
    }

    override val valueArrayId = R.array.speed_unit_values
    override val nameArrayId = R.array.speed_units
    override val voiceArrayId = R.array.speed_unit_voices

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