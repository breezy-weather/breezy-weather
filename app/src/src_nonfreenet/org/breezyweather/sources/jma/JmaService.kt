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

package org.breezyweather.sources.jma

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.jma.json.JmaAlertResult
import org.breezyweather.sources.jma.json.JmaAmedasResult
import org.breezyweather.sources.jma.json.JmaAreasResult
import org.breezyweather.sources.jma.json.JmaBulletinResult
import org.breezyweather.sources.jma.json.JmaCurrentResult
import org.breezyweather.sources.jma.json.JmaDailyResult
import org.breezyweather.sources.jma.json.JmaForecastAreaResult
import org.breezyweather.sources.jma.json.JmaHourlyResult
import org.breezyweather.sources.jma.json.JmaWeekAreaResult
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.floor
import kotlin.time.Duration.Companion.hours

class JmaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "jma"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "気象庁"
        } else {
            "JMA (${Locale(context.currentLocale.code, "JP").displayCountry})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "https://www.jma.go.jp/jma/kishou/info/coment.html"
        } else {
            "https://www.jma.go.jp/jma/en/copyright.html"
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(JMA_BASE_URL)
            .build()
            .create(JmaApi::class.java)
    }

    private val okHttpClient = OkHttpClient()

    private val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "気象庁"
        } else {
            "Japan Meteorological Agency"
        }
    }
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isReverseGeocodingSupportedForLocation(location)
    }

    override fun isReverseGeocodingSupportedForLocation(location: Location): Boolean {
        return location.countryCode.equals("JP", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val parameters = location.parameters.getOrElse(id) { null }
        val class20s = parameters?.getOrElse("class20s") { null }
        val class10s = parameters?.getOrElse("class10s") { null }
        val prefArea = parameters?.getOrElse("prefArea") { null }
        val weekArea05 = parameters?.getOrElse("weekArea05") { null }
        val weekAreaAmedas = parameters?.getOrElse("weekAreaAmedas") { null }
        val forecastAmedas = parameters?.getOrElse("forecastAmedas") { null }
        val currentAmedas = parameters?.getOrElse("currentAmedas") { null }

        if (class20s.isNullOrEmpty() ||
            class10s.isNullOrEmpty() ||
            prefArea.isNullOrEmpty() ||
            weekArea05.isNullOrEmpty() ||
            weekAreaAmedas.isNullOrEmpty() ||
            forecastAmedas.isNullOrEmpty() ||
            currentAmedas.isNullOrEmpty()
        ) {
            return Observable.error(InvalidLocationException())
        }

        // Special case for Amami, Kagoshima Prefecture
        val forecastPrefArea = if (prefArea == "460040") {
            "460100"
        } else {
            prefArea
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily(forecastPrefArea).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly(class10s).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(JmaHourlyResult())
            }
        } else {
            Observable.just(JmaHourlyResult())
        }

        // CURRENT
        // Need to first get the correct timestamp for latest observation data.
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            val request = Request.Builder().url(JMA_BASE_URL + "bosai/amedas/data/latest_time.txt").build()
            val incomingFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
            val outgoingFormatter = SimpleDateFormat("yyyyMMdd_HH", Locale.ENGLISH)
            incomingFormatter.timeZone = TimeZone.getTimeZone("Asia/Tokyo")
            outgoingFormatter.timeZone = TimeZone.getTimeZone("Asia/Tokyo")

            okHttpClient.newCall(request).execute().use { call ->
                if (call.isSuccessful) {
                    val latestTime = incomingFormatter.parse(call.body!!.string())!!.time

                    // Observation data is recorded in 3-hourly files.
                    val timestamp = (
                        floor(latestTime.toDouble() / 3.hours.inWholeMilliseconds) *
                            3.hours.inWholeMilliseconds
                        ).toLong()
                    mApi.getCurrent(
                        amedas = currentAmedas,
                        timestamp = outgoingFormatter.format(timestamp)
                    ).onErrorResumeNext {
                        failedFeatures[SourceFeature.CURRENT] = it
                        Observable.just(emptyMap())
                    }
                } else {
                    failedFeatures[SourceFeature.CURRENT] = WeatherException()
                    Observable.just(emptyMap())
                }
            }
        } else {
            Observable.just(emptyMap())
        }

        val bulletin = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getBulletin(forecastPrefArea).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(JmaBulletinResult())
            }
        } else {
            Observable.just(JmaBulletinResult())
        }

        // ALERT
        val alert = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlert(prefArea).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(JmaAlertResult())
            }
        } else {
            Observable.just(JmaAlertResult())
        }

        return Observable.zip(current, bulletin, daily, hourly, alert) {
                currentResult: Map<String, JmaCurrentResult>,
                bulletinResult: JmaBulletinResult,
                dailyResult: List<JmaDailyResult>,
                hourlyResult: JmaHourlyResult,
                alertResult: JmaAlertResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyResult, class10s, weekArea05, weekAreaAmedas, forecastAmedas)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult, bulletinResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, alertResult, class20s)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(dailyResult, weekAreaAmedas)
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
        val areas = mApi.getAreas()
        val class20s = mApi.getRelm().map { relm ->
            val features = mutableListOf<Any?>()
            relm.forEachIndexed { i, it ->
                if (location.latitude <= it.ne[0] &&
                    location.longitude <= it.ne[1] &&
                    location.latitude >= it.sw[0] &&
                    location.longitude >= it.sw[1]
                ) {
                    features.addAll(mApi.getClass20s(i).blockingFirst().features)
                }
            }
            features
        }

        return Observable.zip(areas, class20s) {
                areasResult: JmaAreasResult,
                class20sFeatures: List<Any?>,
            ->
            convert(context, location, areasResult, class20sFeatures)
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val parameters = location.parameters.getOrElse(id) { null }

        val class20s = parameters?.getOrElse("class20s") { null }
        val class10s = parameters?.getOrElse("class10s") { null }
        val prefArea = parameters?.getOrElse("prefArea") { null }
        val weekArea05 = parameters?.getOrElse("weekArea05") { null }
        val weekAreaAmedas = parameters?.getOrElse("weekAreaAmedas") { null }
        val forecastAmedas = parameters?.getOrElse("forecastAmedas") { null }
        val currentAmedas = parameters?.getOrElse("currentAmedas") { null }

        return class20s.isNullOrEmpty() ||
            class10s.isNullOrEmpty() ||
            prefArea.isNullOrEmpty() ||
            weekArea05.isNullOrEmpty() ||
            weekAreaAmedas.isNullOrEmpty() ||
            forecastAmedas.isNullOrEmpty() ||
            currentAmedas.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val areas = mApi.getAreas()
        val class20s = mApi.getRelm().map { relm ->
            val features = mutableListOf<Any?>()
            relm.forEachIndexed { i, it ->
                if (location.latitude <= it.ne[0] &&
                    location.longitude <= it.ne[1] &&
                    location.latitude >= it.sw[0] &&
                    location.longitude >= it.sw[1]
                ) {
                    features.addAll(mApi.getClass20s(i).blockingFirst().features)
                }
            }
            features
        }
        val weekArea = mApi.getWeekArea()
        val weekArea05 = mApi.getWeekArea05()
        val forecastArea = mApi.getForecastArea()
        val amedas = mApi.getAmedas()

        return Observable.zip(areas, class20s, weekArea, weekArea05, forecastArea, amedas) {
                areasResult: JmaAreasResult,
                class20sFeatures: List<Any?>,
                weekAreaResult: Map<String, List<JmaWeekAreaResult>>,
                weekArea05Result: Map<String, List<String>>,
                forecastAreaResult: Map<String, List<JmaForecastAreaResult>>,
                amedasResult: Map<String, JmaAmedasResult>,
            ->
            convert(
                location = location,
                areasResult = areasResult,
                class20sFeatures = class20sFeatures,
                weekAreaResult = weekAreaResult,
                weekArea05Result = weekArea05Result,
                forecastAreaResult = forecastAreaResult,
                amedasResult = amedasResult
            )
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val JMA_BASE_URL = "https://www.jma.go.jp/"
    }
}
