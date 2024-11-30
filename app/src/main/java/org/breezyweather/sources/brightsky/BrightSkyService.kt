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
import android.graphics.Color
import breezyweather.domain.feature.SourceFeature
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.brightsky.json.BrightSkyAlertsResult
import org.breezyweather.sources.brightsky.json.BrightSkyCurrentWeatherResult
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

class BrightSkyService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") val client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ConfigurableSource {

    override val id = "brightsky"
    override val name = "Bright Sky (DWD)"
    override val privacyPolicyUrl = "https://brightsky.dev/"

    override val color = Color.rgb(240, 177, 138)
    override val weatherAttribution =
        "Bright Sky, Data basis: Deutscher Wetterdienst, reproduced graphically and with missing data computed or extrapolated by Breezy Weather"

    private val mApi: BrightSkyApi
        get() {
            return client
                .baseUrl(instance)
                .build()
                .create(BrightSkyApi::class.java)
        }

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_ALERT
    )

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SourceFeature?,
    ): Boolean {
        return location.countryCode.equals("DE", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val initialDate = Date().toTimezoneNoHour(location.javaTimeZone)
        val date = initialDate!!.toCalendarWithTimeZone(location.javaTimeZone).apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
        }.time
        val lastDate = initialDate.toCalendarWithTimeZone(location.javaTimeZone).apply {
            add(Calendar.DAY_OF_YEAR, 12)
            set(Calendar.HOUR_OF_DAY, 0)
        }.time

        val weather = mApi.getWeather(
            location.latitude,
            location.longitude,
            date.getFormattedDate("yyyy-MM-dd'T'HH:mm:ss", location),
            lastDate.getFormattedDate("yyyy-MM-dd'T'HH:mm:ss", location)
        )

        val failedFeatures = mutableListOf<SourceFeature>()
        val curWeather = if (!ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrentWeather(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(BrightSkyCurrentWeatherResult())
            }
        } else {
            Observable.just(BrightSkyCurrentWeatherResult())
        }

        val alerts = if (!ignoreFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getAlerts(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_ALERT)
                Observable.just(BrightSkyAlertsResult())
            }
        } else {
            Observable.just(BrightSkyAlertsResult())
        }

        return Observable.zip(weather, curWeather, alerts) { brightSkyWeather, brightSkyCurWeather, brightSkyAlerts ->
            val languageCode = context.currentLocale.code
            convert(
                brightSkyWeather,
                brightSkyCurWeather,
                brightSkyAlerts,
                location,
                languageCode,
                failedFeatures
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        val failedFeatures = mutableListOf<SourceFeature>()
        val currentWeather = if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getCurrentWeather(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(BrightSkyCurrentWeatherResult())
            }
        } else {
            Observable.just(BrightSkyCurrentWeatherResult())
        }

        val alerts = if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getAlerts(
                location.latitude,
                location.longitude
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_ALERT)
                Observable.just(BrightSkyAlertsResult())
            }
        } else {
            Observable.just(BrightSkyAlertsResult())
        }

        return Observable.zip(currentWeather, alerts) { brightSkyCurrentWeather, brightSkyAlerts ->
            val languageCode = context.currentLocale.code
            convertSecondary(brightSkyCurrentWeather, brightSkyAlerts, languageCode, failedFeatures)
        }
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    override val isConfigured = true
    override val isRestricted = false
    private var instance: String
        set(value) {
            config.edit().putString("instance", value).apply()
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
                content = instance,
                regex = EditTextPreference.URL_REGEX,
                regexError = context.getString(R.string.settings_source_instance_invalid),
                onValueChanged = {
                    instance = it
                }
            )
        )
    }

    companion object {
        private const val BRIGHT_SKY_BASE_URL = "https://api.brightsky.dev/"
    }
}
