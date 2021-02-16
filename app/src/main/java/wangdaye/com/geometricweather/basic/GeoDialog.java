package wangdaye.com.geometricweather.basic;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LifecycleEventObserver;

import wangdaye.com.geometricweather.R;

public abstract class GeoDialog extends DialogFragment {

    private boolean mForeground = false;

    public static void injectStyle(DialogFragment f) {
        f.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            switch (event) {
                case ON_CREATE:
                    f.setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog_MinWidth);
                    break;

                case ON_START:
                    f.requireDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                    break;
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        injectStyle(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mForeground = true;
        ((GeoActivity) requireActivity()).setTopDialog(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mForeground = false;
        ((GeoActivity) requireActivity()).checkToCleanTopDialog(this);
    }

    public abstract View getSnackbarContainer();

    public boolean isForeground() {
        return mForeground;
    }
}
