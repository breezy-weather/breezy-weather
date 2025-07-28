package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsResult(
    val pageProps: VedurIsPageProps? = null,
)
