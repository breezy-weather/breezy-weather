package wangdaye.com.geometricweather.basic.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Geometric weather bottom sheet dialog fragment.
 * */
public abstract class GeoBottomSheetDialogFragment extends BottomSheetDialogFragment
        implements IGeoDialogFragment {

    private BottomSheetBehavior behavior;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        if (window != null) {
            window.getDecorView().setFitsSystemWindows(false);

            boolean darkMode = DisplayUtils.isDarkMode(requireActivity());
            DisplayUtils.setSystemBarStyle(requireActivity(), window,
                    false, false, true, !darkMode);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

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

    public void setBehavior(BottomSheetBehavior behavior) {
        this.behavior = behavior;
    }

    public BottomSheetBehavior getBehavior() {
        return behavior;
    }
}
