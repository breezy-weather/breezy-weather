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

package org.breezyweather.sources.nominatim

import android.content.Context
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.sources.nominatim.json.NominatimAddress
import org.breezyweather.sources.nominatim.json.NominatimLocationResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * Nominatim service
 *
 * Search is not possible, as timezone is mandatory
 * Only supports reverse geocoding for current location, by falling back to device timezone
 */
class NominatimService @Inject constructor(
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), LocationSearchSource, ReverseGeocodingSource {

    override val id = "nominatim"
    override val name = "Nominatim"
    override val locationSearchAttribution =
        "Nominatim • Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright"
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
    private val mApi by lazy {
        client
            .baseUrl(NOMINATIM_BASE_URL)
            .build()
            .create(NominatimApi::class.java)
    }

    override fun requestLocationSearch(
        context: Context,
        query: String,
    ): Observable<List<LocationAddressInfo>> {
        return mApi.searchLocations(
            acceptLanguage = context.currentLocale.toLanguageTag(),
            userAgent = USER_AGENT,
            q = query,
            limit = 20
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
        return mApi.getReverseLocation(
            acceptLanguage = context.currentLocale.toLanguageTag(),
            userAgent = USER_AGENT,
            lat = latitude,
            lon = longitude
        ).map {
            if (it.address?.countryCode == null || it.address.countryCode.isEmpty()) {
                throw InvalidLocationException()
            }

            listOf(convertLocation(it)!!)
        }
    }

    private fun convertLocation(locationResult: NominatimLocationResult): LocationAddressInfo? {
        return if (locationResult.address?.countryCode == null || locationResult.address.countryCode.isEmpty()) {
            null
        } else {
            val countryCode = getNonAmbiguousCountryCode(locationResult.address)

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
                city = locationResult.address.town ?: locationResult.name,
                cityCode = locationResult.placeId?.toString(),
                district = locationResult.address.village
            )
        }
    }

    private fun getAdmin1CodeForCountry(
        address: NominatimAddress,
        countryCode: String,
    ): String? {
        return when (countryCode) {
            "FR" -> address.isoLvl4
            else -> null
        }
    }

    private fun getAdmin2CodeForCountry(
        address: NominatimAddress,
        countryCode: String,
    ): String? {
        return when (countryCode) {
            "FR" -> address.isoLvl6
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
                                equals("FR-CP", ignoreCase = true) -> "CP" // Not official, but reserved
                                equals("FR-TF", ignoreCase = true) -> "TF"
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

    // We have no way to distinguish the ones below. Others were deduced with other info in the code above
    override val knownAmbiguousCountryCodes = arrayOf(
        "AU", // Territories: CX, CC, HM (uninhabited), NF
        "NO" // Territories: BV
    )

    companion object {
        private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
        private const val USER_AGENT =
            "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}
