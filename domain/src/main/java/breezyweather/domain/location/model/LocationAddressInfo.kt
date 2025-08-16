/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package breezyweather.domain.location.model

data class LocationAddressInfo(
    /**
     * Mandatory when used in the location search process
     * In the reverse geocoding process, if provided, will throw an error if the returned location is too far away
     * from the originally provided coordinates
     */
    val latitude: Double? = null,
    /**
     * Mandatory when used in the location search process
     * In the reverse geocoding process, if provided, will throw an error if the returned location is too far away
     * from the originally provided coordinates
     */
    val longitude: Double? = null,
    /**
     * Time zone of the location in the TZ identifier format
     * Examples: America/New_York, Europe/Paris
     *
     * The list of accepted time zones can be found here:
     * https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     */
    val timeZoneId: String? = null,

    /**
     * Leave null or empty to let the system translates the country name from the countryCode
     */
    val country: String? = null,
    /**
     * Contrary to its name, this code represents not just countries, but also dependent territories, and special areas
     *  of geographical interest
     * Must be a valid ISO 3166-1 alpha-2 code
     * https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
     *
     * We don't support locations in the middle of the ocean, so ensure this is non empty.
     *
     * Will throw an error if it is not a valid 2 alpha letter code
     */
    val countryCode: String,
    val admin1: String? = null,
    /**
     * Can be an ISO 3166-2 code, or the internal code used by the country
     */
    val admin1Code: String? = null,
    val admin2: String? = null,
    /**
     * Can be an ISO 3166-2 code, or the internal code used by the country
     */
    val admin2Code: String? = null,
    val admin3: String? = null,
    /**
     * Can be an ISO 3166-2 code, or the internal code used by the country
     */
    val admin3Code: String? = null,
    val admin4: String? = null,
    /**
     * Can be an ISO 3166-2 code, or the internal code used by the country
     */
    val admin4Code: String? = null,
    val city: String? = null,
    val cityCode: String? = null,
    val district: String? = null,
)
