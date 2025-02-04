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

package org.breezyweather.ui.main.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.domain.settings.ConfigStore
import javax.inject.Inject

class StatementManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val config: ConfigStore = ConfigStore(context, SP_STATEMENT_RECORD)

    var isLocationPermissionDialogAlreadyShown: Boolean
        get() = config.getBoolean(KEY_LOCATION_PERMISSION_DECLARED, false)
        private set(value) {
            config.edit().putBoolean(KEY_LOCATION_PERMISSION_DECLARED, value).apply()
        }

    var isBackgroundLocationPermissionDialogAlreadyShown: Boolean
        get() = config.getBoolean(KEY_BACKGROUND_LOCATION_DECLARED, false)
        private set(value) {
            config.edit().putBoolean(KEY_BACKGROUND_LOCATION_DECLARED, value).apply()
        }

    var isPostNotificationDialogAlreadyShown: Boolean
        get() = config.getBoolean(KEY_POST_NOTIFICATION_REQUIRED, false)
        private set(value) {
            config.edit().putBoolean(KEY_POST_NOTIFICATION_REQUIRED, value).apply()
        }

    var isAppUpdateCheckDialogAlreadyShown: Boolean
        get() = config.getBoolean(KEY_APP_UPDATE_CHECK_ASKED, false)
        private set(value) {
            config.edit().putBoolean(KEY_APP_UPDATE_CHECK_ASKED, value).apply()
        }

    private fun getPermissionDeniedKey(permission: String): String =
        permission.replace(".", "_").lowercase() + "_denied"

    fun setLocationPermissionDialogAlreadyShown() {
        isLocationPermissionDialogAlreadyShown = true
    }

    fun setBackgroundLocationPermissionDialogAlreadyShown() {
        isBackgroundLocationPermissionDialogAlreadyShown = true
    }

    fun setPostNotificationDialogAlreadyShown() {
        isPostNotificationDialogAlreadyShown = true
    }

    fun setAppUpdateCheckDialogAlreadyShown() {
        isAppUpdateCheckDialogAlreadyShown = true
    }

    fun isPermissionDenied(permission: String): Boolean =
        config.getBoolean(getPermissionDeniedKey(permission), false)

    fun setPermissionDenied(permission: String) {
        config.edit().putBoolean(getPermissionDeniedKey(permission), true).apply()
    }

    companion object {
        private const val SP_STATEMENT_RECORD = "statement_record"
        private const val KEY_LOCATION_PERMISSION_DECLARED = "location_permission_declared"
        private const val KEY_BACKGROUND_LOCATION_DECLARED = "background_location_declared"
        private const val KEY_POST_NOTIFICATION_REQUIRED = "post_notification_required"
        private const val KEY_APP_UPDATE_CHECK_ASKED = "app_update_check_asked"
    }
}
