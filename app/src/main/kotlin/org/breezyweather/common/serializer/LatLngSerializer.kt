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

import com.google.maps.android.model.LatLng
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LatLngSerializer : KSerializer<LatLng> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LatLng", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LatLng) {
        encoder.encodeString("${value.latitude},${value.longitude}")
    }

    override fun deserialize(decoder: Decoder): LatLng {
        val jsonValue = decoder.decodeString()
        try {
            return LatLng.parse(jsonValue)
        } catch (e: Exception) {
            throw SerializationException("Failed parsing '$jsonValue' as LatLng")
        }
    }
}
