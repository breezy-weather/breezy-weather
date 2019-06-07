package wangdaye.com.geometricweather.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Navigation bar view.
 *
 * */

public class NavigationBarView extends View {

    public NavigationBarView(Context context) {
        super(context);
    }

    public NavigationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                DisplayUtils.getNavigationBarHeight(getResources())
        );
    }
}