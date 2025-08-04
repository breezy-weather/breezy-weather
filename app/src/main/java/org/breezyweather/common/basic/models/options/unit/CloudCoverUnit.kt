/*
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

package org.breezyweather.common.basic.models.options.unit

import android.content.Context
import org.breezyweather.R
import kotlin.math.roundToInt

// We don't need any unit, it's just a percent, but we need some helpers

/**
 * @param context
 * @param cloudCover in % (0-100)
 */
fun getCloudCoverDescription(context: Context, cloudCover: Int?): String? {
    if (cloudCover == null) return null
    return when (cloudCover) {
        in 0..<CLOUD_COVER_SKC.roundToInt() -> context.getString(R.string.common_weather_text_clear_sky)
        in CLOUD_COVER_SKC.roundToInt()..<CLOUD_COVER_FEW.roundToInt(),
        -> context.getString(R.string.common_weather_text_mostly_clear)
        in CLOUD_COVER_FEW.roundToInt()..<CLOUD_COVER_SCT.roundToInt(),
        -> context.getString(R.string.common_weather_text_partly_cloudy)
        in CLOUD_COVER_SCT.roundToInt()..<CLOUD_COVER_BKN.roundToInt(),
        -> context.getString(R.string.common_weather_text_mostly_cloudy)
        in CLOUD_COVER_BKN.roundToInt()..CLOUD_COVER_OVC.roundToInt(),
        -> context.getString(R.string.common_weather_text_cloudy)
        else -> null
    }
}

/**
 * Source: WMO Cloud distribution for aviation
 */
const val CLOUD_COVER_SKC = 12.5 // 1 okta
const val CLOUD_COVER_FEW = 37.5 // 3 okta
const val CLOUD_COVER_SCT = 67.5 // 5 okta
const val CLOUD_COVER_BKN = 87.5 // 7 okta
const val CLOUD_COVER_OVC = 100.0 // 8 okta
