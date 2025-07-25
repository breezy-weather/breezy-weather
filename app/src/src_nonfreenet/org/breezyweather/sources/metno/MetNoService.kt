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

package org.breezyweather.sources.metno

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.metno.json.MetNoAirQualityResult
import org.breezyweather.sources.metno.json.MetNoAlertResult
import org.breezyweather.sources.metno.json.MetNoForecastResult
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class MetNoService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    override val id = "metno"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("no") -> "Meteorologisk institutt"
                else -> "MET Norway"
            }
        }
    }
    override val continent = SourceContinent.WORLDWIDE // The only exception here. It's a source commonly used worldwide
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("no") -> "https://www.met.no/om-oss/personvern"
                else -> "https://www.met.no/en/About-us/privacy"
            }
        }
    }

    private val mApi by lazy {
        client
            .baseUrl(METNO_BASE_URL)
            .build()
            .create(MetNoApi::class.java)
    }

    private val weatherAttribution = "MET Norway (NLOD / CC BY 4.0)"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "MET Norway" to "https://www.met.no/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            // Nowcast only for Norway, Sweden, Finland and Denmark
            // Covered area is slightly larger as per https://api.met.no/doc/nowcast/datamodel
            // but safer to limit to guaranteed countries
            SourceFeature.CURRENT, SourceFeature.MINUTELY -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("NO", "SE", "FI", "DK").any {
                    it.equals(location.countryCode, ignoreCase = true)
                }

            // Air quality only for Norway
            SourceFeature.AIR_QUALITY -> !location.countryCode.isNullOrEmpty() &&
                location.countryCode.equals("NO", ignoreCase = true)

            // Alerts only for Norway and Svalbard & Jan Mayen
            SourceFeature.ALERT -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("NO", "SJ").any {
                    it.equals(location.countryCode, ignoreCase = true)
                }

            SourceFeature.FORECAST -> true

            else -> false
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                USER_AGENT,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(MetNoForecastResult())
            }
        } else {
            Observable.just(MetNoForecastResult())
        }

        val nowcast = if (SourceFeature.CURRENT in requestedFeatures ||
            SourceFeature.MINUTELY in requestedFeatures
        ) {
            mApi.getNowcast(
                USER_AGENT,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                if (SourceFeature.MINUTELY in requestedFeatures) {
                    failedFeatures[SourceFeature.MINUTELY] = it
                }
                if (SourceFeature.CURRENT in requestedFeatures) {
                    failedFeatures[SourceFeature.CURRENT] = it
                }
                Observable.just(MetNoNowcastResult())
            }
        } else {
            Observable.just(MetNoNowcastResult())
        }

        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mApi.getAirQuality(
                USER_AGENT,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(MetNoAirQualityResult())
            }
        } else {
            Observable.just(MetNoAirQualityResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts(
                USER_AGENT,
                if (context.currentLocale.toString().lowercase().startsWith("no")) "no" else "en",
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(MetNoAlertResult())
            }
        } else {
            Observable.just(MetNoAlertResult())
        }

        return Observable.zip(forecast, nowcast, airQuality, alerts) {
                forecastResult: MetNoForecastResult,
                nowcastResult: MetNoNowcastResult,
                airQualityResult: MetNoAirQualityResult,
                metNoAlerts: MetNoAlertResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(
                        location,
                        forecastResult.properties?.timeseries
                    )
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(
                        context,
                        forecastResult.properties?.timeseries
                    )
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(nowcastResult, context)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    val airQualityHourly: MutableMap<Date, AirQuality> = mutableMapOf()
                    airQualityResult.data?.time?.forEach {
                        airQualityHourly[it.from] = AirQuality(
                            pM25 = it.variables?.pm25Concentration?.value,
                            pM10 = it.variables?.pm10Concentration?.value,
                            sO2 = it.variables?.so2Concentration?.value,
                            nO2 = it.variables?.no2Concentration?.value,
                            o3 = it.variables?.o3Concentration?.value
                        )
                    }
                    AirQualityWrapper(hourlyForecast = airQualityHourly)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(nowcastResult.properties?.timeseries)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlerts(metNoAlerts)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val METNO_BASE_URL = "https://api.met.no/weatherapi/"
        private const val USER_AGENT =
            "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}
