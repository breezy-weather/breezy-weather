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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.mf.json.MfCurrentResult
import org.breezyweather.sources.mf.json.MfEphemerisResult
import org.breezyweather.sources.mf.json.MfForecastResult
import org.breezyweather.sources.mf.json.MfNormalsResult
import org.breezyweather.sources.mf.json.MfOverseasWarningsResult
import org.breezyweather.sources.mf.json.MfRainResult
import org.breezyweather.sources.mf.json.MfWarningsResult
import retrofit2.Retrofit
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

/**
 * Mf weather service.
 */
class MfService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource, ConfigurableSource {

    override val id = "mf"
    override val name = "Météo-France"
    override val privacyPolicyUrl =
        "https://meteofrance.com/application-meteo-france-politique-de-confidentialite"

    override val color = Color.rgb(0, 87, 147)
    override val weatherAttribution = "Météo-France (Etalab)"

    private val mApi by lazy {
        client
            .baseUrl(MF_BASE_URL)
            .build()
            .create(MfApi::class.java)
    }

    private val mOverseasApi by lazy {
        client
            .baseUrl(MF_OVERSEAS_BASE_URL)
            .build()
            .create(MfOverseasApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>,
    ): Observable<WeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val languageCode = context.currentLocale.code
        val token = getToken()
        val current = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                USER_AGENT,
                location.latitude,
                location.longitude,
                languageCode,
                "iso",
                token
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MfCurrentResult())
            }
        }
        val forecast = mApi.getForecast(
            USER_AGENT,
            location.latitude,
            location.longitude,
            "iso",
            token
        )
        val ephemeris = mApi.getEphemeris(
            USER_AGENT,
            location.latitude,
            location.longitude,
            "en", // English required to convert moon phase
            "iso",
            token
        )
        val rain = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mApi.getRain(
                USER_AGENT,
                location.latitude,
                location.longitude,
                languageCode,
                "iso",
                token
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MfRainResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MfRainResult())
            }
        }
        val warnings = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT) &&
            !location.countryCode.isNullOrEmpty() &&
            (
                location.countryCode.equals("FR", ignoreCase = true) &&
                    !location.admin2Code.isNullOrEmpty() &&
                    !OVERSEAS_DEPTS_REGEX.matches(location.admin2Code!!)
                ) ||
            location.countryCode.equals("AD", ignoreCase = true)
        ) {
            mApi.getWarnings(
                USER_AGENT,
                if (!location.countryCode.equals("AD", ignoreCase = true)) {
                    location.admin2Code!!
                } else {
                    "99"
                },
                "iso",
                token
            ).onErrorResumeNext {
                Observable.just(MfWarningsResult())
            }
        } else {
            Observable.just(MfWarningsResult())
        }

        val overseasWarnings = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT) &&
            !location.countryCode.isNullOrEmpty() &&
            (
                location.countryCode.equals("FR", ignoreCase = true) &&
                    !location.admin2Code.isNullOrEmpty() &&
                    OVERSEAS_DEPTS_REGEX.matches(location.admin2Code!!)
                ) ||
            OVERSEAS_ISOS.any { it.equals(location.countryCode, ignoreCase = true) }
        ) {
            val domain = getOverseasWarningDomain(
                if (location.countryCode.equals("FR", ignoreCase = true)) {
                    location.admin2Code
                } else {
                    location.countryCode
                }
            )
            val warningType = if (domain == "VIGI974") {
                "vigilance4colors"
            } else {
                ""
            }
            if (!domain.isNullOrEmpty()) {
                mOverseasApi.getWarnings(
                    userAgent = USER_AGENT,
                    domain = domain,
                    warningType = warningType,
                    token = token
                ).onErrorResumeNext {
                    Observable.just(MfOverseasWarningsResult())
                }
            } else { // Wallis & Futuna does not give warnings
                Observable.just(MfOverseasWarningsResult())
            }
        } else {
            Observable.just(MfOverseasWarningsResult())
        }

        // TODO: Only call once a month, unless it’s current position
        val normals = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
            mApi.getNormals(
                USER_AGENT,
                location.latitude,
                location.longitude,
                token
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MfNormalsResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MfNormalsResult())
            }
        }

        return Observable.zip(current, forecast, ephemeris, rain, warnings, overseasWarnings, normals) {
                mfCurrentResult: MfCurrentResult,
                mfForecastResult: MfForecastResult,
                mfEphemerisResult: MfEphemerisResult,
                mfRainResult: MfRainResult,
                mfWarningResults: MfWarningsResult,
                mfOverseasWarningResults: MfOverseasWarningsResult,
                mfNormalsResult: MfNormalsResult,
            ->
            convert(
                location,
                mfCurrentResult,
                mfForecastResult,
                mfEphemerisResult,
                mfRainResult,
                mfWarningResults,
                mfOverseasWarningResults,
                mfNormalsResult
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT,
        SecondaryWeatherSourceFeature.FEATURE_NORMALS
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SecondaryWeatherSourceFeature,
    ): Boolean {
        return isConfigured &&
            (
                feature == SecondaryWeatherSourceFeature.FEATURE_CURRENT &&
                    !location.countryCode.isNullOrEmpty() &&
                    location.countryCode.equals("FR", ignoreCase = true)
                ) ||
            (
                feature == SecondaryWeatherSourceFeature.FEATURE_MINUTELY &&
                    !location.countryCode.isNullOrEmpty() &&
                    location.countryCode.equals("FR", ignoreCase = true)
                ) ||
            (
                feature == SecondaryWeatherSourceFeature.FEATURE_ALERT &&
                    !location.countryCode.isNullOrEmpty() &&
                    arrayOf("FR", "AD").any { it.equals(location.countryCode, ignoreCase = true) } ||
                    OVERSEAS_ISOS.any { it.equals(location.countryCode, ignoreCase = true) }
                ) ||
            (
                // Technically, works anywhere but as a France-focused source, we don’t want the whole
                // world to use this source, as currently the only alternative is AccuWeather
                feature == SecondaryWeatherSourceFeature.FEATURE_NORMALS &&
                    !location.countryCode.isNullOrEmpty() &&
                    arrayOf("FR", "AD", "MC").any { it.equals(location.countryCode, ignoreCase = true) } ||
                    OVERSEAS_ISOS.any { it.equals(location.countryCode, ignoreCase = true) }
                )
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val languageCode = context.currentLocale.code
        val token = getToken()

        val current = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                USER_AGENT,
                location.latitude,
                location.longitude,
                languageCode,
                "iso",
                token
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MfCurrentResult())
            }
        }
        val rain = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mApi.getRain(
                USER_AGENT,
                location.latitude,
                location.longitude,
                languageCode,
                "iso",
                token
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MfRainResult())
            }
        }

        val warnings = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT) &&
            !location.countryCode.isNullOrEmpty() &&
            (
                location.countryCode.equals("FR", ignoreCase = true) &&
                    !location.admin2Code.isNullOrEmpty() &&
                    !OVERSEAS_DEPTS_REGEX.matches(location.admin2Code!!)
                ) ||
            location.countryCode.equals("AD", ignoreCase = true)
        ) {
            mApi.getWarnings(
                USER_AGENT,
                if (!location.countryCode.equals("AD", ignoreCase = true)) {
                    location.admin2Code!!
                } else {
                    "99"
                },
                "iso",
                token
            )
        } else {
            Observable.just(MfWarningsResult())
        }

        val overseasWarnings = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT) &&
            !location.countryCode.isNullOrEmpty() &&
            (
                location.countryCode.equals("FR", ignoreCase = true) &&
                    !location.admin2Code.isNullOrEmpty() &&
                    OVERSEAS_DEPTS_REGEX.matches(location.admin2Code!!)
                ) ||
            OVERSEAS_ISOS.any { it.equals(location.countryCode, ignoreCase = true) }
        ) {
            val domain = getOverseasWarningDomain(
                if (location.countryCode.equals("FR", ignoreCase = true)) {
                    location.admin2Code
                } else {
                    location.countryCode
                }
            )
            val warningType = if (domain == "VIGI974") {
                "vigilance4colors"
            } else {
                ""
            }
            if (!domain.isNullOrEmpty()) {
                mOverseasApi.getWarnings(
                    userAgent = USER_AGENT,
                    domain = domain,
                    warningType = warningType,
                    token = token
                )
            } else { // Wallis & Futuna does not give warnings
                Observable.just(MfOverseasWarningsResult())
            }
        } else {
            Observable.just(MfOverseasWarningsResult())
        }

        val normals = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
            mApi.getNormals(
                USER_AGENT,
                location.latitude,
                location.longitude,
                token
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(MfNormalsResult())
            }
        }

        return Observable.zip(current, rain, warnings, overseasWarnings, normals) {
                mfCurrentResult: MfCurrentResult,
                mfRainResult: MfRainResult,
                mfWarningResults: MfWarningsResult,
                mfOverseasWarningResults: MfOverseasWarningsResult,
                mfNormalsResult: MfNormalsResult,
            ->
            convertSecondary(
                location,
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
                    mfCurrentResult
                } else {
                    null
                },
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
                    mfRainResult
                } else {
                    null
                },
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
                    mfWarningResults
                } else {
                    null
                },
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
                    mfOverseasWarningResults
                } else {
                    null
                },
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_NORMALS)) {
                    mfNormalsResult
                } else {
                    null
                }
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
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

    private fun getOverseasWarningDomain(
        code: String?,
    ): String? {
        return when (code) {
            "971", "GP" -> "VIGI971" // Guadeloupe
            "972", "MQ" -> "VIGI972" // Martinique
            "973", "GF" -> "VIGI973" // French Guiana
            "974", "RE" -> "VIGI974" // Réunion
            "975", "PM" -> "VIGI975" // St. Pierre & Miquelon
            "976", "YT" -> "VIGI976" // Mayotte
            "977", "BL" -> "VIGI978-977" // St. Barthélemy
            "978", "MF" -> "VIGI978-977" // St. Martin
            "987", "PF" -> "VIGI987" // French Polynesia
            "988", "NC" -> "VIGI988" // New Caledonia
            else -> null
        }
    }

    companion object {
        private const val MF_BASE_URL = "https://webservice.meteofrance.com/"
        private const val MF_OVERSEAS_BASE_URL = "https://rpcache-aa.meteofrance.com/"
        private const val USER_AGENT = "okhttp/4.9.2"
        private val OVERSEAS_DEPTS_REGEX = Regex("""^9\d{2}$""")
        private val OVERSEAS_ISOS = arrayOf("BL", "GF", "GP", "MF", "MQ", "NC", "PF", "PM", "RE", "WF", "YT")
    }
}
