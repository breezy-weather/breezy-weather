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

package org.breezyweather.common.source

import android.content.Context
import android.os.Bundle
import breezyweather.domain.location.model.Location

/**
 * Broadcast services
 */
interface BroadcastSource : Source {

    // Make sure to also add it to the Manifest!
    val intentAction: String

    /**
     * Return null if anything happens and you no longer want to send any data
     */
    fun getExtras(
        context: Context,
        locations: List<Location>,
    ): Bundle?
}
