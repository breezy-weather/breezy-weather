package org.breezyweather.common.basic.insets

import android.graphics.Rect
import android.view.View
import android.view.WindowInsets
import org.breezyweather.common.basic.insets.FitBothSideBarView.FitSide

class FitBothSideBarHelper @JvmOverloads constructor(
    private val mTarget: View,
    private var mFitSide: Int = FitBothSideBarView.SIDE_TOP or FitBothSideBarView.SIDE_BOTTOM,
    private var mFitTopSideEnabled: Boolean = true,
    private var mFitBottomSideEnabled: Boolean = true
) {
    private var mWindowInsets: Rect

    init {
        mWindowInsets = Rect(0, 0, 0, 0)
    }

    fun onApplyWindowInsets(
        insets: WindowInsets,
        consumer: () -> Unit = { mTarget.requestLayout() }
    ): WindowInsets {
        mWindowInsets = Rect(
            insets.systemWindowInsetLeft,
            insets.systemWindowInsetTop,
            insets.systemWindowInsetRight,
            insets.systemWindowInsetBottom
        )
        consumer()
        return insets
    }

    fun fitSystemWindows(
        r: Rect,
        consumer: () -> Unit = { mTarget.requestLayout() }
    ): Boolean {
        mWindowInsets = r
        consumer()
        return false
    }

    val windowInsets: Rect
        get() = sRootInsetsCache.get() ?: mWindowInsets

    fun left(): Int {
        return windowInsets.left
    }

    fun top(): Int {
        return if (mFitSide and FitBothSideBarView.SIDE_TOP != 0 && mFitTopSideEnabled) {
            windowInsets.top
        } else 0
    }

    fun right(): Int {
        return windowInsets.right
    }

    fun bottom(): Int {
        return if (mFitSide and FitBothSideBarView.SIDE_BOTTOM != 0 && mFitBottomSideEnabled) {
            windowInsets.bottom
        } else 0
    }

    fun addFitSide(@FitSide side: Int) {
        if (mFitSide and side != 0) {
            mFitSide = mFitSide or side
            mTarget.requestLayout()
        }
    }

    fun removeFitSide(@FitSide side: Int) {
        if (mFitSide and side != 0) {
            mFitSide = mFitSide xor side
            mTarget.requestLayout()
        }
    }

    fun setFitSystemBarEnabled(top: Boolean, bottom: Boolean) {
        if (mFitTopSideEnabled != top || mFitBottomSideEnabled != bottom) {
            mFitTopSideEnabled = top
            mFitBottomSideEnabled = bottom
            mTarget.requestLayout()
        }
    }

    companion object {
        private val sRootInsetsCache: ThreadLocal<Rect> = ThreadLocal()

        fun setRootInsetsCache(rootInsets: Rect) {
            sRootInsetsCache.set(rootInsets)
        }
    }
}