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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.atmoaura.json.AtmoAuraPointResult
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
) : HttpSource(), WeatherSource, ConfigurableSource {

    override val id = "atmoaura"
    override val name = "ATMO Auvergne-Rhône-Alpes (${Locale(context.currentLocale.code, "FR").displayCountry})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.atmo-auvergnerhonealpes.fr/article/politique-de-confidentialite"

    override val color = Color.rgb(49, 77, 154)

    private val mApi by lazy {
        client
            .baseUrl(ATMO_AURA_BASE_URL)
            .build()
            .create(AtmoAuraAirQualityApi::class.java)
    }

    override val supportedFeatures = mapOf(
        SourceFeature.AIR_QUALITY to "ATMO Auvergne-Rhône-Alpes"
    )
    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        // FIXME: We no longer have admin2Code, use a geometry instead of hardcoding departments
        return feature == SourceFeature.AIR_QUALITY &&
            !location.countryCode.isNullOrEmpty() &&
            location.countryCode.equals("FR", ignoreCase = true) &&
            (
                location.admin1 in arrayOf(
                    "Auvergne-Rhône-Alpes",
                    "Auvergne-Rhone-Alpes",
                    "Auvergne Rhône Alpes",
                    "Auvergne Rhone Alpes"
                ) ||
                    location.admin1Code == "84" ||
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
                    ) ||
                    location.admin2Code in arrayOf(
                        "01", // Ain
                        "03", // Allier
                        "07", // Ardèche
                        "15", // Cantal
                        "26", // Drôme
                        "38", // Isère
                        "42", // Loire
                        "43", // Haute-Loire
                        "63", // Puy-de-Dôme
                        "69", // Rhône
                        "73", // Savoie
                        "74" // Haute-Savoie
                    )
                )
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

    private fun getAirQuality(requestedDate: Date, aqiAtmoAuraResult: AtmoAuraPointResult): AirQuality {
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
                    }?.concentration?.toDouble()
                    "no2" -> no2 = p.horaires?.firstOrNull {
                        it.datetimeEcheance == requestedDate
                    }?.concentration?.toDouble()
                    "pm2.5" -> pm25 = p.horaires?.firstOrNull {
                        it.datetimeEcheance == requestedDate
                    }?.concentration?.toDouble()
                    "pm10" -> pm10 = p.horaires?.firstOrNull {
                        it.datetimeEcheance == requestedDate
                    }?.concentration?.toDouble()
                    "so2" -> so2 = p.horaires?.firstOrNull {
                        it.datetimeEcheance == requestedDate
                    }?.concentration?.toDouble()
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
