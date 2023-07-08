package org.breezyweather.common.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.os.Build
import android.view.View
import android.view.Window
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.core.graphics.ColorUtils
import kotlin.math.ln

object DisplayUtils {
    val FLOATING_DECELERATE_INTERPOLATOR: Interpolator = DecelerateInterpolator(1f)
    const val DEFAULT_CARD_LIST_ITEM_ELEVATION_DP = 2f
    fun setSystemBarStyle(
        window: Window,
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
        window.decorView.systemUiVisibility = visibility

        // colors.
        if (!statusShader) {
            window.statusBarColor = Color.TRANSPARENT
        } else {
            window.statusBarColor = ColorUtils.setAlphaComponent(
                if (lightStatus) Color.WHITE else Color.BLACK,
                ((if (lightStatus) 0.5 else 0.2) * 255).toInt()
            )
        }
        if (!navigationShader) {
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            window.navigationBarColor = ColorUtils.setAlphaComponent(
                if (lightNavigation) Color.WHITE else Color.BLACK,
                ((if (lightNavigation) 0.5 else 0.2) * 255).toInt()
            )
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    @ColorInt
    fun bitmapToColorInt(bitmap: Bitmap): Int {
        return ThumbnailUtils.extractThumbnail(bitmap, 1, 1)
            .getPixel(0, 0)
    }

    fun isLightColor(@ColorInt color: Int): Boolean {
        val alpha = 0xFF shl 24
        var grey = color
        val red = grey and 0x00FF0000 shr 16
        val green = grey and 0x0000FF00 shr 8
        val blue = grey and 0x000000FF
        grey = (red * 0.3 + green * 0.59 + blue * 0.11).toInt()
        grey = alpha or (grey shl 16) or (grey shl 8) or grey
        return grey > -0x424243
    }

    @ColorInt
    fun blendColor(@ColorInt foreground: Int, @ColorInt background: Int): Int {
        val scr = Color.red(foreground)
        val scg = Color.green(foreground)
        val scb = Color.blue(foreground)
        val sa = foreground ushr 24
        val dcr = Color.red(background)
        val dcg = Color.green(background)
        val dcb = Color.blue(background)
        val color_r = dcr * (0xff - sa) / 0xff + scr * sa / 0xff
        val color_g = dcg * (0xff - sa) / 0xff + scg * sa / 0xff
        val color_b = dcb * (0xff - sa) / 0xff + scb * sa / 0xff
        return (color_r shl 16) + (color_g shl 8) + color_b or -0x1000000
    }

    // translationY, scaleX, scaleY
    @Size(3)
    fun getFloatingOvershotEnterAnimators(view: View): Array<Animator> {
        return getFloatingOvershotEnterAnimators(view, 1.5f)
    }

    @Size(3)
    fun getFloatingOvershotEnterAnimators(view: View, overshootFactor: Float): Array<Animator> {
        return getFloatingOvershotEnterAnimators(
            view, overshootFactor,
            view.translationY, view.scaleX, view.scaleY
        )
    }

    @Size(3)
    fun getFloatingOvershotEnterAnimators(
        view: View?,
        overshootFactor: Float,
        translationYFrom: Float,
        scaleXFrom: Float,
        scaleYFrom: Float
    ): Array<Animator> {
        val translation: Animator = ObjectAnimator.ofFloat(
            view, "translationY", translationYFrom, 0f
        )
        translation.interpolator = OvershootInterpolator(overshootFactor)
        val scaleX: Animator = ObjectAnimator.ofFloat(
            view, "scaleX", scaleXFrom, 1f
        )
        scaleX.interpolator = FLOATING_DECELERATE_INTERPOLATOR
        val scaleY: Animator = ObjectAnimator.ofFloat(
            view, "scaleY", scaleYFrom, 1f
        )
        scaleY.interpolator = FLOATING_DECELERATE_INTERPOLATOR
        return arrayOf(translation, scaleX, scaleY)
    }

    @ColorInt
    fun getWidgetSurfaceColor(
        elevationDp: Float,
        @ColorInt tintColor: Int,
        @ColorInt surfaceColor: Int
    ): Int {
        if (elevationDp == 0f) return surfaceColor
        val foreground = ColorUtils.setAlphaComponent(
            tintColor, ((4.5f * ln((elevationDp + 1).toDouble()) + 2f) / 100f * 255).toInt()
        )
        return blendColor(foreground, surfaceColor)
    }
}
