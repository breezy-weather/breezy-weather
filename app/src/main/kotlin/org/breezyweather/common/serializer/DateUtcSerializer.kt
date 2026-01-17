/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.utils.ISO8601Utils
import java.text.ParseException
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object DateUtcSerializer : KSerializer<Date?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DateUtc", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date?) {
        value?.let {
            encoder.encodeString(ISO8601Utils.format(it))
        } ?: encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): Date? {
        val jsonValue = decoder.decodeString()
        try {
            if (jsonValue.isEmpty()) return null
            if (jsonValue.length < 16 ||
                !jsonValue.matches(
                    // Supports dates from 2000 to 2099
                    Regex("20[0-9][0-9]-(0[1-9]|1[0-2])-([0-2][0-9]|3[0-1])[ T]([0-1][0-9]|2[0-3]):[0-5][0-9](.*)")
                )
            ) {
                throw SerializationException("Failed parsing '$jsonValue' as UTC Date")
            } else {
                val timeZone = TimeZone.getTimeZone("UTC")
                return jsonValue.toDateNoHour(timeZone)?.toCalendarWithTimeZone(timeZone)?.apply {
                    set(Calendar.HOUR_OF_DAY, jsonValue.substring(11, 13).toInt())
                    set(Calendar.MINUTE, jsonValue.substring(14, 16).toInt())
                    // We could add parsing of second but not really useful, let’s be efficient
                }?.time
            }
        } catch (e: ParseException) {
            throw SerializationException("Failed parsing '$jsonValue' as UTC Date")
        }
    }
}
