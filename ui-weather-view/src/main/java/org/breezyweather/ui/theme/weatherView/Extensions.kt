package org.breezyweather.ui.theme.weatherView

import android.content.Context
import android.content.res.Configuration
import android.hardware.SensorManager
import androidx.annotation.Px
import androidx.core.content.getSystemService
import kotlin.math.min

/**
 * Duplicate of existing extensions, so that the lib can be compiled separately
 * TODO: Move into a dedicated module to avoid duplicate
 */
private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_PHONE = 512
private const val MAX_TABLET_ADAPTIVE_LIST_WIDTH_DIP_TABLET = 600

val Context.isTabletDevice: Boolean
    get() = (
        this.resources.configuration.screenLayout
            and Configuration.SCREENLAYOUT_SIZE_MASK
        ) >= Configuration.SCREENLAYOUT_SIZE_LARGE

val Context.isLandscape: Boolean
    get() = this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun Context.dpToPx(dp: Float): Float {
    return dp * (this.resources.displayMetrics.densityDpi / 160f)
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

val Context.sensorManager: SensorManager?
    get() = getSystemService()
