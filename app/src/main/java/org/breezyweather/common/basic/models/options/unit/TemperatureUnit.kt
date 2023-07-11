package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import android.text.BidiFormatter
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.extensions.isRtl

enum class TemperatureUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Float> {

    C("c", 1f) {
        override fun getValueWithoutUnit(valueInDefaultUnit: Float) = valueInDefaultUnit
        override fun getValueInDefaultUnit(valueInCurrentUnit: Float) = valueInCurrentUnit
    },
    F("f", 1f) {
        override fun getValueWithoutUnit(
            valueInDefaultUnit: Float
        ) = (32 + valueInDefaultUnit * 1.8).toFloat()

        override fun getValueInDefaultUnit(
            valueInCurrentUnit: Float
        ) = ((valueInCurrentUnit - 32) / 1.8).toFloat()
    },
    K("k", 1f) {
        override fun getValueWithoutUnit(
            valueInDefaultUnit: Float
        ) = (273.15 + valueInDefaultUnit).toFloat()

        override fun getValueInDefaultUnit(
            valueInCurrentUnit: Float
        ) = (valueInCurrentUnit - 273.15).toFloat()
    };

    companion object {
        fun getInstance(
            value: String
        ) = when (value) {
            "f" -> F
            "k" -> K
            else -> C
        }
    }

    override val valueArrayId = R.array.temperature_unit_values
    override val nameArrayId = R.array.temperature_units
    private val shortArrayId = R.array.temperature_units_short
    override val voiceArrayId = R.array.temperature_units

    override fun getName(context: Context) = Utils.getName(context, this)

    fun getShortName(
        context: Context
    ) = Utils.getNameByValue(
        res = context.resources,
        value = id,
        nameArrayId = shortArrayId,
        valueArrayId = valueArrayId
    )!!

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Float
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit, 0)!!

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
    ) = getValueText(context, valueInDefaultUnit, context.isRtl)

    fun getValueText(
        context: Context,
        valueInDefaultUnit: Float,
        decimalNumber: Int = 1
    ) = Utils.getValueText(
        context = context,
        enum = this,
        valueInDefaultUnit = valueInDefaultUnit,
        decimalNumber = decimalNumber,
        rtl = context.isRtl
    )

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

    fun getShortValueText(
        context: Context,
        valueInDefaultUnit: Float
    ) = getShortValueText(context, valueInDefaultUnit, 0, context.isRtl)

    fun getShortValueText(
        context: Context,
        valueInDefaultUnit: Float,
        decimalNumber: Int,
        rtl: Boolean
    ) = if (rtl) {
        (BidiFormatter
            .getInstance()
            .unicodeWrap(
                Utils.formatFloat(
                    getValueWithoutUnit(valueInDefaultUnit),
                    decimalNumber
                )
            )
                + getShortName(context))
    } else {
        (Utils.formatFloat(
            getValueWithoutUnit(valueInDefaultUnit),
            decimalNumber
        )
                + getShortName(context))
    }

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
        decimalNumber = 0,
        rtl = rtl
    )
}