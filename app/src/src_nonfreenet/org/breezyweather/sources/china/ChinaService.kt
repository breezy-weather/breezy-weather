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
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.china.json.ChinaForecastResult
import org.breezyweather.sources.china.json.ChinaMinutelyResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class ChinaService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, LocationSearchSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "china"
    override val name = context.currentLocale.getCountryName("CN")
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "https://privacy.mi.com/all/zh_CN"
                else -> "https://privacy.mi.com/all/en_US"
            }
        }
    }

    override val locationSearchAttribution = "北京天气、彩云天气、中国环境监测总站"

    private val mApi by lazy {
        client
            .baseUrl(CHINA_WEATHER_BASE_URL)
            .build()
            .create(ChinaApi::class.java)
    }

    private val weatherAttribution = "北京天气、彩云天气、中国环境监测总站"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to name
    )
    override val attributionLinks = mapOf(
        "彩云天气" to "https://caiyunapp.com/",
        "中国环境监测总站" to "https://www.cnemc.cn/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("CN", ignoreCase = true)
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
        val locationKey = location.parameters.getOrElse(id) { null }?.getOrElse("locationKey") { null }

        if (locationKey.isNullOrEmpty()) {
            return if (location.isCurrentPosition) {
                Observable.error(ReverseGeocodingException())
            } else {
                Observable.error(InvalidLocationException())
            }
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val main = if (SourceFeature.FORECAST in requestedFeatures ||
            SourceFeature.CURRENT in requestedFeatures ||
            SourceFeature.AIR_QUALITY in requestedFeatures ||
            SourceFeature.ALERT in requestedFeatures
        ) {
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
            ).onErrorResumeNext {
                if (SourceFeature.FORECAST in requestedFeatures) {
                    failedFeatures[SourceFeature.FORECAST] = it
                }
                if (SourceFeature.CURRENT in requestedFeatures) {
                    failedFeatures[SourceFeature.CURRENT] = it
                }
                if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    failedFeatures[SourceFeature.AIR_QUALITY] = it
                }
                if (SourceFeature.ALERT in requestedFeatures) {
                    failedFeatures[SourceFeature.ALERT] = it
                }
                Observable.just(ChinaForecastResult())
            }
        } else {
            Observable.just(ChinaForecastResult())
        }
        val minutely = if (SourceFeature.MINUTELY in requestedFeatures) {
            mApi.getMinutelyWeather(
                location.latitude,
                location.longitude,
                context.currentLocale.toString().lowercase(),
                isGlobal = false,
                appKey = CHINA_APP_KEY,
                locationKey = "weathercn%3A$locationKey",
                sign = CHINA_SIGN
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.MINUTELY] = it
                Observable.just(ChinaMinutelyResult())
            }
        } else {
            Observable.just(ChinaMinutelyResult())
        }
        return Observable.zip(main, minutely) { mainResult: ChinaForecastResult, minutelyResult: ChinaMinutelyResult ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(
                        mainResult.current?.pubTime,
                        location,
                        mainResult.forecastDaily
                    )
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(
                        mainResult.current?.pubTime,
                        location,
                        mainResult.forecastHourly
                    )
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(mainResult.current, minutelyResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    mainResult.aqi?.let {
                        AirQualityWrapper(
                            current = AirQuality(
                                pM25 = it.pm25?.toDoubleOrNull(),
                                pM10 = it.pm10?.toDoubleOrNull(),
                                sO2 = it.so2?.toDoubleOrNull(),
                                nO2 = it.no2?.toDoubleOrNull(),
                                o3 = it.o3?.toDoubleOrNull(),
                                cO = it.co?.toDoubleOrNull()
                            )
                        )
                    }
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(
                        location,
                        minutelyResult
                    )
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(mainResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getLocationSearch(
            query,
            context.currentLocale.code
        ).map { results ->
            results
                .filter { it.locationKey?.startsWith("weathercn:") == true && it.status == 0 }
                .map { convert(it) }
        }
    }

    override fun requestNearestLocation(
        context: Context,
        location: Location,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getLocationByGeoPosition(
            location.latitude,
            location.longitude,
            context.currentLocale.code
        ).map { results ->
            results
                .filter { it.locationKey?.startsWith("weathercn:") == true && it.status == 0 }
                .map { convert(it) }
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentLocationKey = location.parameters
            .getOrElse(id) { null }?.getOrElse("locationKey") { null }

        return currentLocationKey.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getLocationByGeoPosition(
            location.latitude,
            location.longitude,
            context.currentLocale.code
        ).map {
            if (it.getOrNull(0)?.locationKey?.startsWith("weathercn:") == true && it[0].status == 0) {
                mapOf("locationKey" to it[0].locationKey!!.replace("weathercn:", ""))
            } else {
                throw InvalidLocationException()
            }
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val CHINA_WEATHER_BASE_URL = "https://weatherapi.market.xiaomi.com/wtr-v3/"
        private const val CHINA_APP_KEY = "weather20151024"
        private const val CHINA_SIGN = "zUFJoAR2ZVrDy1vF3D07"
    }
}
