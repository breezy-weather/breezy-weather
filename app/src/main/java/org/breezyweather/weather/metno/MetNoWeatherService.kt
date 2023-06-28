package org.breezyweather.weather.metno

import android.content.Context
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
import org.breezyweather.weather.metno.json.MetNoEphemerisResult
import org.breezyweather.weather.metno.json.MetNoForecastResult
import org.breezyweather.weather.openmeteo.OpenMeteoGeocodingApi
import org.breezyweather.weather.openmeteo.convert
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResults
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

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
        val formattedDate = DisplayUtils.getFormattedDate(Date(), location.timeZone, "yyyy-MM-dd")
        val ephemeris = mApi.getEphemeris(
            userAgent,
            formattedDate,
            days = 15,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            getTimezoneOffset(location.timeZone)
        )
        Observable.zip(forecast, ephemeris) {
            metNoForecast: MetNoForecastResult,
            metNoEphemeris: MetNoEphemerisResult
            ->
            convert(
                context,
                location,
                metNoForecast,
                metNoEphemeris
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
        locationList.add(location)
        callback.requestLocationSuccess(
            location.latitude.toString() + "," + location.longitude,
            locationList
        )
    }

    override fun cancel() {
        mCompositeDisposable.clear()
    }

    companion object {
        protected fun getTimezoneOffset(tz: TimeZone): String {
            val cal = GregorianCalendar.getInstance(tz)
            val offsetInMillis = tz.getOffset(cal.timeInMillis)
            var offset = String.format(
                "%02d:%02d",
                abs(offsetInMillis / 3600000),
                abs(offsetInMillis / 60000 % 60)
            )
            offset = (if (offsetInMillis >= 0) "+" else "-") + offset
            return offset
        }
    }
}