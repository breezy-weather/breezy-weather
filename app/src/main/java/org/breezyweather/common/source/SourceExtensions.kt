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
