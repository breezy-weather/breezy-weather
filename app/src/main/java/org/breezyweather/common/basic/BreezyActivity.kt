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

package org.breezyweather.common.basic

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.snackbar.SnackbarContainer
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.theme.ThemeManager

abstract class BreezyActivity : AppCompatActivity() {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        applyAppTheme()

        BreezyWeather.instance.addActivity(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        BreezyWeather.instance.setTopActivity(this)
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        BreezyWeather.instance.setTopActivity(this)
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        BreezyWeather.instance.checkToCleanTopActivity(this)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        BreezyWeather.instance.removeActivity(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkToUpdateDarkMode()
    }

    private fun applyAppTheme() {
        setTheme(R.style.BreezyWeatherTheme)
    }

    protected open fun checkToUpdateDarkMode() {
        if (ThemeManager.isLightTheme(this) != !isDarkMode) {
            BreezyWeather.instance.updateDayNightMode(SettingsManager.getInstance(this).darkMode.value)
        }
    }

    open val snackbarContainer: SnackbarContainer
        get() = SnackbarContainer(
            this,
            findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup,
            true
        )

    fun provideSnackbarContainer(): SnackbarContainer = snackbarContainer

    val isActivityCreated: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    val isActivityStarted: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    val isActivityResumed: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
}
