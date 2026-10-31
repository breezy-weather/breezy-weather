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

package org.breezyweather.ui.main.compose

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.ui.main.MainActivityViewModel

/**
 * City-list swipe actions.
 * Swipe toward the end (LTR: right) deletes; swipe toward the start toggles resident / opens providers.
 */
object LocationListActions {

    fun onSwipeTowardEnd(
        context: Context,
        viewModel: MainActivityViewModel,
        position: Int,
    ) {
        val list = viewModel.validLocationList.value
        if (list.size <= 1) {
            SnackbarHelper.showSnackbar(
                context.getString(R.string.location_message_list_cannot_be_empty)
            )
            return
        }
        val deleted = viewModel.deleteLocation(position)
        SnackbarHelper.showSnackbar(
            content = context.getString(R.string.location_message_deleted),
            action = context.getString(R.string.action_undo)
        ) {
            viewModel.addLocation(deleted, position)
        }
    }
}
