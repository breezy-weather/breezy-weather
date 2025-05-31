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

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import org.breezyweather.R
import org.breezyweather.common.exceptions.ApiKeyMissingException
import org.breezyweather.common.exceptions.ApiLimitReachedException
import org.breezyweather.common.exceptions.ApiUnauthorizedException
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.exceptions.LocationAccessOffException
import org.breezyweather.common.exceptions.LocationException
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.exceptions.MissingPermissionLocationBackgroundException
import org.breezyweather.common.exceptions.MissingPermissionLocationException
import org.breezyweather.common.exceptions.NoNetworkException
import org.breezyweather.common.exceptions.OutdatedServerDataException
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.exceptions.ReverseGeocodingException
import org.breezyweather.common.exceptions.SourceNotInstalledException
import org.breezyweather.common.exceptions.UnsupportedFeatureException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.getStringByLocale
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.ui.main.dialogs.ApiHelpDialog
import org.breezyweather.ui.main.dialogs.LocationHelpDialog
import org.breezyweather.ui.main.dialogs.SourceNoLongerAvailableHelpDialog
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import javax.net.ssl.SSLHandshakeException

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
    SERVER_INSECURE(shortMessage = R.string.message_server_insecure_title),
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
        shortMessage = R.string.location_message_permission_missing,
        showDialogAction = { IntentHelper.startApplicationDetailsActivity(it) },
        actionButtonMessage = R.string.action_allow
    ),
    ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING(
        shortMessage = R.string.location_message_permission_background_missing,
        showDialogAction = { IntentHelper.startApplicationDetailsActivity(it) },
        actionButtonMessage = R.string.action_allow
    ),
    LOCATION_ACCESS_OFF(
        shortMessage = R.string.location_message_location_access_off,
        showDialogAction = { IntentHelper.startLocationSettingsActivity(it) },
        actionButtonMessage = R.string.action_enable
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
    OUTDATED_SERVER_DATA(
        shortMessage = R.string.message_outdated_server_data
    ),
    DATA_REFRESH_FAILED(
        shortMessage = R.string.weather_message_data_refresh_failed
    ),
    ;

    companion object {
        fun getTypeFromThrowable(
            context: Context,
            e: Throwable,
            defaultRefreshError: RefreshErrorType,
        ): RefreshErrorType {
            val refreshErrorType = when (e) {
                is NoNetworkException -> NETWORK_UNAVAILABLE
                // Can mean different things but most of the time, it’s a network issue:
                is UnknownHostException -> NETWORK_UNAVAILABLE
                is HttpException -> {
                    LogHelper.log(msg = "HttpException ${e.code()}")
                    when (e.code()) {
                        401, 403 -> API_UNAUTHORIZED
                        409, 429 -> API_LIMIT_REACHED
                        in 500..599 -> SERVER_UNAVAILABLE
                        else -> {
                            e.printStackTrace()
                            defaultRefreshError
                        }
                    }
                }
                is SSLHandshakeException -> {
                    e.printStackTrace()
                    SERVER_INSECURE
                }
                is SocketTimeoutException -> SERVER_TIMEOUT
                is ApiLimitReachedException -> API_LIMIT_REACHED
                is ApiKeyMissingException -> API_KEY_REQUIRED_MISSING
                is ApiUnauthorizedException -> API_UNAUTHORIZED
                is InvalidLocationException -> INVALID_LOCATION
                is LocationException -> LOCATION_FAILED
                is LocationAccessOffException -> LOCATION_ACCESS_OFF
                is MissingPermissionLocationException -> ACCESS_LOCATION_PERMISSION_MISSING
                is MissingPermissionLocationBackgroundException -> ACCESS_BACKGROUND_LOCATION_PERMISSION_MISSING
                is ReverseGeocodingException -> REVERSE_GEOCODING_FAILED
                is MissingFieldException, is SerializationException, is ParsingException, is ParseException -> {
                    e.printStackTrace()
                    PARSING_ERROR
                }
                is SourceNotInstalledException -> SOURCE_NOT_INSTALLED
                is LocationSearchException -> LOCATION_SEARCH_FAILED
                is InvalidOrIncompleteDataException -> INVALID_INCOMPLETE_DATA
                is OutdatedServerDataException -> OUTDATED_SERVER_DATA
                is UnsupportedFeatureException -> UNSUPPORTED_FEATURE
                is WeatherException -> DATA_REFRESH_FAILED
                else -> {
                    e.printStackTrace()
                    defaultRefreshError
                }
            }

            LogHelper.log(msg = "Refresh error: ${context.getStringByLocale(refreshErrorType.shortMessage)}")

            return refreshErrorType
        }
    }
}
