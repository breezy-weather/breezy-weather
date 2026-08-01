/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.breezyupdatenotifier

import android.content.Context
import android.os.Bundle
import breezyweather.domain.location.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.source.BroadcastSource
import javax.inject.Inject

/**
 * Broadcasts the following:
 * - UpdatedLocationIds: An array of location IDs which got a weather update. Can be empty.
 * - AllLocationIds: An array of all location IDs. Helpful to detect added, swapped or deleted locations.
 */
class BreezyUpdateNotifierService @Inject constructor(
    @ApplicationContext context: Context,
) : BroadcastSource {

    override val id = "breezyweatherupdatenotifier"
    override val name = context.getString(
        R.string.broadcast_source_breezy_weather_update_notifier,
        context.getString(R.string.brand_name)
    )

    override val intentAction = "${BuildConfig.APPLICATION_ID}.ACTION_UPDATE_NOTIFIER"

    override fun getExtras(
        context: Context,
        allLocations: List<Location>,
        updatedLocationIds: Array<String>?,
    ): Bundle {
        return Bundle().apply {
            putStringArray(
                "UpdatedLocationIds",
                updatedLocationIds ?: emptyArray<String>()
            )
            putStringArray(
                "AllLocationIds",
                allLocations.map { it.formattedId }.toTypedArray()
            )
        }
    }
}
