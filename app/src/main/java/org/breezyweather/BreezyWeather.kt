package org.breezyweather

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Process
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
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
class BreezyWeather : Application(),
    Configuration.Provider {

    companion object {

        @JvmStatic
        lateinit var instance: BreezyWeather
            private set

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