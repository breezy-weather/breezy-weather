package wangdaye.com.geometricweather.common.basic.models.options.unit

import android.content.Context
import wangdaye.com.geometricweather.common.basic.models.options._basic.UnitEnum
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils
import wangdaye.com.geometricweather.common.utils.DisplayUtils

enum class RelativeHumidityUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Int> {

    PERCENT("%", 1f);

    override val valueArrayId = 0
    override val nameArrayId = 0
    override val voiceArrayId = 0

    override fun getName(context: Context) = "%"

    override fun getVoice(context: Context) = "%"

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
    ) = getValueText(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

    override fun getValueText(
        context: Context,
        valueInDefaultUnit: Int,
        rtl: Boolean
    ) = Utils.formatInt(valueInDefaultUnit) + "\u202f" + id

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Int
    ) = getValueVoice(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

    override fun getValueVoice(
        context: Context,
        valueInDefaultUnit: Int,
        rtl: Boolean
    ) = Utils.formatInt(valueInDefaultUnit) + "\u202f" + id
}