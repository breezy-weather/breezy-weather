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

package org.breezyweather.sources.atmoaura

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.settings.SourceConfigStore
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

/**
 * ATMO Auvergne-Rhône-Alpes air quality service.
 */
class AtmoAuraService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), SecondaryWeatherSource, ConfigurableSource {

    override val id = "atmoaura"
    override val name = "ATMO Auvergne-Rhône-Alpes (${Locale(context.currentLocale.code, "FR").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.atmo-auvergnerhonealpes.fr/article/politique-de-confidentialite"

    private val mApi by lazy {
        client
            .baseUrl(ATMO_AURA_BASE_URL)
            .build()
            .create(AtmoAuraAirQualityApi::class.java)
    }

    override val supportedFeaturesInSecondary = listOf(SourceFeature.FEATURE_AIR_QUALITY)
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        // FIXME: We no longer have admin2Code, use a geometry instead of hardcoding departments
        return feature == SourceFeature.FEATURE_AIR_QUALITY &&
            !location.countryCode.isNullOrEmpty() &&
            location.countryCode.equals("FR", ignoreCase = true) &&
            location.admin2 in arrayOf(
                "Ain", // 01
                "Allier", // 03
                "Ardèche", // 07
                "Ardeche", // 07
                "Cantal", // 15
                "Drôme", // 26
                "Drome", // 26
                "Isère", // 38
                "Isere", // 38
                "Loire", // 42
                "Haute Loire", // 43
                "Haute-Loire", // 43
                "Puy-de-Dôme", // 63
                "Puy-de-Dome", // 63
                "Puy de Dôme", // 63
                "Puy de Dome", // 63
                "Rhône", // 69
                "Rhone", // 69
                "Savoie", // 73
                "Haute-Savoie", // 74
                "Haute Savoie" // 74
            )
    }
    override val currentAttribution = null
    override val airQualityAttribution = "ATMO Auvergne-Rhône-Alpes"
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = null
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        if (!isConfigured) {
            return Observable.error(ApiKeyMissingException())
        }
        if (!isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_AIR_QUALITY)) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }

        val calendar = Date().toCalendarWithTimeZone(location.javaTimeZone).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return mApi.getPointDetails(
            getApiKeyOrDefault(),
            location.longitude,
            location.latitude, // Tomorrow because it gives access to D-1 and D+1
            calendar.time.getFormattedDate("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", location)
        ).map {
            convert(it)
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
        return apikey.ifEmpty { BuildConfig.ATMO_AURA_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted = false

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_atmo_aura_api_key,
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
        private const val ATMO_AURA_BASE_URL = "https://api.atmo-aura.fr/"
    }
}
