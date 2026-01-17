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

package breezyweather.data.location

import breezyweather.domain.location.model.Location
import java.util.TimeZone

object LocationMapper {

    fun mapLocation(
        cityId: String?,
        latitude: Double,
        longitude: Double,
        timeZone: TimeZone,
        customName: String?,
        country: String,
        countryCode: String?,
        admin1: String?,
        admin1Code: String?,
        admin2: String?,
        admin2Code: String?,
        admin3: String?,
        admin3Code: String?,
        admin4: String?,
        admin4Code: String?,
        city: String,
        district: String?,
        weatherSource: String,
        currentSource: String?,
        airQualitySource: String?,
        pollenSource: String?,
        minutelySource: String?,
        alertSource: String?,
        normalsSource: String?,
        reverseGeocodingSource: String?,
        isCurrentPosition: Boolean,
        needsGeocodeRefresh: Boolean,
        backgroundWeatherKind: String?, // TODO: Deprecated
        backgroundDayNightType: String?, // TODO: Deprecated
    ): Location = Location(
        cityId = cityId,
        latitude = latitude,
        longitude = longitude,
        timeZone = timeZone,
        customName = customName,
        country = country,
        countryCode = countryCode,
        admin1 = admin1,
        admin1Code = admin1Code,
        admin2 = admin2,
        admin2Code = admin2Code,
        admin3 = admin3,
        admin3Code = admin3Code,
        admin4 = admin4,
        admin4Code = admin4Code,
        city = city,
        district = district,
        forecastSource = weatherSource,
        currentSource = currentSource,
        airQualitySource = airQualitySource,
        pollenSource = pollenSource,
        minutelySource = minutelySource,
        alertSource = alertSource,
        normalsSource = normalsSource,
        reverseGeocodingSource = reverseGeocodingSource,
        isCurrentPosition = isCurrentPosition,
        needsGeocodeRefresh = needsGeocodeRefresh
    )
}
