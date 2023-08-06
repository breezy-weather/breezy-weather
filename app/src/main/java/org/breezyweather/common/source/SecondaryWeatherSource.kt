/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
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
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper

/**
 * Initial implementation of secondary weather source
 * Interface may change any time
 */
interface SecondaryWeatherSource : Source {

    val supportedFeatures: List<SecondaryWeatherSourceFeature>

    fun isFeatureSupportedForLocation(feature: SecondaryWeatherSourceFeature, location: Location): Boolean = true

    // TODO: Improve
    val airQualityAttribution: String?
    val allergenAttribution: String?
    val minutelyAttribution: String?
    val alertAttribution: String?

    /**
     * Returns secondary weather converted to Breezy Weather Weather object
     * For efficiency reasons, we have one single functions, but don’t worry, you will never
     * be asked to provide allergen if you don’t support allergen
     * Only process things you are asked to process and that you support
     * @return an Observable of the Secondary Weather wrapper containing elements asked
     */
    fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper>

}
