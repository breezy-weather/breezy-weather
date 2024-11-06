package org.breezyweather.sources.hko.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonDecoder

@Serializer(forClass = Any::class)
object HkoAnySerializer : KSerializer<Any?> {
    override fun deserialize(decoder: Decoder): Any? {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            return element.toString()
        }
        throw SerializationException("Unknown serialization error")
    }
}