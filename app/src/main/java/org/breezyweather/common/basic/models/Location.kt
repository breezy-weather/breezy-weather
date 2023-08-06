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

package org.breezyweather.common.basic.models

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Weather
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Location(
    val cityId: String? = null,

    val latitude: Float = 0f,
    val longitude: Float = 0f,
    val timeZone: TimeZone = TimeZone.getDefault(),

    val country: String = "",
    val countryCode: String? = null,
    val province: String? = null,
    val provinceCode: String? = null,
    val city: String = "",
    val district: String? = null,

    val weather: Weather? = null,
    val weatherSource: String = BuildConfig.DEFAULT_WEATHER_SOURCE,
    val airQualitySource: String? = null,
    val allergenSource: String? = null,
    val minutelySource: String? = null,
    val alertSource: String? = null,

    val isCurrentPosition: Boolean = false,
    val isResidentPosition: Boolean = false,

    val needsGeocodeRefresh: Boolean = false
) : Parcelable {

    val formattedId: String
        get() = if (isCurrentPosition) {
            CURRENT_POSITION_ID
        } else {
            String.format(Locale.US, "%f", latitude) + "&" +
                    String.format(Locale.US, "%f", longitude) + "&" +
                    weatherSource
        }

    val isDaylight: Boolean
        get() = isDayLight(this)

    val isUsable: Boolean
        // Sorry people living exactly at 0,0
        get() = latitude != 0f || longitude != 0f

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cityId)
        parcel.writeFloat(latitude)
        parcel.writeFloat(longitude)
        parcel.writeSerializable(timeZone)
        parcel.writeString(country)
        parcel.writeString(countryCode)
        parcel.writeString(province)
        parcel.writeString(provinceCode)
        parcel.writeString(city)
        parcel.writeString(district)
        parcel.writeString(weatherSource)
        parcel.writeString(airQualitySource)
        parcel.writeString(allergenSource)
        parcel.writeString(minutelySource)
        parcel.writeString(alertSource)
        parcel.writeByte(if (isCurrentPosition) 1 else 0)
        parcel.writeByte(if (isResidentPosition) 1 else 0)
        parcel.writeByte(if (needsGeocodeRefresh) 1 else 0)
    }

    override fun describeContents() = 0

    constructor(parcel: Parcel) : this(
        cityId = parcel.readString(),
        latitude = parcel.readFloat(),
        longitude = parcel.readFloat(),
        timeZone = parcel.readSerializable()!! as TimeZone,
        country = parcel.readString()!!,
        countryCode = parcel.readString(),
        province = parcel.readString(),
        provinceCode = parcel.readString(),
        city = parcel.readString()!!,
        district = parcel.readString(),
        weatherSource = parcel.readString()!!,
        airQualitySource = parcel.readString(),
        allergenSource = parcel.readString(),
        minutelySource = parcel.readString(),
        alertSource = parcel.readString(),
        isCurrentPosition = parcel.readByte() != 0.toByte(),
        isResidentPosition = parcel.readByte() != 0.toByte(),
        needsGeocodeRefresh = parcel.readByte() != 0.toByte()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is Location) {
            return false
        }

        if (formattedId != other.formattedId) {
            return false
        }

        if (isResidentPosition != other.isResidentPosition) {
            return false
        }

        if (weatherSource != other.weatherSource) {
            return false
        }

        if (airQualitySource != other.airQualitySource) {
            return false
        }

        if (allergenSource != other.allergenSource) {
            return false
        }

        if (minutelySource != other.minutelySource) {
            return false
        }

        if (alertSource != other.alertSource) {
            return false
        }

        if (needsGeocodeRefresh != other.needsGeocodeRefresh) {
            return false
        }

        val thisWeather = weather
        val otherWeather = other.weather
        if (thisWeather == null && otherWeather == null) {
            return true
        }
        return if (thisWeather != null && otherWeather != null) {
            thisWeather.base.updateDate.time == otherWeather.base.updateDate.time
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return formattedId.hashCode()
    }

    override fun toString(): String {
        val builder = StringBuilder("$country $province")
        if (province != city
            && city.isNotEmpty()
        ) {
            builder.append(" ").append(city)
        }
        if (city != district
            && !district.isNullOrEmpty()
        ) {
            builder.append(" ").append(district)
        }
        return builder.toString()
    }

    fun getPlace(context: Context, showCurrentPositionInPriority: Boolean = false): String {
        if (showCurrentPositionInPriority && isCurrentPosition) {
            return context.getString(R.string.location_current)
        }
        val builder = StringBuilder()
        if (city.isNotEmpty()) {
            builder.append(city)
        }
        if (!district.isNullOrEmpty()) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(district)
        }
        if (builder.toString().isEmpty() && isCurrentPosition) {
            return context.getString(R.string.location_current)
        }
        return builder.toString()
    }

    fun administrationLevels(): String {
        val builder = StringBuilder()
        if (country.isNotEmpty()) {
            builder.append(country)
        }
        if (!province.isNullOrEmpty()) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(province)
        }
        return builder.toString()
    }

    private fun isCloseTo(c: Context, location: Location): Boolean {
        if (cityId == location.cityId) {
            return true
        }
        if (isEquals(province, location.province)
            && isEquals(city, location.city)
        ) {
            return true
        }
        return if (isEquals(province, location.province)
            && getPlace(c) == location.getPlace(c)
        ) {
            true
        } else {
            distance(this, location) < (20 * 1000)
        }
    }

    val airQualitySourceNotNull: String
        get() = if (airQualitySource.isNullOrEmpty()) weatherSource else airQualitySource
    val allergenSourceNotNull: String
        get() = if (allergenSource.isNullOrEmpty()) weatherSource else allergenSource
    val minutelySourceNotNull: String
        get() = if (minutelySource.isNullOrEmpty()) weatherSource else minutelySource
    val alertSourceNotNull: String
        get() = if (alertSource.isNullOrEmpty()) weatherSource else alertSource

    companion object {

        const val CURRENT_POSITION_ID = "CURRENT_POSITION"

        fun isDayLight(location: Location? = null): Boolean {
            val sunRiseProgress = Astro.getRiseProgress(
                astro = location?.weather?.dailyForecast?.getOrNull(0)?.sun,
                timeZone = location?.timeZone ?: TimeZone.getDefault()
            )
            return 0 < sunRiseProgress && sunRiseProgress < 1
        }

        private fun isEquals(a: String?, b: String?): Boolean {
            return if (a.isNullOrEmpty() && b.isNullOrEmpty()) {
                true
            } else if (!a.isNullOrEmpty() && !b.isNullOrEmpty()) {
                a == b
            } else {
                false
            }
        }

        fun excludeInvalidResidentLocation(context: Context, list: List<Location>): List<Location> {
            val currentLocation: Location = list.firstOrNull { it.isCurrentPosition } ?: return list

            return list.filter {
                !it.isResidentPosition || !it.isCloseTo(context, currentLocation)
            }
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<Location> {

            override fun createFromParcel(parcel: Parcel): Location {
                return Location(parcel)
            }

            override fun newArray(size: Int): Array<Location?> {
                return arrayOfNulls(size)
            }
        }

        fun distance(location1: Location, location2: Location): Double {
            return distance(
                location1.latitude.toDouble(), location1.longitude.toDouble(),
                location2.latitude.toDouble(), location2.longitude.toDouble()
            )
        }

        /**
         * Adapted from https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
         *
         * Calculate distance between two points in latitude and longitude taking
         * into account height difference. Uses Haversine method as its base.
         *
         * @returns Distance in Meters
         */
        fun distance(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val R = 6371 // Radius of the earth

            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a = (sin(latDistance / 2) * sin(latDistance / 2)
                    + (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
                    * sin(lonDistance / 2) * sin(lonDistance / 2)))
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            var distance = R * c * 1000 // convert to meters

            distance = distance.pow(2.0)

            return sqrt(distance)
        }
    }
}