/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.background.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import org.breezyweather.common.extensions.workManager

/**
 * Receiver to force app to autostart on boot
 * Does nothing, it’s just that some OEM do not respect Android policy to keep scheduled workers
 * regardless of if the app is started or not
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action.isNullOrEmpty()) return
        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                /**
                 * We don’t use the return value, but querying the work manager might help bringing back
                 * scheduled workers after the app has been killed/shutdown on some devices
                 */
                context.workManager.getWorkInfosLiveData(WorkQuery.fromStates(WorkInfo.State.ENQUEUED))
            }
        }
    }
}