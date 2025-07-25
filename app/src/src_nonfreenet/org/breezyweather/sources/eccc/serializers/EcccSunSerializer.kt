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

package org.breezyweather.sources.eccc.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import org.breezyweather.sources.eccc.json.EcccSun

@Serializer(forClass = EcccSun::class)
object EcccSunSerializer : KSerializer<EcccSun?> {
    override fun deserialize(decoder: Decoder): EcccSun? {
        return try {
            val json = ((decoder as JsonDecoder).decodeJsonElement() as JsonObject)
            EcccSun(value = Json.decodeFromString(json["value"].toString()))
        } catch (ignored: Exception) {
            null
        }
    }
}
