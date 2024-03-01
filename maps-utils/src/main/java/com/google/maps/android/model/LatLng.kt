package com.google.maps.android.model

import kotlin.math.max
import kotlin.math.min

class LatLng(
    latitude: Double,
    longitude: Double
) {
    val latitude: Double
    val longitude: Double

    init {
        this.latitude = max(-90.0, min(90.0, latitude))
        this.longitude = max(-180.0, min(180.0, longitude))
    }

    override fun toString(): String {
        return "LatLng: [latitude=$latitude, longitude=$longitude]"
    }
}