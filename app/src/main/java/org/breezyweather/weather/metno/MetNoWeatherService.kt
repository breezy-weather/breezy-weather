package org.breezyweather.weather.metno

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.rxjava.ApiObserver
import org.breezyweather.common.rxjava.ObserverContainer
import org.breezyweather.common.rxjava.SchedulerTransformer
import org.breezyweather.main.utils.RequestErrorType
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.WeatherService
import org.breezyweather.weather.metno.json.MetNoAirQualityResult
import org.breezyweather.weather.metno.json.MetNoSunResult
import org.breezyweather.weather.metno.json.MetNoForecastResult
import org.breezyweather.weather.metno.json.MetNoMoonResult
import org.breezyweather.weather.metno.json.MetNoNowcastResult
import org.breezyweather.weather.openmeteo.OpenMeteoGeocodingApi
import org.breezyweather.weather.openmeteo.convert
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResults
import java.util.*
import javax.inject.Inject

class MetNoWeatherService @Inject constructor(
    private val mApi: MetNoApi,
    private val mGeocodingApi: OpenMeteoGeocodingApi,
    private val mCompositeDisposable: CompositeDisposable
) : WeatherService() {

    protected val userAgent = "BreezyWeather/" + BuildConfig.VERSION_NAME + " github.com/breezy-weather/breezy-weather/issues"

    override fun isConfigured(context: Context) = true

    override fun requestWeather(
        context: Context,
        location: Location,
        callback: RequestWeatherCallback
    ) {
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
        val nowcast = if (!location.countryCode.isNullOrEmpty()
            && location.countryCode in arrayOf("NO", "SE", "FI", "DK")) {
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
        val airQuality = if (!location.countryCode.isNullOrEmpty()
            && location.countryCode == "NO") {
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

        Observable.zip(forecast, sun, moon, nowcast, airQuality) {
            metNoForecast: MetNoForecastResult,
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
        }.compose(SchedulerTransformer.create())
            .subscribe(ObserverContainer(mCompositeDisposable, object : ApiObserver<WeatherResultWrapper>() {
                override fun onSucceed(t: WeatherResultWrapper) {
                    if (t.result != null) {
                        callback.requestWeatherSuccess(location.copy(weather = t.result))
                    } else {
                        onFailed()
                    }
                }

                override fun onFailed() {
                    if (isApiLimitReached) {
                        callback.requestWeatherFailed(location, RequestErrorType.API_LIMIT_REACHED)
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
        val languageCode = SettingsManager.getInstance(context).language.code
        var apiResults: OpenMeteoLocationResults? = null
        try {
            apiResults = mGeocodingApi.callWeatherLocation(
                query,
                count = 20,
                languageCode
            ).execute().body()
        } catch (e: Exception) {
            if (BreezyWeather.instance.debugMode) {
                e.printStackTrace()
            }
        }
        return apiResults?.results?.map {
            convert(null, it, WeatherSource.METNO)
        } ?: emptyList()
    }

    override fun requestReverseLocationSearch(
        context: Context,
        location: Location,
        callback: RequestLocationCallback
    ) {
        // Currently there is no reverse geocoding, so we just return the same location
        // TimeZone is initialized with the TimeZone from the phone (which is probably the same as the current position)
        // Hopefully, one day we will have a reverse geocoding API
        val locationList: MutableList<Location> = ArrayList()
        locationList.add(location.copy(cityId = location.latitude.toString() + "," + location.longitude))
        callback.requestLocationSuccess(
            location.latitude.toString() + "," + location.longitude,
            locationList
        )
    }

    override fun cancel() {
        mCompositeDisposable.clear()
    }
}