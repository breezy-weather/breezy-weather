package wangdaye.com.geometricweather.ui.widget.trendView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Trend item container layout.
 * */

public class TrendItemContainerLayout extends LinearLayout {

    public TrendItemContainerLayout(Context context) {
        super(context);
    }

    public TrendItemContainerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TrendItemContainerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int targetWidth = (int) ((getResources().getDisplayMetrics().widthPixels
                - 2 * getResources().getDimensionPixelSize(R.dimen.little_margin)) / 5f);
        targetWidth = (int) Math.max(DisplayUtils.dpToPx(getContext(), 56), targetWidth);
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(targetWidth, MeasureSpec.EXACTLY),
                heightMeasureSpec);
    }
}
