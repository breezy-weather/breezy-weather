package wangdaye.com.geometricweather.basic.dialog;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import wangdaye.com.geometricweather.basic.GeoActivity;

/**
 * Geometric weather dialog fragment.
 * */

public abstract class GeoDialogFragment extends DialogFragment
        implements IGeoDialogFragment {

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
}
