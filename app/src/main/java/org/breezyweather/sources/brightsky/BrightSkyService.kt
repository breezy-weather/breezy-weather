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

package org.breezyweather.sources.brightsky

import android.content.Context
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.brightsky.json.BrightSkyAlertsResult
import org.breezyweather.sources.brightsky.json.BrightSkyCurrentWeatherResult
import org.breezyweather.sources.brightsky.json.BrightSkyWeatherResult
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class BrightSkyService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") val client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ConfigurableSource {

    override val id = "brightsky"
    override val name = "Bright Sky (DWD) (${context.currentLocale.getCountryName("DE")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://brightsky.dev/"

    private val weatherAttribution = "Bright Sky, Data basis: Deutscher Wetterdienst. " +
        context.getString(R.string.data_modified, context.getString(R.string.breezy_weather))

    private val mApi: BrightSkyApi
        get() {
            return client
                .baseUrl(instance!!)
                .build()
                .create(BrightSkyApi::class.java)
        }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution
    )
    override val attributionLinks = mapOf(
        "Bright Sky" to "https://brightsky.dev/",
        "Deutscher Wetterdienst" to "https://www.dwd.de/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("DE", ignoreCase = true)
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
        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()
        val weather = if (SourceFeature.FORECAST in requestedFeatures) {
            val initialDate = Date().toTimezoneNoHour(location.timeZone)
            val date = initialDate.toCalendarWithTimeZone(location.timeZone).apply {
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0)
            }.time
            val lastDate = initialDate.toCalendarWithTimeZone(location.timeZone).apply {
                add(Calendar.DAY_OF_YEAR, 12)
                set(Calendar.HOUR_OF_DAY, 0)
            }.time

            mApi.getWeather(
                location.latitude,
                location.longitude,
                date.getFormattedDate("yyyy-MM-dd'T'HH:mm:ss", location),
                lastDate.getFormattedDate("yyyy-MM-dd'T'HH:mm:ss", location)
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(BrightSkyWeatherResult())
            }
        } else {
            Observable.just(BrightSkyWeatherResult())
        }

        val curWeather = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrentWeather(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(BrightSkyCurrentWeatherResult())
            }
        } else {
            Observable.just(BrightSkyCurrentWeatherResult())
        }

        val alerts = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getAlerts(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(BrightSkyAlertsResult())
            }
        } else {
            Observable.just(BrightSkyAlertsResult())
        }

        return Observable.zip(weather, curWeather, alerts) { brightSkyWeather, brightSkyCurWeather, brightSkyAlerts ->
            val languageCode = context.currentLocale.code
            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, brightSkyWeather.weather)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(brightSkyWeather.weather)
                } else {
                    null
                },
                current = if (SourceFeature.CURRENT in requestedFeatures) {
                    getCurrent(brightSkyCurWeather.weather)
                } else {
                    null
                },
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlertList(brightSkyAlerts.alerts, languageCode)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    override val isConfigured = true
    override val isRestricted = false
    private var instance: String?
        set(value) {
            value?.let {
                config.edit().putString("instance", it).apply()
            } ?: config.edit().remove("instance").apply()
        }
        get() = config.getString("instance", null) ?: BRIGHT_SKY_BASE_URL
    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_bright_sky_instance,
                summary = { _, content ->
                    content.ifEmpty {
                        BRIGHT_SKY_BASE_URL
                    }
                },
                content = if (instance != BRIGHT_SKY_BASE_URL) instance else null,
                placeholder = BRIGHT_SKY_BASE_URL,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                keyboardType = KeyboardType.Uri,
                onValueChanged = {
                    instance = if (it == BRIGHT_SKY_BASE_URL) null else it.ifEmpty { null }
                }
            )
        )
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        private const val BRIGHT_SKY_BASE_URL = "https://api.brightsky.dev/"
    }
}
