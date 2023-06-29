package org.breezyweather.common.basic.models.weather

import java.io.Serializable

/**
 * WindDegree.
 */
class WindDegree(
    val degree: Float? = null,
    val isNoDirection: Boolean = false
) : Serializable {

    val windArrow: String?
        get() = if (degree == null) {
            null
        } else if (isNoDirection) {
            "⟳"
        } else when(degree) {
            in 22.5..67.5 -> "↙"
            in 67.5..112.5 -> "←"
            in 112.5..157.5 -> "↖"
            in 157.5..202.5 -> "↑"
            in 202.5..247.5 -> "↗"
            in 247.5..292.5 -> "→"
            in 292.5..337.5 -> "↘"
            else -> "↓"
        }
}
