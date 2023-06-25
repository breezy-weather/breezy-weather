package org.breezyweather.weather.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mf warning current phenomenons
 */
@Serializable
data class MfWarningsResult(
    val timelaps: List<MfWarningTimelaps>?,
    @SerialName("phenomenons_items") val phenomenonsItems: List<MfWarningPhenomenonMaxColor>?,
    val advices: List<MfWarningAdvice>?,
    val consequences: List<MfWarningConsequence>?,
    @SerialName("max_count_items") val maxCountItems: List<MfWarningMaxCountItems>?,
    val comments: MfWarningComments?,
    val text: MfWarningComments?,
    @SerialName("text_avalanche") val textAvalanche: MfWarningComments?
)