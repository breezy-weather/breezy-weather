package org.breezyweather.common.ui.behaviors

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import org.breezyweather.common.snackbar.Snackbar

class FloatingAboveSnackbarBehavior<V : View>(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<V>(context, attrs) {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        child.translationY = dependency.y - parent.measuredHeight
        return false
    }
}
