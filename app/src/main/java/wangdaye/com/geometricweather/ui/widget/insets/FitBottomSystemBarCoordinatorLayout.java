package wangdaye.com.geometricweather.ui.widget.insets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class FitBottomSystemBarCoordinatorLayout extends CoordinatorLayout {

    private float insetsBottom = 0;

    public FitBottomSystemBarCoordinatorLayout(@NonNull Context context) {
        super(context);
    }

    public FitBottomSystemBarCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FitBottomSystemBarCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        insetsBottom = insets.bottom;
        setPadding(0, 0, 0, (int) insetsBottom);
        return false;
    }

    public float getInsetsBottom() {
        return insetsBottom;
    }
}
