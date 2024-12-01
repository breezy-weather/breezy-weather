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
import android.graphics.Color
import breezyweather.domain.feature.SourceFeature
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.sources.metno.json.MetNoAirQualityResult
import org.breezyweather.sources.metno.json.MetNoAlertResult
import org.breezyweather.sources.metno.json.MetNoForecastResult
import org.breezyweather.sources.metno.json.MetNoMoonResult
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import org.breezyweather.sources.metno.json.MetNoSunResult
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class MetNoService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource {

    override val id = "metno"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("no") -> "Meteorologisk institutt"
                else -> "MET Norway"
            }
        }
    }
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("no") -> "https://www.met.no/om-oss/personvern"
                else -> "https://www.met.no/en/About-us/privacy"
            }
        }
    }

    override val color = Color.rgb(11, 69, 94)
    override val weatherAttribution = "MET Norway (NLOD / CC BY 4.0)"

    private val mApi by lazy {
        client
            .baseUrl(METNO_BASE_URL)
            .build()
            .create(MetNoApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_AIR_QUALITY,
        SourceFeature.FEATURE_MINUTELY,
        SourceFeature.FEATURE_ALERT
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableListOf<SourceFeature>()
        val forecast = mApi.getForecast(
            USER_AGENT,
            location.latitude,
            location.longitude
        )

        val formattedDate = Date().getFormattedDate("yyyy-MM-dd", location)
        val sun = mApi.getSun(
            USER_AGENT,
            location.latitude,
            location.longitude,
            formattedDate
        ).onErrorResumeNext {
            /*if (BreezyWeather.instance.debugMode) {
                failedFeatures.add(SourceFeature.FEATURE_OTHER)
            }*/
            Observable.just(MetNoSunResult())
        }
        val moon = mApi.getMoon(
            USER_AGENT,
            location.latitude,
            location.longitude,
            formattedDate
        ).onErrorResumeNext {
            /*if (BreezyWeather.instance.debugMode) {
                failedFeatures.add(SourceFeature.FEATURE_OTHER)
            }*/
            Observable.just(MetNoMoonResult())
        }

        // Nowcast only for Norway, Sweden, Finland and Denmark
        // Covered area is slightly larger as per https://api.met.no/doc/nowcast/datamodel
        // but safer to limit to guaranteed countries
        val nowcast = if (!location.countryCode.isNullOrEmpty() &&
            arrayOf("NO", "SE", "FI", "DK").any {
                it.equals(location.countryCode, ignoreCase = true)
            } &&
            !(
                ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT) &&
                    ignoreFeatures.contains(SourceFeature.FEATURE_MINUTELY)
                )
        ) {
            mApi.getNowcast(
                USER_AGENT,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(MetNoNowcastResult())
            }
        } else {
            Observable.just(MetNoNowcastResult())
        }

        // Air quality only for Norway
        val airQuality =
            if (!ignoreFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY) &&
                !location.countryCode.isNullOrEmpty() &&
                location.countryCode.equals("NO", ignoreCase = true)
            ) {
                mApi.getAirQuality(
                    USER_AGENT,
                    location.latitude,
                    location.longitude
                ).onErrorResumeNext {
                    failedFeatures.add(SourceFeature.FEATURE_AIR_QUALITY)
                    Observable.just(MetNoAirQualityResult())
                }
            } else {
                Observable.just(MetNoAirQualityResult())
            }

        // Alerts only for Norway and Svalbard & Jan Mayen
        val alerts =
            if (!ignoreFeatures.contains(SourceFeature.FEATURE_ALERT) &&
                !location.countryCode.isNullOrEmpty() &&
                arrayOf("NO", "SJ").any {
                    it.equals(location.countryCode, ignoreCase = true)
                }
            ) {
                mApi.getAlerts(
                    USER_AGENT,
                    if (context.currentLocale.toString().lowercase().startsWith("no")) "no" else "en",
                    location.latitude,
                    location.longitude
                ).onErrorResumeNext {
                    failedFeatures.add(SourceFeature.FEATURE_ALERT)
                    Observable.just(MetNoAlertResult())
                }
            } else {
                Observable.just(MetNoAlertResult())
            }

        return Observable.zip(forecast, sun, moon, nowcast, airQuality, alerts) {
                metNoForecast: MetNoForecastResult,
                metNoSun: MetNoSunResult,
                metNoMoon: MetNoMoonResult,
                metNoNowcast: MetNoNowcastResult,
                metNoAirQuality: MetNoAirQualityResult,
                metNoAlerts: MetNoAlertResult,
            ->
            convert(
                context,
                location,
                metNoForecast,
                metNoSun,
                metNoMoon,
                metNoNowcast,
                metNoAirQuality,
                metNoAlerts,
                failedFeatures
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_AIR_QUALITY,
        SourceFeature.FEATURE_MINUTELY,
        SourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return (
            (
                feature == SourceFeature.FEATURE_CURRENT ||
                    feature == SourceFeature.FEATURE_MINUTELY
                ) &&
                !location.countryCode.isNullOrEmpty() &&
                arrayOf("NO", "SE", "FI", "DK").any {
                    it.equals(location.countryCode, ignoreCase = true)
                }
            ) ||
            (
                feature == SourceFeature.FEATURE_AIR_QUALITY &&
                    !location.countryCode.isNullOrEmpty() &&
                    location.countryCode.equals("NO", ignoreCase = true)
                ) ||
            (
                feature == SourceFeature.FEATURE_ALERT &&
                    !location.countryCode.isNullOrEmpty() &&
                    arrayOf("NO", "SJ").any {
                        it.equals(location.countryCode, ignoreCase = true)
                    }
                )
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = weatherAttribution
    override val pollenAttribution = null
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        val failedFeatures = mutableListOf<SourceFeature>()
        val nowcast = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT) ||
            requestedFeatures.contains(SourceFeature.FEATURE_MINUTELY)
        ) {
            mApi.getNowcast(
                USER_AGENT,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
                    failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                }
                if (requestedFeatures.contains(SourceFeature.FEATURE_MINUTELY)) {
                    failedFeatures.add(SourceFeature.FEATURE_MINUTELY)
                }
                Observable.just(MetNoNowcastResult())
            }
        } else {
            Observable.just(MetNoNowcastResult())
        }

        val airQuality = if (requestedFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getAirQuality(
                USER_AGENT,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_AIR_QUALITY)
                Observable.just(MetNoAirQualityResult())
            }
        } else {
            Observable.just(MetNoAirQualityResult())
        }

        // Alerts only for Norway and Svalbard & Jan Mayen
        val alerts =
            if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT) &&
                !location.countryCode.isNullOrEmpty() &&
                arrayOf("NO", "SJ").any {
                    it.equals(location.countryCode, ignoreCase = true)
                }
            ) {
                mApi.getAlerts(
                    USER_AGENT,
                    if (context.currentLocale.toString().lowercase().startsWith("no")) "no" else "en",
                    location.latitude,
                    location.longitude
                ).onErrorResumeNext {
                    failedFeatures.add(SourceFeature.FEATURE_ALERT)
                    Observable.just(MetNoAlertResult())
                }
            } else {
                Observable.just(MetNoAlertResult())
            }

        return Observable.zip(
            nowcast,
            airQuality,
            alerts
        ) { metNoNowcast: MetNoNowcastResult, metNoAirQuality: MetNoAirQualityResult, metNoAlerts: MetNoAlertResult ->
            convertSecondary(
                if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT) ||
                    requestedFeatures.contains(SourceFeature.FEATURE_MINUTELY)
                ) {
                    metNoNowcast
                } else {
                    null
                },
                if (requestedFeatures.contains(SourceFeature.FEATURE_AIR_QUALITY)) {
                    metNoAirQuality
                } else {
                    null
                },
                if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
                    metNoAlerts
                } else {
                    null
                },
                context,
                failedFeatures
            )
        }
    }

    companion object {
        private const val METNO_BASE_URL = "https://api.met.no/weatherapi/"
        private const val USER_AGENT =
            "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}
