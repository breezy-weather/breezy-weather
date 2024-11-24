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
import android.view.View
import android.view.Window
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.Px
import androidx.annotation.Size
import androidx.annotation.StyleRes
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.resources.TextAppearance
import kotlin.math.min

private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE = 512
private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET = 600
val FLOATING_DECELERATE_INTERPOLATOR: Interpolator = DecelerateInterpolator(1f)
const val DEFAULT_CARD_LIST_ITEM_ELEVATION_DP = 2f

val Context.isTabletDevice: Boolean
    get() = (
        this.resources.configuration.screenLayout
            and Configuration.SCREENLAYOUT_SIZE_MASK
        ) >= Configuration.SCREENLAYOUT_SIZE_LARGE

val Context.isLandscape: Boolean
    get() = this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val Context.isRtl: Boolean
    get() = this.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

val Context.isDarkMode: Boolean
    get() = (
        this.resources.configuration.uiMode
            and Configuration.UI_MODE_NIGHT_MASK
        ) == Configuration.UI_MODE_NIGHT_YES

val Context.isMotionReduced: Boolean
    get() {
        return try {
            Settings.Global.getFloat(this.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE) == 0f
        } catch (e: SettingNotFoundException) {
            false
        }
    }

fun Context.dpToPx(dp: Float): Float {
    return dp * (this.resources.displayMetrics.densityDpi / 160f)
}

fun Context.spToPx(sp: Int): Float {
    return sp * this.resources.displayMetrics.scaledDensity
}

fun Context.pxToDp(@Px px: Int): Float {
    return px / (this.resources.displayMetrics.densityDpi / 160f)
}

@Px
fun Context.getTabletListAdaptiveWidth(@Px width: Int): Int {
    return if (!this.isTabletDevice && !this.isLandscape) {
        width
    } else {
        min(
            width.toFloat(),
            this.dpToPx(
                if (this.isTabletDevice) {
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

fun Window.setSystemBarStyle(
    statusShaderP: Boolean,
    lightStatusP: Boolean,
    navigationShaderP: Boolean,
    lightNavigationP: Boolean,
) {
    var lightStatus = lightStatusP
    var statusShader = statusShaderP
    var lightNavigation = lightNavigationP
    var navigationShader = navigationShaderP

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        // Use default dark and light platform colors from EdgeToEdge
        val colorSystemBarDark = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
        val colorSystemBarLight = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Always apply a dark shader as a light or transparent status bar is not supported
            lightStatus = false
            statusShader = true
        }
        this.statusBarColor = if (statusShader) {
            if (lightStatus) colorSystemBarLight else colorSystemBarDark
        } else {
            Color.TRANSPARENT
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Always apply a dark shader as a light or transparent navigation bar is not supported
            lightNavigation = false
            navigationShader = true
        }
        this.navigationBarColor = if (navigationShader) {
            if (lightNavigation) colorSystemBarLight else colorSystemBarDark
        } else {
            Color.TRANSPARENT
        }
    } else {
        this.isStatusBarContrastEnforced = statusShader
        this.isNavigationBarContrastEnforced = navigationShaderP
    }

    // Contrary to the documentation FALSE applies a light foreground color and TRUE a dark foreground color
    WindowInsetsControllerCompat(this, this.decorView).run {
        isAppearanceLightStatusBars = lightStatus
        isAppearanceLightNavigationBars = lightNavigation
    }
}

fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(
        this.intrinsicWidth,
        this.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, this.intrinsicWidth, this.intrinsicHeight)
    this.draw(canvas)
    return bitmap
}

// translationY, scaleX, scaleY
@Size(3)
fun View.getFloatingOvershotEnterAnimators(): Array<Animator> {
    return this.getFloatingOvershotEnterAnimators(1.5f)
}

@Size(3)
fun View.getFloatingOvershotEnterAnimators(overshootFactor: Float): Array<Animator> {
    return this.getFloatingOvershotEnterAnimators(overshootFactor, this.translationY, this.scaleX, this.scaleY)
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
