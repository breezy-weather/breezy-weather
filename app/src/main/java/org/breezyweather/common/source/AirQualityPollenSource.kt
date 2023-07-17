package org.breezyweather.common.source

/**
 * Does nothing for now.
 * It’s just a workaround to have credits for Air quality & Pollen on Open-Meteo provider
 */
interface AirQualityPollenSource : Source {
    /**
     * Credits and acknowledgments that will be shown at the bottom of main screen
     * Please check terms of the source to be sure to put the correct term here
     * Example: MyGreatApi CC BY 4.0
     *
     * Will not be displayed if identical to weatherAttribution
     */
    val airQualityPollenAttribution: String

}
