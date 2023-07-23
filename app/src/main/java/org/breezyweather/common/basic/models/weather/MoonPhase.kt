package org.breezyweather.common.basic.models.weather

import android.content.Context
import org.breezyweather.R
import org.shredzone.commons.suncalc.MoonPhase
import java.io.Serializable
import java.util.Locale

/**
 * Moon phase.
 */
class MoonPhase(
    /**
     * Angle between 0 to 360 (no negative! Add 180 if you have negative numbers)
     */
    val angle: Int? = null
) : Serializable {

    val isValid: Boolean
        get() = angle != null

    fun getDescription(context: Context): String? {
        if (angle == null) return null

        return when (MoonPhase.Phase.toPhase(angle.toDouble())) {
            MoonPhase.Phase.NEW_MOON -> context.getString(R.string.ephemeris_moon_phase_new_moon)
            MoonPhase.Phase.WAXING_CRESCENT -> context.getString(R.string.ephemeris_moon_phase_waxing_crescent)
            MoonPhase.Phase.FIRST_QUARTER -> context.getString(R.string.ephemeris_moon_phase_first_quarter)
            MoonPhase.Phase.WAXING_GIBBOUS -> context.getString(R.string.ephemeris_moon_phase_waxing_gibbous)
            MoonPhase.Phase.FULL_MOON -> context.getString(R.string.ephemeris_moon_phase_full_moon)
            MoonPhase.Phase.WANING_GIBBOUS -> context.getString(R.string.ephemeris_moon_phase_waning_gibbous)
            MoonPhase.Phase.LAST_QUARTER -> context.getString(R.string.ephemeris_moon_phase_last_quarter)
            MoonPhase.Phase.WANING_CRESCENT -> context.getString(R.string.ephemeris_moon_phase_waning_crescent)
            else -> null
        }
    }

    companion object {
        fun getAngleFromEnglishDescription(phase: String?): Int? {
            return if (phase.isNullOrEmpty()) {
                null
            } else when (phase.lowercase(Locale.getDefault())) {
                "waxingcrescent", "waxing crescent" -> 45
                "first", "firstquarter", "first quarter" -> 90
                "waxinggibbous", "waxing gibbous" -> 135
                "full", "fullmoon", "full moon" -> 180
                "waninggibbous", "waning gibbous" -> 225
                "third", "thirdquarter", "third quarter", "last", "lastquarter", "last quarter" -> 270
                "waningcrescent", "waning crescent" -> 315
                else -> 360
            }
        }
    }
}
