package org.breezyweather.sources.eccc.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import org.breezyweather.sources.eccc.json.EcccEpochTime

@Serializer(forClass = EcccEpochTime::class)
object EcccEpochTimeSerializer : KSerializer<EcccEpochTime?> {
    override fun deserialize(decoder: Decoder): EcccEpochTime? {
        return try {
            val json = ((decoder as JsonDecoder).decodeJsonElement() as JsonObject)
            val ecccRoundedEpochTime: String = Json.decodeFromString(json["epochTimeRounded"].toString())
            EcccEpochTime(epochTimeRounded = ecccRoundedEpochTime.ifEmpty { null }?.toIntOrNull())
        } catch (ignored: Exception) {
            null
        }
    }
}
