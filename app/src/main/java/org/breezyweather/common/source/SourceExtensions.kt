package org.breezyweather.common.source

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import org.breezyweather.R

fun Source.getName(context: Context, feature: SourceFeature? = null, location: Location? = null): String {
    return if (this is ConfigurableSource && !isConfigured) {
        context.getString(R.string.settings_weather_source_not_configured, name)
    } else if (this is WeatherSource &&
        location != null &&
        feature != null &&
        feature != SourceFeature.REVERSE_GEOCODING &&
        !isFeatureSupportedForLocation(location, feature)
    ) {
        context.getString(R.string.settings_weather_source_unavailable, name)
    } else if (this is ReverseGeocodingSource &&
        location != null &&
        feature == SourceFeature.REVERSE_GEOCODING &&
        !isReverseGeocodingSupportedForLocation(location)
    ) {
        context.getString(R.string.settings_weather_source_unavailable, name)
    } else {
        name
    }
}
