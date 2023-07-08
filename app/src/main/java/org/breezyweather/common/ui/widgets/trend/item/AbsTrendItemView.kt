package org.breezyweather.common.ui.widgets.trend.item

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import org.breezyweather.common.ui.widgets.trend.chart.AbsChartItemView

abstract class AbsTrendItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    abstract var chartItemView: AbsChartItemView?
    abstract val chartTop: Int
    abstract val chartBottom: Int
}
