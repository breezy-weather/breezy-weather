package org.breezyweather.common.basic;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import org.breezyweather.common.snackbar.SnackbarContainer;

public class GeoFragment extends Fragment {

    private boolean mViewCreated = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewCreated = true;
    }

    public boolean isFragmentCreated() {
        return getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED);
    }

    public boolean isFragmentViewCreated() {
        return mViewCreated;
    }

    public boolean isFragmentStarted() {
        return getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }

    public boolean isFragmentResumed() {
        return getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED);
    }

    public SnackbarContainer getSnackbarContainer() {
        return new SnackbarContainer(this, (ViewGroup) requireView(), true);
    }
}
