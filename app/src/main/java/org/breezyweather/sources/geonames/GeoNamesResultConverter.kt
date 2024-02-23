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
import java.util.TimeZone

fun convert(
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
        timeZone = TimeZone.getTimeZone(result.timezone?.timeZoneId),
        country = result.countryName ?: "",
        countryCode = result.countryCode ?: "",
        province = if (result.adminName2.isNullOrEmpty()) {
            if (result.adminName1.isNullOrEmpty()) {
                if (result.adminName3.isNullOrEmpty()) {
                    result.adminName4
                } else result.adminName3
            } else result.adminName1
        } else result.adminName2,
        provinceCode = if (result.adminName2.isNullOrEmpty()) {
            if (result.adminName1.isNullOrEmpty()) {
                if (result.adminName3.isNullOrEmpty()) {
                    result.adminCode4
                } else result.adminCode3
            } else result.adminCode1
        } else result.adminCode2,
        city = getLocalizedName(result, languageCode) ?: ""
    )
}

private fun getLocalizedName(
    result: GeoNamesLocation,
    languageCode: String
): String? {
    val localizedName = result.alternateNames?.firstOrNull {
        it.lang.equals(languageCode, ignoreCase = true)
    }
    return if (!localizedName?.name.isNullOrEmpty()) localizedName!!.name else result.name
}