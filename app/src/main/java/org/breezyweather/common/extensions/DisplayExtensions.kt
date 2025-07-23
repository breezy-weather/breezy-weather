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

package org.breezyweather.common.extensions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.AttrRes
import androidx.annotation.Px
import androidx.annotation.Size
import androidx.annotation.StyleRes
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.resources.TextAppearance
import kotlin.math.min

private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE = 512
private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET = 600
val FLOATING_DECELERATE_INTERPOLATOR: Interpolator = DecelerateInterpolator(1f)
const val DEFAULT_CARD_LIST_ITEM_ELEVATION_DP = 2f
const val MAX_FONT_SCALE_FOR_HALF_BLOCKS = 1.2

val Context.isTabletDevice: Boolean
    get() = (
        resources.configuration.screenLayout
            and Configuration.SCREENLAYOUT_SIZE_MASK
        ) >= Configuration.SCREENLAYOUT_SIZE_LARGE

val Context.isLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

/**
 * Returns true if there is enough width to display two blocks
 * Allowed down to 320 dp and 1.0 font scale
 * Then follows a linear progress between width and font scale
 */
val Context.isWidthHalfSizeable: Boolean
    get() {
        val currentWindowWidth = windowWidth.toFloat().div(density)
        val currentFontScale = fontScale

        // Below 320 dp
        if (currentWindowWidth < 2.0) return false

        /**
         * Examples:
         * window width = 2.0 (320 dp), max font scale = 1.0
         * window width = 2.5 (400 dp), max font scale = 1.5
         * window width = 3.0 (480 dp), max font scale = 2.0
         */
        val maxFontScale = currentWindowWidth - 1.0

        return currentFontScale <= maxFontScale
    }

val Context.isRtl: Boolean
    get() = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

val Context.isDarkMode: Boolean
    get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

val Context.isMotionReduced: Boolean
    get() {
        return try {
            Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE) == 0f
        } catch (e: SettingNotFoundException) {
            false
        }
    }

val Context.density: Int
    get() {
        return resources.displayMetrics.densityDpi
    }

val Context.fontScale: Float
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.fontScale *
                resources.displayMetrics.densityDpi.div(android.util.DisplayMetrics.DENSITY_DEVICE_STABLE)
        } else {
            1f // Let’s just ignore it on old Android versions
        }
    }

val Context.windowHeightInDp: Float
    get() {
        return pxToDp(resources.displayMetrics.heightPixels)
    }

val Context.windowWidthInDp: Float
    get() {
        return pxToDp(resources.displayMetrics.widthPixels)
    }

val Context.windowWidth: Int
    @Px
    get() {
        return resources.displayMetrics.widthPixels
    }

fun Context.dpToPx(dp: Float): Float {
    return dp * (resources.displayMetrics.densityDpi / 160f)
}

fun Context.spToPx(sp: Int): Float {
    return sp * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1.0f, resources.displayMetrics)
}

@Suppress("unused")
fun Context.pxToDp(@Px px: Int): Float {
    return px / (resources.displayMetrics.densityDpi / 160f)
}

@Px
fun Context.getTabletListAdaptiveWidth(@Px width: Int): Int {
    return if (!isTabletDevice && !isLandscape) {
        width
    } else {
        min(
            width.toFloat(),
            dpToPx(
                if (isTabletDevice) {
                    MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET
                } else {
                    MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE
                }.toFloat()
            )
        ).toInt()
    }
}

@SuppressLint("RestrictedApi", "VisibleForTests")
fun Context.getTypefaceFromTextAppearance(
    @StyleRes textAppearanceId: Int,
): Typeface {
    return TextAppearance(this, textAppearanceId).getFont(this)
}

fun Context.getThemeColor(
    @AttrRes id: Int,
): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(id, typedValue, true)
    return typedValue.data
}

@Suppress("DEPRECATION")
fun Window.setSystemBarStyle(
    lightStatus: Boolean,
) {
    var newLightStatus = lightStatus

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        // Use default dark and light platform colors from EdgeToEdge
        val colorSystemBarDark = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
        val colorSystemBarLight = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Always apply a dark shader as a light or transparent status bar is not supported
            newLightStatus = false
        }
        statusBarColor = Color.TRANSPARENT

        navigationBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && lightStatus) {
            colorSystemBarLight
        } else {
            colorSystemBarDark
        }
    } else {
        isStatusBarContrastEnforced = false
        isNavigationBarContrastEnforced = true
    }

    // Contrary to the documentation FALSE applies a light foreground color and TRUE a dark foreground color
    WindowInsetsControllerCompat(this, decorView).run {
        isAppearanceLightStatusBars = newLightStatus
        isAppearanceLightNavigationBars = lightStatus
    }
}

fun Drawable.toBitmap(): Bitmap {
    val bitmap = createBitmap(intrinsicWidth, intrinsicHeight)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    draw(canvas)
    return bitmap
}

// translationY, scaleX, scaleY
@Size(3)
fun View.getFloatingOvershotEnterAnimators(): Array<Animator> {
    return getFloatingOvershotEnterAnimators(1.5f)
}

@Size(3)
fun View.getFloatingOvershotEnterAnimators(overshootFactor: Float): Array<Animator> {
    return getFloatingOvershotEnterAnimators(overshootFactor, translationY, scaleX, scaleY)
}

@Size(3)
fun View.getFloatingOvershotEnterAnimators(
    overshootFactor: Float,
    translationYFrom: Float,
    scaleXFrom: Float,
    scaleYFrom: Float,
): Array<Animator> {
    val translation: Animator = ObjectAnimator.ofFloat(this, "translationY", translationYFrom, 0f)
    translation.interpolator = OvershootInterpolator(overshootFactor)
    val scaleX: Animator = ObjectAnimator.ofFloat(this, "scaleX", scaleXFrom, 1f)
    scaleX.interpolator = FLOATING_DECELERATE_INTERPOLATOR
    val scaleY: Animator = ObjectAnimator.ofFloat(this, "scaleY", scaleYFrom, 1f)
    scaleY.interpolator = FLOATING_DECELERATE_INTERPOLATOR
    return arrayOf(translation, scaleX, scaleY)
}
