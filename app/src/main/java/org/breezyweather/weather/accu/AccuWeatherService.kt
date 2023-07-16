package org.breezyweather.weather.accu

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.settings.SettingsManager
import org.breezyweather.weather.WeatherService
import org.breezyweather.weather.accu.json.*
import javax.inject.Inject

class AccuWeatherService @Inject constructor(
    private val mApi: AccuWeatherApi,
    private val mCompositeDisposable: CompositeDisposable
) : WeatherService() {
    protected fun getApiKey(context: Context) = SettingsManager.getInstance(context).providerAccuWeatherKey

    override fun isConfigured(context: Context) = getApiKey(context).isNotEmpty()

    override fun requestWeather(
        context: Context, location: Location
    ): Observable<WeatherResultWrapper> {
        if (!isConfigured(context)) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKey(context)
        val settings = SettingsManager.getInstance(context)
        val languageCode = settings.language.code
        val current = mApi.getCurrent(
            location.cityId,
            apiKey,
            languageCode,
            details = true
        )
        val daily = mApi.getDaily(
            settings.customAccuDays.id,
            location.cityId,
            apiKey,
            languageCode,
            details = true,
            metric = true // Converted later
        )
        val hourly = mApi.getHourly(
            settings.customAccuHours.id,
            location.cityId,
            apiKey,
            languageCode,
            details = true,
            metric = true // Converted later
        )
        val minute = mApi.getMinutely(
            apiKey,
            location.latitude.toString() + "," + location.longitude,
            languageCode,
            details = true
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(AccuMinutelyResult())
            }
        }
        val alert = mApi.getAlert(
            apiKey,
            location.latitude.toString() + "," + location.longitude,
            languageCode,
            details = true
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(ArrayList())
            }
        }
        val airQuality = mApi.getAirQuality(
            location.cityId, apiKey,
            pollutants = true,
            languageCode
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(AccuAirQualityResult())
            }
        }
        return Observable.zip(current, daily, hourly, minute, alert, airQuality) {
                accuRealtimeResults: List<AccuCurrentResult>,
                accuDailyResult: AccuForecastDailyResult,
                accuHourlyResults: List<AccuForecastHourlyResult>,
                accuMinutelyResult: AccuMinutelyResult,
                accuAlertResults: List<AccuAlertResult>,
                accuAirQualityResult: AccuAirQualityResult
            ->
            convert(
                context,
                location,
                accuRealtimeResults[0],
                accuDailyResult,
                accuHourlyResults,
                accuMinutelyResult,
                accuAlertResults,
                accuAirQualityResult
            )
        }
    }

    override fun requestLocationSearch(
        context: Context,
        query: String
    ): Observable<List<Location>> {
        if (!isConfigured(context)) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKey(context)
        val languageCode = SettingsManager.getInstance(context).language.code
        return mApi.getWeatherLocation(
            apiKey,
            query,
            languageCode,
            details = false,
            alias = "Always"
        ).map { results ->
            // TODO: Why? This will use searched terms as zip code even if the zip code is incomplete
            val zipCode = if (query.matches("[a-zA-Z0-9]*".toRegex())) query else null

            results.map {
                convert(null, it, zipCode)
            }
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        if (!isConfigured(context)) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKey(context)
        val languageCode = SettingsManager.getInstance(context).language.code
        return mApi.getWeatherLocationByGeoPosition(
            apiKey,
            languageCode,
            details = false,
            location.latitude.toString() + "," + location.longitude
        ).map {
            val locationList: MutableList<Location> = ArrayList()
            locationList.add(convert(location, it, null))
            locationList
        }
    }

    override fun cancel() {
        mCompositeDisposable.clear()
    }
}