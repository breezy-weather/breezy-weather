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

package org.breezyweather.sources.mgm

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.mgm.json.MgmAlertResult
import org.breezyweather.sources.mgm.json.MgmCurrentResult
import org.breezyweather.sources.mgm.json.MgmDailyForecastResult
import org.breezyweather.sources.mgm.json.MgmHourlyForecastResult
import org.breezyweather.sources.mgm.json.MgmNormalsResult
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named

class MgmService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "mgm"
    override val name = "MGM (${Locale(context.currentLocale.code, "TR").displayCountry})"
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = "https://www.mgm.gov.tr/site/gizlilik-politikasi.aspx"

    private val mApi by lazy {
        client
            .baseUrl(MGM_BASE_URL)
            .build()
            .create(MgmApi::class.java)
    }

    private val weatherAttribution = "Meteoroloji Genel Müdürlüğü"
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.mgm.gov.tr/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isReverseGeocodingSupportedForLocation(location)
    }

    override fun isReverseGeocodingSupportedForLocation(location: Location): Boolean {
        return location.countryCode.equals("TR", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val hourlyStation = location.parameters.getOrElse(id) { null }?.getOrElse("hourlyStation") { null }
        val dailyStation = location.parameters.getOrElse(id) { null }?.getOrElse("dailyStation") { null }
        // Not checking hourlyStation: some rural locations in Türkiye are not assigned to one
        if (currentStation.isNullOrEmpty() || dailyStation.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"), Locale.ENGLISH)

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(currentStation).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily(dailyStation).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        // Some rural locations in Türkiye are not assigned to an hourlyStation
        val hourly = if (SourceFeature.FORECAST in requestedFeatures && !hourlyStation.isNullOrEmpty()) {
            mApi.getHourly(hourlyStation).onErrorResumeNext {
                /*if (BreezyWeather.instance.debugMode) {
                    failedFeatures.add(SourceFeature.OTHER)
                }*/
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val todayAlerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlert("today").onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        val tomorrowAlerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlert("tomorrow").onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        // can pull multiple days but seems to be an overkill
        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getNormals(
                station = dailyStation,
                month = now.get(Calendar.MONTH) + 1,
                day = now.get(Calendar.DATE)
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, daily, hourly, todayAlerts, tomorrowAlerts, normals) {
                currentResult: List<MgmCurrentResult>,
                dailyForecastResult: List<MgmDailyForecastResult>,
                hourlyForecastResult: List<MgmHourlyForecastResult>,
                todayAlertResult: List<MgmAlertResult>,
                tomorrowAlertResult: List<MgmAlertResult>,
                normalsResult: List<MgmNormalsResult>,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyForecastResult.getOrNull(0))
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, hourlyForecastResult.getOrNull(0)?.forecast)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult.getOrNull(0))
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(currentStation.toInt(), todayAlertResult, tomorrowAlertResult)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(normalsResult.getOrNull(0))
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
        return mApi.getLocation(
            lat = location.latitude,
            lon = location.longitude
        ).map {
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it))
            locationList
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        // Not checking hourlyStation: some rural locations in Türkiye are not assigned to one
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val dailyStation = location.parameters.getOrElse(id) { null }?.getOrElse("dailyStation") { null }

        return currentStation.isNullOrEmpty() || dailyStation.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getLocation(
            lat = location.latitude,
            lon = location.longitude
        ).map {
            mapOf(
                "currentStation" to it.currentStationId.toString(),
                "hourlyStation" to if (it.hourlyStationId !== null) {
                    it.hourlyStationId.toString()
                } else {
                    ""
                },
                "dailyStation" to it.dailyStationId.toString()
            )
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val MGM_BASE_URL = "https://servis.mgm.gov.tr/"
    }
}
