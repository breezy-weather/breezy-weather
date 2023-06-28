package org.breezyweather.weather.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mf warning current phenomenons
 */
@Serializable
data class MfWarningsResult(
    val timelaps: List<MfWarningTimelaps>? = null,
    @SerialName("phenomenons_items") val phenomenonsItems: List<MfWarningPhenomenonMaxColor>? = null,
    val advices: List<MfWarningAdvice>? = null,
    val consequences: List<MfWarningConsequence>? = null,
    @SerialName("max_count_items") val maxCountItems: List<MfWarningMaxCountItems>? = null,
    val comments: MfWarningComments? = null,
    val text: MfWarningComments? = null,
    @SerialName("text_avalanche") val textAvalanche: MfWarningComments? = null
)