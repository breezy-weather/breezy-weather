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
import androidx.core.graphics.ColorUtils
import com.google.android.material.resources.TextAppearance
import kotlin.math.min

private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE = 512
private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET = 600
val FLOATING_DECELERATE_INTERPOLATOR: Interpolator = DecelerateInterpolator(1f)
const val DEFAULT_CARD_LIST_ITEM_ELEVATION_DP = 2f

val Context.isTabletDevice: Boolean
    get() = (this.resources.configuration.screenLayout
            and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE

val Context.isLandscape: Boolean
    get() = this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

val Context.isRtl: Boolean
    get() = this.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

val Context.isDarkMode: Boolean
    get() = (this.resources.configuration.uiMode
            and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

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
    } else min(
        width.toFloat(),
        this.dpToPx((if (this.isTabletDevice) {
            MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET
        } else MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE).toFloat())
    ).toInt()
}

@SuppressLint("RestrictedApi", "VisibleForTests")
fun Context.getTypefaceFromTextAppearance(
    @StyleRes textAppearanceId: Int
): Typeface {
    return TextAppearance(this, textAppearanceId).getFont(this)
}

fun Window.setSystemBarStyle(
    statusShaderP: Boolean,
    lightStatusP: Boolean,
    navigationShaderP: Boolean,
    lightNavigationP: Boolean
) {
    var statusShader = statusShaderP
    var lightStatus = lightStatusP
    var navigationShader = navigationShaderP
    var lightNavigation = lightNavigationP
    var visibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

    // status bar.
    if (lightStatus) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            visibility = visibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            lightStatus = false
            statusShader = true
        }
    }

    // navigation bar.
    if (lightNavigation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            visibility = visibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            lightNavigation = false
            navigationShader = true
        }
    }
    navigationShader = navigationShader and (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)

    // flags.
    this.decorView.systemUiVisibility = visibility

    // colors.
    if (!statusShader) {
        this.statusBarColor = Color.TRANSPARENT
    } else {
        this.statusBarColor = ColorUtils.setAlphaComponent(
            if (lightStatus) Color.WHITE else Color.BLACK,
            ((if (lightStatus) 0.5 else 0.2) * 255).toInt()
        )
    }
    if (!navigationShader) {
        this.navigationBarColor = Color.TRANSPARENT
    } else {
        this.navigationBarColor = ColorUtils.setAlphaComponent(
            if (lightNavigation) Color.WHITE else Color.BLACK,
            ((if (lightNavigation) 0.5 else 0.2) * 255).toInt()
        )
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
    return this.getFloatingOvershotEnterAnimators(
        overshootFactor, this.translationY, this.scaleX, this.scaleY
    )
}

@Size(3)
fun View.getFloatingOvershotEnterAnimators(
    overshootFactor: Float,
    translationYFrom: Float,
    scaleXFrom: Float,
    scaleYFrom: Float
): Array<Animator> {
    val translation: Animator = ObjectAnimator.ofFloat(
        this, "translationY", translationYFrom, 0f
    )
    translation.interpolator = OvershootInterpolator(overshootFactor)
    val scaleX: Animator = ObjectAnimator.ofFloat(
        this, "scaleX", scaleXFrom, 1f
    )
    scaleX.interpolator = FLOATING_DECELERATE_INTERPOLATOR
    val scaleY: Animator = ObjectAnimator.ofFloat(
        this, "scaleY", scaleYFrom, 1f
    )
    scaleY.interpolator = FLOATING_DECELERATE_INTERPOLATOR
    return arrayOf(translation, scaleX, scaleY)
}