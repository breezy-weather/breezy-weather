package wangdaye.com.geometricweather.main.dialogs;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialog;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class BackgroundLocationDialog extends GeoDialog {

    public interface Callback {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        void requestBackgroundLocationPermission();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_background_location, container, false);
        view.findViewById(R.id.dialog_background_location_setButton).setOnClickListener(v -> {
            ((Callback) requireActivity()).requestBackgroundLocationPermission();
            dismiss();
        });
        return view;
    }

    @Override
    public ViewGroup getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_background_location_container);
    }
}
