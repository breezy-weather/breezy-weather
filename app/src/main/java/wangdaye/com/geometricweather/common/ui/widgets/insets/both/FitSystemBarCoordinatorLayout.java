package wangdaye.com.geometricweather.common.ui.widgets.insets.both;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import wangdaye.com.geometricweather.common.ui.widgets.insets.Utils;

public class FitSystemBarCoordinatorLayout extends CoordinatorLayout
        implements FitBothSideBarView {

    private Rect mInnerInsets = new Rect(0, 0, 0, 0);
    private final Rect mWindowInsets = new Rect(0, 0, 0, 0);

    private boolean mFitTopSideEnabled = true;
    private boolean mFitBottomSideEnabled = true;

    public FitSystemBarCoordinatorLayout(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitSystemBarCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewCompat.setOnApplyWindowInsetsListener(this, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
        super.setOnApplyWindowInsetsListener((v, insets) -> {
            if (listener != null) {
                return listener.onApplyWindowInsets(v, insets);
            }

            Rect waterfull = Utils.getWaterfullInsets(insets);
            fitSystemWindows(
                    new Rect(
                            insets.getSystemWindowInsetLeft() + waterfull.left,
                            insets.getSystemWindowInsetTop() + waterfull.top,
                            insets.getSystemWindowInsetRight() + waterfull.right,
                            insets.getSystemWindowInsetBottom() + waterfull.bottom
                    )
            );
            return insets;
        });
    }

    @Override
    public boolean fitSystemWindows(Rect insets) {
        mInnerInsets = insets;
        ensureInsets();
        requestLayout();
        return false;
    }

    private void ensureInsets() {
        mWindowInsets.set(
                mInnerInsets.left,
                mFitTopSideEnabled ? mInnerInsets.top : 0,
                mInnerInsets.right,
                mFitBottomSideEnabled ? mInnerInsets.bottom : 0
        );
    }

    public Rect getWindowInsets() {
        return mWindowInsets;
    }

    @Override
    public void setFitSystemBarEnabled(boolean top, boolean bottom) {
        if (mFitTopSideEnabled != top || mFitBottomSideEnabled != bottom) {
            mFitTopSideEnabled = top;
            mFitBottomSideEnabled = bottom;
            ensureInsets();
            requestLayout();
        }
    }
}
