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
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.atmo.json.AtmoFrancePollenProperties
import org.breezyweather.sources.atmo.json.AtmoFrancePollenResult
import retrofit2.Retrofit
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt

/**
 * ATMO France
 */
class AtmoFranceService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") jsonClient: Retrofit.Builder,
) : HttpSource(), WeatherSource, LocationParametersSource, ConfigurableSource {

    override val id = "atmofrance"
    override val name = "ATMO France"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://www.atmo-france.org/article/politique-de-confidentialite"

    private val mApi by lazy {
        jsonClient
            .baseUrl(ATMO_FRANCE_BASE_URL)
            .build()
            .create(AtmoFranceApi::class.java)
    }

    private val mGeoApi by lazy {
        jsonClient
            .baseUrl(DATA_GOUV_GEO_BASE_URL)
            .build()
            .create(GeoApi::class.java)
    }

    override val supportedFeatures
        get() = mapOf(
            SourceFeature.POLLEN to "ATMO France + data.gouv.fr (Etalab 2.0)"
        )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return feature == SourceFeature.POLLEN &&
            !location.countryCode.isNullOrEmpty() &&
            location.countryCode.equals("FR", ignoreCase = true)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val currentCityCode = location.parameters.getOrElse(id) { null }?.getOrElse("citycode") { null }
        if (currentCityCode == null) {
            return Observable.error(InvalidLocationException())
        }

        val calendar = Date().toCalendarWithTimeZone(location.javaTimeZone)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val overmorrow = calendar.time

        val todayCall = mApi.getPollen(
            apiToken = getApiKeyOrDefault(),
            apiCode = 122,
            codeInsee = currentCityCode,
            dateEch = today.getFormattedDate("yyyy-MM-dd", location)
        ).onErrorResumeNext {
            Observable.just(AtmoFrancePollenResult())
        }

        val tomorrowCall = mApi.getPollen(
            apiToken = getApiKeyOrDefault(),
            apiCode = 122,
            codeInsee = currentCityCode,
            dateEch = tomorrow.getFormattedDate("yyyy-MM-dd", location)
        ).onErrorResumeNext {
            Observable.just(AtmoFrancePollenResult())
        }

        // Bulletin for today, tomorrow and overmorrow is published at 13:00
        val overmorrowCall = if (currentHour >= 13) {
            mApi.getPollen(
                apiToken = getApiKeyOrDefault(),
                apiCode = 122,
                codeInsee = currentCityCode,
                dateEch = overmorrow.getFormattedDate("yyyy-MM-dd", location)
            ).onErrorResumeNext {
                Observable.just(AtmoFrancePollenResult())
            }
        } else {
            Observable.just(AtmoFrancePollenResult())
        }

        return Observable.zip(
            todayCall,
            tomorrowCall,
            overmorrowCall
        ) {
                todayPollenResult: AtmoFrancePollenResult,
                tomorrowPollenResult: AtmoFrancePollenResult,
                overmorrowPollenResult: AtmoFrancePollenResult,
            ->
            val pollenDaily = mutableMapOf<Date, Pollen>()
            todayPollenResult.features?.getOrNull(0)?.properties?.let {
                pollenDaily[today] = getPollen(it)
            }
            tomorrowPollenResult.features?.getOrNull(0)?.properties?.let {
                pollenDaily[tomorrow] = getPollen(it)
            }
            overmorrowPollenResult.features?.getOrNull(0)?.properties?.let {
                pollenDaily[overmorrow] = getPollen(it)
            }

            WeatherWrapper(
                pollen = PollenWrapper(
                    dailyForecast = pollenDaily
                )
            )
        }
    }

    private fun getPollen(pollenResult: AtmoFrancePollenProperties): Pollen {
        return Pollen(
            alder = pollenResult.concAul?.roundToInt(),
            birch = pollenResult.concBoul?.roundToInt(),
            grass = pollenResult.concGram?.roundToInt(),
            mugwort = pollenResult.concArm?.roundToInt(),
            olive = pollenResult.concOliv?.roundToInt(),
            ragweed = pollenResult.concAmbr?.roundToInt()
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (!features.contains(SourceFeature.POLLEN)) return false

        if (coordinatesChanged) return true

        val currentCityCode = location.parameters.getOrElse(id) { null }?.getOrElse("citycode") { null }

        return currentCityCode.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mGeoApi.getReverseAddress(location.longitude, location.latitude)
            .map { result ->
                if (result.features.isNotEmpty()) {
                    mapOf("citycode" to getInseeCodeWithoutArrondissements(result.features[0].properties.citycode))
                } else {
                    throw InvalidLocationException()
                }
            }
    }

    private fun getInseeCodeWithoutArrondissements(cityCode: String): String {
        return when (cityCode) {
            "75101", "75102", "75103", "75104", "75105", "75106", "75107", "75108", "75109", "75110", "75111",
            "75112", "75113", "75114", "75115", "75116", "75117", "75118", "75119", "75120",
            -> "75056" // Paris
            "13201", "13202", "13203", "13204", "13205", "13206", "13207", "13208", "13209", "13210", "13211",
            "13212", "13213", "13214", "13215", "13216",
            -> "13055" // Marseille
            "69381", "69382", "69383", "69384", "69385", "69386", "69387", "69388", "69389",
            -> "69123" // Lyon
            else -> cityCode
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
        return apikey.ifEmpty { BuildConfig.ATMO_FRANCE_KEY }
    }
    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted = false

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_atmo_france_api_key,
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
        private const val ATMO_FRANCE_BASE_URL = "https://admindata.atmo-france.org/openapi/"
        private const val DATA_GOUV_GEO_BASE_URL = "https://api-adresse.data.gouv.fr/"
    }
}
