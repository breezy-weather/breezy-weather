package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import android.text.BidiFormatter
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.UnitEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import org.breezyweather.common.utils.DisplayUtils

enum class TemperatureUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Int> {

    C("c", 1f) {

        override fun getValueWithoutUnit(valueInDefaultUnit: Int) = valueInDefaultUnit
        override fun getValueInDefaultUnit(valueInCurrentUnit: Int) = valueInCurrentUnit
    },
    F("f", 1f) {

        override fun getValueWithoutUnit(
            valueInDefaultUnit: Int
        ) = (32 + valueInDefaultUnit * 1.8f).toInt()

        override fun getValueInDefaultUnit(
            valueInCurrentUnit: Int
        ) = ((valueInCurrentUnit - 32) / 1.8).toInt()
    },
    K("k", 1f) {

        override fun getValueWithoutUnit(
            valueInDefaultUnit: Int
        ) = (273.15 + valueInDefaultUnit).toInt()

        override fun getValueInDefaultUnit(
            valueInCurrentUnit: Int
        ) = (valueInCurrentUnit - 273.15).toInt()
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
    private val longArrayId = R.array.temperature_units_long
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

    fun getLongName(
        context: Context
    ) = Utils.getNameByValue(
        res = context.resources,
        value = id,
        nameArrayId = longArrayId,
        valueArrayId = valueArrayId
    )!!

    override fun getValueTextWithoutUnit(
        valueInDefaultUnit: Int
    ) = Utils.getValueTextWithoutUnit(this, valueInDefaultUnit)!!

    override fun getVoice(context: Context) = Utils.getVoice(context, this)

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Int
    ) = getValueText(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

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

    fun getShortValueText(
        context: Context,
        valueInDefaultUnit: Int
    ) = getShortValueText(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

    fun getShortValueText(
        context: Context,
        valueInDefaultUnit: Int,
        rtl: Boolean
    ) = if (rtl) {
        (BidiFormatter
            .getInstance()
            .unicodeWrap(
                Utils.formatInt(getValueWithoutUnit(valueInDefaultUnit))
            )
                + getShortName(context))
    } else {
        (Utils.formatInt(getValueWithoutUnit(valueInDefaultUnit))
                + getShortName(context))
    }

    fun getLongValueText(
        context: Context,
        valueInDefaultUnit: Int
    ) = getLongValueText(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

    fun getLongValueText(
        context: Context,
        valueInDefaultUnit: Int,
        rtl: Boolean
    ) = if (rtl) {
        (BidiFormatter
            .getInstance()
            .unicodeWrap(
                Utils.formatInt(getValueWithoutUnit(valueInDefaultUnit))
            )
                + "\u202f"
                + getLongName(context))
    } else {
        (Utils.formatInt(getValueWithoutUnit(valueInDefaultUnit))
                + "\u202f"
                + getLongName(context))
    }

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Int
    ) = getValueVoice(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

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