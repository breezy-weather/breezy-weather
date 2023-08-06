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

package org.breezyweather.sources.here

import android.content.Context
import android.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.SourceConfigStore
import retrofit2.Retrofit
import javax.inject.Inject

class HereService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, LocationSearchSource, ReverseGeocodingSource,
    ConfigurableSource {
    override val id = "here"
    override val name = "HERE"
    override val privacyPolicyUrl = "https://legal.here.com/privacy/policy"

    override val color = Color.rgb(72, 218, 208)
    override val weatherAttribution = "HERE"
    override val locationSearchAttribution = "HERE"

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
        context: Context, location: Location,
        ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }

        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.codeWithCountry
        val products = listOf(
            "observation",
            "forecast7daysSimple",
            "forecastHourly",
            "forecastAstronomy"
        )
        val forecast = mWeatherApi.getForecast(
            apiKey,
            products.joinToString(separator = ",") +
                    if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT)) {
                        ",nwsAlerts"
                    } else "",
            "${location.latitude},${location.longitude}",
            "metric",
            languageCode,
            oneObservation = true
        )

        return forecast.map { convert(it) }
    }

    /**
     * Returns cities matching a query
     */
    override fun requestLocationSearch(
        context: Context,
        query: String
    ): Observable<List<Location>> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }

        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code

        val locationResult = mGeocodingApi.geoCode(
            apiKey,
            query,
            types = "city",
            limit = 20,
            languageCode,
            show = "tz" // we need timezone info
        )

        return locationResult.map {
            if (it.items == null) {
                throw LocationSearchException()
            } else {
                convert(null, it.items)
            }
        }
    }

    override fun isUsable(location: Location): Boolean = true

    /**
     * Returns cities near provided coordinates
     */
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location
    ): Observable<List<Location>> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }

        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.codeWithCountry

        val locationResult = mRevGeocodingApi.revGeoCode(
            apiKey,
            "${location.latitude},${location.longitude}",
            types = "city",
            limit = 20,
            languageCode,
            show = "tz"
        )

        return locationResult.map {
            if (it.items == null) {
                throw ReverseGeocodingException()
            } else {
                convert(location, it.items)
            }
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.HERE_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

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