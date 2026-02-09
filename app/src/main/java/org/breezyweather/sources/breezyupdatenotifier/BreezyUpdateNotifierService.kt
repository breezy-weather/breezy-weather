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
 * Known limitations: The updated location IDs only work if refreshed from the background (not from the main screen)
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
            /*
             * Contains the list of updated location IDs
             * If null, means the updated location is unknown.
             * It could be a deleted location, or a refresh on main screen
             */
            putStringArray(
                "UpdatedLocationIds",
                updatedLocationIds
            )
            /*
             * Contains the list of all known location IDs
             * If some locations were removed during the process, it won't be included in this list
             */
            putStringArray(
                "AllLocationIds",
                allLocations.map { it.formattedId }.toTypedArray()
            )
        }
    }
}
