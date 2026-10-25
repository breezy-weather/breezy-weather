package org.breezyweather.sources.imgw

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.breezyweather.common.extensions.toTimezoneSpecificHour
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date
import java.util.TimeZone

object ImgwDailyDateSerializer: KSerializer<Date> {
    override val descriptor: SerialDescriptor
        get() = DateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Date) = DateSerializer.serialize(encoder, value)

    override fun deserialize(decoder: Decoder) = DateSerializer.deserialize(decoder)
        .toTimezoneSpecificHour(
            timeZone = TimeZone.getTimeZone("UTC"),
            hour = 0,
        )

}
