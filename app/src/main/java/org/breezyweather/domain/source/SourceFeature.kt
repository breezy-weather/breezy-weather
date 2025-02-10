package org.breezyweather.domain.source

import breezyweather.domain.source.SourceFeature
import org.breezyweather.R

val SourceFeature.resourceName: Int?
    get() = when (this) {
        SourceFeature.FORECAST -> R.string.forecast
        SourceFeature.CURRENT -> R.string.current_weather
        SourceFeature.AIR_QUALITY -> R.string.air_quality
        SourceFeature.POLLEN -> R.string.pollen
        SourceFeature.MINUTELY -> R.string.precipitation_nowcasting
        SourceFeature.ALERT -> R.string.alerts
        SourceFeature.NORMALS -> R.string.temperature_normals
        SourceFeature.REVERSE_GEOCODING -> R.string.location_reverse_geocoding
        else -> null
    }
