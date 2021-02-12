package wangdaye.com.geometricweather.settings.dialogs;

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
import wangdaye.com.geometricweather.utils.helpters.IntentHelper;

/**
 * Running in background dialog.
 * */
@RequiresApi(api = Build.VERSION_CODES.M)
public class RunningInBackgroundDialog extends GeoDialog {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_running_in_background, container, false);
        view.findViewById(R.id.dialog_running_in_background_setBtn).setOnClickListener(v ->
                IntentHelper.startBatteryOptimizationActivity(requireActivity()));
        return view;
    }

    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_running_in_background_container);
    }
}
