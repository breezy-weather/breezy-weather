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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
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
    @Named("JsonClient") client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "mgm"
    override val name = "Meteoroloji Genel Müdürlüğü (MGM)"
    override val privacyPolicyUrl = "https://www.mgm.gov.tr/site/gizlilik-politikasi.aspx"

    override val color = Color.rgb(255, 222, 0)
    override val weatherAttribution = "Meteoroloji Genel Müdürlüğü"

    private val mApi by lazy {
        client
            .baseUrl(MGM_BASE_URL)
            .build()
            .create(MgmApi::class.java)
    }

    override val supportedFeaturesInMain = listOf<SecondaryWeatherSourceFeature>(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature?
    ): Boolean {
        return location.countryCode.equals("TR", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val hourlyStation = location.parameters.getOrElse(id) { null }?.getOrElse("hourlyStation") { null }
        val dailyStation = location.parameters.getOrElse(id) { null }?.getOrElse("dailyStation") { null }
        // Not checking hourlyStation: some rural locations in Türkiye are not assigned to one
        if (currentStation.isNullOrEmpty() || dailyStation.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"), Locale.ENGLISH)

        val current = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(currentStation).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(listOf())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        val daily = mApi.getDaily(dailyStation).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        // Some rural locations in Türkiye are not assigned to an hourlyStation
        val hourly = if (!hourlyStation.isNullOrEmpty()) {
            mApi.getHourly(hourlyStation).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(listOf())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        val today = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getAlert("today").onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(listOf())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        val tomorrow = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getAlert("tomorrow").onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(listOf())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        // can pull multiple days but seems to be an overkill
        val normals = if(!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
            mApi.getNormals(
                station = dailyStation,
                month = now.get(Calendar.MONTH) + 1,
                day = now.get(Calendar.DATE)
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(listOf())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        return Observable.zip(current, daily, hourly, today, tomorrow, normals) {
                currentResult: List<MgmCurrentResult>,
                dailyForecastResult: List<MgmDailyForecastResult>,
                hourlyForecastResult: List<MgmHourlyForecastResult>,
                todayAlertResult: List<MgmAlertResult>,
                tomorrowAlertResult: List<MgmAlertResult>,
                normalsResult: List<MgmNormalsResult>
            ->
            convert(
                context = context,
                townCode = currentStation.toInt(),
                currentResult = currentResult.getOrNull(0),
                dailyResult = dailyForecastResult.getOrNull(0),
                hourlyForecastResult = hourlyForecastResult.getOrNull(0),
                todayAlertResult = todayAlertResult,
                tomorrowAlertResult = tomorrowAlertResult,
                normalsResult = normalsResult.getOrNull(0)
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_CURRENT)
            || !isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_ALERT)
            || !isFeatureSupportedInSecondaryForLocation(location, SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val dailyStation = location.parameters.getOrElse(id) { null }?.getOrElse("dailyStation") { null }
        // Not checking hourlyStation: some rural locations in Türkiye are not assigned to one
        // also: not needed for secondary
        if (currentStation.isNullOrEmpty() || dailyStation.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"), Locale.ENGLISH)

        val current = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(currentStation).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(listOf())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        val today = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getAlert("today").onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(listOf())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        val tomorrow = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
            mApi.getAlert("tomorrow").onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(listOf())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        val normals = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
            mApi.getNormals(
                station = dailyStation,
                month = now.get(Calendar.MONTH) + 1,
                day = now.get(Calendar.DATE)
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(listOf())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(listOf())
            }
        }

        return Observable.zip(current, today, tomorrow, normals) {
                currentResult: List<MgmCurrentResult>,
                todayResult: List<MgmAlertResult>,
                tomorrowResult: List<MgmAlertResult>,
                normalsResult: List<MgmNormalsResult>
            ->
            convertSecondary(
                context = context,
                townCode = currentStation.toInt(),
                currentResult = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
                    currentResult.getOrNull(0)
                } else null,
                todayAlertResult = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
                    todayResult
                } else null,
                tomorrowAlertResult = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
                    tomorrowResult
                } else null,
                normalsResult = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
                    normalsResult.getOrNull(0)
                } else null
            )
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
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
        features: List<SecondaryWeatherSourceFeature>
    ): Boolean {
        if (coordinatesChanged) return true

        // Not checking hourlyStation: some rural locations in Türkiye are not assigned to one
        val currentStation = location.parameters.getOrElse(id) { null }?.getOrElse("currentStation") { null }
        val dailyStation = location.parameters.getOrElse(id) { null }?.getOrElse("dailyStation") { null }

        return currentStation.isNullOrEmpty() || dailyStation.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context, location: Location
    ): Observable<Map<String, String>> {
        return mApi.getLocation(
            lat = location.latitude,
            lon = location.longitude
        ).map {
            mapOf(
                "currentStation" to it.currentStationId.toString(),
                "hourlyStation" to if (it.hourlyStationId !== null) {
                    it.hourlyStationId.toString()
                } else "",
                "dailyStation" to it.dailyStationId.toString()
            )
        }
    }

    companion object {
        private const val MGM_BASE_URL = "https://servis.mgm.gov.tr/"
    }

}
