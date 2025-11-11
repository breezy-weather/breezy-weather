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
import androidx.annotation.ColorRes
import androidx.annotation.Px
import androidx.annotation.Size
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.resources.TextAppearance
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE = 512
private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET = 600
val FLOATING_DECELERATE_INTERPOLATOR: Interpolator = DecelerateInterpolator(1f)
const val DEFAULT_CARD_LIST_ITEM_ELEVATION_DP = 2f
private const val SQUISHED_BLOCK_FACTOR = 1.1f

val Context.isTabletDevice: Boolean
    get() = (
        resources.configuration.screenLayout
            and Configuration.SCREENLAYOUT_SIZE_MASK
        ) >= Configuration.SCREENLAYOUT_SIZE_LARGE

val Context.isLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

/**
 * Minimum size is adjusted from font scale:
 * - At 1.0 font scale, minimum size for a block is 1.0 (160 dp)
 * - At 2.0 font scale, minimum size for a block is 2.0 (320 dp)
 */
val Context.minBlockWidth: Float
    get() = 0.5f + fontScale.div(2f)

/**
 * @param widthInDp available width in which blocks will be displayed
 * @return a number of blocks between 1 and 5 that can fit
 */
fun Context.getBlocksPerRow(
    widthInDp: Float = windowWidth.toFloat().div(density),
): Int {
    val potentialResult = floor(widthInDp.div(minBlockWidth)).roundToInt().coerceIn(1..5)
    return if (potentialResult > 2) {
        // if more than 2 blocks can fit, we prefer displaying less blocks and have a bit more room
        // rather than having squished blocks
        floor(widthInDp.div(minBlockWidth * SQUISHED_BLOCK_FACTOR)).roundToInt().coerceIn(1..5)
    } else {
        potentialResult
    }
}

/**
 * Simplified estimation by taking into account more than 2 blocks are never squished,
 * and that devices with drawer layout always have space for at least 2 non-squished blocks
 */
val Context.areBlocksSquished: Boolean
    get() = getBlocksPerRow().let { blocksPerRow ->
        if (blocksPerRow > 2) {
            false
        } else {
            windowWidth.toFloat().div(density).div(minBlockWidth * SQUISHED_BLOCK_FACTOR) < blocksPerRow
        }
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
                resources.displayMetrics.densityDpi.div(android.util.DisplayMetrics.DENSITY_DEVICE_STABLE.toFloat())
        } else {
            1f // Let’s just ignore it on old Android versions
        }
    }

// Take into account font scale, but not as much
// For example a font scale of 1.6 makes the width 1.3 times larger
val Context.fontScaleToApply: Float
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fontScale.let {
                if (it != 1f) 1f + abs(it - 1f).div(2f).times(if (it > 1f) 1f else -1f) else it
            }
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

fun Context.getColorResource(@ColorRes id: Int): androidx.compose.ui.graphics.Color {
    return androidx.compose.ui.graphics.Color(ResourcesCompat.getColor(resources, id, theme))
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
