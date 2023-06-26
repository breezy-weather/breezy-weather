package org.breezyweather.common.basic.models.options

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options._basic.Utils

enum class UpdateInterval(
    override val id: String,
    val intervalInHour: Float?
): BaseEnum {

    INTERVAL_NEVER("never", null),
    INTERVAL_0_30("0:30", 0.5f),
    INTERVAL_1_00("1:00", 1.0f),
    INTERVAL_1_30("1:30", 1.5f),
    INTERVAL_2_00("2:00", 2.0f),
    INTERVAL_3_00("3:00", 3.0f),
    INTERVAL_6_00("6:00", 6.0f),
    INTERVAL_12_00("12:00", 12.0f),
    INTERVAL_24_00("24:00", 24.0f);

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ) = when (value) {
            "never" -> INTERVAL_NEVER
            "0:30" -> INTERVAL_0_30
            "1:00" -> INTERVAL_1_00
            "2:00" -> INTERVAL_2_00
            "3:00" -> INTERVAL_3_00
            "6:00" -> INTERVAL_6_00
            "12:00" -> INTERVAL_12_00
            "24:00" -> INTERVAL_24_00
            else -> INTERVAL_1_30
        }
    }

    override val valueArrayId = R.array.automatic_refresh_rate_values
    override val nameArrayId = R.array.automatic_refresh_rates

    override fun getName(context: Context) = Utils.getName(context, this)
}