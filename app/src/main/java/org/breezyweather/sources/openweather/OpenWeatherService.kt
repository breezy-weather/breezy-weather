package org.breezyweather.sources.openweather

import android.content.Context
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherResultWrapper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallResult
import retrofit2.Retrofit
import javax.inject.Inject

class OpenWeatherService @Inject constructor(
    client: Retrofit.Builder
) : HttpSource(), WeatherSource {

    override val id = "openweather"
    override val name = "OpenWeather"
    override val privacyPolicyUrl = "https://openweather.co.uk/privacy-policy"

    override val color = -0x1491b5
    override val weatherAttribution = "OpenWeather"

    private val mApi by lazy {
        client
            .baseUrl(OPEN_WEATHER_BASE_URL)
            .build()
            .create(OpenWeatherApi::class.java)
    }

    private fun getApiKey(context: Context) = SettingsManager.getInstance(context).providerOpenWeatherKey

    private fun isConfigured(context: Context) = getApiKey(context).isNotEmpty()

    override fun requestWeather(
        context: Context, location: Location
    ): Observable<WeatherResultWrapper> {
        if (!isConfigured(context)) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKey(context)
        val languageCode = SettingsManager.getInstance(context).language.code
        val oneCall = mApi.getOneCall(
            SettingsManager.getInstance(context).customOpenWeatherOneCallVersion.id,
            apiKey,
            location.latitude.toDouble(),
            location.longitude.toDouble(),
            "metric",
            languageCode
        )
        val airPollution = mApi.getAirPollution(
            apiKey,
            location.latitude.toDouble(),
            location.longitude.toDouble()
        ).onErrorResumeNext {
            Observable.create { emitter ->
                emitter.onNext(OpenWeatherAirPollutionResult())
            }
        }
        return Observable.zip(oneCall, airPollution) {
                openWeatherOneCallResult: OpenWeatherOneCallResult,
                openWeatherAirPollutionResult: OpenWeatherAirPollutionResult
            ->
            convert(
                context,
                location,
                openWeatherOneCallResult,
                openWeatherAirPollutionResult
            )
        }
    }

    companion object {
        private const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/"
    }
}