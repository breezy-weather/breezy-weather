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

package org.breezyweather.sources.geonames

import android.content.Context
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiLimitReachedException
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.extensions.codeForGeonames
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.geonames.json.GeoNamesLocation
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

class GeoNamesService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), LocationSearchSource, ConfigurableSource {

    override val id = "geonames"
    override val name = "GeoNames"
    override val continent = SourceContinent.WORLDWIDE
    override val privacyPolicyUrl = ""

    override val locationSearchAttribution = "GeoNames (CC BY 4.0)"
    override val attributionLinks = mapOf(
        name to "https://www.geonames.org/"
    )

    private val mApi by lazy {
        client
            .baseUrl(GEO_NAMES_BASE_URL)
            .build()
            .create(GeoNamesApi::class.java)
    }

    override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<LocationAddressInfo>> {
        val apiKey = getApiKeyOrDefault()
        val languageCode = context.currentLocale.codeForGeonames
        return mApi.getLocation(
            query,
            fuzzy = 0.8,
            maxRows = 20,
            apiKey,
            style = "FULL"
        ).map { results ->
            if (results.status != null) {
                when (results.status.value) {
                    15 -> emptyList() // No result
                    18, 19, 20 -> throw ApiLimitReachedException() // Hourly, daily, weekly limit
                    else -> throw LocationSearchException()
                }
            } else {
                results.geonames
                    ?.filter { (it.lat != 0.0 || it.lng != 0.0) && !it.countryCode.isNullOrEmpty() }
                    ?.map { convertLocation(it, languageCode) }
                    ?: emptyList()
            }
        }
    }

    private fun convertLocation(
        result: GeoNamesLocation,
        languageCode: String,
    ): LocationAddressInfo {
        return LocationAddressInfo(
            latitude = result.lat,
            longitude = result.lng,
            timeZoneId = result.timezone?.timeZoneId,
            country = result.countryName,
            countryCode = result.countryCode!!,
            admin1 = result.adminName1,
            admin1Code = result.adminCode1,
            admin2 = result.adminName2,
            admin2Code = result.adminCode2,
            admin3 = result.adminName3,
            admin3Code = result.adminCode3,
            admin4 = result.adminName4,
            admin4Code = result.adminCode4,
            city = getLocalizedName(result, languageCode),
            cityCode = result.geonameId.toString()
        )
    }

    private fun getLocalizedName(
        result: GeoNamesLocation,
        languageCode: String,
    ): String? {
        val localizedName = result.alternateNames?.firstOrNull {
            it.lang.equals(languageCode, ignoreCase = true)
        }
        return if (!localizedName?.name.isNullOrEmpty()) localizedName.name else result.name
    }

    // CONFIG
    private val config = SourceConfigStore(context, id)
    private var apikey: String
        set(value) {
            config.edit().putString("apikey", value).apply()
        }
        get() = config.getString("apikey", null) ?: ""

    private fun getApiKeyOrDefault(): String {
        return apikey.ifEmpty { BuildConfig.GEO_NAMES_KEY }
    }

    override val isConfigured
        get() = getApiKeyOrDefault().isNotEmpty()

    override val isRestricted
        get() = apikey.isEmpty()

    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_source_geonames_api_key,
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

    // TODO: Same as Open-Meteo
    override val knownAmbiguousCountryCodes: Array<String>? = null

    companion object {
        private const val GEO_NAMES_BASE_URL = "https://secure.geonames.org/"
    }
}
