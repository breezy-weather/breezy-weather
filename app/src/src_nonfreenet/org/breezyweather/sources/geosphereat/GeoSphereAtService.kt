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

package org.breezyweather.sources.geosphereat

import android.content.Context
import android.graphics.Color
import breezyweather.domain.feature.SourceFeature
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.sources.geosphereat.json.GeoSphereAtTimeseriesResult
import org.breezyweather.sources.geosphereat.json.GeoSphereAtWarningsResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class GeoSphereAtService @Inject constructor(
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource {

    override val id = "geosphereat"
    override val name = "GeoSphere Austria"
    override val privacyPolicyUrl = "https://www.geosphere.at/de/legal"

    override val color = Color.rgb(191, 206, 64)
    override val weatherAttribution = "GeoSphere Austria (Creative Commons Attribution 4.0)"

    private val mApi by lazy {
        client
            .baseUrl(GEOSPHERE_AT_BASE_URL)
            .build()
            .create(GeoSphereAtApi::class.java)
    }
    private val mWarningApi by lazy {
        client
            .baseUrl(GEOSPHERE_AT_WARNINGS_BASE_URL)
            .build()
            .create(GeoSphereAtWarningApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_AIR_QUALITY,
        SourceFeature.FEATURE_MINUTELY,
        SourceFeature.FEATURE_ALERT
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SourceFeature?,
    ): Boolean {
        val latLng = LatLng(location.latitude, location.longitude)
        return when (feature) {
            null -> hourlyBbox.contains(latLng)
            SourceFeature.FEATURE_AIR_QUALITY -> airQuality12KmBbox.contains(latLng)
            SourceFeature.FEATURE_MINUTELY -> nowcastBbox.contains(latLng)
            SourceFeature.FEATURE_ALERT -> location.countryCode.equals("AT", ignoreCase = true)
            else -> false
        }
    }

    private val airQualityParameters = arrayOf(
        "pm25surf",
        "pm10surf",
        "no2surf",
        "o3surf"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val hourlyParameters = arrayOf(
            "sy", // Weather symbol
            "t2m", // Temperature at 2 meters
            "rr_acc", // Total precipitation amount
            "rain_acc", // Total rainfall amount
            "snow_acc", // Total surface snow amount
            "u10m", // 10 m wind speed in eastward direction
            "ugust", // u component of maximum wind gust
            "v10m", // 10 m wind speed in northward direction
            "vgust", // v component of maximum wind gust
            "rh2m", // Relative humidity 2 meters
            "tcc", // Total cloud cover
            "sp" // Surface pressure
        )

        val hourly = mApi.getHourlyForecast(
            "${location.latitude},${location.longitude}",
            hourlyParameters.joinToString(",")
        )

        val airQuality = if (!ignoreFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY) &&
            isFeatureSupportedInMainForLocation(location, SourceFeature.FEATURE_AIR_QUALITY)
        ) {
            val latLng = LatLng(location.latitude, location.longitude)
            mApi.getAirQuality(
                if (airQuality4KmBbox.contains(latLng)) 4 else 12,
                "${location.latitude},${location.longitude}",
                airQualityParameters.joinToString(",")
            )
        } else {
            Observable.just(GeoSphereAtTimeseriesResult())
        }

        val nowcast = if (!ignoreFeatures.contains(SourceFeature.FEATURE_MINUTELY) &&
            isFeatureSupportedInMainForLocation(location, SourceFeature.FEATURE_MINUTELY)
        ) {
            mApi.getNowcast(
                "${location.latitude},${location.longitude}",
                "rr" // precipitation sum
            )
        } else {
            Observable.just(GeoSphereAtTimeseriesResult())
        }

        val alerts = if (!ignoreFeatures.contains(SourceFeature.FEATURE_ALERT) &&
            isFeatureSupportedInMainForLocation(location, SourceFeature.FEATURE_ALERT)
        ) {
            mWarningApi.getWarningsForCoords(
                location.longitude,
                location.latitude,
                if (context.currentLocale.code == "de") "de" else "en"
            )
        } else {
            Observable.just(GeoSphereAtWarningsResult())
        }

        return Observable.zip(
            hourly,
            airQuality,
            nowcast,
            alerts
        ) { hourlyResult, airQualityResult, nowcastResult, alertsResult ->
            convert(hourlyResult, airQualityResult, nowcastResult, alertsResult, location)
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_AIR_QUALITY,
        SourceFeature.FEATURE_MINUTELY,
        SourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = null
    override val airQualityAttribution = weatherAttribution
    override val pollenAttribution = null
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        // TODO: Should be checked earlier for each requested feature
        if (requestedFeatures.contains(SourceFeature.FEATURE_MINUTELY) &&
            !isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_MINUTELY)
        ) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }
        if (requestedFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY) &&
            !isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_AIR_QUALITY)
        ) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }
        if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT) &&
            !isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_ALERT)
        ) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

        val airQuality = if (requestedFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY)) {
            val latLng = LatLng(location.latitude, location.longitude)
            mApi.getAirQuality(
                if (airQuality4KmBbox.contains(latLng)) 4 else 12,
                "${location.latitude},${location.longitude}",
                airQualityParameters.joinToString(",")
            )
        } else {
            Observable.just(GeoSphereAtTimeseriesResult())
        }

        val nowcast = if (requestedFeatures.contains(SourceFeature.FEATURE_MINUTELY)) {
            mApi.getNowcast(
                "${location.latitude},${location.longitude}",
                "rr" // precipitation sum
            )
        } else {
            Observable.just(GeoSphereAtTimeseriesResult())
        }

        val alerts = if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mWarningApi.getWarningsForCoords(
                location.longitude,
                location.latitude,
                if (context.currentLocale.code == "de") "de" else "en"
            )
        } else {
            Observable.just(GeoSphereAtWarningsResult())
        }

        return Observable.zip(airQuality, nowcast, alerts) { airQualityResult, nowcastResult, alertsResult ->
            convertSecondary(airQualityResult, nowcastResult, alertsResult)
        }
    }

    companion object {
        private const val GEOSPHERE_AT_BASE_URL = "https://dataset.api.hub.geosphere.at/"
        private const val GEOSPHERE_AT_WARNINGS_BASE_URL = "https://warnungen.zamg.at/wsapp/api/"

        val hourlyBbox = LatLngBounds.parse(west = 5.49, south = 42.98, east = 22.1, north = 51.82)
        val airQuality12KmBbox = LatLngBounds.parse(west = -59.21, south = 17.65, east = 83.21, north = 76.49)
        val airQuality4KmBbox = LatLngBounds.parse(west = 4.31, south = 41.72, east = 18.99, north = 50.15)
        val nowcastBbox = LatLngBounds.parse(west = 8.1, south = 45.5, east = 17.74, north = 49.48)
    }
}
