package wangdaye.com.geometricweather.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialogFragment;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class LocationHelpDialog extends GeoDialogFragment {

    private CoordinatorLayout container;

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_location_help, null, false);
        initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @SuppressLint("SetTextI18n")
    private void initWidget(View view) {
        this.container = view.findViewById(R.id.dialog_location_help_container);

        view.findViewById(R.id.dialog_location_help_permissionContainer)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IntentHelper.startApplicationDetailsActivity(getActivity());
                    }
                });

        view.findViewById(R.id.dialog_location_help_locationContainer)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IntentHelper.startLocationSettingsActivity(getActivity());
                    }
                });

        view.findViewById(R.id.dialog_location_help_providerContainer)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IntentHelper.startSelectProviderActivity(getActivity());
                    }
                });
    }
}
