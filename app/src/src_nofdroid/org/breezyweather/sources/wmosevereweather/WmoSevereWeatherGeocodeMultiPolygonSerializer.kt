package org.breezyweather.sources.wmosevereweather

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import org.breezyweather.sources.wmosevereweather.json.WmoSevereWeatherAlertCoordGeocode

@Serializer(forClass = WmoSevereWeatherAlertCoordGeocode::class)
object WmoSevereWeatherGeocodeMultiPolygonSerializer : KSerializer<WmoSevereWeatherAlertCoordGeocode> {
    override fun deserialize(decoder: Decoder): WmoSevereWeatherAlertCoordGeocode {
        val json = ((decoder as JsonDecoder).decodeJsonElement() as JsonObject)
        val coordinates = json["coordinates"] ?: return WmoSevereWeatherAlertCoordGeocode()
        return WmoSevereWeatherAlertCoordGeocode(
            coordinates = try {
                (coordinates as JsonArray).map { firstArray ->
                    try {
                        listOf(Json.decodeFromString(firstArray.toString()))
                    } catch (e: Exception) {
                        (firstArray as JsonArray).map { secondArray ->
                            Json.decodeFromString(secondArray.toString())
                        }
                    }
                }
            } catch (ignored: Exception) {
                // Once, there was one geocode that was a single point in the middle of the ocean
                // Not sure what to do with that, so adding it anyway so it doesn’t cause parsing
                // issues
                listOf(listOf(Json.decodeFromString(coordinates.toString())))
            }
        )
    }
}
