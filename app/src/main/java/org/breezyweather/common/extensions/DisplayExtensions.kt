package org.breezyweather.common.extensions

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.view.View
import androidx.annotation.Px
import kotlin.math.min

private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE = 512
private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET = 600

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