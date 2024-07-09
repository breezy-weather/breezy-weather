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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import breezyweather.domain.location.model.Location
import org.breezyweather.common.ui.activities.AlertActivity
import org.breezyweather.common.ui.activities.PollenActivity
import org.breezyweather.daily.DailyWeatherActivity
import org.breezyweather.main.MainActivity
import org.breezyweather.search.SearchActivity
import org.breezyweather.settings.activities.AboutActivity
import org.breezyweather.settings.activities.CardDisplayManageActivity
import org.breezyweather.settings.activities.DailyTrendDisplayManageActivity
import org.breezyweather.settings.activities.DependenciesActivity
import org.breezyweather.settings.activities.DetailDisplayManageActivity
import org.breezyweather.settings.activities.HourlyTrendDisplayManageActivity
import org.breezyweather.settings.activities.MainScreenSettingsActivity
import org.breezyweather.settings.activities.PreviewIconActivity
import org.breezyweather.settings.activities.PrivacyPolicyActivity
import org.breezyweather.settings.activities.SelectLocationProviderActivity
import org.breezyweather.settings.activities.SelectWeatherProviderActivity
import org.breezyweather.settings.activities.SettingsActivity

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
        index: Int
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
        formattedId: String?, index: Int
    ) {
        activity.startActivity(
            Intent(activity, DailyWeatherActivity::class.java).apply {
                putExtra(DailyWeatherActivity.KEY_FORMATTED_LOCATION_ID, formattedId)
                putExtra(DailyWeatherActivity.KEY_CURRENT_DAILY_INDEX, index)
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

    fun startSearchActivityForResult(activity: Activity, requestCode: Int) {
        ActivityCompat.startActivityForResult(
            activity,
            Intent(activity, SearchActivity::class.java),
            requestCode,
            null
        )
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
        activity.startActivity(Intent(activity, MainScreenSettingsActivity::class.java))
    }

    fun startSelectLocationProviderActivity(activity: Activity) {
        activity.startActivity(Intent(activity, SelectLocationProviderActivity::class.java))
    }

    fun startSelectWeatherProviderActivity(activity: Activity) {
        activity.startActivity(Intent(activity, SelectWeatherProviderActivity::class.java))
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

    fun startWebViewActivity(context: Context, url: String?) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (isIntentAvailable(context, intent)) {
            context.startActivity(intent)
        } else {
            SnackbarHelper.showSnackbar("Unavailable internet browser.")
        }
    }

    @SuppressLint("WrongConstant")
    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        return context.packageManager
            .queryIntentActivities(intent, PackageManager.GET_ACTIVITIES)
            .size > 0
    }
}
