package breezyweather.data.location

import breezyweather.domain.location.model.Location

object LocationMapper {

    fun mapLocation(
        cityId: String?,
        latitude: Double,
        longitude: Double,
        timeZone: String,
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
        isCurrentPosition: Boolean,
        needsGeocodeRefresh: Boolean,
        backgroundWeatherKind: String?,
        backgroundDayNightType: String?
    ): Location = Location(
        cityId = cityId,
        latitude = latitude,
        longitude = longitude,
        timeZone = timeZone,
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
        weatherSource = weatherSource,
        currentSource = currentSource,
        airQualitySource = airQualitySource,
        pollenSource = pollenSource,
        minutelySource = minutelySource,
        alertSource = alertSource,
        normalsSource = normalsSource,
        isCurrentPosition = isCurrentPosition,
        needsGeocodeRefresh = needsGeocodeRefresh,
        backgroundWeatherKind = backgroundWeatherKind,
        backgroundDayNightType = backgroundDayNightType
    )
}
