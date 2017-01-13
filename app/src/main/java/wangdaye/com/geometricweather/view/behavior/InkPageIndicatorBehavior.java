package wangdaye.com.geometricweather.view.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.view.widget.InkPageIndicator;

/**
 * Ink page indicator behavior.
 * */

public class InkPageIndicatorBehavior<V extends InkPageIndicator> extends CoordinatorLayout.Behavior<V> {

    public InkPageIndicatorBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        int marginBottom = (int) DisplayUtils.dpToPx(parent.getContext(), 16);
        child.layout(0,
                parent.getMeasuredHeight() - child.getMeasuredHeight() - marginBottom,
                parent.getMeasuredWidth(),
                parent.getMeasuredHeight() - marginBottom);
        return true;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        child.setTranslationY(dependency.getY() - parent.getMeasuredHeight());
        return false;
    }
}
