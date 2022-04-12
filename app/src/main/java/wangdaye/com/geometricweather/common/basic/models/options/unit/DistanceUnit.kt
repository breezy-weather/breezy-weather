package wangdaye.com.geometricweather.common.basic.models.options.unit

import android.content.Context
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.UnitEnum
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils
import wangdaye.com.geometricweather.common.utils.DisplayUtils

// actual distance = distance(km) * factor.
enum class DistanceUnit(
    override val id: String,
    override val unitFactor: Float
): UnitEnum<Float> {

    KM("km", 1f),
    M("m", 1000f),
    MI("mi", 0.6213f),
    NMI("nmi", 0.5399f),
    FT("ft", 3280.8398f);

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ) = when (value) {
            "m" -> M
            "mi" -> MI
            "nmi" -> NMI
            "ft" -> FT
            else -> KM
        }
    }

    override val valueArrayId = R.array.distance_unit_values
    override val nameArrayId = R.array.distance_units
    override val voiceArrayId = R.array.distance_unit_voices

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
    ) = getValueText(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

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
    ) = getValueVoice(context, valueInDefaultUnit, DisplayUtils.isRtl(context))

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