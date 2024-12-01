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
import android.graphics.Color
import breezyweather.domain.feature.SourceFeature
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
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
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource, LocationParametersSource {
    override val id = "jma"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "気象庁"
        } else {
            "Japan Meteorological Agency"
        }
    }
    override val privacyPolicyUrl by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "https://www.jma.go.jp/jma/kishou/info/coment.html"
        } else {
            "https://www.jma.go.jp/jma/en/copyright.html"
        }
    }
    override val color = Color.rgb(64, 86, 106)
    override val weatherAttribution by lazy {
        if (context.currentLocale.code.startsWith("ja")) {
            "気象庁"
        } else {
            "Japan Meteorological Agency"
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(JMA_BASE_URL)
            .build()
            .create(JmaApi::class.java)
    }

    private val okHttpClient = OkHttpClient()

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_NORMALS,
        SourceFeature.FEATURE_ALERT
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SourceFeature?,
    ): Boolean {
        return location.countryCode.equals("JP", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
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

        val failedFeatures = mutableListOf<SourceFeature>()
        val daily = mApi.getDaily(forecastPrefArea)
        val hourly = mApi.getHourly(class10s)

        // CURRENT
        // Need to first get the correct timestamp for latest observation data.
        val current = if (!ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
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
                        failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                        Observable.just(emptyMap())
                    }
                } else {
                    failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                    Observable.just(emptyMap())
                }
            }
        } else {
            Observable.just(emptyMap())
        }

        val bulletin = if (!ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getBulletin(forecastPrefArea).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(JmaBulletinResult())
            }
        } else {
            Observable.just(JmaBulletinResult())
        }

        // ALERT
        val alert = if (!ignoreFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getAlert(prefArea).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_ALERT)
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
            convert(
                context = context,
                location = location,
                currentResult = currentResult,
                bulletinResult = bulletinResult,
                dailyResult = dailyResult,
                hourlyResult = hourlyResult,
                alertResult = alertResult,
                ignoreFeatures = ignoreFeatures,
                failedFeatures = failedFeatures
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_ALERT,
        SourceFeature.FEATURE_NORMALS
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
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
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
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

        val failedFeatures = mutableListOf<SourceFeature>()
        // CURRENT
        // Need to first get the correct timestamp for latest observation data.
        val current = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
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
                        failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                        Observable.just(emptyMap())
                    }
                } else {
                    failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                    Observable.just(emptyMap())
                }
            }
        } else {
            Observable.just(emptyMap())
        }

        val bulletin = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getBulletin(forecastPrefArea).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(JmaBulletinResult())
            }
        } else {
            Observable.just(JmaBulletinResult())
        }

        // NORMALS
        // Normals are recorded in the daily forecast.
        val daily = if (requestedFeatures.contains(SourceFeature.FEATURE_NORMALS)) {
            mApi.getDaily(forecastPrefArea).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_NORMALS)
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        // ALERT
        val alert = if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getAlert(prefArea).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_ALERT)
                Observable.just(JmaAlertResult())
            }
        } else {
            Observable.just(JmaAlertResult())
        }

        return Observable.zip(current, bulletin, daily, alert) {
                currentResult: Map<String, JmaCurrentResult>,
                bulletinResult: JmaBulletinResult,
                dailyResult: List<JmaDailyResult>,
                alertResult: JmaAlertResult,
            ->
            convertSecondary(
                context = context,
                location = location,
                currentResult = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
                    currentResult
                } else {
                    null
                },
                bulletinResult = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
                    bulletinResult
                } else {
                    null
                },
                dailyResult = if (requestedFeatures.contains(SourceFeature.FEATURE_NORMALS)) {
                    dailyResult
                } else {
                    null
                },
                alertResult = if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
                    alertResult
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
            SecondaryWeatherWrapper()
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

    companion object {
        private const val JMA_BASE_URL = "https://www.jma.go.jp/"
    }
}
