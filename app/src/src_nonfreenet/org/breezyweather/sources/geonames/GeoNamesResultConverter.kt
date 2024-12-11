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

import breezyweather.domain.location.model.Location
import org.breezyweather.sources.geonames.json.GeoNamesLocation

internal fun convert(
    result: GeoNamesLocation,
    languageCode: String,
): Location? {
    if ((result.lat == 0.0 && result.lng == 0.0) || result.timezone?.timeZoneId.isNullOrEmpty()) {
        return null
    }
    return Location(
        cityId = result.geonameId.toString(),
        latitude = result.lat,
        longitude = result.lng,
        timeZone = result.timezone!!.timeZoneId!!,
        country = result.countryName ?: "",
        countryCode = result.countryCode ?: "",
        admin1 = result.adminName1,
        admin1Code = result.adminCode1,
        admin2 = result.adminName2,
        admin2Code = result.adminCode2,
        admin3 = result.adminName3,
        admin3Code = result.adminCode3,
        admin4 = result.adminName4,
        admin4Code = result.adminCode4,
        city = getLocalizedName(result, languageCode) ?: ""
    )
}

private fun getLocalizedName(
    result: GeoNamesLocation,
    languageCode: String,
): String? {
    val localizedName = result.alternateNames?.firstOrNull {
        it.lang.equals(languageCode, ignoreCase = true)
    }
    return if (!localizedName?.name.isNullOrEmpty()) localizedName!!.name else result.name
}
