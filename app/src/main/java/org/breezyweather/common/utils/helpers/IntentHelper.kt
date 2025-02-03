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

package org.breezyweather.common.utils.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import breezyweather.domain.location.model.Location
import org.breezyweather.ui.about.AboutActivity
import org.breezyweather.ui.alert.AlertActivity
import org.breezyweather.ui.daily.DailyActivity
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.pollen.PollenActivity
import org.breezyweather.ui.search.SearchActivity
import org.breezyweather.ui.settings.activities.CardDisplayManageActivity
import org.breezyweather.ui.settings.activities.DailyTrendDisplayManageActivity
import org.breezyweather.ui.settings.activities.DependenciesActivity
import org.breezyweather.ui.settings.activities.DetailDisplayManageActivity
import org.breezyweather.ui.settings.activities.HourlyTrendDisplayManageActivity
import org.breezyweather.ui.settings.activities.PreviewIconActivity
import org.breezyweather.ui.settings.activities.PrivacyPolicyActivity
import org.breezyweather.ui.settings.activities.SettingsActivity
import org.breezyweather.ui.settings.compose.SettingsScreenRouter

/**
 * Intent helper.
 */
object IntentHelper {
    fun startMainActivityForManagement(activity: Activity) {
        activity.startActivity(
            Intent(MainActivity.ACTION_MANAGEMENT).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        )
    }

    fun buildMainActivityIntent(location: Location?): Intent {
        var formattedId: String? = null
        if (location != null) {
            formattedId = location.formattedId
        }
        return Intent(MainActivity.ACTION_MAIN).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID, formattedId)
        }
    }

    fun buildMainActivityShowAlertsIntent(location: Location?, alertId: String? = null): Intent {
        var formattedId: String? = null
        if (location != null) {
            formattedId = location.formattedId
        }
        return Intent(MainActivity.ACTION_SHOW_ALERTS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID, formattedId)
            if (alertId != null) {
                putExtra(MainActivity.KEY_MAIN_ACTIVITY_ALERT_ID, alertId)
            }
        }
    }

    fun buildMainActivityShowDailyForecastIntent(
        location: Location?,
        index: Int,
    ): Intent {
        var formattedId: String? = null
        if (location != null) {
            formattedId = location.formattedId
        }
        return Intent(MainActivity.ACTION_SHOW_DAILY_FORECAST).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.KEY_MAIN_ACTIVITY_LOCATION_FORMATTED_ID, formattedId)
            putExtra(MainActivity.KEY_DAILY_INDEX, index)
        }
    }

    fun startDailyWeatherActivity(
        activity: Activity,
        formattedId: String?,
        index: Int,
    ) {
        activity.startActivity(
            Intent(activity, DailyActivity::class.java).apply {
                putExtra(DailyActivity.KEY_FORMATTED_LOCATION_ID, formattedId)
                putExtra(DailyActivity.KEY_CURRENT_DAILY_INDEX, index)
            }
        )
    }

    fun startAlertActivity(activity: Activity, formattedId: String?, alertId: String? = null) {
        activity.startActivity(
            Intent(activity, AlertActivity::class.java).apply {
                putExtra(AlertActivity.KEY_FORMATTED_ID, formattedId)
                if (alertId != null) {
                    putExtra(AlertActivity.KEY_ALERT_ID, alertId)
                }
            }
        )
    }

    fun startPollenActivity(activity: Activity, location: Location) {
        activity.startActivity(
            Intent(activity, PollenActivity::class.java).apply {
                putExtra(
                    PollenActivity.KEY_POLLEN_ACTIVITY_LOCATION_FORMATTED_ID,
                    location.formattedId
                )
            }
        )
    }

    fun buildSearchActivityIntent(activity: Activity): Intent {
        return Intent(activity, SearchActivity::class.java)
    }

    fun startSettingsActivity(activity: Activity) {
        activity.startActivity(Intent(activity, SettingsActivity::class.java))
    }

    fun startCardDisplayManageActivity(activity: Activity) {
        activity.startActivity(Intent(activity, CardDisplayManageActivity::class.java))
    }

    fun startDailyTrendDisplayManageActivity(activity: Activity) {
        activity.startActivity(Intent(activity, DailyTrendDisplayManageActivity::class.java))
    }

    fun startHourlyTrendDisplayManageActivityForResult(activity: Activity) {
        activity.startActivity(Intent(activity, HourlyTrendDisplayManageActivity::class.java))
    }

    fun startDetailDisplayManageActivity(activity: Activity) {
        activity.startActivity(Intent(activity, DetailDisplayManageActivity::class.java))
    }

    fun startMainScreenSettingsActivity(activity: Activity) {
        activity.startActivity(
            Intent(activity, SettingsActivity::class.java).apply {
                putExtra(
                    SettingsActivity.KEY_SETTINGS_ACTIVITY_START_DESTINATION,
                    SettingsScreenRouter.MainScreen.route
                )
            }
        )
    }

    fun startLocationProviderSettingsActivity(activity: Activity) {
        activity.startActivity(
            Intent(activity, SettingsActivity::class.java).apply {
                putExtra(
                    SettingsActivity.KEY_SETTINGS_ACTIVITY_START_DESTINATION,
                    SettingsScreenRouter.Location.route
                )
            }
        )
    }

    fun startWeatherProviderSettingsActivity(activity: Activity) {
        activity.startActivity(
            Intent(activity, SettingsActivity::class.java).apply {
                putExtra(
                    SettingsActivity.KEY_SETTINGS_ACTIVITY_START_DESTINATION,
                    SettingsScreenRouter.WeatherProviders.route
                )
            }
        )
    }

    fun startPreviewIconActivity(activity: Activity, packageName: String?) {
        activity.startActivity(
            Intent(activity, PreviewIconActivity::class.java).apply {
                putExtra(
                    PreviewIconActivity.KEY_ICON_PREVIEW_ACTIVITY_PACKAGE_NAME,
                    packageName
                )
            }
        )
    }

    fun startAboutActivity(context: Context) {
        context.startActivity(Intent(context, AboutActivity::class.java))
    }

    fun startDependenciesActivity(activity: Activity) {
        activity.startActivity(Intent(activity, DependenciesActivity::class.java))
    }

    fun startPrivacyPolicyActivity(activity: Activity) {
        activity.startActivity(Intent(activity, PrivacyPolicyActivity::class.java))
    }

    fun startApplicationDetailsActivity(context: Context, pkgName: String? = context.packageName) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", pkgName, null)
            }
        )
    }

    fun startLocationSettingsActivity(context: Context) {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startNotificationSettingsActivity(context: Context, pkgName: String? = context.packageName) {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, pkgName)
            }
        )
    }

    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        return context.packageManager
            .queryIntentActivities(intent, PackageManager.GET_ACTIVITIES)
            .size > 0
    }
}
