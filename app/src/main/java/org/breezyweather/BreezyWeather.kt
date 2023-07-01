package org.breezyweather

import android.content.pm.ApplicationInfo
import android.os.Process
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.utils.LanguageUtils
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.db.ObjectBox
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.settings.SettingsManager
import org.breezyweather.theme.ThemeManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject

@HiltAndroidApp
class BreezyWeather : MultiDexApplication(),
    Configuration.Provider {

    companion object {

        @JvmStatic
        lateinit var instance: BreezyWeather
            private set

        // widgets.
        // day.
        const val WIDGET_DAY_PENDING_INTENT_CODE_WEATHER = 11
        const val WIDGET_DAY_PENDING_INTENT_CODE_CALENDAR = 13

        // week.
        const val WIDGET_WEEK_PENDING_INTENT_CODE_WEATHER = 21
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 211
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 212
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 213
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 214
        const val WIDGET_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 215

        // day + week.
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_WEATHER = 31
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_1 = 311
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_2 = 312
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_3 = 313
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_4 = 314
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_DAILY_FORECAST_5 = 315
        const val WIDGET_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 33

        // clock + day (vertical).
        const val WIDGET_CLOCK_DAY_VERTICAL_PENDING_INTENT_CODE_WEATHER = 41
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
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CALENDAR = 63
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_LIGHT = 64
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_NORMAL = 65
        const val WIDGET_CLOCK_DAY_HORIZONTAL_PENDING_INTENT_CODE_CLOCK_BLACK = 66

        // clock + day + details.
        const val WIDGET_CLOCK_DAY_DETAILS_PENDING_INTENT_CODE_WEATHER = 71
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
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CALENDAR = 83
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_LIGHT = 84
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_NORMAL = 85
        const val WIDGET_CLOCK_DAY_WEEK_PENDING_INTENT_CODE_CLOCK_BLACK = 86

        // text.
        const val WIDGET_TEXT_PENDING_INTENT_CODE_WEATHER = 91
        const val WIDGET_TEXT_PENDING_INTENT_CODE_CALENDAR = 93

        // trend daily.
        const val WIDGET_TREND_DAILY_PENDING_INTENT_CODE_WEATHER = 101

        // trend hourly.
        const val WIDGET_TREND_HOURLY_PENDING_INTENT_CODE_WEATHER = 111

        // multi city.
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_1 = 121
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_2 = 123
        const val WIDGET_MULTI_CITY_PENDING_INTENT_CODE_WEATHER_3 = 125

        // material you.
        const val WIDGET_MATERIAL_YOU_FORECAST_PENDING_INTENT_CODE_WEATHER = 131
        const val WIDGET_MATERIAL_YOU_CURRENT_PENDING_INTENT_CODE_WEATHER = 132

        fun getProcessName() = try {
            val file = File("/proc/" + Process.myPid() + "/" + "cmdline")
            val mBufferedReader = BufferedReader(FileReader(file))
            val processName = mBufferedReader.readLine().trim {
                it <= ' '
            }
            mBufferedReader.close()

            processName
        } catch (e: Exception) {
            e.printStackTrace()

            null
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

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        instance = this

        ObjectBox.init(this)

        LanguageUtils.setLanguage(
            this,
            SettingsManager.getInstance(this).language.locale
        )

        setupNotificationChannels()

        if (getProcessName().equals(packageName)) {
            setDayNightMode()
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

    fun recreateAllActivities() {
        for (a in activitySet) {
            a.recreate()
        }
    }

    private fun setDayNightMode() {
        AppCompatDelegate.setDefaultNightMode(
            ThemeManager.getInstance(this).uiMode.value!!
        )
        ThemeManager.getInstance(this).uiMode.observeForever {
            AppCompatDelegate.setDefaultNightMode(it)
        }
    }

    private fun setupNotificationChannels() {
        try {
            Notifications.createChannels(this)
        } catch (e: Exception) {
            LogHelper.log(msg = "Failed to setup notification channels")
        }
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}