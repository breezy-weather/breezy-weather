package org.breezyweather.common.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Serializer(forClass = Any::class)
object StringOrStringListSerializer : KSerializer<List<String>?> {
    override fun deserialize(decoder: Decoder): List<String>? {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonNull) {
                return null
            }
            if (element is JsonArray) {
                return element.map {
                    if (it is JsonPrimitive) {
                        it.content
                    } else {
                        ""
                    }
                }
            }
            if (element is JsonPrimitive) {
                if (element.isString) {
                    return listOf(element.content)
                }
            }
            throw SerializationException("Invalid Json element $element")
        }
        throw SerializationException("Unknown serialization type")
    }
}
