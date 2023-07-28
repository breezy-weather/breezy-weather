package org.breezyweather.sources.here

import android.content.Context
import android.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.WeatherResultWrapper
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.here.json.HereWeatherForecastResult
import org.breezyweather.sources.here.json.HereWeatherStatusResult
import retrofit2.Retrofit
import javax.inject.Inject

class HereService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), WeatherSource, LocationSearchSource, ReverseGeocodingSource, ConfigurableSource {
    override val id = "here"
    override val name = "Here.com"
    override val privacyPolicyUrl = "https://legal.here.com/privacy/policy"

    override val color = Color.rgb(0, 175, 170)
    override val weatherAttribution = "Here.com"        // can't find
    override val locationSearchAttribution = "Here.com" // can't find

    private val mWeatherApi by lazy {
        client
            .baseUrl(if (BreezyWeather.instance.debugMode) HERE_WEATHER_DEV_BASE_URL else HERE_WEATHER_BASE_URL)
            .build()
            .create(HereWeatherApi::class.java)
    }

    private val mGeocodingApi by lazy {
        client
            .baseUrl(HERE_GEOCODING_BASE_URL)
            .build()
            .create(HereGeocodingApi::class.java)
    }

    private val mRevGeocodingApi by lazy {
        client
            .baseUrl(HERE_REV_GEOCODING_BASE_URL)
            .build()
            .create(HereRevGeocodingApi::class.java)
    }

    /**
     * Returns weather
     */
    override fun requestWeather(
        context: Context, location: Location
    ): Observable<WeatherResultWrapper> {
        if (!isConfigured()) {
            return Observable.error(ApiKeyMissingException())
        }

        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code
        val products = listOf(
            "observation",
            "forecast7days",
            "forecast7daysSimple",
            "forecastHourly",
            "forecastAstronomy",
            "alerts",
            "nwsAlerts",
        )
        val forecast = mWeatherApi.getForecast(
            apiKey,
            products.joinToString(separator = ","),
            listOf(
                location.latitude.toDouble(),
                location.longitude.toDouble(),
            ).joinToString(separator = ","),
            "metric",
            if (languageCode == "en") "en-US" else languageCode, //errors when lang=en
            oneObservation = true
        )

        val status = mWeatherApi.getStatus(
            apiKey
        )

        return Observable.zip(
            forecast,
            status
        ) { hereWeatherForecastResult: HereWeatherForecastResult,
            hereWeatherStatusResult: HereWeatherStatusResult
            ->
            convert(
                hereWeatherForecastResult,
                hereWeatherStatusResult
            )
        }
    }

    /**
     * Returns cities matching a query
     */
    override fun requestLocationSearch(
        context: Context,
        query: String
    ): Observable<List<Location>> {
        if (!isConfigured()) {
            return Observable.error(ApiKeyMissingException())
        }

        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code

        val locationResult = mGeocodingApi.geoCode(
            apiKey,
            query,
            types = "city",
            limit = 20,     // default api value
            languageCode,
            show = "tz"     // adds timezone info
        )

        return locationResult.map { convert(it) }
    }

    /**
     * Returns cities near provided coordinates
     */
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        if (!isConfigured()) {
            return Observable.error(ApiKeyMissingException())
        }

        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code

        val locationResult = mRevGeocodingApi.revGeoCode(
            apiKey,
            listOf(
                location.latitude.toDouble(),
                location.longitude.toDouble(),
            ).joinToString(separator = ","),
            types = "city",
            limit = 20,
            languageCode,
            show = "tz"
        )

        return locationResult.map { convert(it) }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault() = apikey.ifEmpty { BuildConfig.HERE_KEY }
    private fun isConfigured() = getApiKeyOrDefault().isNotEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_provider_here_api_key,
                summary = { c, content ->
                    content.ifEmpty {
                        c.getString(R.string.settings_source_default_value)
                    }
                },
                content = apikey,
                onValueChanged = {
                    apikey = it
                }
            ),
        )
    }

    companion object {
        private const val HERE_WEATHER_BASE_URL = "https://weather.cc.api.here.com/"
        private const val HERE_WEATHER_DEV_BASE_URL = "https://weather.cit.cc.api.here.com/"
        private const val HERE_GEOCODING_BASE_URL = "https://geocode.search.hereapi.com/"
        private const val HERE_REV_GEOCODING_BASE_URL = "https://revgeocode.search.hereapi.com/"
    }
}