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

package org.breezyweather.sources.wmosevereweather

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import org.breezyweather.BreezyWeather
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.common.utils.helpers.LogHelper
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * World Meteorological Organization
 * Supports severe weather from 134 issuing organizations
 */
class WmoSevereWeatherService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), SecondaryWeatherSource {

    override val id = "wmosevereweather"
    override val name = "World Meteorological Organization (WMO) Severe Weather"
    override val privacyPolicyUrl = "https://severeweather.wmo.int/v2/privacy.html"

    private val mAlertsApi by lazy {
        client
            .baseUrl(WMO_ALERTS_BASE_URL)
            .build()
            .create(WmoSevereWeatherApi::class.java)
    }

    override val supportedFeatures = listOf(SecondaryWeatherSourceFeature.FEATURE_ALERT)

    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = "Hong Kong Observatory on behalf of World Meteorological Organization (WMO) + 134 issuing organizations https://severeweather.wmo.int/v2/sources.html"
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedForLocation(SecondaryWeatherSourceFeature.FEATURE_ALERT, location)) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

        // TODO: Remove debug
        if (BreezyWeather.instance.debugMode) {
            // TODO Untested countries: hk, myanmar, spain, newzealand, png, jamaica, australia, macao, algeria, trinidadAndTobago, thailand
            // TODO And generic needs further testing: all, others
            LogHelper.log(msg = "Country code: ${location.countryCode}")
        }

        val alertRequests = WmoSevereWeatherAlertLibrary
            .getLibrariesForLocation(location)
            .map {
                mAlertsApi.getAlertsByRegion(it)
            }

        return Observable.concat(alertRequests)
            .toList()
            .toObservable()
            .map { results ->
                SecondaryWeatherWrapper(
                    alertList = results.map {
                        convert(location, it)
                    }.flatten()
                )
            }
    }

    companion object {
        private const val WMO_ALERTS_BASE_URL = "https://severeweather.wmo.int/"
        const val WMO_ALERTS_CAP_URL_BASE_URL = "https://8xieiqdnye.execute-api.us-west-2.amazonaws.com/swic/capUrl/"
        const val WMO_ALERTS_URL_BASE_URL = "https://cvzxdcwxid.execute-api.us-west-2.amazonaws.com/swic/url/"
        const val WMO_MARKER_RADIUS = 50000 // in meters
    }
}