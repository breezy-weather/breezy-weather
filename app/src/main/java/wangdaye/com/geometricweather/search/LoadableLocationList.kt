package wangdaye.com.geometricweather.search

import wangdaye.com.geometricweather.common.basic.models.Location

class LoadableLocationList(
    val dataList: List<Location>,
    val status: Status
) {
    enum class Status {
        LOADING, ERROR, SUCCESS
    }
}