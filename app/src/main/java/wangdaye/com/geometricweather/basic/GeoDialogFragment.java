package wangdaye.com.geometricweather.basic;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.View;

/**
 * Geometric weather dialog fragment.
 * */

public abstract class GeoDialogFragment extends DialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity instanceof GeoActivity) {
            ((GeoActivity) activity).getDialogList().add(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Activity activity = getActivity();
        if (activity instanceof GeoActivity) {
            ((GeoActivity) activity).getDialogList().remove(this);
        }
    }

    public abstract View getSnackbarContainer();
}
