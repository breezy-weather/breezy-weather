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

package org.breezyweather.sources.openweather

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.sources.openweather.json.OpenWeatherForecast
import org.breezyweather.sources.openweather.json.OpenWeatherForecastResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * OpenWeatherMap
 * No longer based on OneCall API as it is now billing-only
 */
class OpenWeatherService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ConfigurableSource {

    override val id = "openweather"
    override val name = "OpenWeather"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = "https://openweather.co.uk/privacy-policy"

    override val color = Color.rgb(235, 110, 75)

    private val mApi by lazy {
        client
            .baseUrl(OPEN_WEATHER_BASE_URL)
            .build()
            .create(OpenWeatherApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to name,
        SourceFeature.CURRENT to name,
        SourceFeature.AIR_QUALITY to name
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = context.currentLocale.code
        val failedFeatures = mutableListOf<SourceFeature>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                apiKey,
                location.latitude,
                location.longitude,
                "metric",
                languageCode
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FORECAST)
                Observable.just(OpenWeatherForecastResult())
            }
        } else {
            Observable.just(OpenWeatherForecastResult())
        }
        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                apiKey,
                location.latitude,
                location.longitude,
                "metric",
                languageCode
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.CURRENT)
                Observable.just(OpenWeatherForecast())
            }
        } else {
            Observable.just(OpenWeatherForecast())
        }
        val airPollution = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mApi.getAirPollution(
                apiKey,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.AIR_QUALITY)
                Observable.just(OpenWeatherAirPollutionResult())
            }
        } else {
            Observable.just(OpenWeatherAirPollutionResult())
        }
        return Observable.zip(forecast, current, airPollution) {
                forecastResult: OpenWeatherForecastResult,
                currentResult: OpenWeatherForecast,
                airPollutionResult: OpenWeatherAirPollutionResult,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyList(forecastResult.list, location)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyList(forecastResult.list)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(currentResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(hourlyForecast = getHourlyAirQuality(airPollutionResult.list))
                } else {
                    null
                },
                failedFeatures = failedFeatures
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

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.OPEN_WEATHER_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_open_weather_api_key,
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
        private const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/"
    }
}
