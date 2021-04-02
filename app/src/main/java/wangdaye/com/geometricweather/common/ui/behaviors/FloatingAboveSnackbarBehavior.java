package wangdaye.com.geometricweather.common.ui.behaviors;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import wangdaye.com.geometricweather.common.snackbar.Snackbar;

public class FloatingAboveSnackbarBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    public FloatingAboveSnackbarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
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
