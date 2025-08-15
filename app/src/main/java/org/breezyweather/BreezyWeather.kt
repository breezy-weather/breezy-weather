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

package org.breezyweather

import android.app.Application
import android.app.UiModeManager
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Process
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkQuery
import dagger.hilt.android.HiltAndroidApp
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.uiModeManager
import org.breezyweather.common.extensions.workManager
import org.breezyweather.common.utils.helpers.LogHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.remoteviews.Notifications
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.inject.Inject

@HiltAndroidApp
class BreezyWeather : Application(), Configuration.Provider {

    companion object {

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

    private val activitySet: MutableSet<BreezyActivity> by lazy {
        HashSet()
    }
    var topActivity: BreezyActivity? = null
        private set

    val debugMode: Boolean by lazy {
        applicationInfo != null && applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        instance = this

        setupNotificationChannels()

        if (getProcessName().equals(packageName)) {
            // Sets and persists the night mode setting for this app. This allows the system to know
            // if the app wants to be displayed in dark mode before it launches so that the splash
            // screen can be displayed accordingly.
            setDayNightMode()
        }

        /**
         * We don’t use the return value, but querying the work manager might help bringing back
         * scheduled workers after the app has been killed/shutdown on some devices
         */
        this.workManager.getWorkInfosLiveData(WorkQuery.fromStates(WorkInfo.State.ENQUEUED))
    }

    fun addActivity(a: BreezyActivity) {
        activitySet.add(a)
    }

    fun removeActivity(a: BreezyActivity) {
        activitySet.remove(a)
    }

    fun setTopActivity(a: BreezyActivity) {
        topActivity = a
    }

    fun checkToCleanTopActivity(a: BreezyActivity) {
        if (topActivity === a) {
            topActivity = null
        }
    }

    fun recreateAllActivities() {
        val topA = topActivity
        for (a in activitySet) {
            if (a != topA) a.recreate()
        }
        // ensure that top activity stays on top by recreating it last
        topA?.recreate()
    }

    private fun setDayNightMode() {
        updateDayNightMode(SettingsManager.getInstance(this).darkMode.value)
    }

    fun updateDayNightMode(dayNightMode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager?.setApplicationNightMode(
                when (dayNightMode) {
                    AppCompatDelegate.MODE_NIGHT_NO -> UiModeManager.MODE_NIGHT_NO
                    AppCompatDelegate.MODE_NIGHT_YES -> UiModeManager.MODE_NIGHT_YES
                    else -> UiModeManager.MODE_NIGHT_AUTO
                }
            )
        } else {
            AppCompatDelegate.setDefaultNightMode(dayNightMode)
        }
    }

    private fun setupNotificationChannels() {
        try {
            Notifications.createChannels(this)
        } catch (e: Exception) {
            LogHelper.log(msg = "Failed to setup notification channels")
        }
    }

    override val workManagerConfiguration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
