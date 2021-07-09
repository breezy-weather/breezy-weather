package wangdaye.com.geometricweather

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.basic.models.options.DarkMode
import wangdaye.com.geometricweather.common.utils.LanguageUtils
import wangdaye.com.geometricweather.common.utils.helpers.BuglyHelper
import wangdaye.com.geometricweather.settings.SettingsManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject

/**
 * Geometric weather application class.
 */
@HiltAndroidApp
class GeometricWeather : MultiDexApplication(), Configuration.Provider {

    companion object {

        @JvmStatic
        lateinit var instance: GeometricWeather
            private set

        // notifications.
        const val NOTIFICATION_CHANNEL_ID_NORMALLY = "normally"
        const val NOTIFICATION_CHANNEL_ID_ALERT = "alert"
        const val NOTIFICATION_CHANNEL_ID_FORECAST = "forecast"
        const val NOTIFICATION_CHANNEL_ID_LOCATION = "location"
        const val NOTIFICATION_CHANNEL_ID_BACKGROUND = "background"

        const val NOTIFICATION_ID_NORMALLY = 1
        const val NOTIFICATION_ID_TODAY_FORECAST = 2
        const val NOTIFICATION_ID_TOMORROW_FORECAST = 3
        const val NOTIFICATION_ID_LOCATION = 4
        const val NOTIFICATION_ID_RUNNING_IN_BACKGROUND = 5
        const val NOTIFICATION_ID_UPDATING_NORMALLY = 6
        const val NOTIFICATION_ID_UPDATING_TODAY_FORECAST = 7
        const val NOTIFICATION_ID_UPDATING_TOMORROW_FORECAST = 8
        const val NOTIFICATION_ID_UPDATING_AWAKE = 9
        const val NOTIFICATION_ID_ALERT_MIN = 1000
        const val NOTIFICATION_ID_ALERT_MAX = 1999
        const val NOTIFICATION_ID_ALERT_GROUP = 2000
        const val NOTIFICATION_ID_PRECIPITATION = 3000

        // widgets.

        // day.
        const val WIDGET_DAY_PENDING_INTENT_CODE_WEATHER = 11
        const val WIDGET_DAY_PENDING_INTENT_CODE_REFRESH = 12
        const val WIDGET_DAY_PENDING_INTENT_CODE_CALENDAR = 13

        // week.
        const val WIDGET_WEEK_PENDING_INTENT_CODE_WEATHER = 21
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 211
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 212
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 213
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 214
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 215
        const val WIDGET_WEEK_PENDING_INTENT_CODE_REFRESH = 22

        // day + week.
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_WEATHER = 31
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 311
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 312
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 313
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 314
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 315
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_REFRESH = 32
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 33

        // clock + day (vertical).
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_WEATHER = 41
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_REFRESH = 42
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_LIGHT = 43
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_NORMAL = 44
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_BLACK = 45
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_LIGHT = 46
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_LIGHT = 47
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_NORMAL = 48
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_NORMAL = 49
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_1_BLACK = 50
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_CLOCK_2_BLACK = 51

        // clock + day (horizontal).
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_WEATHER = 61
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_REFRESH = 62
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CALENDAR = 63
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_LIGHT = 64
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_NORMAL = 65
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_BLACK = 66

        // clock + day + details.
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_WEATHER = 71
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_REFRESH = 72
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CALENDAR = 73
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_LIGHT = 74
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_NORMAL = 75
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_CLOCK_BLACK = 76

        // clock + day + week.
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_WEATHER = 81
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 821
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 822
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 823
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 824
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 825
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_REFRESH = 82
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 83
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_LIGHT = 84
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_NORMAL = 85
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_BLACK = 86

        // text.
        const val WIDGET_TEXT_PENDING_INTENT_CODE_WEATHER = 91
        const val WIDGET_TEXT_PENDING_INTENT_CODE_REFRESH = 92
        const val WIDGET_TEXT_PENDING_INTENT_CODE_CALENDAR = 93

        // trend daily.
        const val WIDGET_TREND_DAILY_PENDING_INTENT_CODE_WEATHER = 101
        const val WIDGET_TREND_DAILY_PENDING_INTENT_CODE_REFRESH = 102

        // trend hourly.
        const val WIDGET_TREND_HOURLY_PENDING_INTENT_CODE_WEATHER = 111
        const val WIDGET_TREND_HOURLY_PENDING_INTENT_CODE_REFRESH = 112

        // multi city.
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_1 = 121
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_REFRESH_1 = 122
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_2 = 123
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_REFRESH_2 = 124
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_3 = 125
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_REFRESH_3 = 126

        fun getProcessName(): String? = try {
            val file = File("/proc/" + Process.myPid() + "/" + "cmdline")
            val mBufferedReader = BufferedReader(FileReader(file))
            val processName = mBufferedReader.readLine().trim { it <= ' ' }
            mBufferedReader.close()

            processName
        } catch (e: Exception) {
            e.printStackTrace()

            null
        }

        @JvmStatic
        fun getNotificationChannelName(context: Context, channelId: String): String {
            return when (channelId) {
                NOTIFICATION_CHANNEL_ID_ALERT -> (context.getString(R.string.geometric_weather)
                        + " " + context.getString(R.string.action_alert))

                NOTIFICATION_CHANNEL_ID_FORECAST -> (context.getString(R.string.geometric_weather)
                        + " " + context.getString(R.string.forecast))

                NOTIFICATION_CHANNEL_ID_LOCATION -> (context.getString(R.string.geometric_weather)
                        + " " + context.getString(R.string.feedback_request_location))

                NOTIFICATION_CHANNEL_ID_BACKGROUND -> (context.getString(R.string.geometric_weather)
                        + " " + context.getString(R.string.background_information))

                else -> context.getString(R.string.geometric_weather)
            }
        }
    }

    private val activitySet: MutableSet<GeoActivity> by lazy {
        HashSet()
    }
    var topActivity: GeoActivity? = null
        private set

    val debugMode: Boolean by lazy {
        applicationInfo != null
                && applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        instance = this
        LanguageUtils.setLanguage(this,
                SettingsManager.getInstance(this).getLanguage().locale)

        BuglyHelper.init(this)

        if (getProcessName().equals(packageName)) {
            resetDayNightMode()
        }
    }

    fun addActivity(a: GeoActivity) {
        activitySet.add(a)
    }

    fun removeActivity(a: GeoActivity) {
        activitySet.remove(a)
    }

    fun setTopActivity(a: GeoActivity) {
        topActivity = a
    }

    fun checkToCleanTopActivity(a: GeoActivity) {
        if (topActivity === a) {
            topActivity = null
        }
    }

    fun resetDayNightMode() {
        when (SettingsManager.getInstance(this).getDarkMode()) {
            DarkMode.AUTO -> {
                // do nothing.
            }
            DarkMode.SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            DarkMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            DarkMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun recreateAllActivities() {
        for (a in activitySet) {
            a.recreate()
        }
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}