package wangdaye.com.geometricweather.common.ui.widgets.insets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import wangdaye.com.geometricweather.R;

public class FitSystemBarSwipeRefreshLayout extends SwipeRefreshLayout {

    public FitSystemBarSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFitsSystemWindows(false);
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
            fitSystemBar(
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
