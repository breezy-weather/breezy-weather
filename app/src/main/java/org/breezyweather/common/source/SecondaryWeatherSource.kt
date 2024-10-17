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
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import io.reactivex.rxjava3.core.Observable

/**
 * Initial implementation of secondary weather source
 */
interface SecondaryWeatherSource : Source {

    val supportedFeaturesInSecondary: List<SecondaryWeatherSourceFeature>

    fun isFeatureSupportedInSecondaryForLocation(
        location: Location, feature: SecondaryWeatherSourceFeature
    ): Boolean = true

    val currentAttribution: String?
    val airQualityAttribution: String?
    val pollenAttribution: String?
    val minutelyAttribution: String?
    val alertAttribution: String?
    val normalsAttribution: String?

    /**
     * Returns secondary weather converted to Breezy Weather Weather object
     * For efficiency reasons, we have one single functions, but don’t worry, you will never
     * be asked to provide pollen if you don’t support pollen
     * Only process things you are asked to process and that you support, otherwise return null
     * for that element.
     * Exception: if you are requested two features that are part of the same endpoint, you
     * can return both. App will not take it into account if the user did not set up this
     * source as its secondary weather source, but will if the data was still considered valid
     * @return an Observable of the Secondary Weather wrapper containing elements asked
     */
    fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper>
}
