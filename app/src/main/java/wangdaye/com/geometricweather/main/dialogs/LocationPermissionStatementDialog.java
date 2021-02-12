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

@RequiresApi(api = Build.VERSION_CODES.M)
public class LocationPermissionStatementDialog extends GeoDialog {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_location_permission_statement, container, false);
        view.findViewById(R.id.dialog_location_permission_statement_nextButton).setOnClickListener(v -> {
            ((Callback) requireActivity()).requestLocationPermissions();
            dismiss();
        });
        return view;
    }

    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_location_permission_statement_container);
    }

    public interface Callback {
        @RequiresApi(api = Build.VERSION_CODES.M)
        void requestLocationPermissions();
    }
}
