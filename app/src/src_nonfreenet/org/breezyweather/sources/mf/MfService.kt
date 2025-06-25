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

package org.breezyweather.sources.mf

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.mf.json.MfCurrentResult
import org.breezyweather.sources.mf.json.MfEphemerisResult
import org.breezyweather.sources.mf.json.MfForecastResult
import org.breezyweather.sources.mf.json.MfNormalsResult
import org.breezyweather.sources.mf.json.MfRainResult
import org.breezyweather.sources.mf.json.MfWarningDictionaryResult
import org.breezyweather.sources.mf.json.MfWarningsOverseasResult
import org.breezyweather.sources.mf.json.MfWarningsResult
import retrofit2.Retrofit
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

/**
 * Mf weather service.
 */
class MfService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource, ConfigurableSource {

    override val id = "mf"
    val countryName = Locale(context.currentLocale.code, "FR").displayCountry
    override val name = "Météo-France".let {
        if (it.contains(countryName)) {
            it
        } else {
            "$it ($countryName)"
        }
    }
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://meteofrance.com/application-meteo-france-politique-de-confidentialite"

    private val mApi by lazy {
        client
            .baseUrl(MF_BASE_URL)
            .build()
            .create(MfApi::class.java)
    }

    private val weatherAttribution = "Météo-France (Etalab)"
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "Météo-France" to "https://meteofrance.com/"
    )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            SourceFeature.CURRENT -> !location.countryCode.isNullOrEmpty() &&
                location.countryCode.equals("FR", ignoreCase = true)
            SourceFeature.MINUTELY -> !location.countryCode.isNullOrEmpty() &&
                location.countryCode.equals("FR", ignoreCase = true)
            SourceFeature.ALERT -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("FR", "AD", "BL", "GF", "GP", "MF", "MQ", "NC", "PF", "PM", "RE", "WF", "YT")
                    .any { location.countryCode.equals(it, ignoreCase = true) }
            SourceFeature.NORMALS -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("FR", "AD", "MC").any { location.countryCode.equals(it, ignoreCase = true) }
            SourceFeature.FORECAST -> true // Main source available worldwide
            else -> false
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val languageCode = context.currentLocale.code
        val token = getToken()
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                USER_AGENT,
                location.latitude,
                location.longitude,
                languageCode,
                "iso",
                token
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(MfCurrentResult())
            }
        } else {
            Observable.just(MfCurrentResult())
        }
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                USER_AGENT,
                location.latitude,
                location.longitude,
                "iso",
                token
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(MfForecastResult())
            }
        } else {
            Observable.just(MfForecastResult())
        }
        val ephemeris = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getEphemeris(
                USER_AGENT,
                location.latitude,
                location.longitude,
                "en", // English required to convert moon phase
                "iso",
                token
            ).onErrorResumeNext {
                /*if (BreezyWeather.instance.debugMode) {
                    failedFeatures.add(SourceFeature.OTHER)
                }*/
                Observable.just(MfEphemerisResult())
            }
        } else {
            Observable.just(MfEphemerisResult())
        }
        val rain = if (SourceFeature.MINUTELY in requestedFeatures) {
            mApi.getRain(
                USER_AGENT,
                location.latitude,
                location.longitude,
                languageCode,
                "iso",
                token
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.MINUTELY] = it
                Observable.just(MfRainResult())
            }
        } else {
            Observable.just(MfRainResult())
        }

        val domain = location.parameters.getOrElse(id) { null }?.getOrElse("domain") { null }
        if (SourceFeature.ALERT in requestedFeatures && domain.isNullOrEmpty()) {
            failedFeatures[SourceFeature.ALERT] = InvalidLocationException()
        }

        val warningsJ0 = if (SourceFeature.ALERT in requestedFeatures) {
            if (!domain.isNullOrEmpty() && !domain.startsWith("VIGI")) {
                mApi.getWarnings(
                    USER_AGENT,
                    domain,
                    "J0",
                    "iso",
                    token
                ).onErrorResumeNext {
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(MfWarningsResult())
                }
            } else {
                // Added to failedFeatures earlier, don't add here or it would fail for oversea territories
                Observable.just(MfWarningsResult())
            }
        } else {
            Observable.just(MfWarningsResult())
        }
        val warningsJ1 = if (SourceFeature.ALERT in requestedFeatures) {
            if (!domain.isNullOrEmpty() && !domain.startsWith("VIGI")) {
                mApi.getWarnings(
                    USER_AGENT,
                    domain,
                    "J1",
                    "iso",
                    token
                ).onErrorResumeNext {
                    // Don't fail on tomorrow. At 00:00, J1 alerts are not yet generated and fails
                    Observable.just(MfWarningsResult())
                }
            } else {
                // Added to failedFeatures earlier, don't add here or it would fail for oversea territories
                Observable.just(MfWarningsResult())
            }
        } else {
            Observable.just(MfWarningsResult())
        }

        val warningsOverseasDictionary = if (SourceFeature.ALERT in requestedFeatures) {
            if (!domain.isNullOrEmpty() && domain.startsWith("VIGI")) {
                mApi.getOverseasWarningsDictionary(
                    USER_AGENT,
                    domain,
                    token
                ).onErrorResumeNext {
                    it.printStackTrace()
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(MfWarningDictionaryResult())
                }
            } else {
                // Added to failedFeatures earlier, don't add here or it would fail for metropolitan area
                Observable.just(MfWarningDictionaryResult())
            }
        } else {
            Observable.just(MfWarningDictionaryResult())
        }

        val warningsOverseas = if (SourceFeature.ALERT in requestedFeatures) {
            if (!domain.isNullOrEmpty() && domain.startsWith("VIGI")) {
                mApi.getOverseasWarnings(
                    USER_AGENT,
                    domain,
                    if (domain == "VIGI974") "vigilance4colors" else null,
                    "iso",
                    token
                ).onErrorResumeNext {
                    it.printStackTrace()
                    failedFeatures[SourceFeature.ALERT] = it
                    Observable.just(MfWarningsOverseasResult())
                }
            } else {
                // Added to failedFeatures earlier, don't add here or it would fail for metropolitan area
                Observable.just(MfWarningsOverseasResult())
            }
        } else {
            Observable.just(MfWarningsOverseasResult())
        }

        // TODO: Only call once a month, unless it’s current position
        val normals = if (SourceFeature.NORMALS in requestedFeatures) {
            mApi.getNormals(
                USER_AGENT,
                location.latitude,
                location.longitude,
                token
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.NORMALS] = it
                Observable.just(MfNormalsResult())
            }
        } else {
            Observable.just(MfNormalsResult())
        }

        return Observable.zip(
            current,
            forecast,
            ephemeris,
            rain,
            warningsJ0,
            warningsJ1,
            warningsOverseasDictionary,
            warningsOverseas,
            normals
        ) {
                currentResult: MfCurrentResult,
                forecastResult: MfForecastResult,
                ephemerisResult: MfEphemerisResult,
                rainResult: MfRainResult,
                warningsJ0Result: MfWarningsResult,
                warningsJ1Result: MfWarningsResult,
                warningsDictionaryResult: MfWarningDictionaryResult,
                warningsOverseasResult: MfWarningsOverseasResult,
                normalsResult: MfNormalsResult,
            ->
            WeatherWrapper(
                /*base = Base(
                    publishDate = forecastResult.updateTime ?: Date()
                ),*/
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(
                        location,
                        forecastResult.properties?.dailyForecast,
                        ephemerisResult.properties?.ephemeris
                    )
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(
                        forecastResult.properties?.forecast,
                        forecastResult.properties?.probabilityForecast
                    )
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(currentResult)
                } else {
                    null
                },
                minutelyForecast = if (SourceFeature.MINUTELY in requestedFeatures) {
                    getMinutelyList(rainResult)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    if (!domain.isNullOrEmpty()) {
                        if (domain.startsWith("VIGI")) {
                            getOverseasWarningsList(warningsDictionaryResult, warningsOverseasResult)
                        } else {
                            getWarningsList(warningsJ0Result, warningsJ1Result)
                        }
                    } else {
                        null
                    }
                } else {
                    null
                },
                normals = if (SourceFeature.NORMALS in requestedFeatures) {
                    getNormals(location, normalsResult)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getForecast(
            USER_AGENT,
            location.latitude,
            location.longitude,
            "iso",
            getToken()
        ).map {
            listOf(convert(location, it))
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        /*
         * FIXME: Empty doesn't always include alerts
         * Just to be safe we query it when Meteo-France is the main source
         * See also #1497
         */
        if (SourceFeature.ALERT !in features) return false

        if (coordinatesChanged) return true

        return location.parameters.getOrElse(id) { null }?.getOrElse("domain") { null }.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getForecast(
            USER_AGENT,
            location.latitude,
            location.longitude,
            "iso",
            getToken()
        ).map {
            if (it.properties?.frenchDepartment.isNullOrEmpty()) {
                throw InvalidLocationException()
            }

            val domain = if (frenchDepartments.containsKey(it.properties!!.frenchDepartment!!)) {
                it.properties.frenchDepartment!!
            } else if (overseaTerritories.containsKey(it.properties.frenchDepartment)) {
                if (it.properties.frenchDepartment in arrayOf("977", "978")) {
                    "VIGI978-977"
                } else {
                    "VIGI${it.properties.frenchDepartment}"
                }
            } else {
                null
            }

            if (domain == null) {
                throw InvalidLocationException()
            }

            mapOf(
                "domain" to domain
            )
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var wsftKey: String
        set(value) {
            config.edit().putString("wsft_key", value).apply()
        }
        get() = config.getString("wsft_key", null) ?: ""

    private fun getWsftKeyOrDefault(): String {
        return wsftKey.ifEmpty { BuildConfig.MF_WSFT_KEY }
    }

    override val isConfigured
        get() = getToken().isNotEmpty()

    override val isRestricted = false

    private fun getToken(): String {
        return if (getWsftKeyOrDefault() != BuildConfig.MF_WSFT_KEY) {
            // If default key was changed, we want to use it
            getWsftKeyOrDefault()
        } else {
            // Otherwise, we try first a JWT key, otherwise fallback on regular API key
            try {
                Jwts.builder().apply {
                    header().add("typ", "JWT")
                    claims().empty().add("class", "mobile")
                    issuedAt(Date())
                    id(UUID.randomUUID().toString())
                    signWith(
                        Keys.hmacShaKeyFor(
                            BuildConfig.MF_WSFT_JWT_KEY.toByteArray(
                                StandardCharsets.UTF_8
                            )
                        ),
                        Jwts.SIG.HS256
                    )
                }.compact()
            } catch (ignored: Exception) {
                BuildConfig.MF_WSFT_KEY
            }
        }
    }

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_mf_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = wsftKey,
                onValueChanged = {
                    wsftKey = it
                }
            )
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val MF_BASE_URL = "https://webservice.meteofrance.com/"
        private const val USER_AGENT = "okhttp/4.9.2"
    }
}
