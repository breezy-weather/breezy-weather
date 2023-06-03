package wangdaye.com.geometricweather.common.basic.models.weather

import java.io.Serializable

/**
 * Precipitation duration.
 *
 * default unit : [DurationUnit.H]
 */
class PrecipitationDuration(
    val total: Float? = null,
    val thunderstorm: Float? = null,
    val rain: Float? = null,
    val snow: Float? = null,
    val ice: Float? = null
) : Serializable
