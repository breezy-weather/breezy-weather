package wangdaye.com.geometricweather.main

import wangdaye.com.geometricweather.common.basic.models.Location

class Indicator(val total: Int, val index: Int) {

    override fun equals(other: Any?): Boolean {
        return if (other is Indicator) {
            other.index == index && other.total == total
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = total
        result = 31 * result + index
        return result
    }
}

class PermissionsRequest(
    val permissionList: List<String>,
    val target: Location?,
    val triggeredByUser: Boolean
) {

    private var consumed = false

    fun consume(): Boolean {
        if (consumed) {
            return false
        }

        consumed = true
        return true
    }
}

class SelectableLocationList(
    val locationList: List<Location>,
    val selectedId: String,
) {

    override fun equals(other: Any?): Boolean {
        if (other is SelectableLocationList) {
            return locationList == other.locationList
                    && selectedId == other.selectedId
        }
        return false
    }

    override fun hashCode(): Int {
        var result = locationList.hashCode()
        result = 31 * result + selectedId.hashCode()
        return result
    }
}

enum class MainMessage {
    LOCATION_FAILED,
    WEATHER_REQ_FAILED,
    API_LIMIT_REACHED,
}

class DayNightLocation(
    val location: Location,
    val daylight: Boolean = location.isDaylight
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is DayNightLocation) {
            return location == other.location
                    && daylight == other.daylight
        }

        return false
    }

    override fun hashCode(): Int {
        var result = location.hashCode()
        result = 31 * result + daylight.hashCode()
        return result
    }
}