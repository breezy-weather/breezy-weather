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

package org.breezyweather.sources.atmo

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.R
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.atmo.json.AtmoPointResult
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date

/**
 * ATMO services
 */
abstract class AtmoService : HttpSource(), WeatherSource, ConfigurableSource {

    protected abstract val context: Context
    protected abstract val jsonClient: Retrofit.Builder

    protected abstract val attribution: String

    override val continent = SourceContinent.EUROPE

    override val color = Color.rgb(49, 77, 154)

    /**
     * E.g. https://api.atmo-aura.fr/air2go/v3/
     */
    protected abstract val baseUrl: String
    private val mApi by lazy {
        jsonClient
            .baseUrl(baseUrl)
            .build()
            .create(AtmoApi::class.java)
    }

    /**
     * E.g. R.string.settings_weather_source_atmo_aura_api_key
     */
    protected abstract val apiKeyPreference: Int
    protected abstract val builtInApiKey: String

    override val supportedFeatures
        get() = mapOf(
            SourceFeature.AIR_QUALITY to attribution
        )
    protected abstract fun isLocationInRegion(location: Location): Boolean
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return feature == SourceFeature.AIR_QUALITY &&
            !location.countryCode.isNullOrEmpty() &&
            location.countryCode.equals("FR", ignoreCase = true) &&
            isLocationInRegion(location)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
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
            val airQualityHourly = mutableMapOf<Date, AirQuality>()
            it.polluants?.getOrNull(0)?.horaires?.forEach { h ->
                airQualityHourly[h.datetimeEcheance] = getAirQuality(h.datetimeEcheance, it)
            }

            WeatherWrapper(
                airQuality = AirQualityWrapper(
                    hourlyForecast = airQualityHourly
                )
            )
        }
    }

    private fun getAirQuality(requestedDate: Date, aqiAtmoAuraResult: AtmoPointResult): AirQuality {
        var pm25: Double? = null
        var pm10: Double? = null
        var so2: Double? = null
        var no2: Double? = null
        var o3: Double? = null

        aqiAtmoAuraResult.polluants
            ?.filter { p -> p.horaires?.firstOrNull { it.datetimeEcheance == requestedDate } != null }
            ?.forEach { p ->
                when (p.polluant) {
                    "o3" -> o3 = p.horaires?.firstOrNull {
                        it.datetimeEcheance == requestedDate
                    }?.concentration
                    "no2" -> no2 = p.horaires?.firstOrNull {
                        it.datetimeEcheance == requestedDate
                    }?.concentration
                    "pm2.5" -> pm25 = p.horaires?.firstOrNull {
                        it.datetimeEcheance == requestedDate
                    }?.concentration
                    "pm10" -> pm10 = p.horaires?.firstOrNull {
                        it.datetimeEcheance == requestedDate
                    }?.concentration
                    "so2" -> so2 = p.horaires?.firstOrNull {
                        it.datetimeEcheance == requestedDate
                    }?.concentration
                }
            }

        return AirQuality(
            pM25 = pm25,
            pM10 = pm10,
            sO2 = so2,
            nO2 = no2,
            o3 = o3
        )
    }

    // CONFIG
    private val config
        get() = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""
    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { builtInApiKey }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted = false

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = apiKeyPreference,
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
}
