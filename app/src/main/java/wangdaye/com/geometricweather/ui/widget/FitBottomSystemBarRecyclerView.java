package wangdaye.com.geometricweather.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FitBottomSystemBarRecyclerView extends RecyclerView {

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
        Rect r = new Rect(insets);
        r.top = 0;
        return super.fitSystemWindows(r);
    }
}
