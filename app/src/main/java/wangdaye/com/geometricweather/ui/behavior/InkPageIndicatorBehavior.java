package wangdaye.com.geometricweather.ui.behavior;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import android.util.AttributeSet;
import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.ui.widget.InkPageIndicator;

/**
 * Ink page indicator behavior.
 * */

public class InkPageIndicatorBehavior<V extends InkPageIndicator> extends CoordinatorLayout.Behavior<V> {

    public InkPageIndicatorBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull V child,
                                 int layoutDirection) {
        int marginBottom = (int) (
                DisplayUtils.dpToPx(parent.getContext(), 16)
                        + GeometricWeather.getInstance().getWindowInsets().bottom
        );
        child.layout(0,
                parent.getMeasuredHeight() - child.getMeasuredHeight() - marginBottom,
                parent.getMeasuredWidth(),
                parent.getMeasuredHeight() - marginBottom);
        return true;
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull V child,
                                   @NonNull View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull V child,
                                          @NonNull View dependency) {
        child.setTranslationY(dependency.getY() - parent.getMeasuredHeight());
        return false;
    }
}
