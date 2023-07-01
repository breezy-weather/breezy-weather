package org.breezyweather.weather.accu.preferences

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options._basic.Utils

enum class AccuHoursPreference(
    override val id: String
): BaseEnum {

    ONE("1"),
    TWELVE("12"),
    TWENTY_FOUR("24"),
    SEVENTY_TWO("72"),
    HUNDRED_TWENTY("120");

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "1" -> ONE
            "12" -> TWELVE
            "24" -> TWENTY_FOUR
            "72" -> SEVENTY_TWO
            else -> HUNDRED_TWENTY
        }
    }

    override val valueArrayId = R.array.accu_preference_hour_values
    override val nameArrayId = R.array.accu_preference_hours

    override fun getName(context: Context) = Utils.getName(context, this)
}