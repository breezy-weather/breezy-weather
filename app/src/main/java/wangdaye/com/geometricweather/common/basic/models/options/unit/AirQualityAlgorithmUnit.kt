package wangdaye.com.geometricweather.common.basic.models.options.unit

import android.content.Context
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.BaseEnum
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils

enum class AirQualityAlgorithmUnit(
    override val id: String,
    val levelsArrayId: Int,
    val colorsArrayId: Int
): BaseEnum {

    EPA("epa", R.array.air_quality_levels_epa, R.array.air_quality_levels_epa_colors),
    MEE("mee", R.array.air_quality_levels_mee, R.array.air_quality_levels_mee_colors),
    EEA("eea", R.array.air_quality_levels_eea, R.array.air_quality_levels_eea_colors);

    companion object {
        @JvmStatic
        fun getInstance(
            value: String
        ) = when (value) {
            "epa" -> EPA
            "mee" -> MEE
            "eea" -> EEA
            else -> EPA
        }
    }

    override val valueArrayId = R.array.air_quality_levels_values
    override val nameArrayId = R.array.air_quality_levels

    override fun getName(context: Context) = Utils.getName(context, this)
}