package org.breezyweather.gadgetbridge.json

import kotlinx.serialization.Serializable


/**
 * Refer to
 * https://codeberg.org/Freeyourgadget/Gadgetbridge/src/branch/master/app/src/main/java/nodomain/freeyourgadget/gadgetbridge/model/WeatherSpec.java
 */
@Serializable
data class GadgetBridgeData (
    val timestamp: Int? = null,
    val location: String? = null,
    val currentTemp: Int? = null,
    val currentConditionCode : Int? = null,
    val currentCondition: String? = null,
    val currentHumidity: Int? = null,
    val todayMaxTemp: Int? = null,
    val todayMinTemp:  Int? = null,
    val windSpeed: Float? = null,
    val windDirection: Int? = null,
    val uvIndex: Float? = null,
    val precipProbability: Int? = null,
    val forecasts: List<GadgetBridgeDailyForecast>? = null
)