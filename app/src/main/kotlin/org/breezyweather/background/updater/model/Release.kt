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

package org.breezyweather.background.updater.model

import android.os.Build
import org.breezyweather.BuildConfig

/*
 * Taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/c83037eeab3b180c7b82355331131df6950f5d45/domain/src/main/java/tachiyomi/domain/release/model/Release.kt
 */

/**
 * Contains information about the latest release.
 */
data class Release(
    val version: String,
    val info: String,
    val releaseLink: String,
    private val assets: List<String>,
) {

    /**
     * Get download link of latest release from the assets.
     * @return download link of latest release.
     */
    fun getDownloadLink(): String {
        val apkVariant = when (Build.SUPPORTED_ABIS[0]) {
            "arm64-v8a" -> "-arm64-v8a"
            "armeabi-v7a" -> "-armeabi-v7a"
            "x86" -> "-x86"
            "x86_64" -> "-x86_64"
            else -> ""
        }

        return assets.find {
            it.startsWith("${BuildConfig.GITHUB_RELEASE_PREFIX}$apkVariant-") && !it.contains("freenet")
        } ?: assets[0] // FIXME
    }

    /**
     * Assets class containing download url.
     */
    data class Assets(val downloadLink: String)
}
