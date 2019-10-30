package wangdaye.com.geometricweather.ui.widget.trend.abs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class ChartItemView extends View {

    public ChartItemView(Context context) {
        super(context);
    }

    public ChartItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChartItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract int getMarginTop();

    public abstract int getMarginBottom();
}
