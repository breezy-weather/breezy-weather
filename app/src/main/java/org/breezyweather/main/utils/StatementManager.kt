package org.breezyweather.main.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.settings.ConfigStore
import javax.inject.Inject

class StatementManager @Inject constructor(@ApplicationContext context: Context) {
    private val config: ConfigStore = ConfigStore(context, SP_STATEMENT_RECORD)
    var isLocationPermissionDeclared: Boolean = config.getBoolean(KEY_LOCATION_PERMISSION_DECLARED, false)
        private set
    var isBackgroundLocationDeclared: Boolean = config.getBoolean(KEY_BACKGROUND_LOCATION_DECLARED, false)
        private set
    var isPostNotificationRequired: Boolean = config.getBoolean(KEY_POST_NOTIFICATION_REQUIRED, false)
        private set

    fun setLocationPermissionDeclared() {
        isLocationPermissionDeclared = true
        config.edit()
            .putBoolean(KEY_LOCATION_PERMISSION_DECLARED, true)
            .apply()
    }

    fun setBackgroundLocationDeclared() {
        isBackgroundLocationDeclared = true
        config.edit()
            .putBoolean(KEY_BACKGROUND_LOCATION_DECLARED, true)
            .apply()
    }

    fun setPostNotificationRequired() {
        isPostNotificationRequired = true
        config.edit()
            .putBoolean(KEY_POST_NOTIFICATION_REQUIRED, true)
            .apply()
    }

    companion object {
        private const val SP_STATEMENT_RECORD = "statement_record"
        private const val KEY_LOCATION_PERMISSION_DECLARED = "location_permission_declared"
        private const val KEY_BACKGROUND_LOCATION_DECLARED = "background_location_declared"
        private const val KEY_POST_NOTIFICATION_REQUIRED = "post_notification_required"
    }
}
