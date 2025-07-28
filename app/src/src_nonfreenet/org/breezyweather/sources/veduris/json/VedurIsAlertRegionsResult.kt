package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable
import org.breezyweather.sources.veduris.serializers.VedurIsAnySerializer

@Serializable
data class VedurIsAlertRegionsResult(
    val features: List<
        @Serializable(with = VedurIsAnySerializer::class)
        Any?
        > = listOf(),
)
