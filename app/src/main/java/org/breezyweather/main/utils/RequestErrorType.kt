package org.breezyweather.main.utils

import android.app.Activity
import androidx.annotation.StringRes
import org.breezyweather.main.dialogs.ApiHelpDialog
import org.breezyweather.main.dialogs.LocationHelpDialog
import org.breezyweather.R

enum class RequestErrorType(
    @StringRes val shortMessage: Int,
    val showDialogAction: ((activity: Activity) -> Unit)? = null,
    @StringRes val actionButtonMessage: Int = R.string.action_help
) {
    // Common
    NETWORK_UNAVAILABLE(
        shortMessage = R.string.message_network_unavailable
    ),
    API_KEY_REQUIRED_MISSING(
        shortMessage = R.string.weather_api_key_required_missing_title,
        showDialogAction = {
            ApiHelpDialog.show(
                it,
                R.string.weather_api_key_required_missing_title,
                R.string.weather_api_key_required_missing_content
            )
        }
    ),
    API_LIMIT_REACHED(
        shortMessage = R.string.weather_api_limit_reached_title,
        showDialogAction = {
            ApiHelpDialog.show(
                it,
                R.string.weather_api_limit_reached_title,
                R.string.weather_api_limit_reached_content
            )
        }
    ),
    API_UNAUTHORIZED(
        shortMessage = R.string.weather_api_unauthorized_title,
        showDialogAction = {
            ApiHelpDialog.show(
                it,
                R.string.weather_api_unauthorized_title,
                R.string.weather_api_unauthorized_content
            )
        }
    ),

    // Location-specific
    LOCATION_FAILED(
        shortMessage = R.string.location_message_failed_to_locate,
        showDialogAction = { LocationHelpDialog.show(it) }
    ),
    ACCESS_LOCATION_PERMISSION_MISSING(
        shortMessage = R.string.location_message_permission_missing,
        //showDialogAction = { } // TODO
    ),
    ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING(
        shortMessage = R.string.location_message_permission_background_missing,
        //showDialogAction = { } // TODO
    ),

    // Location search-specific
    LOCATION_SEARCH_FAILED(
        shortMessage = R.string.location_message_failed_to_locate
    ),

    // Weather-specific
    WEATHER_REQ_FAILED(
        shortMessage = R.string.weather_message_data_refresh_failed
    );
}