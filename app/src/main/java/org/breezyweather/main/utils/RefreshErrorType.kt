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

import android.app.Activity
import androidx.annotation.StringRes
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.main.dialogs.ApiHelpDialog
import org.breezyweather.main.dialogs.LocationHelpDialog
import org.breezyweather.main.dialogs.SourceNoLongerAvailableHelpDialog

enum class RefreshErrorType(
    @StringRes val shortMessage: Int,
    val showDialogAction: ((activity: Activity) -> Unit)? = null,
    @StringRes val actionButtonMessage: Int = R.string.action_help,
) {
    // Common
    NETWORK_UNAVAILABLE(
        shortMessage = R.string.message_network_unavailable
    ),
    SERVER_TIMEOUT(
        shortMessage = R.string.message_server_timeout
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
    SERVER_UNAVAILABLE(
        shortMessage = R.string.message_server_unavailable_title
        /*showDialogAction = { TODO
            ServerUnavailableErrorHelpDialog.show(
                it,
                R.string.message_server_unavailable_title,
                R.string.message_server_unavailable_content
            )
        }*/
    ),
    PARSING_ERROR(
        shortMessage = R.string.message_parsing_error_title
        /*showDialogAction = { TODO
            ParsingErrorHelpDialog.show(
                it,
                R.string.message_parsing_error_title,
                R.string.message_parsing_error_content
            )
        }*/
    ),
    SOURCE_NOT_INSTALLED(
        shortMessage = R.string.message_source_not_installed_error_title,
        showDialogAction = {
            SourceNoLongerAvailableHelpDialog.show(
                it,
                R.string.message_source_not_installed_error_title
            )
        }
    ),

    // Location-specific
    LOCATION_FAILED(
        shortMessage = R.string.location_message_failed_to_locate,
        showDialogAction = { LocationHelpDialog.show(it) }
    ),
    ACCESS_LOCATION_PERMISSION_MISSING(
        shortMessage = R.string.location_message_permission_missing
        // showDialogAction = { } // TODO
    ),
    ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING(
        shortMessage = R.string.location_message_permission_background_missing
        // showDialogAction = { } // TODO
    ),
    LOCATION_ACCESS_OFF(
        shortMessage = R.string.location_message_location_access_off,
        showDialogAction = { IntentHelper.startLocationSettingsActivity(it) }
    ),
    REVERSE_GEOCODING_FAILED(
        shortMessage = R.string.location_message_reverse_geocoding_failed
    ),

    // Location search-specific
    LOCATION_SEARCH_FAILED(
        shortMessage = R.string.location_message_search_failed
    ),

    // Weather-specific
    INVALID_LOCATION(
        shortMessage = R.string.weather_message_invalid_location
    ),
    UNSUPPORTED_FEATURE(
        shortMessage = R.string.weather_message_unsupported_feature
    ),
    INVALID_INCOMPLETE_DATA(
        shortMessage = R.string.message_invalid_incomplete_data
    ),
    DATA_REFRESH_FAILED(
        shortMessage = R.string.weather_message_data_refresh_failed
    ),
}
