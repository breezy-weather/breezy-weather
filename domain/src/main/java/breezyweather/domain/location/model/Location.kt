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

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import breezyweather.domain.weather.model.Weather
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Location(
    val cityId: String? = null,

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timeZone: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        android.icu.util.TimeZone.getDefault().id
    } else {
        java.util.TimeZone.getDefault().id
    },

    val customName: String? = null,
    val country: String = "",
    val countryCode: String? = null,
    val admin1: String? = null,
    val admin1Code: String? = null,
    val admin2: String? = null,
    val admin2Code: String? = null,
    val admin3: String? = null,
    val admin3Code: String? = null,
    val admin4: String? = null,
    val admin4Code: String? = null,
    val city: String = "",
    val district: String? = null,

    val weather: Weather? = null,
    val forecastSource: String = "openmeteo",
    val currentSource: String? = null,
    val airQualitySource: String? = null,
    val pollenSource: String? = null,
    val minutelySource: String? = null,
    val alertSource: String? = null,
    val normalsSource: String? = null,
    val reverseGeocodingSource: String? = null,

    val isCurrentPosition: Boolean = false,

    val needsGeocodeRefresh: Boolean = false,

    /**
     * "accu": {"cityId": "230"}
     * "nws": {"gridId": "8", "gridX": "20", "gridY": "30"}
     * etc
     */
    val parameters: Map<String, Map<String, String>> = emptyMap(),
) : Parcelable {

    val javaTimeZone: java.util.TimeZone = java.util.TimeZone.getTimeZone(timeZone)

    val formattedId: String
        get() = if (isCurrentPosition) {
            CURRENT_POSITION_ID
        } else {
            String.format(Locale.US, "%f", latitude) +
                "&" +
                String.format(Locale.US, "%f", longitude) +
                "&" +
                forecastSource
        }

    val isUsable: Boolean
        // Sorry people living exactly at 0,0
        get() = latitude != 0.0 || longitude != 0.0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cityId)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(timeZone)
        parcel.writeString(customName)
        parcel.writeString(country)
        parcel.writeString(countryCode)
        parcel.writeString(admin1)
        parcel.writeString(admin1Code)
        parcel.writeString(admin2)
        parcel.writeString(admin2Code)
        parcel.writeString(admin3)
        parcel.writeString(admin3Code)
        parcel.writeString(admin4)
        parcel.writeString(admin4Code)
        parcel.writeString(city)
        parcel.writeString(district)
        parcel.writeString(forecastSource)
        parcel.writeString(currentSource)
        parcel.writeString(airQualitySource)
        parcel.writeString(pollenSource)
        parcel.writeString(minutelySource)
        parcel.writeString(alertSource)
        parcel.writeString(normalsSource)
        parcel.writeString(reverseGeocodingSource)
        parcel.writeByte(if (isCurrentPosition) 1 else 0)
        parcel.writeByte(if (needsGeocodeRefresh) 1 else 0)
    }

    override fun describeContents() = 0

    constructor(parcel: Parcel) : this(
        cityId = parcel.readString(),
        latitude = parcel.readDouble(),
        longitude = parcel.readDouble(),
        timeZone = parcel.readString()!!,
        customName = parcel.readString(),
        country = parcel.readString()!!,
        countryCode = parcel.readString(),
        admin1 = parcel.readString(),
        admin1Code = parcel.readString(),
        admin2 = parcel.readString(),
        admin2Code = parcel.readString(),
        admin3 = parcel.readString(),
        admin3Code = parcel.readString(),
        admin4 = parcel.readString(),
        admin4Code = parcel.readString(),
        city = parcel.readString()!!,
        district = parcel.readString(),
        forecastSource = parcel.readString()!!,
        currentSource = parcel.readString(),
        airQualitySource = parcel.readString(),
        pollenSource = parcel.readString(),
        minutelySource = parcel.readString(),
        alertSource = parcel.readString(),
        normalsSource = parcel.readString(),
        reverseGeocodingSource = parcel.readString(),
        isCurrentPosition = parcel.readByte() != 0.toByte(),
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

        if (customName != other.customName) {
            return false
        }

        if (forecastSource != other.forecastSource) {
            return false
        }

        if (currentSource != other.currentSource) {
            return false
        }

        if (airQualitySource != other.airQualitySource) {
            return false
        }

        if (pollenSource != other.pollenSource) {
            return false
        }

        if (minutelySource != other.minutelySource) {
            return false
        }

        if (alertSource != other.alertSource) {
            return false
        }

        if (normalsSource != other.normalsSource) {
            return false
        }

        if (needsGeocodeRefresh != other.needsGeocodeRefresh) {
            return false
        }

        if (parameters != other.parameters) {
            return false
        }

        val thisWeather = weather
        val otherWeather = other.weather
        if (thisWeather == null && otherWeather == null) {
            return true
        }
        return if (thisWeather != null && otherWeather != null) {
            thisWeather.base.refreshTime?.time == otherWeather.base.refreshTime?.time
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return formattedId.hashCode()
    }

    override fun toString(): String {
        val builder = StringBuilder("$country $admin1 $admin2 $admin3 $admin4")
        if (admin4 != city && city.isNotEmpty()) {
            builder.append(" ").append(city)
        }
        if (city != district && !district.isNullOrEmpty()) {
            builder.append(" ").append(district)
        }
        return builder.toString()
    }

    fun administrationLevels(): String {
        val builder = StringBuilder()
        if (country.isNotEmpty()) {
            builder.append(country)
        }
        if (!admin1.isNullOrEmpty()) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(admin1)
        }
        if (!admin2.isNullOrEmpty()) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(admin2)
        }
        if (!admin3.isNullOrEmpty() && (!admin4.isNullOrEmpty() || admin3 != city)) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(admin3)
        }
        if (!admin4.isNullOrEmpty() && admin4 != city) {
            if (builder.toString().isNotEmpty()) {
                builder.append(", ")
            }
            builder.append(admin4)
        }
        return builder.toString()
    }

    val cityAndDistrict: String
        get() {
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
            return builder.toString()
        }

    fun isCloseTo(location: Location): Boolean {
        if (cityId == location.cityId) {
            return true
        }
        if (isEquals(admin1, location.admin1) &&
            isEquals(admin2, location.admin2) &&
            isEquals(admin3, location.admin3) &&
            isEquals(admin4, location.admin4) &&
            isEquals(city, location.city)
        ) {
            return true
        }
        return if (isEquals(admin1, location.admin1) &&
            isEquals(admin2, location.admin2) &&
            isEquals(admin3, location.admin3) &&
            isEquals(admin4, location.admin4) &&
            cityAndDistrict == location.cityAndDistrict
        ) {
            true
        } else {
            distance(this, location) < (20 * 1000)
        }
    }

    companion object {

        const val CURRENT_POSITION_ID = "CURRENT_POSITION"

        fun isEquals(a: String?, b: String?): Boolean {
            return if (a.isNullOrEmpty() && b.isNullOrEmpty()) {
                true
            } else if (!a.isNullOrEmpty() && !b.isNullOrEmpty()) {
                a == b
            } else {
                false
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
                location1.latitude,
                location1.longitude,
                location2.latitude,
                location2.longitude
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
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double,
        ): Double {
            val r = 6371 // Radius of the earth

            val latDistance = Math.toRadians(lat2 - lat1)
            val lonDistance = Math.toRadians(lon2 - lon1)
            val a = sin(latDistance / 2) *
                sin(latDistance / 2) +
                (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(lonDistance / 2) * sin(lonDistance / 2))
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            var distance = r * c * 1000 // convert to meters

            distance = distance.pow(2.0)

            return sqrt(distance)
        }
    }
}
