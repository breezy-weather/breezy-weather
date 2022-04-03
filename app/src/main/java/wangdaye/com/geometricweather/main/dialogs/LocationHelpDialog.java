package wangdaye.com.geometricweather.main.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.main.MainActivity;

public class LocationHelpDialog extends GeoDialog {

    public static LocationHelpDialog getInstance() {
        return new LocationHelpDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_location_help, container, false);
        initWidget(view);
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void initWidget(View view) {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }

        view.findViewById(R.id.dialog_location_help_permissionContainer)
                .setOnClickListener(v -> IntentHelper.startApplicationDetailsActivity(getActivity()));

        view.findViewById(R.id.dialog_location_help_locationContainer)
                .setOnClickListener(v -> IntentHelper.startLocationSettingsActivity(requireContext()));

        view.findViewById(R.id.dialog_location_help_providerContainer)
                .setOnClickListener(v -> IntentHelper.startSelectProviderActivity(requireActivity()));

        view.findViewById(R.id.dialog_location_help_manageContainer).setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).setManagementFragmentVisibility(true);
            } else {
                IntentHelper.startMainActivityForManagement(requireActivity());
            }
            dismiss();
        });
        ((TextView) view.findViewById(R.id.dialog_location_help_manageTitle)).setText(
                getString(R.string.feedback_add_location_manually).replace(
                        "$", getString(R.string.current_location)
                )
        );
    }
}
