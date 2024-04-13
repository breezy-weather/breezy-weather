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
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSourceFeature
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallResult
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Deprecated: will be removed in June 2024
 * See: https://github.com/breezy-weather/breezy-weather/issues/934
 */
class OpenWeatherService @Inject constructor(
    @ApplicationContext context: Context,
    client: Retrofit.Builder
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
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )

    override fun requestWeather(
        context: Context, location: Location, ignoreFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<WeatherWrapper> {
        if (getApiKeyOrDefault().isEmpty()) {
            return Observable.error(ApiKeyMissingException())
        }
        val apiKey = getApiKeyOrDefault()
        val languageCode = context.currentLocale.code
        val oneCall = mApi.getOneCall(
            oneCallVersion,
            apiKey,
            location.latitude,
            location.longitude,
            "metric",
            languageCode
        )
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
        return Observable.zip(oneCall, airPollution) {
                openWeatherOneCallResult: OpenWeatherOneCallResult,
                openWeatherAirPollutionResult: OpenWeatherAirPollutionResult
            ->
            convert(
                location,
                openWeatherOneCallResult,
                openWeatherAirPollutionResult
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY,
        SecondaryWeatherSourceFeature.FEATURE_MINUTELY,
        SecondaryWeatherSourceFeature.FEATURE_ALERT
    )
    override val airQualityAttribution = weatherAttribution
    override val pollenAttribution = null
    override val minutelyAttribution = weatherAttribution
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context, location: Location,
        requestedFeatures: List<SecondaryWeatherSourceFeature>
    ): Observable<SecondaryWeatherWrapper> {
        if (getApiKeyOrDefault().isEmpty()) {
            return Observable.error(ApiKeyMissingException())
        }

        val apiKey = getApiKeyOrDefault()
        val languageCode = context.currentLocale.code
        val oneCall = if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT) ||
            requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
            mApi.getOneCall(
                oneCallVersion,
                apiKey,
                location.latitude,
                location.longitude,
                "metric",
                languageCode
            )
        } else {
            Observable.create { emitter ->
                emitter.onNext(OpenWeatherOneCallResult())
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
        return Observable.zip(oneCall, airPollution) {
                openWeatherOneCallResult: OpenWeatherOneCallResult,
                openWeatherAirPollutionResult: OpenWeatherAirPollutionResult
            ->
            convertSecondary(
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_ALERT) ||
                    requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_MINUTELY)) {
                    openWeatherOneCallResult
                } else null,
                if (requestedFeatures.contains(SecondaryWeatherSourceFeature.FEATURE_AIR_QUALITY)) {
                    openWeatherAirPollutionResult
                } else null
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

    private var oneCallVersion: String
        set(value) {
            config.edit().putString("one_call_version", value).apply()
        }
        get() = config.getString("one_call_version", null) ?: "2.5"

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.OPEN_WEATHER_KEY }
    }
    override val isConfigured
        get() = false // Hack to get the source to no longer show for new locations as we deprecate it

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf()
    }

    companion object {
        private const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/"
    }
}
