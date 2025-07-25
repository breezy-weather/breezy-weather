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

package org.breezyweather.background.updater

import android.content.Context
import org.breezyweather.BuildConfig
import org.breezyweather.background.updater.interactor.GetApplicationRelease
import org.breezyweather.common.extensions.withIOContext
import javax.inject.Inject

/**
 * Taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/aa498360db90350f2642e6320dc55e7d474df1fd/app/src/main/java/eu/kanade/tachiyomi/data/updater/AppUpdateChecker.kt
 */
class AppUpdateChecker @Inject constructor(
    private val getApplicationRelease: GetApplicationRelease,
) {

    suspend fun checkForUpdate(
        context: Context,
        forceCheck: Boolean = false,
    ): GetApplicationRelease.Result {
        // Disable app update checks for older Android versions that we're going to drop support for
        // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        //     return GetApplicationRelease.Result.OsTooOld
        // }

        return withIOContext {
            val result = getApplicationRelease.await(
                GetApplicationRelease.Arguments(
                    BuildConfig.VERSION_NAME,
                    GITHUB_ORG,
                    GITHUB_REPO,
                    forceCheck
                )
            )

            when (result) {
                is GetApplicationRelease.Result.NewUpdate -> AppUpdateNotifier(context).promptUpdate(result.release)
                else -> {}
            }

            result
        }
    }
}

val GITHUB_ORG = "breezy-weather"
val GITHUB_REPO = "breezy-weather"

val RELEASE_URL = "https://github.com/${GITHUB_REPO}/releases/tag/v${BuildConfig.VERSION_NAME}"
