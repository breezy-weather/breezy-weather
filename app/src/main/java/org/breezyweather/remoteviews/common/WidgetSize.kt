package org.breezyweather.remoteviews.common

import android.content.res.Resources
import kotlin.math.roundToInt

data class WidgetSize(
    private val widthDp: Float,
    private val heightDp: Float,
) {
    val widthPx: Int = (widthDp * Resources.getSystem().displayMetrics.density).roundToInt()
    val heightPx: Int = (heightDp * Resources.getSystem().displayMetrics.density).roundToInt()
}
