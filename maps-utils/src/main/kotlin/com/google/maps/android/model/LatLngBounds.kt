package com.google.maps.android.model

data class LatLngBounds(
    val southwest: LatLng,
    val northeast: LatLng,
) {

    fun contains(point: LatLng): Boolean {
        if (southwest.latitude <= point.latitude) {
            if (point.latitude <= northeast.latitude) {
                return if (southwest.longitude <= northeast.longitude) {
                    point.longitude in southwest.longitude..northeast.longitude
                } else if (southwest.longitude <= point.longitude || point.longitude <= northeast.longitude) {
                    true
                } else {
                    false
                }
            }
            return false
        }
        return false
    }

    companion object {
        fun parse(
            west: Double,
            south: Double,
            east: Double,
            north: Double,
        ): LatLngBounds {
            return LatLngBounds(
                southwest = LatLng(south, west),
                northeast = LatLng(north, east)
            )
        }
    }
}
