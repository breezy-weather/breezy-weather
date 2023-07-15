package org.breezyweather.weather.mf

import android.content.Context
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.rxjava.ApiObserver
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.WeatherService
import org.breezyweather.weather.mf.json.*
import org.breezyweather.weather.mf.json.atmoaura.AtmoAuraPointResult
import org.breezyweather.weather.openmeteo.OpenMeteoGeocodingApi
import org.breezyweather.weather.openmeteo.convert
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResults
import java.nio.charset.StandardCharsets
import java.util.*
import javax.inject.Inject

/**
 * Mf weather service.
 */
class MfWeatherService @Inject constructor(
    private val mMfApi: MfWeatherApi,
    private val mGeocodingApi: OpenMeteoGeocodingApi,
    private val mAtmoAuraApi: AtmoAuraIqaApi,
    private val mCompositeDisposable: CompositeDisposable
) : WeatherService() {

    override fun isConfigured(context: Context) = getToken(context).isNotEmpty()

    override fun requestWeather(
        context: Context, location: Location
    ): Observable<WeatherResultWrapper> {
        if (!isConfigured(context)) {
            return Observable.error(Exception(context.getString(R.string.weather_api_key_required_missing_title)))
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

    override fun requestLocationSearch(
        context: Context,
        query: String
    ): List<Location> {
        var apiResults: OpenMeteoLocationResults? = null
        try {
            apiResults = mGeocodingApi.callWeatherLocation(
                query,
                count = 20,
                "fr"
            ).execute().body()
        } catch (e: Exception) {
            if (BreezyWeather.instance.debugMode) {
                e.printStackTrace()
            }
        }
        return apiResults?.results?.map {
            convert(null, it, WeatherSource.MF)
        } ?: emptyList()
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        if (!isConfigured(context)) {
            return Observable.error(Exception(context.getString(R.string.weather_api_key_required_missing_title)))
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
                throw Exception(context.getString(R.string.location_message_reverse_geocoding_failed))
            }
        }
    }

    override fun cancel() {
        mCompositeDisposable.clear()
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

    protected val userAgent = "okhttp/4.9.2"
}