/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
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
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.extensions.codeWithCountry
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class HereService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, ConfigurableSource {
    override val id = "here"
    override val name = "HERE"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://legal.here.com/privacy/policy"

    override val color = Color.rgb(72, 218, 208)

    private val mWeatherApi by lazy {
        client
            .baseUrl(if (BreezyWeather.instance.debugMode) HERE_WEATHER_DEV_BASE_URL else HERE_WEATHER_BASE_URL)
            .build()
            .create(HereWeatherApi::class.java)
    }

    /*private val mGeocodingApi by lazy {
        client
            .baseUrl(HERE_GEOCODING_BASE_URL)
            .build()
            .create(HereGeocodingApi::class.java)
    }*/

    private val mRevGeocodingApi by lazy {
        client
            .baseUrl(HERE_REV_GEOCODING_BASE_URL)
            .build()
            .create(HereRevGeocodingApi::class.java)
    }

    private val weatherAttribution = "HERE"
    override val reverseGeocodingAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )

    /**
     * Returns weather
     */
    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        val products = buildList {
            if (SourceFeature.CURRENT in requestedFeatures) {
                add("observation")
            }
            if (SourceFeature.FORECAST in requestedFeatures) {
                add("forecast7daysSimple")
                add("forecastHourly")
                add("forecastAstronomy")
            }
        }

        return mWeatherApi.getForecast(
            apiKey,
            products.joinToString(separator = ","),
            "${location.latitude},${location.longitude}",
            "metric",
            context.currentLocale.codeWithCountry,
            oneObservation = true
        ).map { hereWeatherForecastResult ->
            if (hereWeatherForecastResult.places.isNullOrEmpty()) {
                throw InvalidOrIncompleteDataException()
            }

            WeatherWrapper(
                /*base = Base(
                    publishDate = currentForecast?.time ?: Date()
                ),*/
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    val dailySimpleForecasts = hereWeatherForecastResult.places.firstNotNullOfOrNull {
                        it.dailyForecasts?.getOrNull(0)?.forecasts
                    }
                    val astronomyForecasts = hereWeatherForecastResult.places.firstNotNullOfOrNull {
                        it.astronomyForecasts?.getOrNull(0)?.forecasts
                    }
                    getDailyForecast(dailySimpleForecasts, astronomyForecasts)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    val hourlyForecasts = hereWeatherForecastResult.places.firstNotNullOfOrNull {
                        it.hourlyForecasts?.getOrNull(0)?.forecasts
                    }
                    getHourlyForecast(hourlyForecasts)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    val currentForecast = hereWeatherForecastResult.places.firstNotNullOfOrNull {
                        it.observations?.getOrNull(0)
                    }
                    getCurrentForecast(currentForecast)
                } else {
                    null
                }
            )
        }
    }

    /**
     * Returns cities matching a query
     */
    /*override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<Location>> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = SettingsManager.getInstance(context).language.code

        return mGeocodingApi.geoCode(
            apiKey,
            query,
            types = "city",
            limit = 20,
            languageCode,
            show = "tz" // we need timezone info
        ).map {
            if (it.items == null) {
                throw LocationSearchException()
            } else {
                convert(null, it.items)
            }
        }
    }*/

    /**
     * Returns cities near provided coordinates
     */
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val apiKey = getApiKeyOrDefault()

        return mRevGeocodingApi.revGeoCode(
            apiKey,
            "${location.latitude},${location.longitude}",
            types = "city",
            limit = 20,
            context.currentLocale.codeWithCountry,
            show = "tz"
        ).map {
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

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_here_api_key,
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

    override val testingLocations: List<Location> = emptyList()

    companion object {
        // private const val HERE_GEOCODING_BASE_URL = "https://geocode.search.hereapi.com/"
        private const val HERE_WEATHER_BASE_URL = "https://weather.cc.api.here.com/"
        private const val HERE_WEATHER_DEV_BASE_URL = "https://weather.cit.cc.api.here.com/"
        private const val HERE_REV_GEOCODING_BASE_URL = "https://revgeocode.search.hereapi.com/"
    }
}
