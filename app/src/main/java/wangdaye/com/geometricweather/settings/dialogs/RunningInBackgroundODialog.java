package wangdaye.com.geometricweather.settings.dialogs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.common.utils.helpters.IntentHelper;

/**
 * Running in background O dialog.
 * */
@RequiresApi(api = Build.VERSION_CODES.O)
public class RunningInBackgroundODialog extends GeoDialog
        implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_running_in_background_o, container, false);
        initWidget(view);
        return view;
    }

    private void initWidget(View view) {
        view.findViewById(R.id.dialog_running_in_background_o_setNotificationGroupBtn).setOnClickListener(this);
        view.findViewById(R.id.dialog_running_in_background_o_ignoreBatteryOptBtn).setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
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
                IntentHelper.startBatteryOptimizationActivity(requireContext());
                break;
        }
    }

    @Override
    public ViewGroup getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_running_in_background_o_container);
    }
}
