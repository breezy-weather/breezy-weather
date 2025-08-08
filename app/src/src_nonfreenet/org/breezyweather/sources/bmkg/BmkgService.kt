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

package org.breezyweather.sources.bmkg

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.bmkg.json.BmkgCurrentResult
import org.breezyweather.sources.bmkg.json.BmkgForecastResult
import org.breezyweather.sources.bmkg.json.BmkgIbfResult
import org.breezyweather.sources.bmkg.json.BmkgPm25Result
import org.breezyweather.sources.bmkg.json.BmkgWarningResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class BmkgService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, ConfigurableSource {

    override val id = "bmkg"
    override val name = "BMKG (${context.currentLocale.getCountryName("ID")})"
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = ""

    private val mApi by lazy {
        client
            .baseUrl(BMKG_BASE_URL)
            .build()
            .create(BmkgApi::class.java)
    }

    private val mAppApi by lazy {
        client
            .baseUrl(BMKG_APP_BASE_URL)
            .build()
            .create(BmkgAppApi::class.java)
    }

    private val weatherAttribution = "Badan Meteorologi, Klimatologi, dan Geofisika"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.bmkg.go.id/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("ID", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        // API Key is needed for warnings, but not for current/forecast.
        // Only throw exception if warnings are needed.
        val apiKey = getApiKeyOrDefault()
        if (apiKey.isEmpty() && SourceFeature.ALERT in requestedFeatures) {
            return Observable.error(ApiKeyMissingException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val forecast = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                lat = location.latitude,
                lon = location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(BmkgForecastResult())
            }
        } else {
            Observable.just(BmkgForecastResult())
        }

        val current = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                lat = location.latitude,
                lon = location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(BmkgCurrentResult())
            }
        } else {
            Observable.just(BmkgCurrentResult())
        }

        val warning = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getWarning(
                apiKey = apiKey,
                lat = location.latitude,
                lon = location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(BmkgWarningResult())
            }
        } else {
            Observable.just(BmkgWarningResult())
        }

        // Impact based forecasts provide early warnings of heavy rain up to 3 days
        val ibf = mutableListOf<Observable<BmkgIbfResult>>()
        for (day in 1..3) {
            ibf.add(
                if (SourceFeature.ALERT in requestedFeatures) {
                    mApi.getIbf(
                        apiKey = apiKey,
                        lat = location.latitude,
                        lon = location.longitude,
                        day = day
                    ).onErrorResumeNext {
                        failedFeatures[SourceFeature.ALERT] = it
                        Observable.just(BmkgIbfResult())
                    }
                } else {
                    Observable.just(BmkgIbfResult())
                }
            )
        }

        val pm25 = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
            mAppApi.getPm25().onErrorResumeNext {
                failedFeatures[SourceFeature.AIR_QUALITY] = it
                Observable.just(emptyList())
            }
        } else {
            Observable.just(emptyList())
        }

        return Observable.zip(current, forecast, warning, ibf[0], ibf[1], ibf[2], pm25) {
                currentResult: BmkgCurrentResult,
                forecastResult: BmkgForecastResult,
                warningResult: BmkgWarningResult,
                ibf1Result: BmkgIbfResult,
                ibf2Result: BmkgIbfResult,
                ibf3Result: BmkgIbfResult,
                pm25Result: List<BmkgPm25Result>,
            ->
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(context, location, forecastResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(context, forecastResult)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(context, currentResult)
                } else {
                    null
                },
                airQuality = if (SourceFeature.AIR_QUALITY in requestedFeatures) {
                    AirQualityWrapper(current = getAirQuality(location, pm25Result))
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(context, warningResult, ibf1Result, ibf2Result, ibf3Result)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    override fun requestNearestLocation(
        context: Context,
        location: Location,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.getLocation(
            lat = location.latitude,
            lon = location.longitude
        ).map {
            listOf(convert(context, it))
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
        return apikey.ifEmpty { BuildConfig.BMKG_KEY }
    }

    // Always true, as we will filter depending on the feature requested
    override val isConfigured
        get() = true // getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_bmkg_api_key,
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
        private const val BMKG_BASE_URL = "https://cuaca.bmkg.go.id/"
        private const val BMKG_APP_BASE_URL = "https://api-apps.bmkg.go.id/"
    }
}
