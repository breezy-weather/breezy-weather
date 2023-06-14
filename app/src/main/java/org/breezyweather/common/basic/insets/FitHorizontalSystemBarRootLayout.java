package org.breezyweather.common.basic.insets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;

public class FitHorizontalSystemBarRootLayout extends FrameLayout {

    private boolean mFitKeyboardExpanded;

    public FitHorizontalSystemBarRootLayout(Context context) {
        this(context, null);
    }

    public FitHorizontalSystemBarRootLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitHorizontalSystemBarRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFitKeyboardExpanded = false;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        Rect r = new Rect(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom()
        );
        FitBothSideBarHelper.setRootInsetsCache(
                new Rect(0, r.top, 0, mFitKeyboardExpanded ? 0 : r.bottom));
        setPadding(r.left, 0, r.right, 0);
        return insets;
    }

    public void setFitKeyboardExpanded(boolean fit) {
        mFitKeyboardExpanded = fit;
        ViewCompat.requestApplyInsets(this);
        requestLayout();
    }
}
