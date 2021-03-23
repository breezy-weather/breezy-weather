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
import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.main.utils.MainPalette;

public class LocationHelpDialog extends GeoDialog {

    private static final String KEY_PALETTE = "palette";

    public static LocationHelpDialog getInstance(MainPalette palette) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_PALETTE, palette);

        LocationHelpDialog dialog = new LocationHelpDialog();
        dialog.setArguments(bundle);
        return dialog;
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

        MainPalette palette = bundle.getParcelable(KEY_PALETTE);
        if (palette == null) {
            return;
        }

        CoordinatorLayout container = view.findViewById(R.id.dialog_location_help_container);
        container.setBackgroundColor(palette.rootColor);

        ((TextView) view.findViewById(R.id.dialog_location_help_title)).setTextColor(palette.titleColor);

        view.findViewById(R.id.dialog_location_help_permissionContainer)
                .setOnClickListener(v -> IntentHelper.startApplicationDetailsActivity(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_permissionTitle)).setTextColor(palette.contentColor);

        view.findViewById(R.id.dialog_location_help_locationContainer)
                .setOnClickListener(v -> IntentHelper.startLocationSettingsActivity(requireContext()));
        ((TextView) view.findViewById(R.id.dialog_location_help_locationTitle)).setTextColor(palette.contentColor);

        view.findViewById(R.id.dialog_location_help_providerContainer)
                .setOnClickListener(v -> IntentHelper.startSelectProviderActivity(requireActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_providerTitle)).setTextColor(palette.contentColor);

        view.findViewById(R.id.dialog_location_help_manageContainer).setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).setManagementFragmentVisibility(true);
            } else {
                IntentHelper.startMainActivityForManagement(requireActivity());
            }
            dismiss();
        });
        ((TextView) view.findViewById(R.id.dialog_location_help_manageTitle)).setTextColor(palette.contentColor);
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
