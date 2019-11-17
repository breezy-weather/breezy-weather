package wangdaye.com.geometricweather.main.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.dialog.GeoDialogFragment;
import wangdaye.com.geometricweather.main.ui.MainColorPicker;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class LocationHelpDialog extends GeoDialogFragment {

    private CoordinatorLayout container;
    private MainColorPicker colorPicker;

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
        if (getActivity() == null) {
            return;
        }

        this.container = view.findViewById(R.id.dialog_location_help_container);
        container.setBackgroundColor(colorPicker.getRootColor(getActivity()));

        ((TextView) view.findViewById(R.id.dialog_location_help_title)).setTextColor(
                colorPicker.getTextTitleColor(getActivity())
        );

        view.findViewById(R.id.dialog_location_help_permissionContainer)
                .setOnClickListener(v -> IntentHelper.startApplicationDetailsActivity(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_permissionTitle)).setTextColor(
                colorPicker.getTextContentColor(getActivity())
        );

        view.findViewById(R.id.dialog_location_help_locationContainer)
                .setOnClickListener(v -> IntentHelper.startLocationSettingsActivity(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_locationTitle)).setTextColor(
                colorPicker.getTextContentColor(getActivity())
        );

        view.findViewById(R.id.dialog_location_help_providerContainer)
                .setOnClickListener(v -> IntentHelper.startSelectProviderActivity(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_providerTitle)).setTextColor(
                colorPicker.getTextContentColor(getActivity())
        );
    }

    public LocationHelpDialog setColorPicker(@NonNull MainColorPicker colorPicker) {
        this.colorPicker = colorPicker;
        return this;
    }
}
