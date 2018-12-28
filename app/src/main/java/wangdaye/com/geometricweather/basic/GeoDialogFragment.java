package wangdaye.com.geometricweather.basic;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;

/**
 * Geometric weather dialog fragment.
 * */

public abstract class GeoDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity != null) {
            activity.getDialogList().add(this);
        }
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GeoActivity activity = GeometricWeather.getInstance().getTopActivity();
        if (activity != null) {
            activity.getDialogList().remove(this);
        }
    }

    public abstract View getSnackbarContainer();
}
