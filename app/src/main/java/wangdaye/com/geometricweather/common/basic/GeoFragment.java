package wangdaye.com.geometricweather.common.basic;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import wangdaye.com.geometricweather.common.snackbar.SnackbarContainer;

public class GeoFragment extends Fragment {

    public SnackbarContainer getSnackbarContainer() {
        return new SnackbarContainer(this, (ViewGroup) getView(), true);
    }
}
