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

package org.breezyweather.sources.smg

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
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.smg.json.SmgAirQualityResult
import org.breezyweather.sources.smg.json.SmgAstroResult
import org.breezyweather.sources.smg.json.SmgBulletinResult
import org.breezyweather.sources.smg.json.SmgCurrentResult
import org.breezyweather.sources.smg.json.SmgForecastResult
import org.breezyweather.sources.smg.json.SmgUvResult
import org.breezyweather.sources.smg.json.SmgWarningResult
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Named
import kotlin.text.startsWith

class SmgService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    override val id = "smg"

    // Even in its English bulletins, SMG refers to itself as "SMG" rather than
    // "Macao Meteorological and Geophysical Bureau". Keep the Portuguese name
    // for use in the source list. The English name can be used in attributions.
    override val name by lazy {
        if (context.currentLocale.code.startsWith("zh")) {
            "地球物理氣象局"
        } else {
            "SMG"
        } + " (${Locale(context.currentLocale.code, "MO").displayCountry})"
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "https://www.smg.gov.mo/zh/subpage/21/page/21"
                startsWith("pt") -> "https://www.smg.gov.mo/pt/subpage/21/page/21"
                else -> "https://www.smg.gov.mo/en/subpage/21/page/21"
            }
        }
    }

    override val color = Color.rgb(1, 173, 159)

    private val mApi by lazy {
        client
            .baseUrl(SMG_BASE_URL)
            .build()
            .create(SmgApi::class.java)
    }

    private val mCmsApi by lazy {
        client
            .baseUrl(SMG_CMS_URL)
            .build()
            .create(SmgCmsApi::class.java)
    }

    private val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "地球物理氣象局"
                startsWith("pt") -> "Direcção dos Serviços Meteorológicos e Geofísicos"
                else -> "Macao Meteorological and Geophysical Bureau"
            }
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("MO", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val failedFeatures = mutableListOf<SourceFeature>()
        val daily = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily().onErrorResumeNext {
                failedFeatures.add(SourceFeature.FORECAST)
                Observable.just(SmgForecastResult())
            }
        } else {
            Observable.just(SmgForecastResult())
        }
        val hourly = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getHourly().onErrorResumeNext {
                failedFeatures.add(SourceFeature.FORECAST)
                Observable.just(SmgForecastResult())
            }
        } else {
            Observable.just(SmgForecastResult())
        }

        // ASTRO
        val astro = if (SourceFeature.FORECAST in requestedFeatures) {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            formatter.timeZone = TimeZone.getTimeZone("Asia/Macau")
            val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Macau"))
            val body = """{"date":"${formatter.format(now.time)}"}"""
            mApi.getAstro(
                body.toRequestBody("application/json".toMediaTypeOrNull())
            ).onErrorResumeNext {
                /*if (BreezyWeather.instance.debugMode) {
                    failedFeatures.add(SourceFeature.OTHER)
                }*/
                Observable.just(SmgAstroResult())
            }
        } else {
            Observable.just(SmgAstroResult())
        }

        // CURRENT
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent().onErrorResumeNext {
                failedFeatures.add(SourceFeature.CURRENT)
                Observable.just(SmgCurrentResult())
            }
        } else {
            Observable.just(SmgCurrentResult())
        }

        val lang = with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "c"
                startsWith("pt") -> "p"
                else -> "e"
            }
        }
        val bulletin = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getBulletin(
                lang = lang
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.CURRENT)
                Observable.just(SmgBulletinResult())
            }
        } else {
            Observable.just(SmgBulletinResult())
        }
        val uv = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getUVIndex().onErrorResumeNext {
                failedFeatures.add(SourceFeature.CURRENT)
                Observable.just(SmgUvResult())
            }
        } else {
            Observable.just(SmgUvResult())
        }

        // ALERT
        val alerts = MutableList(SMG_ALERT_TYPES.size) {
            if (SourceFeature.ALERT in requestedFeatures) {
                mApi.getWarning(SMG_ALERT_TYPES[it], lang).onErrorResumeNext {
                    failedFeatures.add(SourceFeature.ALERT)
                    Observable.just(SmgWarningResult())
                }
            } else {
                Observable.just(SmgWarningResult())
            }
        }

        val warnings = if (SourceFeature.ALERT in requestedFeatures) {
            Observable.zip(alerts[0], alerts[1], alerts[2], alerts[3], alerts[4], alerts[5]) {
                    typhoonResult: SmgWarningResult,
                    rainstormResult: SmgWarningResult,
                    monsoonResult: SmgWarningResult,
                    thunderstormResult: SmgWarningResult,
                    stormsurgeResult: SmgWarningResult,
                    tsunamiResult: SmgWarningResult,
                ->
                listOf(
                    typhoonResult,
                    rainstormResult,
                    monsoonResult,
                    thunderstormResult,
                    stormsurgeResult,
                    tsunamiResult
                )
            }
        } else {
            Observable.just(emptyList())
        }

        // AIR QUALITY
        val airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mCmsApi.getAirQuality(Calendar.getInstance().timeInMillis).onErrorResumeNext {
                failedFeatures.add(SourceFeature.AIR_QUALITY)
                Observable.just(SmgAirQualityResult())
            }
        } else {
            Observable.just(SmgAirQualityResult())
        }

        return Observable.zip(current, daily, hourly, bulletin, uv, warnings, airQuality, astro) {
                currentResult: SmgCurrentResult,
                dailyResult: SmgForecastResult,
                hourlyResult: SmgForecastResult,
                bulletinResult: SmgBulletinResult,
                uvResult: SmgUvResult,
                warningsResult: List<SmgWarningResult>,
                airQualityResult: SmgAirQualityResult,
                astroResult: SmgAstroResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, dailyResult, astroResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, hourlyResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(currentResult, bulletinResult, uvResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(
                        current = getAirQuality(airQualityResult)
                    )
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, warningsResult)
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals()
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    companion object {
        private const val SMG_BASE_URL = "https://new-api.smg.gov.mo/"
        private const val SMG_CMS_URL = "https://cms.smg.gov.mo/"
        private val SMG_ALERT_TYPES = listOf("typhoon", "rainstorm", "monsoon", "thunderstorm", "stormsurge", "tsunami")
    }
}
