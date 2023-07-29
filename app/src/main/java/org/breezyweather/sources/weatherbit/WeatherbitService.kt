package org.breezyweather.sources.weatherbit

import android.content.Context
import android.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.WeatherResultWrapper
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.weatherbit.json.WeatherbitAirQuality
import org.breezyweather.sources.weatherbit.json.WeatherbitCurrentResponse
import org.breezyweather.sources.weatherbit.json.WeatherbitDaily
import org.breezyweather.sources.weatherbit.json.WeatherbitHourly
import org.breezyweather.sources.weatherbit.json.WeatherbitMinutely
import org.breezyweather.sources.weatherbit.json.WeatherbitResponse
import retrofit2.Retrofit
import javax.inject.Inject

class WeatherbitService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), WeatherSource, ConfigurableSource {
    override val id = "weatherbit"
    override val name = "Weatherbit"
    override val privacyPolicyUrl = "https://www.weatherbit.io/privacy"

    override val color = Color.rgb(10, 65, 102)
    override val weatherAttribution = "Weatherbit, https://www.weatherbit.io"

    private val mApi by lazy {
        client
            .baseUrl(WEATHERBIT_BASE_URL)
            .build()
            .create(WeatherbitApi::class.java)
    }

    override fun requestWeather(
        context: Context, location: Location
    ): Observable<WeatherResultWrapper> {
        if (!isConfigured()) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code

        val current = mApi.getCurrentWeather(
            apiKey,
            location.latitude,
            location.longitude,
            "M",
            languageCode,
            "alerts"
        )

//        val minutely = mApi.getMinutelyForecast(
//            apiKey,
//            location.latitude,
//            location.longitude,
//            "M",
//        )

//        val hourly = mApi.getHourlyForecast(
//            apiKey,
//            location.latitude,
//            location.longitude,
//            "M",
//            languageCode,
//            hours = 48      // up to 240
//        )

        val daily = mApi.getDailyForecast(
            apiKey,
            location.latitude,
            location.longitude,
            "M",
            languageCode,
            days = 14       // up to 16
        )

//        val currentAQ = mApi.getCurrentAirQuality(
//            apiKey,
//            location.latitude,
//            location.longitude
//        )

//        val hourlyAQ = mApi.getHourlyAQForecast(
//            apiKey,
//            location.latitude,
//            location.longitude,
//            hours = 48      // up to 72
//        )

//        return Observable.zip(current, minutely, hourly, daily, currentAQ, hourlyAQ) {
//                currentResult: WeatherbitCurrentResponse,
//                minutelyResult: WeatherbitResponse<WeatherbitMinutely>,
//                hourlyResult: WeatherbitResponse<WeatherbitHourly>,
//                dailyResults: WeatherbitResponse<WeatherbitDaily>,
//                currentAQResults: WeatherbitResponse<WeatherbitAirQuality>,
//                hourlyAQResults: WeatherbitResponse<WeatherbitAirQuality>
//            ->
//            convert(
//                currentResult,
//                minutelyResult,
//                hourlyResult,
//                dailyResults,
//                currentAQResults,
//                hourlyAQResults
//            )
//        }

        return Observable.zip(current, daily) { currentResult: WeatherbitCurrentResponse,
                                                dailyResults: WeatherbitResponse<WeatherbitDaily>
            ->
            convert(
                currentResult,
                null,
                null,
                dailyResults,
                null,
                null
            )
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault() = apikey.ifEmpty { BuildConfig.WEATHERBIT_KEY }
    private fun isConfigured() = getApiKeyOrDefault().isNotEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_provider_weatherbit_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            )
        )
    }

    companion object {
        private const val WEATHERBIT_BASE_URL = "https://api.weatherbit.io/v2.0/"
    }
}