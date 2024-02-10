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

package org.breezyweather.sources.nws

import android.content.Context
import android.graphics.Color
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ParameterizedLocationSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.brightsky.json.BrightSkyAlertsResult
import org.breezyweather.sources.nws.json.NwsAlertsResult
import retrofit2.Retrofit
import javax.inject.Inject

class NwsService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, ParameterizedLocationSource {

    override val id = "nws"
    override val name = "National Weather Service (NWS)"
    override val privacyPolicyUrl = "https://www.weather.gov/privacy"

    override val color = Color.rgb(51, 176, 225)
    override val weatherAttribution = "National Weather Service (NWS)"

    private val mApi by lazy {
        client
            .baseUrl(NWS_BASE_URL)
            .build()
            .create(NwsApi::class.java)
    }

    override val supportedFeaturesInMain = listOf<SecondaryWeatherSourceFeature>(
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )

    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val gridId = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridId") { null }
        val gridX = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridX") { null }
        val gridY = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridY") { null }

        if (gridId.isNullOrEmpty() || gridX.isNullOrEmpty() || gridY.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val nwsForecastResult = mApi.getForecast(
            userAgent,
            gridId,
            gridX.toInt(),
            gridY.toInt(),
            "si"
        )

        val nwsAlertsResult = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getActiveAlerts(
                userAgent,
                "${location.latitude},${location.longitude}"
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(NwsAlertsResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(NwsAlertsResult())
            }
        }

        return Observable.zip(
            nwsForecastResult,
            nwsAlertsResult
        ) {
            forecastResult,
            alertResult
            ->
            convert(forecastResult, alertResult, location.timeZone)
        }
    }


    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SecondaryWeatherSourceFeature>
    ): Boolean {
        // Not needed for alert endpoint
        if (features.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) return false
        if (coordinatesChanged) return true

        val currentGridId = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridId") { null }
        val currentGridX = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridX") { null }
        val currentGridY = location.parameters
            .getOrElse(id) { null }?.getOrElse("gridY") { null }

        return currentGridId.isNullOrEmpty() || currentGridX.isNullOrEmpty()
                || currentGridY.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context, location: Location
    ): Observable<Map<String, String>> {
        return mApi.getPoints(
            userAgent,
            location.latitude,
            location.longitude
        ).map {
            if (it.properties == null) {
                throw InvalidLocationException()
            }
            mapOf(
                "gridId" to it.properties.gridId,
                "gridX" to it.properties.gridX.toString(),
                "gridY" to it.properties.gridY.toString()
            )
        }
    }

    companion object {
        private const val NWS_BASE_URL = "https://api.weather.gov/"
        private const val userAgent =
            "(BreezyWeather/${BuildConfig.VERSION_NAME}, github.com/breezy-weather/breezy-weather/issues)"
    }
}