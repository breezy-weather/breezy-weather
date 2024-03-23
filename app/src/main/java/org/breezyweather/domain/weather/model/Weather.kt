package org.breezyweather.domain.weather.model

import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Weather

val Weather.validAirQuality: AirQuality?
    get() = if (current?.airQuality != null && current!!.airQuality!!.isIndexValid) {
        current!!.airQuality
    } else if (today?.airQuality != null && today!!.airQuality!!.isIndexValid) {
        today!!.airQuality
    } else null
