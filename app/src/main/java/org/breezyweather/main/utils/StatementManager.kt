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

package org.breezyweather.main.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.settings.ConfigStore
import javax.inject.Inject

class StatementManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val config: ConfigStore = ConfigStore(context, SP_STATEMENT_RECORD)
    var isLocationPermissionDialogAlreadyShown: Boolean = config.getBoolean(
        KEY_LOCATION_PERMISSION_DECLARED,
        false
    )
        private set
    var isBackgroundLocationPermissionDialogAlreadyShown: Boolean = config.getBoolean(
        KEY_BACKGROUND_LOCATION_DECLARED,
        false
    )
        private set
    var isPostNotificationDialogAlreadyShown: Boolean = config.getBoolean(
        KEY_POST_NOTIFICATION_REQUIRED,
        false
    )
        private set
    var isAppUpdateCheckDialogAlreadyShown: Boolean = config.getBoolean(
        KEY_APP_UPDATE_CHECK_ASKED,
        false
    )
        private set

    fun setLocationPermissionDialogAlreadyShown() {
        isLocationPermissionDialogAlreadyShown = true
        config.edit()
            .putBoolean(KEY_LOCATION_PERMISSION_DECLARED, true)
            .apply()
    }

    fun setBackgroundLocationPermissionDialogAlreadyShown() {
        isBackgroundLocationPermissionDialogAlreadyShown = true
        config.edit()
            .putBoolean(KEY_BACKGROUND_LOCATION_DECLARED, true)
            .apply()
    }

    fun setPostNotificationDialogAlreadyShown() {
        isPostNotificationDialogAlreadyShown = true
        config.edit()
            .putBoolean(KEY_POST_NOTIFICATION_REQUIRED, true)
            .apply()
    }

    fun setAppUpdateCheckDialogAlreadyShown() {
        isAppUpdateCheckDialogAlreadyShown = true
        config.edit()
            .putBoolean(KEY_APP_UPDATE_CHECK_ASKED, true)
            .apply()
    }

    companion object {
        private const val SP_STATEMENT_RECORD = "statement_record"
        private const val KEY_LOCATION_PERMISSION_DECLARED = "location_permission_declared"
        private const val KEY_BACKGROUND_LOCATION_DECLARED = "background_location_declared"
        private const val KEY_POST_NOTIFICATION_REQUIRED = "post_notification_required"
        private const val KEY_APP_UPDATE_CHECK_ASKED = "app_update_check_asked"
    }
}
