package org.breezyweather.common.basic.models.weather

import android.content.Context
import org.breezyweather.R
import java.io.Serializable
import java.util.Locale

/**
 * Moon phase.
 */
class MoonPhase(
    val angle: Int? = null,
    val description: String? = null
) : Serializable {

    val isValid: Boolean
        get() = angle != null && description != null

    fun getMoonPhase(context: Context): String? {
        return if (description.isNullOrEmpty()) {
            null
        } else when (description.lowercase(Locale.getDefault())) {
            "waxingcrescent", "waxing crescent" -> context.getString(R.string.phase_waxing_crescent)
            "first", "firstquarter", "first quarter" -> context.getString(R.string.phase_first)
            "waxinggibbous", "waxing gibbous" -> context.getString(R.string.phase_waxing_gibbous)
            "full", "fullmoon", "full moon" -> context.getString(R.string.phase_full)
            "waninggibbous", "waning gibbous" -> context.getString(R.string.phase_waning_gibbous)
            "third", "thirdquarter", "third quarter", "last", "lastquarter", "last quarter" -> context.getString(
                R.string.phase_third
            )
            "waningcrescent", "waning crescent" -> context.getString(R.string.phase_waning_crescent)
            else -> null
        }
    }
}
