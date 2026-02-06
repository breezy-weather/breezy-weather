package org.breezyweather.sources.nlsc

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.NonFreeNetSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE

/**
 * The actual implementation is in the src_freenet and src_nonfreenet folders
 */
abstract class NlscServiceStub(context: Context) :
    HttpSource(),
    ReverseGeocodingSource,
    NonFreeNetSource {

    override val id = "nlsc"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "內政部國土測繪中心"
                else -> "NLSC"
            }
        } +
            " (${context.currentLocale.getCountryName("TW")})"
    }
    override val continent = SourceContinent.ASIA

    protected val reverseGeocodingAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                startsWith("zh") -> "內政部國土測繪中心"
                else -> "National Land Survey and Mapping Center"
            }
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.REVERSE_GEOCODING to reverseGeocodingAttribution
    )

    override fun isFeatureSupportedForLocation(location: Location, feature: SourceFeature): Boolean {
        val latLng = LatLng(location.latitude, location.longitude)
        return location.countryCode.equals("TW", ignoreCase = true) ||
            TAIWAN_BBOX.contains(latLng) ||
            PENGHU_BBOX.contains(latLng) ||
            KINMEN_BBOX.contains(latLng) ||
            WUQIU_BBOX.contains(latLng) ||
            MATSU_BBOX.contains(latLng)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    // Only supports its own country
    override val knownAmbiguousCountryCodes: Array<String>? = null

    companion object {
        val TAIWAN_BBOX = LatLngBounds.parse(119.99690416, 21.756143532, 122.10915909, 25.633378776)
        val PENGHU_BBOX = LatLngBounds.parse(119.314301816, 23.186561404, 119.726986388, 23.810692086)
        val KINMEN_BBOX = LatLngBounds.parse(118.137979837, 24.160255444, 118.505977425, 24.534228163)
        val WUQIU_BBOX = LatLngBounds.parse(119.443195363, 24.97760013, 119.479213453, 24.999614154)
        val MATSU_BBOX = LatLngBounds.parse(119.908905081, 25.940995457, 120.511750672, 26.385275262)
    }
}
