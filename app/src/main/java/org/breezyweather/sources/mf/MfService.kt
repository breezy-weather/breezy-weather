package org.breezyweather.sources.mf

import android.content.Context
import android.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.settings.SettingsManager
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.atmoaura.AtmoAuraAirQualityApi
import org.breezyweather.sources.mf.json.*
import org.breezyweather.sources.atmoaura.json.AtmoAuraPointResult
import retrofit2.Retrofit
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject

/**
 * Mf weather service.
 */
class MfService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource,
    ReverseGeocodingSource, ConfigurableSource {

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

    override fun requestWeather(
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val languageCode = SettingsManager.getInstance(context).language.code
        val token = getToken()
        val current = mApi.getCurrent(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            languageCode,
            "iso",
            token
        )
        val forecast = mApi.getForecast(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            "iso",
            token
        )
        val ephemeris = mApi.getEphemeris(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            "en", // English required to convert moon phase
            "iso",
            token
        )
        val rain = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mApi.getRain(
                userAgent,
                location.latitude.toDouble(),
                location.longitude.toDouble(),
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
        val warnings = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)
            && !location.countryCode.isNullOrEmpty()
            && location.countryCode.equals("FR", ignoreCase = true)
            && !location.provinceCode.isNullOrEmpty()
        ) {
            mApi.getWarnings(
                userAgent,
                location.provinceCode,
                "iso",
                token
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MfWarningsResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MfWarningsResult())
            }
        }

        return Observable.zip(
            current,
            forecast,
            ephemeris,
            rain,
            warnings
        ) { mfCurrentResult: MfCurrentResult,
            mfForecastResult: MfForecastResult,
            mfEphemerisResult: MfEphemerisResult,
            mfRainResult: MfRainResult,
            mfWarningResults: MfWarningsResult
            ->
            convert(
                location,
                mfCurrentResult,
                mfForecastResult,
                mfEphemerisResult,
                mfRainResult,
                mfWarningResults
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeatures = listOf(
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedForLocation(
        feature: SecondaryWeatherSourceFeature, location: Location
    ): Boolean {
        return isConfigured && (
                feature == SecondaryWeatherSourceFeature.FEATURE_MINUTELY
                        && !location.countryCode.isNullOrEmpty()
                        && location.countryCode.equals("FR", ignoreCase = true)
                ) || (
                feature == SecondaryWeatherSourceFeature.FEATURE_ALERT
                        && !location.countryCode.isNullOrEmpty()
                        && location.countryCode.equals("FR", ignoreCase = true)
                        && !location.provinceCode.isNullOrEmpty()
                )
    }
    override val airQualityAttribution = null
    override val allergenAttribution = null
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        val languageCode = SettingsManager.getInstance(context).language.code
        val token = getToken()

        val rain = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mApi.getRain(
                userAgent,
                location.latitude.toDouble(),
                location.longitude.toDouble(),
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

        val warnings = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)
            && !location.countryCode.isNullOrEmpty()
            && location.countryCode.equals("FR", ignoreCase = true)
            && !location.provinceCode.isNullOrEmpty()
        ) {
            mApi.getWarnings(
                userAgent,
                location.provinceCode,
                "iso",
                token
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(MfWarningsResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(MfWarningsResult())
            }
        }

        return Observable.zip(
            rain,
            warnings
        ) { mfRainResult: MfRainResult,
            mfWarningResults: MfWarningsResult
            ->
            convertSecondary(
                mfRainResult,
                mfWarningResults
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        return mApi.getForecast(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            "iso",
            getToken()
        ).map {
            val locationList: MutableList<Location> = ArrayList()
            val locationConverted = convert(location, it)
            if (locationConverted != null) {
                locationList.add(locationConverted)
                locationList
            } else {
                throw ReverseGeocodingException()
            }
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

    private fun getToken(): String {
        return if (getWsftKeyOrDefault() != BuildConfig.MF_WSFT_KEY) {
            // If default key was changed, we want to use it
            getWsftKeyOrDefault()
        } else {
            // Otherwise, we try first a JWT key, otherwise fallback on regular API key
            try {
                Jwts.builder().apply {
                    setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                    setClaims(
                        mapOf(
                            "class" to "mobile",
                            Claims.ISSUED_AT to (Date().time / 1000).toString(),
                            Claims.ID to UUID.randomUUID().toString()
                        )
                    )
                    signWith(
                        Keys.hmacShaKeyFor(
                            BuildConfig.MF_WSFT_JWT_KEY.toByteArray(
                                StandardCharsets.UTF_8
                            )
                        ), SignatureAlgorithm.HS256
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
                titleId = R.string.settings_weather_provider_mf_api_key,
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

    companion object {
        private const val MF_BASE_URL = "https://webservice.meteofrance.com/"
        private const val userAgent = "okhttp/4.9.2"
    }
}