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
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.rxjava.ApiObserver
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.common.utils.DisplayUtils
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

    override fun requestWeather(context: Context, location: Location, callback: RequestWeatherCallback) {
        if (!isConfigured(context)) {
            callback.requestWeatherFailed(location, RequestErrorType.API_KEY_REQUIRED_MISSING)
            return
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
             && (location.provinceCode == "01" || location.provinceCode == "03"
                    || location.provinceCode == "07" || location.provinceCode == "15"
                    || location.provinceCode == "26" || location.provinceCode == "38"
                    || location.provinceCode == "42" || location.provinceCode == "43"
                    || location.provinceCode == "63" || location.provinceCode == "69"
                    || location.provinceCode == "73" || location.provinceCode == "74")
        ) {
            val c = DisplayUtils.toCalendarWithTimeZone(Date(), location.timeZone)
            c.add(Calendar.DATE, 1)
            c[Calendar.HOUR_OF_DAY] = 0
            c[Calendar.MINUTE] = 0
            c[Calendar.SECOND] = 0
            c[Calendar.MILLISECOND] = 0
            mAtmoAuraApi.getPointDetails(
                atmoAuraKey,
                location.longitude.toDouble(),
                location.latitude.toDouble(),  // Tomorrow because it gives access to D-1 and D+1
                DisplayUtils.getFormattedDate(c.time, location.timeZone, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
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
        Observable.zip(current, forecast, ephemeris, rain, warnings, aqiAtmoAura) {
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
        }.compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : ApiObserver<WeatherResultWrapper>() {
                override fun onSucceed(t: WeatherResultWrapper) {
                    if (t.result != null) {
                        callback.requestWeatherSuccess(
                            Location.copy(location, t.result)
                        )
                    } else {
                        onFailed()
                    }
                }

                override fun onFailed() {
                    if (isApiLimitReached) {
                        callback.requestWeatherFailed(location, RequestErrorType.API_LIMIT_REACHED)
                    } else if (isApiUnauthorized) {
                        callback.requestWeatherFailed(location, RequestErrorType.API_UNAUTHORIZED)
                    } else {
                        callback.requestWeatherFailed(location, RequestErrorType.WEATHER_REQ_FAILED)
                    }
                }
            }))
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

    override fun requestReverseLocationSearch(
        context: Context,
        location: Location,
        callback: RequestLocationCallback
    ) {
        if (!isConfigured(context)) {
            callback.requestLocationFailed(
                location.latitude.toString() + "," + location.longitude,
                RequestErrorType.API_KEY_REQUIRED_MISSING
            )
            return
        }
        mMfApi.getForecast(
            userAgent,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            "iso",
            getToken(context)
        ).compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : ApiObserver<MfForecastResult>() {
                override fun onSucceed(t: MfForecastResult) {
                    val locationList: MutableList<Location> = ArrayList()
                    val locationConverted = convert(null, t)
                    if (locationConverted != null) {
                        locationList.add(locationConverted)
                        callback.requestLocationSuccess(
                            location.latitude.toString() + "," + location.longitude,
                            locationList
                        )
                    } else {
                        onFailed()
                    }
                }

                override fun onFailed() {
                    if (isApiLimitReached) {
                        callback.requestLocationFailed(
                            location.latitude.toString() + "," + location.longitude,
                            RequestErrorType.API_LIMIT_REACHED
                        )
                    } else if (isApiUnauthorized) {
                        callback.requestLocationFailed(
                            location.latitude.toString() + "," + location.longitude,
                            RequestErrorType.API_UNAUTHORIZED
                        )
                    } else {
                        callback.requestLocationFailed(
                            location.latitude.toString() + "," + location.longitude,
                            RequestErrorType.LOCATION_FAILED
                        )
                    }
                }
            }))
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