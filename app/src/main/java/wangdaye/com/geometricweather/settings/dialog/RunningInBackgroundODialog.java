package wangdaye.com.geometricweather.settings.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

/**
 * Running in background O dialog.
 * */
@RequiresApi(api = Build.VERSION_CODES.O)
public class RunningInBackgroundODialog extends DialogFragment
        implements View.OnClickListener {

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_running_in_background_o, null, false);
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    private void initWidget(View view) {
        view.findViewById(R.id.dialog_running_in_background_o_setNotificationGroupBtn).setOnClickListener(this);
        view.findViewById(R.id.dialog_running_in_background_o_ignoreBatteryOptBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_running_in_background_o_setNotificationGroupBtn:
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().getPackageName());
                requireActivity().startActivity(intent);
                break;

            case R.id.dialog_running_in_background_o_ignoreBatteryOptBtn:
                IntentHelper.startBatteryOptimizationActivity((GeoActivity) requireActivity());
                break;
        }
    }
}
