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
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.SourceNotInstalledException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
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
    @Named("JsonClient") client: Retrofit.Builder
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ConfigurableSource {

    override val id = "openweather"
    override val name = "OpenWeather"
    override val privacyPolicyUrl = "https://openweather.co.uk/privacy-policy"

    override val color = Color.rgb(235, 110, 75)
    override val weatherAttribution = "OpenWeather"

    private val mApi by lazy {
        client
            .baseUrl(OPEN_WEATHER_BASE_URL)
            .build()
            .create(OpenWeatherApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY
    )

    override fun requestWeather(
        context: Context, location: Location, ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()
        if (apiKey.isEmpty()) {
            return Observable.error(ApiKeyMissingException())
        }
        val languageCode = context.currentLocale.code
        val forecast = mApi.getForecast(
            apiKey,
            location.latitude,
            location.longitude,
            "metric",
            languageCode
        )
        val current = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                apiKey,
                location.latitude,
                location.longitude,
                "metric",
                languageCode
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(OpenWeatherForecast())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenWeatherForecast())
            }
        }
        val airPollution = if (!ignoreFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getAirPollution(
                apiKey,
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                Observable.create { emitter ->
                    emitter.onNext(OpenWeatherAirPollutionResult())
                }
            }
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenWeatherAirPollutionResult())
            }
        }
        return Observable.zip(forecast, current, airPollution) {
                openWeatherForecastResult: OpenWeatherForecastResult,
                openWeatherCurrentResult: OpenWeatherForecast,
                openWeatherAirPollutionResult: OpenWeatherAirPollutionResult
            ->
            convert(
                location,
                openWeatherForecastResult,
                openWeatherCurrentResult,
                openWeatherAirPollutionResult
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_CURRENT,
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY
    )
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = weatherAttribution
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = null
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (!requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY) ||
            !requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            return Observable.error(SourceNotInstalledException())
        }

        val apiKey = getApiKeyOrDefault()
        if (apiKey.isEmpty()) {
            return Observable.error(ApiKeyMissingException())
        }
        val languageCode = context.currentLocale.code

        val current = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                apiKey,
                location.latitude,
                location.longitude,
                "metric",
                languageCode
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenWeatherForecast())
            }
        }

        val airPollution = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
            mApi.getAirPollution(
                apiKey,
                location.latitude,
                location.longitude
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenWeatherAirPollutionResult())
            }
        }

        return Observable.zip(current, airPollution) {
                openWeatherCurrentResult: OpenWeatherForecast,
                openWeatherAirPollutionResult: OpenWeatherAirPollutionResult
            ->
            convertSecondary(
                openWeatherCurrentResult,
                openWeatherAirPollutionResult
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
