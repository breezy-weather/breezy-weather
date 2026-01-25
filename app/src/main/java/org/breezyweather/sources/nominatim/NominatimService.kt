/*
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

package org.breezyweather.sources.nominatim

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.input.KeyboardType
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.Preference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.domain.settings.SourceConfigStore
import org.breezyweather.sources.nominatim.json.NominatimAddress
import org.breezyweather.sources.nominatim.json.NominatimLocationResult
import retrofit2.Retrofit
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Named

/**
 * Nominatim & LocationIQ service (Unified)
 *
 * Supports standard Nominatim instances OR LocationIQ via API Key (pk.xxxx).
 * Search is not possible for standard Nominatim, but enabled if LocationIQ key is provided.
 */
class NominatimService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), LocationSearchSource, ReverseGeocodingSource, ConfigurableSource {

    override val id = "nominatim"
    override val name = "Nominatim / LocationIQ"
    override val locationSearchAttribution =
        "Nominatim/LocationIQ • Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright"
    override val privacyPolicyUrl = "https://osmfoundation.org/wiki/Privacy_Policy"
    override val continent = SourceContinent.WORLDWIDE

    override val supportedFeatures = mapOf(
        SourceFeature.REVERSE_GEOCODING to locationSearchAttribution
    )
    override val attributionLinks = mapOf(
        name to NOMINATIM_BASE_URL,
        "OpenStreetMap" to "https://osm.org/",
        "https://osm.org/copyright" to "https://osm.org/copyright"
    )

    // Regex for Vietnam Display Name parsing
    // Finds specific administrative prefixes: Xã, Phường, Đặc Khu (case-insensitive, with/without accents)
    private val vnSubProvinceRegex = Pattern.compile("(?iu)(?:^|,\\s*)([^,]*?(?:xã|phường|đặc\\s*khu|xa|phuong|dac\\s*khu)[^,]*)(?:,|$)")

    private val mApi by lazy {
        val isLocationIQ = isLocationIqKey(instance)
        val url = if (isLocationIQ) LOCATIONIQ_BASE_URL else (instance ?: NOMINATIM_BASE_URL)
        Log.d("NominatimService", "Initializing API. URL: $url (LocationIQ: $isLocationIQ)")
        client
            .baseUrl(url)
            .build()
            .create(NominatimApi::class.java)
    }

    private fun isLocationIqKey(value: String?): Boolean {
        return value?.startsWith("pk.") == true
    }

    override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<LocationAddressInfo>> {
        val key = if (isLocationIqKey(instance)) instance else null
        
        return mApi.searchLocations(
            acceptLanguage = context.currentLocale.toLanguageTag(),
            userAgent = USER_AGENT,
            q = query,
            limit = 20,
            key = key
        ).map { results ->
            results.mapNotNull {
                convertLocation(it)
            }
        }
    }

