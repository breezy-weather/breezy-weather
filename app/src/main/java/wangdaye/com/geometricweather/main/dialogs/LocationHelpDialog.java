package wangdaye.com.geometricweather.main.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoDialog;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.utils.helpters.IntentHelper;
import wangdaye.com.geometricweather.utils.managers.ThemeManager;

public class LocationHelpDialog extends GeoDialog {

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
        CoordinatorLayout container = view.findViewById(R.id.dialog_location_help_container);
        container.setBackgroundColor(ThemeManager.getInstance(requireActivity()).getRootColor(getActivity()));

        ((TextView) view.findViewById(R.id.dialog_location_help_title)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextTitleColor(getActivity()));

        view.findViewById(R.id.dialog_location_help_permissionContainer)
                .setOnClickListener(v -> IntentHelper.startApplicationDetailsActivity(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_permissionTitle)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextContentColor(getActivity()));

        view.findViewById(R.id.dialog_location_help_locationContainer)
                .setOnClickListener(v -> IntentHelper.startLocationSettingsActivity(requireContext()));
        ((TextView) view.findViewById(R.id.dialog_location_help_locationTitle)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextContentColor(getActivity()));

        view.findViewById(R.id.dialog_location_help_providerContainer)
                .setOnClickListener(v -> IntentHelper.startSelectProviderActivity(requireActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_providerTitle)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextContentColor(getActivity()));

        view.findViewById(R.id.dialog_location_help_manageContainer).setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).setManagementFragmentVisibility(true);
            } else {
                IntentHelper.startMainActivityForManagement(requireActivity());
            }
            dismiss();
        });
        ((TextView) view.findViewById(R.id.dialog_location_help_manageTitle)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextContentColor(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_manageTitle)).setText(
                getString(R.string.feedback_add_location_manually).replace(
                        "$", getString(R.string.current_location)
                )
        );
    }

    @Override
    public ViewGroup getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_location_help_container);
    }
}
