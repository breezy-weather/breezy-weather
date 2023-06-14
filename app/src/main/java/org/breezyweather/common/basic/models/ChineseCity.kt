package org.breezyweather.common.basic.models

import kotlinx.serialization.Serializable
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import java.util.TimeZone

@Serializable
data class ChineseCity(
    val cityId: String,
    val province: String,
    val city: String,
    val district: String,
    val latitude: String,
    val longitude: String
) {
    fun toLocation() = Location(
        cityId = cityId,
        latitude = latitude.toFloat(),
        longitude = longitude.toFloat(),
        timeZone = TimeZone.getTimeZone("Asia/Shanghai"),
        country = "中国",
        province = province,
        city = city,
        district = if (district == "无") "" else district,
        weather = null,
        weatherSource = WeatherSource.CHINA,
        isCurrentPosition = false,
        isResidentPosition = false,
        isChina = true
    )
}