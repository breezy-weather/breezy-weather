package wangdaye.com.geometricweather.common.basic.insets;

import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.RequiresApi;

public class FitBothSideBarHelper {

    private final View mTarget;

    private Rect mWindowInsets;

    private @FitBothSideBarView.FitSide int mFitSide;
    private boolean mFitTopSideEnabled;
    private boolean mFitBottomSideEnabled;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mWindowInsets = new Rect(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom()
        );
        mTarget.requestLayout();
        return insets;
    }

    public boolean fitSystemWindows(Rect r) {
        mWindowInsets = r;
        mTarget.requestLayout();
        return false;
    }

    public Rect getWindowInsets() {
        return mWindowInsets;
    }

    public int left() {
        return mWindowInsets.left;
    }

    public int top() {
        return ((mFitSide & FitBothSideBarView.SIDE_TOP) != 0 && mFitTopSideEnabled)
                ? mWindowInsets.top : 0;
    }

    public int right() {
        return mWindowInsets.right;
    }

    public int bottom() {
        return ((mFitSide & FitBothSideBarView.SIDE_BOTTOM) != 0 && mFitBottomSideEnabled)
                ? mWindowInsets.bottom : 0;
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