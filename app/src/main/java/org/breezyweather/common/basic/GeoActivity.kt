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
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import org.breezyweather.BreezyWeather
import org.breezyweather.common.basic.insets.FitHorizontalSystemBarRootLayout
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.setSystemBarStyle
import org.breezyweather.common.snackbar.SnackbarContainer

abstract class GeoActivity : AppCompatActivity() {

    lateinit var fitHorizontalSystemBarRootLayout: FitHorizontalSystemBarRootLayout

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fitHorizontalSystemBarRootLayout =
            FitHorizontalSystemBarRootLayout(this)

        BreezyWeather.instance.addActivity(this)

        window.setSystemBarStyle(
            false,
            !this.isDarkMode,
            true,
            !this.isDarkMode
        )

        // decor -> fit horizontal system bar -> decor child.
        val decorView = window.decorView as ViewGroup
        val decorChild = decorView.getChildAt(0) as ViewGroup

        decorView.removeView(decorChild)
        decorView.addView(fitHorizontalSystemBarRootLayout)

        fitHorizontalSystemBarRootLayout.removeAllViews()
        fitHorizontalSystemBarRootLayout.addView(decorChild)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        BreezyWeather.instance.setTopActivity(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
