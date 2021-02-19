package wangdaye.com.geometricweather.common.ui.widgets.insets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;

public class FitSystemBarAppBarLayout extends AppBarLayout {

    public FitSystemBarAppBarLayout(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitSystemBarAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        fitSystemBar(insets);
        return false;
    }

    private void fitSystemBar(Rect insets) {
        setPadding(0, insets.top, 0, 0);
    }
}
