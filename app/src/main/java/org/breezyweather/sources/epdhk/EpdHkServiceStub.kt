/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.epdhk

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import com.google.maps.android.model.LatLng
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.NonFreeNetSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE

/**
 * The actual implementation is in the src_freenet and src_nonfreenet folders
 */
abstract class EpdHkServiceStub(context: Context) :
    HttpSource(),
    WeatherSource,
    NonFreeNetSource {

    override val id = "epdhk"
    override val name by lazy {
        with(context.currentLocale.code) {
            when {
                equals("zh-tw") || equals("zh-hk") || equals("zh-mo") -> "環境保護署"
                startsWith("zh") -> "环境保护署"
                else -> "EPD"
            }
        } +
            " (${context.currentLocale.getCountryName("HK")})"
    }
    override val continent = SourceContinent.ASIA

    protected val weatherAttribution by lazy {
        with(context.currentLocale.code) {
            when {
                equals("zh-tw") || equals("zh-hk") || equals("zh-mo") -> "環境保護署"
                startsWith("zh") -> "环境保护署"
                else -> "Environmental Protection Department"
            }
        }
    }
    override val supportedFeatures = mapOf(
        SourceFeature.AIR_QUALITY to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("HK", ignoreCase = true)
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

    companion object {
        // Source: https://data.gov.hk/en-data/dataset/hk-epd-aqmnteam-air-quality-monitoring-network-of-hong-kong
        val EPD_HK_STATIONS = mapOf(
            "Central/Western" to LatLng(22.28489089, 114.14442071),
            "Southern" to LatLng(22.24746092, 114.1601401),
            "Eastern" to LatLng(22.28288555, 114.21937156),
            "Kwun Tong" to LatLng(22.3096251, 114.23117417),
            "Sham Shui Po" to LatLng(22.3302259, 114.15910913),
            "Kwai Chung" to LatLng(22.35710398, 114.12960136),
            "Tsuen Wan" to LatLng(22.37174191, 114.11453491),
            "Tseung Kwan O" to LatLng(22.31764241, 114.25956137),
            "Yuen Long" to LatLng(22.44515508, 114.02264888),
            "Tuen Mun" to LatLng(22.39114326, 113.97672832),
            "Tung Chung" to LatLng(22.28888887, 113.94365902),
            "Tai Po" to LatLng(22.45095988, 114.16457022),
            "Sha Tin" to LatLng(22.37628072, 114.18453161),
            "North" to LatLng(22.49669723, 114.12824408),
            "Tap Mun" to LatLng(22.47131669, 114.3607185),
            "Causeway Bay" to LatLng(22.28013296, 114.18509009),
            "Central" to LatLng(22.2818145, 114.15812743),
            "Mong Kok" to LatLng(22.32261115, 114.16827176)
        )
    }
}
