package org.breezyweather.sources.imd.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double

@Serializer(forClass = Any::class)
object ImdAnySerializer : KSerializer<Any?> {
    override fun deserialize(decoder: Decoder): Any? {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonNull) {
                return null
            }
            if (element is JsonPrimitive) {
                if (element.isString) {
                    return element.content
                }
                return try {
                    element.boolean
                } catch (e: Throwable) {
                    element.double
                }
            }
            throw SerializationException("Invalid Json element $element")
        }
        throw SerializationException("Unknown serialization type")
    }
}
