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

package org.breezyweather.sources.china

import android.content.Context
import android.graphics.Color
import androidx.compose.ui.text.toLowerCase
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.china.json.ChinaForecastResult
import org.breezyweather.sources.china.json.ChinaMinutelyResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class ChinaService @Inject constructor(
    @Named("JsonClient") client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource,
    LocationSearchSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "china"
    override val name = "中国"
    override val privacyPolicyUrl = "https://privacy.mi.com/all/zh_CN"

    override val color = Color.rgb(255, 105, 0)
    override val weatherAttribution = "北京天气、彩云天气、中国环境监测总站"
    override val locationSearchAttribution = "北京天气、彩云天气、中国环境监测总站"

    private val mApi by lazy {
        client
            .baseUrl(CHINA_WEATHER_BASE_URL)
            .build()
            .create(ChinaApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature?
    ): Boolean {
        return location.countryCode.equals("CN", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context, location: Location, ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val locationKey = location.parameters
            .getOrElse(id) { null }?.getOrElse("locationKey") { null }

        if (locationKey.isNullOrEmpty()) {
            return if (location.isCurrentPosition) {
                Observable.error(ReverseGeocodingException())
            } else {
                Observable.error(InvalidLocationException())
            }
        }

        val mainly = mApi.getForecastWeather(
            location.latitude,
            location.longitude,
            location.isCurrentPosition,
            locationKey = "weathercn%3A$locationKey",
            days = 15,
            appKey = CHINA_APP_KEY,
            sign = CHINA_SIGN,
            isGlobal = false,
            context.currentLocale.toString().lowercase()
        )
        val minutely = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mApi.getMinutelyWeather(
                location.latitude,
                location.longitude,
                context.currentLocale.toString().lowercase(),
                isGlobal = false,
                appKey = CHINA_APP_KEY,
                locationKey = "weathercn%3A$locationKey",
                sign = CHINA_SIGN
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(ChinaMinutelyResult())
            }
        }
        return Observable.zip(mainly, minutely) {
                mainlyResult: ChinaForecastResult,
                minutelyResult: ChinaMinutelyResult
            ->
            convert(
                location,
                mainlyResult,
                minutelyResult
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location, feature: SecondaryWeatherSourceFeature
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val airQualityAttribution = weatherAttribution
    override val pollenAttribution = null
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        val locationKey = location.parameters
            .getOrElse(id) { null }?.getOrElse("locationKey") { null }

        if (locationKey.isNullOrEmpty()) {
            return if (location.isCurrentPosition) {
                Observable.error(ReverseGeocodingException())
            } else {
                Observable.error(InvalidLocationException())
            }
        }

        val mainly = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT) ||
            requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getForecastWeather(
                location.latitude,
                location.longitude,
                location.isCurrentPosition,
                locationKey = "weathercn%3A$locationKey",
                days = 15,
                appKey = CHINA_APP_KEY,
                sign = CHINA_SIGN,
                isGlobal = false,
                context.currentLocale.toString().lowercase()
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(ChinaForecastResult())
            }
        }

        val minutely = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mApi.getMinutelyWeather(
                location.latitude,
                location.longitude,
                context.currentLocale.toString().lowercase(),
                isGlobal = false,
                appKey = CHINA_APP_KEY,
                locationKey = "weathercn%3A$locationKey",
                sign = CHINA_SIGN
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(ChinaMinutelyResult())
            }
        }

        return Observable.zip(mainly, minutely) {
                mainlyResult: ChinaForecastResult,
                minutelyResult: ChinaMinutelyResult
            ->
            convertSecondary(
                location,
                mainlyResult,
                minutelyResult
            )
        }
    }

    override fun requestLocationSearch(
        context: Context, query: String
    ): Observable<List<Location>> {
        return mApi.getLocationSearch(
            query,
            context.currentLocale.code
        )
            .map { results ->
                val locationList = mutableListOf<Location>()
                results.forEach {
                    if (it.locationKey?.startsWith("weathercn:") == true && it.status == 0) {
                        locationList.add(convert(null, it))
                    }
                }
                locationList
            }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        return mApi.getLocationByGeoPosition(
            location.latitude,
            location.longitude,
            context.currentLocale.code
        )
            .map {
                val locationList = mutableListOf<Location>()
                if (it.getOrNull(0)?.locationKey?.startsWith("weathercn:") == true &&
                    it[0].status == 0) {
                    locationList.add(convert(location, it[0]))
                }
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

        val currentLocationKey = location.parameters
            .getOrElse(id) { null }?.getOrElse("locationKey") { null }

        return currentLocationKey.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context, location: Location
    ): Observable<Map<String, String>> {
        return mApi.getLocationByGeoPosition(
            location.latitude,
            location.longitude,
            context.currentLocale.code
        ).map {
            if (it.getOrNull(0)?.locationKey?.startsWith("weathercn:") == true &&
                it[0].status == 0) {
                mapOf(
                    "locationKey" to it[0].locationKey!!.replace("weathercn:", "")
                )
            } else {
                throw InvalidLocationException()
            }
        }
    }

    companion object {
        private const val CHINA_WEATHER_BASE_URL = "https://weatherapi.market.xiaomi.com/wtr-v3/"
        private const val CHINA_APP_KEY = "weather20151024"
        private const val CHINA_SIGN = "zUFJoAR2ZVrDy1vF3D07"
    }
}
