package org.breezyweather.common.ui.widgets.insets

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.breezyweather.R
import org.breezyweather.common.basic.insets.FitBothSideBarHelper
import org.breezyweather.common.basic.insets.FitBothSideBarView
import org.breezyweather.common.basic.insets.FitBothSideBarView.FitSide

class FitSystemBarSwipeRefreshLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs), FitBothSideBarView {
    private val mHelper: FitBothSideBarHelper

    init {
        mHelper = FitBothSideBarHelper(this, FitBothSideBarView.SIDE_TOP)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        return mHelper.onApplyWindowInsets(insets) { fitSystemBar() }
    }

    private fun fitSystemBar() {
        val startPosition = mHelper.top() + resources.getDimensionPixelSize(R.dimen.normal_margin)
        val endPosition = (startPosition + 64 * resources.displayMetrics.density).toInt()
        if (startPosition != progressViewStartOffset
            || endPosition != progressViewEndOffset
        ) {
            setProgressViewOffset(false, startPosition, endPosition)
        }
    }

    override fun addFitSide(@FitSide side: Int) {
        // do nothing.
    }

    override fun removeFitSide(@FitSide side: Int) {
        // do nothing.
    }

    override fun setFitSystemBarEnabled(top: Boolean, bottom: Boolean) {
        mHelper.setFitSystemBarEnabled(top, bottom)
    }

    override val topWindowInset: Int
        get() = mHelper.top()
    override val bottomWindowInset: Int
        get() = 0
}
