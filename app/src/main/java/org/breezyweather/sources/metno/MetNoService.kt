/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
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
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.sources.metno.json.MetNoAirQualityResult
import org.breezyweather.sources.metno.json.MetNoForecastResult
import org.breezyweather.sources.metno.json.MetNoMoonResult
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import org.breezyweather.sources.metno.json.MetNoSunResult
import org.breezyweather.sources.pirateweather.convertSecondary
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject

class MetNoService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource {

    override val id = "metno"
    override val name = "MET Norway"
    override val privacyPolicyUrl = "https://www.met.no/en/About-us/privacy"

    override val color = Color.rgb(11, 69, 94)
    override val weatherAttribution = "MET Norway (NLOD / CC BY 4.0)"

    private val mApi by lazy {
        client
            .baseUrl(METNO_BASE_URL)
            .build()
            .create(MetNoApi::class.java)
    }

    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val forecast = mApi.getForecast(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble()
        )

        val formattedDate = Date().getFormattedDate(location.timeZone, "yyyy-MM-dd")
        val sun = mApi.getSun(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            formattedDate
        )
        val moon = mApi.getMoon(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            formattedDate
        )

        // Nowcast only for Norway, Sweden, Finland and Denmark
        // Covered area is slightly larger as per https://api.met.no/doc/nowcast/datamodel
        // but safer to limit to guaranteed countries
        // Even if minutely is in "ignoredFeatures", we keep it as it also contains "current"
        val nowcast = if (!location.countryCode.isNullOrEmpty()
            && arrayOf("NO", "SE", "FI", "DK").any {
                it.equals(location.countryCode, ignoreCase = true)
            }
        ) {
            mApi.getNowcast(
                userAgent,
                location.latitude.toDouble(),
                location.longitude.toDouble()
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MetNoNowcastResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MetNoNowcastResult())
            }
        }

        // Air quality only for Norway
        val airQuality =
            if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)
                && !location.countryCode.isNullOrEmpty()
                && location.countryCode.equals("NO", ignoreCase = true)
            ) {
                mApi.getAirQuality(
                    userAgent,
                    location.latitude.toDouble(),
                    location.longitude.toDouble()
                ).onErrorResumeNext {
                    Observable.create { emitter ->
                        emitter.onNext(MetNoAirQualityResult())
                    }
                }
            } else {
                Observable.create { emitter ->
                    emitter.onNext(MetNoAirQualityResult())
                }
            }

        return Observable.zip(
            forecast,
            sun,
            moon,
            nowcast,
            airQuality
        ) { metNoForecast: MetNoForecastResult,
            metNoSun: MetNoSunResult,
            metNoMoon: MetNoMoonResult,
            metNoNowcast: MetNoNowcastResult,
            metNoAirQuality: MetNoAirQualityResult
            ->
            convert(
                context,
                location,
                metNoForecast,
                metNoSun,
                metNoMoon,
                metNoNowcast,
                metNoAirQuality
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeatures = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY
    )
    override fun isFeatureSupportedForLocation(
        feature: SecondaryWeatherSourceFeature, location: Location
    ): Boolean {
        return (
                feature == SecondaryWeatherSourceFeature.FEATURE_MINUTELY
                        && !location.countryCode.isNullOrEmpty()
                        && arrayOf("NO", "SE", "FI", "DK").any {
                    it.equals(location.countryCode, ignoreCase = true)
                }
                ) || (
                feature == SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY
                        && !location.countryCode.isNullOrEmpty()
                        && location.countryCode.equals("NO", ignoreCase = true)
                )
    }
    override val airQualityAttribution = weatherAttribution
    override val allergenAttribution = null
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        val nowcast =
            if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
                mApi.getNowcast(
                    userAgent,
                    location.latitude.toDouble(),
                    location.longitude.toDouble()
                ).onErrorResumeNext {
                    Observable.create { emitter ->
                        emitter.onNext(MetNoNowcastResult())
                    }
                }
            } else {
                Observable.create { emitter ->
                    emitter.onNext(MetNoNowcastResult())
                }
            }

        val airQuality =
            if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
                mApi.getAirQuality(
                    userAgent,
                    location.latitude.toDouble(),
                    location.longitude.toDouble()
                ).onErrorResumeNext {
                    Observable.create { emitter ->
                        emitter.onNext(MetNoAirQualityResult())
                    }
                }
            } else {
                Observable.create { emitter ->
                    emitter.onNext(MetNoAirQualityResult())
                }
            }

        return Observable.zip(nowcast, airQuality) { metNoNowcast: MetNoNowcastResult,
                                                     metNoAirQuality: MetNoAirQualityResult
            ->
            convertSecondary(
                metNoNowcast,
                metNoAirQuality
            )
        }
    }

    companion object {
        private const val METNO_BASE_URL = "https://api.met.no/weatherapi/"
        private const val userAgent =
            "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}