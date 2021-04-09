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

public class FitSystemBarSwipeRefreshLayout extends SwipeRefreshLayout {

    public FitSystemBarSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        fitSystemBar(new Rect(
                insets.getSystemWindowInsetLeft(),
                insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),
                insets.getSystemWindowInsetBottom()
        ));
        return insets;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
        fitSystemBar(insets);
        return false;
    }

    private void fitSystemBar(Rect insets) {
        int startPosition = insets.top + getResources().getDimensionPixelSize(R.dimen.normal_margin);
        setProgressViewOffset(
                false,
                startPosition,
                (int) (startPosition + 64 * getResources().getDisplayMetrics().density)
        );
    }
}
