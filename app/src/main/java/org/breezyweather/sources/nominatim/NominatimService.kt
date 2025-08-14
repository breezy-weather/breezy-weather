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
            LocationAddressInfo(
                latitude = locationResult.lat.toDoubleOrNull(),
                longitude = locationResult.lon.toDoubleOrNull(),
                district = locationResult.address.village,
                city = locationResult.address.town ?: locationResult.name,
                cityCode = locationResult.placeId?.toString(),
                admin3 = locationResult.address.municipality,
                admin2 = locationResult.address.county,
                admin1 = locationResult.address.state,
                country = locationResult.address.country,
                countryCode = locationResult.address.countryCode.let {
                    with(it) {
                        when {
                            equals("CN", ignoreCase = true) -> {
                                with(locationResult.address.isoLvl3) {
                                    when {
                                        equals("CN-MO", ignoreCase = true) -> "MO"
                                        equals("CN-HK", ignoreCase = true) -> "HK"
                                        else -> "CN"
                                    }
                                }
                            }
                            equals("FI", ignoreCase = true) -> {
                                with(locationResult.address.isoLvl3) {
                                    when {
                                        equals("FI-01", ignoreCase = true) -> "AX"
                                        else -> "FI"
                                    }
                                }
                            }
                            equals("FR", ignoreCase = true) -> {
                                with(locationResult.address.isoLvl3) {
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
                                        equals("FR-TF", ignoreCase = true) -> "TF"
                                        else -> "FR"
                                    }
                                }
                            }
                            else -> it
                        }
                    }
                }
            )
        }
    }

    /**
     * TODO: In progress
     */
    override val knownAmbiguousCountryCodes = arrayOf(
        "AU", // Territories: CX, CC, HM (uninhabited), NF
        "GB", // Territories: AI, BM, IO, KY, FK, GI, GG, IM, JE, MS, PN, SH, GS (uninhabited), TC, VG
        "NL", // Territories: AW, BQ, CW, SX
        "NO", // Territories: BV, SJ
        "NZ", // Territories: TK. Associated states: CK, NU
        "US" // Territories: AS, GU, MP, PR, UM (uninhabited), VI
    )

    companion object {
        private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
        private const val USER_AGENT =
            "BreezyWeather/${BuildConfig.VERSION_NAME} github.com/breezy-weather/breezy-weather/issues"
    }
}
