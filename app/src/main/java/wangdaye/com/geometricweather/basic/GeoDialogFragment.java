package wangdaye.com.geometricweather.basic;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;

/**
 * Geometric weather dialog fragment.
 * */

public abstract class GeoDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        GeometricWeather.getInstance().getTopActivity().getDialogList().add(this);
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GeometricWeather.getInstance().getTopActivity().getDialogList().remove(this);
    }

    public abstract View getSnackbarContainer();
}
