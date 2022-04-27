package wangdaye.com.geometricweather.common.basic

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import wangdaye.com.geometricweather.GeometricWeather
import wangdaye.com.geometricweather.common.basic.insets.FitHorizontalSystemBarRootLayout
import wangdaye.com.geometricweather.common.snackbar.SnackbarContainer
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.common.utils.LanguageUtils
import wangdaye.com.geometricweather.settings.SettingsManager

abstract class GeoActivity : AppCompatActivity() {

    lateinit var fitHorizontalSystemBarRootLayout: FitHorizontalSystemBarRootLayout

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
            DisplayUtils.getVisibleDisplayFrame(root, r)
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
        fitHorizontalSystemBarRootLayout = FitHorizontalSystemBarRootLayout(this)

        GeometricWeather.instance.addActivity(this)

        LanguageUtils.setLanguage(
            this,
            SettingsManager.getInstance(this).language.locale
        )

        DisplayUtils.setSystemBarStyle(
            this,
            window,
            false,
            !DisplayUtils.isDarkMode(this),
            true,
            !DisplayUtils.isDarkMode(this)
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
        GeometricWeather.instance.setTopActivity(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GeometricWeather.instance.setTopActivity(this)
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        GeometricWeather.instance.setTopActivity(this)
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        GeometricWeather.instance.checkToCleanTopActivity(this)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        GeometricWeather.instance.removeActivity(this)
    }

    open val snackbarContainer: SnackbarContainer?
        get() = SnackbarContainer(
            this,
            findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup,
            true
        )

    fun provideSnackbarContainer(): SnackbarContainer? = snackbarContainer

    val isActivityCreated: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    val isActivityStarted: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    val isActivityResumed: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
}