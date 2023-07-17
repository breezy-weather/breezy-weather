package org.breezyweather.sources.accu

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherResultWrapper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.accu.json.*
import retrofit2.Retrofit
import javax.inject.Inject

class AccuService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), WeatherSource, LocationSearchSource, ReverseGeocodingSource {

    override val id = "accu"
    override val name = "AccuWeather"
    override val privacyPolicyUrl = "https://www.accuweather.com/en/privacy"

    override val color = -0x10a7dd
    override val weatherAttribution = "AccuWeather"
    override val locationSearchAttribution = weatherAttribution

    private val mDeveloperApi by lazy {
        client
            .baseUrl(ACCU_DEVELOPER_BASE_URL)
            .build()
            .create(AccuDeveloperApi::class.java)
    }
    private val mEnterpriseApi by lazy {
        client
            .baseUrl(ACCU_ENTERPRISE_BASE_URL)
            .build()
            .create(AccuEnterpriseApi::class.java)
    }

    private fun getApiKey(context: Context) = SettingsManager.getInstance(context).providerAccuWeatherKey

    private fun isConfigured(context: Context) = getApiKey(context).isNotEmpty()

    override fun requestWeather(
        context: Context, location: Location
    ): Observable<WeatherResultWrapper> {
        if (!isConfigured(context)) {
            return Observable.error(ApiKeyMissingException())
        }

        val apiKey = getApiKey(context)
        val settings = SettingsManager.getInstance(context)

        val mApi = if (settings.customAccuPortal.id == "enterprise") mEnterpriseApi else mDeveloperApi

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
        val minute = if (mApi is AccuEnterpriseApi) {
            mApi.getMinutely(
                apiKey,
                location.latitude.toString() + "," + location.longitude,
                languageCode,
                details = true
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(AccuMinutelyResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(AccuMinutelyResult())
            }
        }
        val alert = if (mApi is AccuEnterpriseApi) {
            mApi.getAlertsByPosition(
                apiKey,
                location.latitude.toString() + "," + location.longitude,
                languageCode,
                details = true
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(ArrayList())
                }
            }
        } else {
            mApi.getAlertsByCityKey(
                apiKey,
                location.cityId,
                languageCode,
                details = true
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(ArrayList())
                }
            }
        }
        val airQuality = if (mApi is AccuEnterpriseApi) {
            mApi.getAirQuality(
                location.cityId, apiKey,
                pollutants = true,
                languageCode
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(AccuAirQualityResult())
                }
            }
        } else {
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
        val settings = SettingsManager.getInstance(context)
        val languageCode = settings.language.code
        val mApi = if (settings.customAccuPortal.id == "enterprise") mEnterpriseApi else mDeveloperApi
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
        val settings = SettingsManager.getInstance(context)
        val languageCode = settings.language.code
        val mApi = if (settings.customAccuPortal.id == "enterprise") mEnterpriseApi else mDeveloperApi
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

    companion object {
        private const val ACCU_DEVELOPER_BASE_URL = "https://dataservice.accuweather.com/"
        private const val ACCU_ENTERPRISE_BASE_URL = "https://api.accuweather.com/"
    }
}