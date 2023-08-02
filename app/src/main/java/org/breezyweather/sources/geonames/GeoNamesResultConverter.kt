package org.breezyweather.sources.geonames

import org.breezyweather.common.basic.models.Location
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
        latitude = result.lat.toFloat(),
        longitude = result.lng.toFloat(),
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