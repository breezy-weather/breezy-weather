package wangdaye.com.geometricweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.view.LayoutInflater;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.GeoDialog;
import wangdaye.com.geometricweather.utils.helpters.IntentHelper;

/**
 * Running in background dialog.
 * */
@RequiresApi(api = Build.VERSION_CODES.M)
public class RunningInBackgroundDialog extends GeoDialog {

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_running_in_background, null, false);
        view.findViewById(R.id.dialog_running_in_background_setBtn).setOnClickListener(v ->
                IntentHelper.startBatteryOptimizationActivity((GeoActivity) requireActivity()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }


    @Override
    public View getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_running_in_background_container);
    }
}
