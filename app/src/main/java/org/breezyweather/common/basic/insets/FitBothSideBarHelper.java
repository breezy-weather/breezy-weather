package org.breezyweather.common.basic.insets;

import android.graphics.Rect;
import android.view.View;
import android.view.WindowInsets;

public class FitBothSideBarHelper {

    private final View mTarget;

    private static final ThreadLocal<Rect> sRootInsetsCache;
    private Rect mWindowInsets;

    private @FitBothSideBarView.FitSide int mFitSide;
    private boolean mFitTopSideEnabled;
    private boolean mFitBottomSideEnabled;

    static {
        sRootInsetsCache = new ThreadLocal<>();
    }

    public interface InsetsConsumer {
        void consume();
    }

    public FitBothSideBarHelper(View target) {
        this(target, FitBothSideBarView.SIDE_TOP | FitBothSideBarView.SIDE_BOTTOM);
    }

    public FitBothSideBarHelper(View target, int fitSide) {
        this(target, fitSide, true, true);
    }

    public FitBothSideBarHelper(View target, int fitSide,
                                boolean fitTopSideEnabled, boolean fitBottomSideEnabled) {
        mTarget = target;

        mWindowInsets = new Rect(0, 0, 0, 0);

        mFitSide = fitSide;
        mFitTopSideEnabled = fitTopSideEnabled;
        mFitBottomSideEnabled = fitBottomSideEnabled;
    }

    public static void setRootInsetsCache(Rect rootInsets) {
        sRootInsetsCache.set(rootInsets);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return onApplyWindowInsets(insets, mTarget::requestLayout);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets, InsetsConsumer consumer) {
        mWindowInsets = new Rect(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom()
        );
        consumer.consume();
        return insets;
    }

    public boolean fitSystemWindows(Rect r) {
        return fitSystemWindows(r, mTarget::requestLayout);
    }

    public boolean fitSystemWindows(Rect r, InsetsConsumer consumer) {
        mWindowInsets = r;
        consumer.consume();
        return false;
    }

    public Rect getWindowInsets() {
        if (sRootInsetsCache.get() != null) {
            return sRootInsetsCache.get();
        }
        return mWindowInsets;
    }

    public int left() {
        return getWindowInsets().left;
    }

    public int top() {
        return ((mFitSide & FitBothSideBarView.SIDE_TOP) != 0 && mFitTopSideEnabled)
                ? getWindowInsets().top : 0;
    }

    public int right() {
        return getWindowInsets().right;
    }

    public int bottom() {
        return ((mFitSide & FitBothSideBarView.SIDE_BOTTOM) != 0 && mFitBottomSideEnabled)
                ? getWindowInsets().bottom : 0;
    }

    public void addFitSide(@FitBothSideBarView.FitSide int side) {
        if ((mFitSide & side) != 0) {
            mFitSide |= side;
            mTarget.requestLayout();
        }
    }

    public void removeFitSide(@FitBothSideBarView.FitSide int side) {
        if ((mFitSide & side) != 0) {
            mFitSide ^= side;
            mTarget.requestLayout();
        }
    }

    public void setFitSystemBarEnabled(boolean top, boolean bottom) {
        if (mFitTopSideEnabled != top || mFitBottomSideEnabled != bottom) {
            mFitTopSideEnabled = top;
            mFitBottomSideEnabled = bottom;
            mTarget.requestLayout();
        }
    }
}