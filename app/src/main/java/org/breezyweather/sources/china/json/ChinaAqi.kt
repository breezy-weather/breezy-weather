package org.breezyweather.sources.china.json

import kotlinx.serialization.Serializable

@Serializable
data class ChinaAqi(
    val pubTime: String?,
    val primary: String?,
    val suggest: String?,
    val src: String?,
    val pm25: String?,
    val pm25Desc: String?,
    val pm10: String?,
    val pm10Desc: String?,
    val o3: String?,
    val o3Desc: String?,
    val so2: String?,
    val so2Desc: String?,
    val no2: String?,
    val no2Desc: String?,
    val co: String?,
    val coDesc: String?
)
