package org.breezyweather.common.basic

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import org.breezyweather.BreezyWeather
import org.breezyweather.settings.SettingsManager

abstract class GeoActivity : AppCompatActivity() {

    lateinit var fitHorizontalSystemBarRootLayout: org.breezyweather.common.basic.insets.FitHorizontalSystemBarRootLayout

    private class KeyboardResizeBugWorkaround private constructor(
        activity: GeoActivity
    ) {
        private val root = activity.fitHorizontalSystemBarRootLayout
        private val rootParams: ViewGroup.LayoutParams
        private var usableHeightPrevious = 0

        private fun possiblyResizeChildOfContent() {
            val usableHeightNow = computeUsableHeight()

            if (usableHeightNow != usableHeightPrevious) {
                val screenHeight = root.rootView.height
                val keyboardExpanded: Boolean

                if (screenHeight - usableHeightNow > screenHeight / 5) {
                    // keyboard probably just became visible.
                    keyboardExpanded = true
                    rootParams.height = usableHeightNow
                } else {
                    // keyboard probably just became hidden.
                    keyboardExpanded = false
                    rootParams.height = screenHeight
                }

                usableHeightPrevious = usableHeightNow
                root.setFitKeyboardExpanded(keyboardExpanded)
            }
        }

        private fun computeUsableHeight(): Int {
            val r = Rect()
            org.breezyweather.common.utils.DisplayUtils.getVisibleDisplayFrame(root, r)
            return r.bottom // - r.top; --> Do not reduce the height of status bar.
        }

        companion object {
            // For more information, see https://issuetracker.google.com/issues/36911528
            // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.
            fun assistActivity(activity: GeoActivity) {
                KeyboardResizeBugWorkaround(activity)
            }
        }

        init {
            root.viewTreeObserver.addOnGlobalLayoutListener { possiblyResizeChildOfContent() }
            rootParams = root.layoutParams
        }
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fitHorizontalSystemBarRootLayout =
            org.breezyweather.common.basic.insets.FitHorizontalSystemBarRootLayout(this)

        BreezyWeather.instance.addActivity(this)

        org.breezyweather.common.utils.LanguageUtils.setLanguage(
            this,
            SettingsManager.getInstance(this).language.locale
        )

        org.breezyweather.common.utils.DisplayUtils.setSystemBarStyle(
            this,
            window,
            false,
            !org.breezyweather.common.utils.DisplayUtils.isDarkMode(this),
            true,
            !org.breezyweather.common.utils.DisplayUtils.isDarkMode(this)
        )
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // decor -> fit horizontal system bar -> decor child.
        val decorView = window.decorView as ViewGroup
        val decorChild = decorView.getChildAt(0) as ViewGroup

        decorView.removeView(decorChild)
        decorView.addView(fitHorizontalSystemBarRootLayout)

        fitHorizontalSystemBarRootLayout.removeAllViews()
        fitHorizontalSystemBarRootLayout.addView(decorChild)

        KeyboardResizeBugWorkaround.assistActivity(this)
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

    open val snackbarContainer: org.breezyweather.common.snackbar.SnackbarContainer?
        get() = org.breezyweather.common.snackbar.SnackbarContainer(
            this,
            findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup,
            true
        )

    fun provideSnackbarContainer(): org.breezyweather.common.snackbar.SnackbarContainer? = snackbarContainer

    val isActivityCreated: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    val isActivityStarted: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    val isActivityResumed: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
}