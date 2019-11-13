package wangdaye.com.geometricweather.ui.widget.insets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FitBottomSystemBarRecyclerView extends RecyclerView {

    private float insetsBottom = 0;

    public FitBottomSystemBarRecyclerView(@NonNull Context context) {
        super(context);
    }

    public FitBottomSystemBarRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FitBottomSystemBarRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
