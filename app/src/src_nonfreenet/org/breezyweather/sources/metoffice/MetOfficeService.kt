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

package org.breezyweather.sources.metoffice

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SourceConfigStore
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class MetOfficeService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ConfigurableSource, ReverseGeocodingSource {

    override val id = "metoffice"
    override val name = "Met Office (${Locale(context.currentLocale.code, "GB").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.metoffice.gov.uk/policies/privacy"

    override val color = Color.rgb(185, 213, 50)

    private val mApi by lazy {
        client
            .baseUrl(MET_OFFICE_BASE_URL)
            .build()
            .create(MetOfficeApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to "Met Office"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val apiKey = getApiKeyOrDefault()

        return Observable.zip(
            mApi.getHourlyForecast(apiKey, location.latitude, location.longitude),
            mApi.getDailyForecast(apiKey, location.latitude, location.longitude)
        ) { hourly, daily ->
            if (hourly.features.isEmpty() || daily.features.isEmpty()) {
                throw InvalidOrIncompleteDataException()
            }
            WeatherWrapper(
                hourlyForecast = getHourlyForecast(hourly, context),
                dailyForecast = getDailyForecast(daily, context)
            )
        }
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val apiKey = getApiKeyOrDefault()

        return mApi.getHourlyForecast(apiKey, location.latitude, location.longitude, true).map {
            buildList {
                it.features.getOrNull(0)?.let { feature ->
                    add(
                        location.copy(
                            district = feature.properties.location?.name
                        )
                    )
                }
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
        return apikey.ifEmpty { BuildConfig.MET_OFFICE_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_met_office_api_key,
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
        private const val MET_OFFICE_BASE_URL =
            "https://data.hub.api.metoffice.gov.uk/sitespecific/v0/"
    }
}
