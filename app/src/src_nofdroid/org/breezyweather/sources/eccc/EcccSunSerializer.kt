package org.breezyweather.sources.eccc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import org.breezyweather.sources.eccc.json.EcccSun
import org.breezyweather.sources.wmosevereweather.json.WmoSevereWeatherAlertCoordGeocode

@Serializer(forClass = EcccSun::class)
object EcccSunSerializer : KSerializer<EcccSun?> {
    override fun deserialize(decoder: Decoder): EcccSun? {
        try {
            val json = ((decoder as JsonDecoder).decodeJsonElement() as JsonObject)
            return EcccSun(value = Json.decodeFromString(json["value"].toString()))
        } catch (ignored: Exception) {
            return null
        }
    }
}