package wangdaye.com.geometricweather.weather.json.atmoaura

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import wangdaye.com.geometricweather.common.serializer.DateSerializer
import java.util.Date

@Serializable
data class AtmoAuraPointHoraire(
    @SerialName("datetime_echeance") @Serializable(DateSerializer::class) val datetimeEcheance: Date,
    @SerialName("indice_atmo") val indiceAtmo: Int?,
    val concentration: Int?
)