    override fun requestNearestLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): Observable<List<LocationAddressInfo>> {
        val key = if (isLocationIqKey(instance)) instance else null
        
        // LocationIQ specific parameter tuning
        val zoom = if (key != null) 18 else 13
        val format = if (key != null) "json" else "jsonv2"

        return mApi.getReverseLocation(
            acceptLanguage = context.currentLocale.toLanguageTag(),
            userAgent = USER_AGENT,
            lat = latitude,
            lon = longitude,
            zoom = zoom,
            format = format,
            key = key
        ).map {
            Log.d("NominatimService", "Reverse Response: $it")
            if (it.address?.countryCode == null || it.address.countryCode.isEmpty()) {
                Log.e("NominatimService", "Invalid Location: Address or CountryCode missing. Raw: $it")
                throw InvalidLocationException()
            }

            listOf(convertLocation(it)!!)
        }
    }

    private fun convertLocation(locationResult: NominatimLocationResult): LocationAddressInfo? {
        Log.d("NominatimService", "convertLocation input: $locationResult")
        
        return if (locationResult.address?.countryCode == null || locationResult.address.countryCode.isEmpty()) {
            Log.d("NominatimService", "Dropped result due to missing countryCode")
            null
        } else {
            val countryCode = getNonAmbiguousCountryCode(locationResult.address)
            
            // Vietnam Special Parsing
            var city = locationResult.address.town ?: locationResult.name
            var district = locationResult.address.village
            val isLocationIQ = isLocationIqKey(instance)

            if (countryCode.equals("vn", ignoreCase = true)) {
                // Try to extract Xa/Phuong/Dac Khu from display_name
                val displayName = locationResult.displayName
                Log.d("NominatimService", "Parsing VN Address - Provider: ${if(isLocationIQ) "LocationIQ" else "Nominatim"}")
                Log.d("NominatimService", "Raw DisplayName: $displayName")
                Log.d("NominatimService", "Initial City: $city, District: $district")

                if (!displayName.isNullOrEmpty()) {
                    val matcher = vnSubProvinceRegex.matcher(displayName)
                    if (matcher.find()) {
                        val matched = matcher.group(1).trim()
                        Log.d("NominatimService", "Regex Matched: $matched")
                        city = matched
                        district = null // Hide district if we found a better name
                    } else if (isLocationIQ) {
                        // Fallback logic for LocationIQ if regex fails: use first part of display_name
                        val fallback = displayName.split(",").firstOrNull()?.trim()
                        Log.d("NominatimService", "Regex Failed. LocationIQ Fallback: $fallback")
                        if (fallback != null) {
                            city = fallback
                            district = null
                        }
                    } else {
                        Log.d("NominatimService", "Regex Failed. No fallback applied.")
                    }
                }
                Log.d("NominatimService", "Final City: $city")
            }

            LocationAddressInfo(
                latitude = locationResult.lat.toDoubleOrNull(),
                longitude = locationResult.lon.toDoubleOrNull(),
                country = locationResult.address.country,
                countryCode = countryCode,
                admin1 = locationResult.address.state,
                admin1Code = getAdmin1CodeForCountry(locationResult.address, countryCode),
                admin2 = locationResult.address.county,
                admin2Code = getAdmin2CodeForCountry(locationResult.address, countryCode),
                admin3 = locationResult.address.municipality,
                city = city,
                cityCode = locationResult.placeId?.toString(),
                district = district
            )
        }
    }

    private fun getAdmin1CodeForCountry(
        address: NominatimAddress,
        countryCode: String,
    ): String? {
        return when (countryCode.uppercase()) {
            // Keep the iso code "FR-XX" as the INSEE code is different
            "AR", "AU", "BR", "CA", "CD", "CL", "CN", "EC", "ES", "FM", "FR", "ID",
            "KI", "KZ", "MN", "MX", "MY", "NZ", "PG", "PT", "RU", "UA", "US",
            -> address.isoLvl4
            else -> null
        }
    }

    private fun getAdmin2CodeForCountry(
        address: NominatimAddress,
        countryCode: String,
    ): String? {
        return when (countryCode.uppercase()) {
            "CY" -> address.isoLvl5
            "FR" -> address.isoLvl6?.replace("FR-", "") // Conversion to INSEE code
                ?.let {
                    when (it) {
                        "69M" -> "69"
                        "75C" -> "75"
                        else -> it
                    }
                }
            else -> null
        }
    }

    private fun getNonAmbiguousCountryCode(address: NominatimAddress): String {
        return address.countryCode!!.let {
            with(it) {
                when {
                    equals("CN", ignoreCase = true) -> {
                        with(address.isoLvl3) {
                            when {
                                equals("CN-MO", ignoreCase = true) -> "MO"
                                equals("CN-HK", ignoreCase = true) -> "HK"
                                else -> "CN"
                            }
                        }
                    }
                    equals("FI", ignoreCase = true) -> {
                        with(address.isoLvl3) {
                            when {
                                equals("FI-01", ignoreCase = true) -> "AX"
                                else -> "FI"
                            }
                        }
                    }
                    equals("FR", ignoreCase = true) -> {
                        with(address.isoLvl3) {
                            when {
                                equals("FR-971", ignoreCase = true) -> "GP"
                                equals("FR-972", ignoreCase = true) -> "MQ"
                                equals("FR-973", ignoreCase = true) -> "GF"
                                equals("FR-974", ignoreCase = true) -> "RE"
                                equals("FR-975", ignoreCase = true) -> "PM"
                                equals("FR-976", ignoreCase = true) -> "YT"
                                equals("FR-977", ignoreCase = true) -> "BL"
                                equals("FR-978", ignoreCase = true) -> "MF"
                                equals("FR-986", ignoreCase = true) -> "WF"
                                equals("FR-987", ignoreCase = true) -> "PF"
                                equals("FR-988", ignoreCase = true) -> "NC"
                                equals("FR-BL", ignoreCase = true) -> "BL"
                                equals("FR-CP", ignoreCase = true) -> "CP" // Not official, but reserved
                                equals("FR-MF", ignoreCase = true) -> "MF"
                                equals("FR-NC", ignoreCase = true) -> "NC"
                                equals("FR-PF", ignoreCase = true) -> "PF"
                                equals("FR-PM", ignoreCase = true) -> "PM"
                                equals("FR-TF", ignoreCase = true) -> "TF"
                                equals("FR-WF", ignoreCase = true) -> "WF"
                                else -> "FR"
                            }
                        }
                    }
                    equals("NL", ignoreCase = true) -> {
                        when {
                            address.isoLvl3?.equals("NL-AW", ignoreCase = true) == true -> "AW"
                            address.isoLvl3?.equals("NL-CW", ignoreCase = true) == true -> "CW"
                            address.isoLvl3?.equals("NL-SX", ignoreCase = true) == true -> "SX"
                            address.isoLvl8?.startsWith("BQ", ignoreCase = true) == true -> "BQ"
                            else -> "NL"
                        }
                    }
                    equals("NO", ignoreCase = true) -> {
                        with(address.isoLvl4) {
                            when {
                                equals("NO-21", ignoreCase = true) -> "SJ"
                                equals("NO-22", ignoreCase = true) -> "SJ"
                                else -> "NO"
                            }
                        }
                    }
                    equals("US", ignoreCase = true) -> {
                        when {
                            address.isoLvl4?.equals("US-AS", ignoreCase = true) == true -> "AS"
                            address.isoLvl4?.equals("US-GU", ignoreCase = true) == true -> "GU"
                            address.isoLvl4?.equals("US-MP", ignoreCase = true) == true -> "MP"
                            address.isoLvl4?.equals("US-PR", ignoreCase = true) == true -> "PR"
                            address.isoLvl4?.equals("US-VI", ignoreCase = true) == true -> "VI"
                            address.isoLvl15?.startsWith("UM", ignoreCase = true) == true -> "UM"
                            else -> "US"
                        }
                    }
                    else -> it
                }
            }
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
        get() = config.getString("instance", null) ?: NOMINATIM_BASE_URL
    override fun getPreferences(context: Context): List<Preference> {
        return listOf(
            EditTextPreference(
                titleId = R.string.settings_weather_source_nominatim_instance,
                summary = { _, content ->
                    if (isLocationIqKey(content)) "LocationIQ" else content.ifEmpty {
                        NOMINATIM_BASE_URL
                    }
                },
                content = if (instance != NOMINATIM_BASE_URL) instance else null,
                placeholder = NOMINATIM_BASE_URL,
                regex = null,
                regexError = null,
                keyboardType = KeyboardType.Text,
                onValueChanged = {
                    instance = if (it == NOMINATIM_BASE_URL) null else it.ifEmpty { null }
                }
            )
        )
    }

    // We have no way to distinguish the ones below. Others were deduced with other info in the code above
    override val knownAmbiguousCountryCodes = arrayOf(
        "AU", // Territories: CX, CC, HM (uninhabited), NF
        "NO" // Territories: BV
    )

    companion object {
        private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
        private const val LOCATIONIQ_BASE_URL = "https://us1.locationiq.com/v1/"
        private const val USER_AGENT =
            "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}
