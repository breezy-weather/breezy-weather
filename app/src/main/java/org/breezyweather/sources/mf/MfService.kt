package org.breezyweather.sources.mf

import android.content.Context
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherResultWrapper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.mf.json.*
import org.breezyweather.sources.mf.json.atmoaura.AtmoAuraPointResult
import retrofit2.Retrofit
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject

/**
 * Mf weather service.
 */
class MfService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), WeatherSource, ReverseGeocodingSource {

    override val id = "mf"
    override val name = "Météo-France"
    override val privacyPolicyUrl = "https://meteofrance.com/application-meteo-france-politique-de-confidentialite"

    override val color = -0xffa76e
    override val weatherAttribution = "Météo-France" // Etalab license for free usages

    private val mMfApi by lazy {
        client
            .baseUrl(MF_WSFT_BASE_URL)
            .build()
            .create(MfApi::class.java)
    }

    private val mAtmoAuraApi by lazy {
        client
            .baseUrl(IQA_ATMO_AURA_URL)
            .build()
            .create(AtmoAuraIqaApi::class.java)
    }

    private fun isConfigured(context: Context) = getToken(context).isNotEmpty()

    override fun requestWeather(
        context: Context, location: Location
    ): Observable<WeatherResultWrapper> {
        if (!isConfigured(context)) {
            return Observable.error(ApiKeyMissingException())
        }
        val languageCode = SettingsManager.getInstance(context).language.code
        val token = getToken(context)
        val current = mMfApi.getCurrent(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            languageCode,
            "iso",
            token
        )
        val forecast = mMfApi.getForecast(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            "iso",
            token
        )
        val ephemeris = mMfApi.getEphemeris(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            "en", // English required to convert moon phase
            "iso",
            token
        )
        val rain = mMfApi.getRain(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            languageCode,
            "iso",
            token
        )
        val warnings = if (!location.countryCode.isNullOrEmpty()
            && location.countryCode == "FR"
            && !location.provinceCode.isNullOrEmpty()) {
            mMfApi.getWarnings(
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

        val atmoAuraKey = SettingsManager.getInstance(context).providerIqaAtmoAuraKey
        val aqiAtmoAura = if (
            (atmoAuraKey.isNotEmpty() && !location.countryCode.isNullOrEmpty() && location.countryCode == "FR")
            && !location.provinceCode.isNullOrEmpty()
            && location.provinceCode in arrayOf("01", "03", "07", "15", "26", "38", "42", "43", "63", "69", "73", "74")
        ) {
            val calendar = Date().toCalendarWithTimeZone(location.timeZone).apply {
                add(Calendar.DATE, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            mAtmoAuraApi.getPointDetails(
                atmoAuraKey,
                location.longitude.toDouble(),
                location.latitude.toDouble(),  // Tomorrow because it gives access to D-1 and D+1
                calendar.time.getFormattedDate(location.timeZone, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(AtmoAuraPointResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(AtmoAuraPointResult())
            }
        }
        return Observable.zip(current, forecast, ephemeris, rain, warnings, aqiAtmoAura) {
                mfCurrentResult: MfCurrentResult,
                mfForecastResult: MfForecastResult,
                mfEphemerisResult: MfEphemerisResult,
                mfRainResult: MfRainResult,
                mfWarningResults: MfWarningsResult,
                aqiAtmoAuraResult: AtmoAuraPointResult
            ->
            convert(
                context,
                location,
                mfCurrentResult,
                mfForecastResult,
                mfEphemerisResult,
                mfRainResult,
                mfWarningResults,
                aqiAtmoAuraResult
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        if (!isConfigured(context)) {
            return Observable.error(ApiKeyMissingException())
        }
        return mMfApi.getForecast(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            "iso",
            getToken(context)
        ).map {
            val locationList: MutableList<Location> = ArrayList()
            val locationConverted = convert(null, it)
            if (locationConverted != null) {
                locationList.add(locationConverted)
                locationList
            } else {
                throw ReverseGeocodingException()
            }
        }
    }

    protected fun getToken(context: Context): String {
        return if (SettingsManager.getInstance(context).providerMfWsftKey != BuildConfig.MF_WSFT_KEY) {
            SettingsManager.getInstance(context).providerMfWsftKey
        } else {
            try {
                Jwts.builder().apply {
                    setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                    setClaims(mapOf(
                        "class" to "mobile",
                        Claims.ISSUED_AT to (Date().time / 1000).toString(),
                        Claims.ID to UUID.randomUUID().toString()
                    ))
                    signWith(Keys.hmacShaKeyFor(BuildConfig.MF_WSFT_JWT_KEY.toByteArray(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                }.compact()
            } catch (ignored: Exception) {
                BuildConfig.MF_WSFT_KEY
            }
        }
    }

    companion object {
        private const val MF_WSFT_BASE_URL = "https://webservice.meteofrance.com/"
        private const val IQA_ATMO_AURA_URL = "https://api.atmo-aura.fr/"
        private val userAgent = "okhttp/4.9.2"
    }
}