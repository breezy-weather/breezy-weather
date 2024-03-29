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
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * World Meteorological Organization Severe Weather Information Centre (WMO SWIC)
 * Supports severe weather from 134 issuing organizations
 *
 * Based on WFS from SWIC v3.0 that was released on 2024-03-29
 */
class WmoSevereWeatherService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), SecondaryWeatherSource {

    override val id = "wmosevereweather"
    override val name = "WMO Severe Weather Information Centre"
    override val privacyPolicyUrl = "https://wmo.int/privacy-policy"

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
    override val alertAttribution = "Hong Kong Observatory on behalf of WMO + 134 issuing organizations https://severeweather.wmo.int/sources.html"
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedForLocation(SecondaryWeatherSourceFeature.FEATURE_ALERT, location)) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

        return mAlertsApi.getAlerts(
            typeName = "local_postgis:postgis_geojsons",
            //cqlFilter = "INTERSECTS(wkb_geometry, POINT (${location.latitude} ${location.longitude})) AND (row_type EQ 'POLYGON' OR row_type EQ 'MULTIPOLYGON' OR row_type EQ 'POINT')"
            cqlFilter = "INTERSECTS(wkb_geometry, POINT (${location.latitude} ${location.longitude})) AND row_type NEQ 'BOUNDARY'"
        ).map {
            convert(it)
        }
    }

    companion object {
        private const val WMO_ALERTS_BASE_URL = "https://severeweather.wmo.int/"
        const val WMO_ALERTS_CAP_URL_BASE_URL = "https://8xieiqdnye.execute-api.us-west-2.amazonaws.com/swic/capUrl/"
        const val WMO_ALERTS_URL_BASE_URL = "https://cvzxdcwxid.execute-api.us-west-2.amazonaws.com/swic/url/"
    }
}
