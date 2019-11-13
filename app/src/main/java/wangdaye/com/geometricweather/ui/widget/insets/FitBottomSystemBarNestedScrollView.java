package wangdaye.com.geometricweather.ui.widget.insets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

public class FitBottomSystemBarNestedScrollView extends NestedScrollView {

    private float insetsBottom = 0;

    public FitBottomSystemBarNestedScrollView(@NonNull Context context) {
        super(context);
    }

    public FitBottomSystemBarNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FitBottomSystemBarNestedScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        insetsBottom = insets.bottom;

        Rect r = new Rect(insets);
        r.top = 0;
        super.fitSystemWindows(r);
        return false;
    }

    public float getInsetsBottom() {
        return insetsBottom;
    }
}
