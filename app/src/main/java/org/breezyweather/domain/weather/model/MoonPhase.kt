package org.breezyweather.domain.weather.model

import android.content.Context
import breezyweather.domain.weather.model.MoonPhase
import org.breezyweather.R
import org.shredzone.commons.suncalc.MoonPhase.Phase

fun MoonPhase.getDescription(context: Context): String? {
    if (angle == null) return null

    return when (Phase.toPhase(angle!!.toDouble())) {
        Phase.NEW_MOON -> context.getString(R.string.ephemeris_moon_phase_new_moon)
        Phase.WAXING_CRESCENT -> context.getString(R.string.ephemeris_moon_phase_waxing_crescent)
        Phase.FIRST_QUARTER -> context.getString(R.string.ephemeris_moon_phase_first_quarter)
        Phase.WAXING_GIBBOUS -> context.getString(R.string.ephemeris_moon_phase_waxing_gibbous)
        Phase.FULL_MOON -> context.getString(R.string.ephemeris_moon_phase_full_moon)
        Phase.WANING_GIBBOUS -> context.getString(R.string.ephemeris_moon_phase_waning_gibbous)
        Phase.LAST_QUARTER -> context.getString(R.string.ephemeris_moon_phase_last_quarter)
        Phase.WANING_CRESCENT -> context.getString(R.string.ephemeris_moon_phase_waning_crescent)
        else -> null
    }
}
