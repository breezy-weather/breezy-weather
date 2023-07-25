package org.breezyweather.common.utils.helpers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.ui.activities.AlertActivity
import org.breezyweather.common.ui.activities.AllergenActivity
import org.breezyweather.daily.DailyWeatherActivity
import org.breezyweather.main.MainActivity
import org.breezyweather.search.SearchActivity
import org.breezyweather.settings.activities.AboutActivity
import org.breezyweather.settings.activities.CardDisplayManageActivity
import org.breezyweather.settings.activities.DailyTrendDisplayManageActivity
import org.breezyweather.settings.activities.DetailDisplayManageActivity
import org.breezyweather.settings.activities.HourlyTrendDisplayManageActivity
import org.breezyweather.settings.activities.PreviewIconActivity
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

    fun buildMainActivityShowAlertsIntent(location: Location?, alertId: Long? = null): Intent {
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

    fun buildAwakeUpdateActivityIntent(): Intent {
        return Intent("org.breezyweather.UPDATE").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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

    fun startAlertActivity(activity: Activity, formattedId: String?, alertId: Long? = null) {
        activity.startActivity(
            Intent(activity, AlertActivity::class.java).apply {
                putExtra(AlertActivity.KEY_FORMATTED_ID, formattedId)
                if (alertId != null) {
                    putExtra(AlertActivity.KEY_ALERT_ID, alertId)
                }
            }
        )
    }

    fun startAllergenActivity(activity: Activity, location: Location) {
        activity.startActivity(
            Intent(activity, AllergenActivity::class.java).apply {
                putExtra(
                    AllergenActivity.KEY_ALLERGEN_ACTIVITY_LOCATION_FORMATTED_ID,
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

    fun startAboutActivity(activity: Activity) {
        activity.startActivity(Intent(activity, AboutActivity::class.java))
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

    fun startAppStoreDetailsActivity(context: Context, packageName: String = context.packageName) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        )
        if (isIntentAvailable(context, intent)) {
            context.startActivity(intent)
        } else {
            SnackbarHelper.showSnackbar("Unavailable AppStore.")
        }
    }

    fun startAppStoreSearchActivity(context: Context, query: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://search?q=$query")
        )
        if (isIntentAvailable(context, intent)) {
            context.startActivity(intent)
        } else {
            SnackbarHelper.showSnackbar("Unavailable AppStore.")
        }
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
