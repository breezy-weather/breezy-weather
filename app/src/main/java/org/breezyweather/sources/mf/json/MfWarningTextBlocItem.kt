package org.breezyweather.sources.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfWarningTextBlocItem(
    val text: List<String>?,
    @SerialName("text_html") val textHtml: List<String>?,
    val title: String?,
    @SerialName("title_html") val titleHtml: String?
)
