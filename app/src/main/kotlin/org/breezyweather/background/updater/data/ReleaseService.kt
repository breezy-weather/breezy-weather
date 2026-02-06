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

package org.breezyweather.background.updater.data

import org.breezyweather.background.updater.model.Release
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * Taken from Mihon
 * Apache License, Version 2.0
 *
 * https://github.com/mihonapp/mihon/blob/02864ebd60ac9eb974a1b54b06368d20b0ca3ce5/data/src/main/java/tachiyomi/data/release/ReleaseServiceImpl.kt
 */
class ReleaseService @Inject constructor(
    @Named("JsonClient") val client: Retrofit.Builder,
) {

    suspend fun latest(org: String, repository: String): Release {
        return client
            .baseUrl("https://api.github.com/")
            .build()
            .create(GithubApi::class.java)
            .getLatest(org, repository)
            .let(releaseMapper)
    }
}
