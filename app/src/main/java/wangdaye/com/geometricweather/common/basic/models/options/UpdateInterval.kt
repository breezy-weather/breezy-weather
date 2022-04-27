package wangdaye.com.geometricweather.common.basic.models.options

import android.content.Context
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.BaseEnum
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils

enum class UpdateInterval(
    override val id: String,
    val intervalInHour: Float
): BaseEnum {

    INTERVAL_0_30("0:30", 0.5f),
    INTERVAL_1_00("1:00", 1.0f),
    INTERVAL_1_30("1:30", 1.5f),
    INTERVAL_2_00("2:00", 2.0f),
    INTERVAL_2_30("2:30", 2.5f),
    INTERVAL_3_00("3:00", 3.0f),
    INTERVAL_3_30("3:30", 3.5f),
    INTERVAL_4_00("4:00", 4.0f),
    INTERVAL_4_30("4:30", 4.5f),
    INTERVAL_5_00("5:00", 5.0f),
    INTERVAL_5_30("5:30", 5.5f),
    INTERVAL_6_00("6:00", 6.0f);

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ) = when (value) {
            "0:30" -> INTERVAL_0_30
            "1:00" -> INTERVAL_1_00
            "2:00" -> INTERVAL_2_00
            "2:30" -> INTERVAL_2_30
            "3:00" -> INTERVAL_3_00
            "3:30" -> INTERVAL_3_30
            "4:00" -> INTERVAL_4_00
            "4:30" -> INTERVAL_4_30
            "5:00" -> INTERVAL_5_00
            "5:30" -> INTERVAL_5_30
            "6:00" -> INTERVAL_6_00
            else -> INTERVAL_1_30
        }
    }

    override val valueArrayId = R.array.automatic_refresh_rate_values
    override val nameArrayId = R.array.automatic_refresh_rates

    override fun getName(context: Context) = Utils.getName(context, this)
}