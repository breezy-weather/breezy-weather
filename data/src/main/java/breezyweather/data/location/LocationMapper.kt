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
        province: String?,
        provinceCode: String?,
        city: String,
        district: String?,
        weatherSource: String,
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
        province = province,
        provinceCode = provinceCode,
        city = city,
        district = district,
        weatherSource = weatherSource,
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
