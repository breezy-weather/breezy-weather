package org.breezyweather.common.basic.models

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import org.breezyweather.R
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.sources.SourceManager
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Location(
    val cityId: String = NULL_ID,

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
    val weatherSource: String = SourceManager.DEFAULT_WEATHER_SOURCE,

    val isCurrentPosition: Boolean = false,
    val isResidentPosition: Boolean = false,
    val isChina: Boolean = false, // TODO: Deprecate me
) : Parcelable {

    val formattedId: String
        get() = if (isCurrentPosition) {
            CURRENT_POSITION_ID
        } else {
            cityId + "&" + weatherSource
        }

    val isDaylight: Boolean
        get() = isDayLight(this)

    val isUsable: Boolean
        get() = cityId != NULL_ID

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
        parcel.writeByte(if (isCurrentPosition) 1 else 0)
        parcel.writeByte(if (isResidentPosition) 1 else 0)
        parcel.writeByte(if (isChina) 1 else 0)
    }

    override fun describeContents() = 0

    constructor(parcel: Parcel) : this(
        cityId = parcel.readString()!!,
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
        isCurrentPosition = parcel.readByte() != 0.toByte(),
        isResidentPosition = parcel.readByte() != 0.toByte(),
        isChina = parcel.readByte() != 0.toByte()
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

    fun getCityName(context: Context): String {
        val text = if (!district.isNullOrEmpty() && district != "市辖区" && district != "无") {
            district
        } else if (city.isNotEmpty() && city != "市辖区") {
            city
        } else if (!province.isNullOrEmpty()) {
            province
        } else if (isCurrentPosition) {
            context.getString(R.string.location_current)
        } else {
            ""
        }

        if (!text.endsWith(")")) {
            return text
        }
        return text.substringBeforeLast('(')
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

    fun place(): String {
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

    fun hasGeocodeInformation(): Boolean {
        return (country.isNotEmpty() || !province.isNullOrEmpty()
                || city.isNotEmpty() || !district.isNullOrEmpty())
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
            && getCityName(c) == location.getCityName(c)
        ) {
            true
        } else {
            distance(this, location) < (20 * 1000)
        }
    }

    companion object {

        private const val NULL_ID = "NULL_ID"

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
            var currentLocation: Location? = null
            for (l in list) {
                if (l.isCurrentPosition) {
                    currentLocation = l
                    break
                }
            }
            val result = ArrayList<Location>(list.size)
            if (currentLocation == null) {
                result.addAll(list)
            } else {
                for (l in list) {
                    if (!l.isResidentPosition || !l.isCloseTo(context, currentLocation)) {
                        result.add(l)
                    }
                }
            }
            return result
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