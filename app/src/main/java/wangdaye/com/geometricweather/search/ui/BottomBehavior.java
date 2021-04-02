package wangdaye.com.geometricweather.search.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import wangdaye.com.geometricweather.common.ui.behaviors.FloatingAboveSnackbarBehavior;
import wangdaye.com.geometricweather.common.ui.widgets.insets.both.FitSystemBarCoordinatorLayout;

public class BottomBehavior<V extends View> extends FloatingAboveSnackbarBehavior<V> {

    private float mBottomBarHeight;
    private float mTranslationY;

    public BottomBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBottomBarHeight = 0;
        mTranslationY = 0;
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
        mBottomBarHeight = ((FitSystemBarCoordinatorLayout) parent).getWindowInsets().bottom;
        setTranslationY(child);
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull V child,
                                          @NonNull View dependency) {
        mTranslationY = dependency.getY() - parent.getMeasuredHeight();
        setTranslationY(child);
        return false;
    }

    private void setTranslationY(View child) {
        child.setTranslationY(mTranslationY - mBottomBarHeight);
    }
}
