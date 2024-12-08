/**
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
import androidx.annotation.ColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable

/**
 * Weather service.
 */
interface WeatherSource : Source {

    /**
     * Official color used by the source
     */
    @get:ColorInt
    val color: Int

    /**
     * List the features by the source as keys
     * Values are credits and acknowledgments that will be shown at the bottom of main screen
     * Please check terms of the source to be sure to put the correct term here
     * Example: <SourceFeature.FORECAST, "MyGreatApi (CC BY 4.0)">
     */
    val supportedFeatures: Map<SourceFeature, String>

    /**
     * May be used when you don't have reverse geocoding implemented and you want to filter
     * location results from default location search source to only include some countries
     * for example
     */
    fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean = true

    /**
     * Returns weather converted to Breezy Weather Weather object
     * @param requestedFeatures List of features requested by the user
     */
    fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper>
}
