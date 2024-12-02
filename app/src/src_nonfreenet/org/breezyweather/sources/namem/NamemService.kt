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

package org.breezyweather.sources.namem

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.sources.namem.json.NamemAirQualityResult
import org.breezyweather.sources.namem.json.NamemCurrentResult
import org.breezyweather.sources.namem.json.NamemDailyResult
import org.breezyweather.sources.namem.json.NamemHourlyResult
import org.breezyweather.sources.namem.json.NamemNormalsResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import kotlin.text.startsWith

class NamemService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "namem"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("mn")) {
            "Цаг уур, орчны шинжилгээний газар"
        } else {
            "NAMEM"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = ""

    override val color = Color.rgb(3, 105, 161)
    override val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("mn")) {
            "Цаг уур, орчны шинжилгээний газар"
        } else {
            "National Agency for Meteorology and Environmental Monitoring"
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(NAMEM_BASE_URL)
            .build()
            .create(NamemApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_NORMALS,
        SourceFeature.FEATURE_AIR_QUALITY
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SourceFeature?,
    ): Boolean {
        return location.countryCode.equals("MN", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }?.toLongOrNull()
        if (stationId == null) {
            return Observable.error(InvalidLocationException())
        }
        val body = """{"sid":$stationId}"""

        val failedFeatures = mutableListOf<SourceFeature>()
        val current = if (!ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(NamemCurrentResult())
            }
        } else {
            Observable.just(NamemCurrentResult())
        }

        val hourly = mApi.getHourly(
            body = body.toRequestBody("application/json".toMediaTypeOrNull())
        )

        val daily = mApi.getDaily(
            body = body.toRequestBody("application/json".toMediaTypeOrNull())
        )

        val normals = if (!ignoreFeatures.contains(SourceFeature.FEATURE_NORMALS)) {
            mApi.getNormals(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_NORMALS)
                Observable.just(NamemNormalsResult())
            }
        } else {
            Observable.just(NamemNormalsResult())
        }

        val airQuality = if (!ignoreFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getAirQuality().onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_AIR_QUALITY)
                Observable.just(NamemAirQualityResult())
            }
        } else {
            Observable.just(NamemAirQualityResult())
        }

        return Observable.zip(current, daily, hourly, normals, airQuality) {
                currentResult: NamemCurrentResult,
                dailyResult: NamemDailyResult,
                hourlyResult: NamemHourlyResult,
                normalsResult: NamemNormalsResult,
                airQualityResult: NamemAirQualityResult,
            ->
            convert(
                context = context,
                location = location,
                currentResult = currentResult,
                normalsResult = normalsResult,
                dailyResult = dailyResult,
                hourlyResult = hourlyResult,
                airQualityResult = airQualityResult,
                failedFeatures = failedFeatures
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_NORMALS,
        SourceFeature.FEATURE_AIR_QUALITY
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = weatherAttribution
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = null
    override val normalsAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_NORMALS) ||
            !isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_CURRENT) ||
            !isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_AIR_QUALITY)
        ) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }?.toLongOrNull()
        if (stationId == null) {
            return Observable.error(InvalidLocationException())
        }

        val body = """{"sid":$stationId}"""

        val failedFeatures = mutableListOf<SourceFeature>()
        val current = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(NamemCurrentResult())
            }
        } else {
            Observable.just(NamemCurrentResult())
        }

        val normals = if (requestedFeatures.contains(SourceFeature.FEATURE_NORMALS)) {
            mApi.getNormals(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_NORMALS)
                Observable.just(NamemNormalsResult())
            }
        } else {
            Observable.just(NamemNormalsResult())
        }

        val airQuality = if (requestedFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getAirQuality().onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_AIR_QUALITY)
                Observable.just(NamemAirQualityResult())
            }
        } else {
            Observable.just(NamemAirQualityResult())
        }

        return Observable.zip(current, normals, airQuality) {
                currentResult: NamemCurrentResult,
                normalsResult: NamemNormalsResult,
                airQualityResult: NamemAirQualityResult,
            ->
            convertSecondary(
                context = context,
                location = location,
                currentResult = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
                    currentResult
                } else {
                    null
                },
                normalsResult = if (requestedFeatures.contains(SourceFeature.FEATURE_NORMALS)) {
                    normalsResult
                } else {
                    null
                },
                airQualityResult = if (requestedFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY)) {
                    airQualityResult
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getStations().map {
            convert(location, it.locations)
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val sid = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }
        return sid.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getStations().map {
            getLocationParameters(location, it.locations)
        }
    }

    companion object {
        private const val NAMEM_BASE_URL = "https://weather.gov.mn/"
    }
}
