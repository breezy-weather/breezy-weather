package wangdaye.com.geometricweather.common.ui.widgets.insets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarHelper;
import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarView;

public class FitSystemBarSwipeRefreshLayout extends SwipeRefreshLayout
        implements FitBothSideBarView {

    private final FitBothSideBarHelper mHelper;

    public FitSystemBarSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mHelper = new FitBothSideBarHelper(this, SIDE_TOP);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return mHelper.onApplyWindowInsets(insets, this::fitSystemBar);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        return mHelper.fitSystemWindows(insets, this::fitSystemBar);
    }

    private void fitSystemBar() {
        int startPosition = mHelper.top() + getResources().getDimensionPixelSize(R.dimen.normal_margin);
        int endPosition = (int) (startPosition + 64 * getResources().getDisplayMetrics().density);

        if (startPosition != getProgressViewStartOffset()
                || endPosition != getProgressViewEndOffset()) {
            setProgressViewOffset(false, startPosition, endPosition);
        }
    }

    @Override
    public void addFitSide(@FitSide int side) {
        // do nothing.
    }

    @Override
    public void removeFitSide(@FitSide int side) {
        // do nothing.
    }

    @Override
    public void setFitSystemBarEnabled(boolean top, boolean bottom) {
        mHelper.setFitSystemBarEnabled(top, bottom);
    }

    @Override
    public int getTopWindowInset() {
        return mHelper.top();
    }

    @Override
    public int getBottomWindowInset() {
        return 0;
    }
}
