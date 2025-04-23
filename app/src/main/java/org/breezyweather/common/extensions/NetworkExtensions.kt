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

package org.breezyweather.common.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService

/**
 * Taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/c5e8c9f01fa6b54425675ee3ebdc6f735aee7ba9/app/src/main/java/eu/kanade/tachiyomi/util/system/NetworkExtensions.kt
 */
val Context.connectivityManager: ConnectivityManager
    get() = getSystemService()!!

fun Context.isOnline(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        val maxTransport = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> NetworkCapabilities.TRANSPORT_LOWPAN
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> NetworkCapabilities.TRANSPORT_WIFI_AWARE
            else -> NetworkCapabilities.TRANSPORT_VPN
        }
        return if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            // If VPN is enabled, but there is no other transport enabled, we are actually offline
            (NetworkCapabilities.TRANSPORT_CELLULAR..maxTransport).forEach {
                Log.d(
                    "NetworkExtensions",
                    "hasTransport ${TRANSPORT_NAMES[it]}: ${if (networkCapabilities.hasTransport(it)) "YES" else "NO"}"
                )
            }
            (NetworkCapabilities.TRANSPORT_CELLULAR..maxTransport).count(networkCapabilities::hasTransport) > 1
        } else {
            (NetworkCapabilities.TRANSPORT_CELLULAR..maxTransport).any(networkCapabilities::hasTransport)
        }
    } else {
        @Suppress("DEPRECATION")
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}

val TRANSPORT_NAMES = arrayOf(
    "CELLULAR",
    "WIFI",
    "BLUETOOTH",
    "ETHERNET",
    "VPN",
    "WIFI_AWARE",
    "LOWPAN",
    "TEST",
    "USB",
    "THREAD",
    "SATELLITE"
)
