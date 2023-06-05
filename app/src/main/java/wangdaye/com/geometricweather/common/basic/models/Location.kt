package wangdaye.com.geometricweather.common.basic.models

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.basic.models.weather.Astro
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import java.util.TimeZone
import kotlin.math.abs

class Location(
    val cityId: String,

    val latitude: Float,
    val longitude: Float,
    val timeZone: TimeZone,

    val country: String,
    val province: String? = null,
    val city: String,
    val district: String? = null,

    val weather: Weather? = null,
    val weatherSource: WeatherSource,

    val isCurrentPosition: Boolean = false,
    val isResidentPosition: Boolean = false,
    val isChina: Boolean,
) : Parcelable {

    val formattedId: String
        get() = if (isCurrentPosition) {
            CURRENT_POSITION_ID
        } else {
            cityId + "&" + weatherSource.name
        }

    val isDaylight: Boolean
        get() = isDayLight(this)

    val isUsable: Boolean
        get() = cityId != NULL_ID

    companion object {
        private const val NULL_ID = "NULL_ID"

        const val CURRENT_POSITION_ID = "CURRENT_POSITION"

        @JvmStatic
        @JvmOverloads
        fun isDayLight(location: Location? = null): Boolean {
            val sunRiseProgress = Astro.getRiseProgress(
                astro = location?.weather?.dailyForecast?.getOrNull(0)?.sun,
                timeZone = location?.timeZone ?: TimeZone.getDefault()
            )
            return 0 < sunRiseProgress && sunRiseProgress < 1
        }

        @JvmStatic
        fun buildLocal(): Location {
            return Location(
                cityId = NULL_ID,
                latitude = 0f,
                longitude = 0f,
                timeZone = TimeZone.getDefault(),
                country = "",
                province = "",
                city = "",
                district = "",
                weatherSource = WeatherSource.ACCU,
                isCurrentPosition = true,
                isResidentPosition = false,
                isChina = false
            )
        }

        @JvmStatic
        fun buildDefaultLocation(weatherSource: WeatherSource): Location {
            return Location(
                cityId = "101924",
                latitude = 39.904000f,
                longitude = 116.391000f,
                timeZone = TimeZone.getTimeZone("Asia/Shanghai"),
                country = "中国",
                province = "直辖市",
                city = "北京",
                district = "",
                weatherSource = weatherSource,
                isCurrentPosition = false,
                isResidentPosition = false,
                isChina = true,
            )
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

        @JvmStatic
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

        @JvmStatic
        fun copy(
            src: Location,
            weather: Weather?,
        ) = src.copy(
            weather = weather
        )

        @JvmStatic
        fun copy(
            src: Location,
            weatherSource: WeatherSource,
        ) = src.copy(
            weatherSource = weatherSource
        )

        @JvmStatic
        fun copy(
            src: Location,
            currentPosition: Boolean,
            residentPosition: Boolean,
        ) = src.copy(
            isCurrentPosition = currentPosition,
            isResidentPosition = residentPosition,
        )

        @JvmStatic
        fun copy(
            src: Location,
            latitude: Float,
            longitude: Float,
            timeZone: TimeZone,
        ) = src.copy(
            latitude = latitude,
            longitude = longitude,
            timeZone = timeZone,
        )

        @JvmField
        val CREATOR = object : Parcelable.Creator<Location> {

            override fun createFromParcel(parcel: Parcel): Location {
                return Location(parcel)
            }

            override fun newArray(size: Int): Array<Location?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cityId)
        parcel.writeFloat(latitude)
        parcel.writeFloat(longitude)
        parcel.writeSerializable(timeZone)
        parcel.writeString(country)
        parcel.writeString(province)
        parcel.writeString(city)
        parcel.writeString(district)
        parcel.writeInt(weatherSource.ordinal)
        parcel.writeByte(if (isCurrentPosition) 1 else 0)
        parcel.writeByte(if (isResidentPosition) 1 else 0)
        parcel.writeByte(if (isChina) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    constructor(parcel: Parcel) : this(
        cityId = parcel.readString()!!,
        latitude = parcel.readFloat(),
        longitude = parcel.readFloat(),
        timeZone = parcel.readSerializable()!! as TimeZone,
        country = parcel.readString()!!,
        province = parcel.readString(),
        city = parcel.readString()!!,
        district = parcel.readString(),
        weatherSource = WeatherSource.values()[parcel.readInt()],
        isCurrentPosition = parcel.readByte() != 0.toByte(),
        isResidentPosition = parcel.readByte() != 0.toByte(),
        isChina = parcel.readByte() != 0.toByte()
    )

    @JvmOverloads
    fun copy(
        cityId: String? = null,
        latitude: Float? = null,
        longitude: Float? = null,
        timeZone: TimeZone? = null,
        country: String? = null,
        province: String? = null,
        city: String? = null,
        district: String? = null,
        weather: Weather? = null,
        weatherSource: WeatherSource? = null,
        isCurrentPosition: Boolean? = null,
        isResidentPosition: Boolean? = null,
        isChina: Boolean? = null,
    ) = Location(
        cityId = cityId ?: this.cityId,
        latitude = latitude ?: this.latitude,
        longitude = longitude ?: this.longitude,
        timeZone = timeZone ?: this.timeZone,
        country = country ?: this.country,
        province = province ?: this.province,
        city = city ?: this.city,
        district = district ?: this.district,
        weather = weather ?: this.weather,
        weatherSource = weatherSource ?: this.weatherSource,
        isCurrentPosition = isCurrentPosition ?: this.isCurrentPosition,
        isResidentPosition = isResidentPosition ?: this.isResidentPosition,
        isChina = isChina ?: this.isChina
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
            context.getString(R.string.current_location)
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
            abs(latitude - location.latitude) < 0.8
                    && abs(longitude - location.longitude) < 0.8
        }
    }
}