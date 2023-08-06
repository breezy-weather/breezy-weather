/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.pirateweather

import android.content.Context
import android.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.openweather.convertSecondary
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallResult
import retrofit2.Retrofit
import javax.inject.Inject

class PirateWeatherService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ConfigurableSource {

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
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        if (!isConfigured) {
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

    // SECONDARY WEATHER SOURCE
    override val supportedFeatures = listOf(
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )
    override val airQualityAttribution = null
    override val allergenAttribution = null
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = weatherAttribution

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!isConfigured) {
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

        return pirateWeatherResult.map { convertSecondary(it) }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.PIRATE_WEATHER_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

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