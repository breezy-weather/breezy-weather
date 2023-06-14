package org.breezyweather.common.basic.models.weather

import java.io.Serializable

/**
 * Precipitation duration.
 *
 * default unit : [ProbabilityUnit.PERCENT]
 */
class PrecipitationProbability(
    val total: Float? = null,
    val thunderstorm: Float? = null,
    val rain: Float? = null,
    val snow: Float? = null,
    val ice: Float? = null
) : Serializable {

    val isValid: Boolean
        get() = total != null && total > 0
}
