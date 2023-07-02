package org.breezyweather.common.basic

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import org.breezyweather.common.snackbar.SnackbarContainer

open class GeoFragment : Fragment() {
    var isFragmentViewCreated = false
        private set

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFragmentViewCreated = true
    }

    val isFragmentCreated: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    val isFragmentStarted: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    val isFragmentResumed: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    val snackbarContainer: SnackbarContainer
        get() = SnackbarContainer(this, (requireView() as ViewGroup), true)
}
