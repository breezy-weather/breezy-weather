package org.breezyweather.domain.source

import breezyweather.domain.source.SourceFeature
import org.breezyweather.R

val SourceFeature.resourceName: Int?
    get() = when (this) {
        SourceFeature.FEATURE_CURRENT -> R.string.current_weather
        SourceFeature.FEATURE_AIR_QUALITY -> R.string.air_quality
        SourceFeature.FEATURE_POLLEN -> R.string.pollen
        SourceFeature.FEATURE_MINUTELY -> R.string.precipitation_nowcasting
        SourceFeature.FEATURE_ALERT -> R.string.alerts
        SourceFeature.FEATURE_NORMALS -> R.string.temperature_normals
        else -> null
    }
