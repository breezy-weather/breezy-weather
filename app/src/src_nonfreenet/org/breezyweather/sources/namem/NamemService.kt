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
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.namem.json.NamemAirQualityResult
import org.breezyweather.sources.namem.json.NamemCurrentResult
import org.breezyweather.sources.namem.json.NamemDailyResult
import org.breezyweather.sources.namem.json.NamemHourlyResult
import org.breezyweather.sources.namem.json.NamemNormalsResult
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlin.text.startsWith

class NamemService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "namem"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("mn")) {
            "Цаг уур, орчны шинжилгээний газар"
        } else {
            "NAMEM (${Locale(context.currentLocale.code, "MN").displayCountry})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = ""

    override val color = Color.rgb(3, 105, 161)

    private val mApi by lazy {
        client
            .baseUrl(NAMEM_BASE_URL)
            .build()
            .create(NamemApi::class.java)
    }

    private val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("mn")) {
            "Цаг уур, орчны шинжилгээний газар"
        } else {
            "National Agency for Meteorology and Environmental Monitoring"
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("MN", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val stationId = location.parameters.getOrElse(id) { null }?.getOrElse("stationId") { null }?.toLongOrNull()
        if (stationId == null) {
            return Observable.error(InvalidLocationException())
        }
        val body = """{"sid":$stationId}"""

        val failedFeatures = mutableListOf<SourceFeature>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.CURRENT)
                Observable.just(NamemCurrentResult())
            }
        } else {
            Observable.just(NamemCurrentResult())
        }

        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FORECAST)
                Observable.just(NamemHourlyResult())
            }
        } else {
            Observable.just(NamemHourlyResult())
        }

        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FORECAST)
                Observable.just(NamemDailyResult())
            }
        } else {
            Observable.just(NamemDailyResult())
        }

        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getNormals(
                body = body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.NORMALS)
                Observable.just(NamemNormalsResult())
            }
        } else {
            Observable.just(NamemNormalsResult())
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mApi.getAirQuality().onErrorResumeNext {
                failedFeatures.add(SourceFeature.AIR_QUALITY)
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
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(
                        current = getAirQuality(location, airQualityResult)
                    )
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(normalsResult)
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
