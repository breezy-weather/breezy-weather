package wangdaye.com.geometricweather.common.basic.models.options.unit

import android.content.Context
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.BaseEnum
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils

enum class AirQualityLevelUnit(
    override val id: String,
    val levelsArrayId: Int,
    val colorsArrayId: Int,
    val pm25valuesArrayId: Int,
    val pm10valuesArrayId: Int,
    val so2valuesArrayId: Int,
    val no2valuesArrayId: Int,
    val o3valuesArrayId: Int,
): BaseEnum {

    AQI("aqi", R.array.air_quality_levels_aqi, R.array.air_quality_levels_aqi_colors,
        R.array.air_quality_levels_aqi_pm25_values, R.array.air_quality_levels_aqi_pm10_values,
        R.array.air_quality_levels_aqi_so2_values, R.array.air_quality_levels_aqi_no2_values,
        R.array.air_quality_levels_aqi_o3_values),
    EAQI("eaqi", R.array.air_quality_levels_eaqi, R.array.air_quality_levels_eaqi_colors,
        R.array.air_quality_levels_eaqi_pm25_values, R.array.air_quality_levels_eaqi_pm10_values,
        R.array.air_quality_levels_eaqi_so2_values, R.array.air_quality_levels_eaqi_no2_values,
        R.array.air_quality_levels_eaqi_o3_values);

    companion object {
        @JvmStatic
        fun getInstance(
            value: String
        ) = when (value) {
            "eaqi" -> EAQI
            "aqi" -> AQI
            else -> AQI
        }
    }

    override val valueArrayId = R.array.air_quality_levels_values
    override val nameArrayId = R.array.air_quality_levels

    override fun getName(context: Context) = Utils.getName(context, this)
}