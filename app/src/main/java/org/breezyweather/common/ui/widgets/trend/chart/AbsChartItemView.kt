package org.breezyweather.common.ui.widgets.trend.chart

import android.content.Context
import android.util.AttributeSet
import android.view.View

abstract class AbsChartItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    abstract val marginTop: Int
    abstract val marginBottom: Int
}
