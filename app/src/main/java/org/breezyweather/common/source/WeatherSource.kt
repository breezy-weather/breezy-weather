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
import androidx.annotation.DrawableRes
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable

/**
 * Weather service.
 */
interface WeatherSource : Source {

    /**
     * An optional icon for the attribution page.
     * /!\ Only include it if it is mandatory in the attribution, as we don’t want to bundle copyrighted icons which
     * we don’t have the right to use!
     * @param isDarkMode May be used to display a different icon depending on light/dark mode. If true, the background
     * is dark.
     * Example: R.drawable.accu_icon_dark or R.drawable_icon_light
     */
    @DrawableRes
    fun getAttributionIcon(isDarkMode: Boolean): Int? {
        return null
    }

    /**
     * List the features by the source as keys
     * Values are credits and acknowledgments that will be shown at the bottom of main screen
     * Please check terms of the source to be sure to put the correct term here
     * Example: <SourceFeature.FORECAST, "MyGreatApi (CC BY 4.0)">
     */
    val supportedFeatures: Map<SourceFeature, String>

    /**
     * One or a few locations that represents use cases you want to test for this source
     * They will be available to add in the debug version
     *
     * Usually, you will need: name, longitude, latitude, timezone, countryCode, xxxxSource
     * Don't bother adding things not useful for the tests such as administration levels
     * To find coordinates and timezone, go to https://open-meteo.com/en/docs/geocoding-api
     *
     * Example:
     * Location(
     *     city = "State College",
     *     latitude = 40.79339,
     *     longitude = -77.86,
     *     timeZone = "America/New_York",
     *     countryCode = "US",
     *     forecastSource = id,
     *     currentSource = id,
     *     airQualitySource = id,
     *     pollenSource = id,
     *     minutelySource = id,
     *     alertSource = id,
     *     normalsSource = id
     * )
     *
     * Can be an emptyList(), although we recommend adding at least one
     */
    val testingLocations: List<Location>

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
