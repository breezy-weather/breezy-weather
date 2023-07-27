package org.breezyweather.sources.pirateweather

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
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import retrofit2.Retrofit
import javax.inject.Inject

class PirateWeatherService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), WeatherSource, ConfigurableSource {

    override val id = "pirateweather"
    override val name = "PirateWeather"
    override val privacyPolicyUrl = "https://pirate-weather.apiable.io/privacy"

    override val color = Color.rgb(113, 129, 145)
    override val weatherAttribution = "PirateWeather"

    private val mApi by lazy {
        client
            .baseUrl(if (BreezyWeather.instance.debugMode) PIRATE_WEATHER_DEV_BASE_URL else PIRATE_WEATHER_BASE_URL)
            .build()
            .create(PirateWeatherApi::class.java)
    }

    override fun requestWeather(
        context: Context, location: Location
    ): Observable<WeatherResultWrapper> {
        if (!isConfigured()) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code
        val pirateWeatherResult = mApi.getForecast(
            apiKey,
            location.latitude,
            location.longitude,
            "si", // represents metric,
            languageCode
        )

        return pirateWeatherResult.map { convert(it) }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault() = apikey.ifEmpty { BuildConfig.PIRATE_WEATHER_KEY }
    private fun isConfigured() = getApiKeyOrDefault().isNotEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_provider_pirate_weather_api_key,
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
        private const val PIRATE_WEATHER_BASE_URL = "https://api.pirateweather.net/"
        private const val PIRATE_WEATHER_DEV_BASE_URL = "https://dev.pirateweather.net/"
    }
}