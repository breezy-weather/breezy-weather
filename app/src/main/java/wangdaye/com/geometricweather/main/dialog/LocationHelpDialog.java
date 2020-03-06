package wangdaye.com.geometricweather.main.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.main.MainActivity;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

public class LocationHelpDialog extends DialogFragment {

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

    @SuppressLint("SetTextI18n")
    private void initWidget(View view) {
        if (getActivity() == null) {
            return;
        }

        CoordinatorLayout container = view.findViewById(R.id.dialog_location_help_container);
        container.setBackgroundColor(ThemeManager.getInstance(requireActivity()).getRootColor(getActivity()));

        ((TextView) view.findViewById(R.id.dialog_location_help_title)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextTitleColor(getActivity()));

        view.findViewById(R.id.dialog_location_help_permissionContainer)
                .setOnClickListener(v -> IntentHelper.startApplicationDetailsActivity(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_permissionTitle)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextContentColor(getActivity()));

        view.findViewById(R.id.dialog_location_help_locationContainer)
                .setOnClickListener(v -> IntentHelper.startLocationSettingsActivity(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_locationTitle)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextContentColor(getActivity()));

        view.findViewById(R.id.dialog_location_help_providerContainer)
                .setOnClickListener(v -> IntentHelper.startSelectProviderActivity(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_providerTitle)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextContentColor(getActivity()));

        view.findViewById(R.id.dialog_location_help_manageContainer).setOnClickListener(v ->
                IntentHelper.startManageActivityForResult(getActivity(), MainActivity.MANAGE_ACTIVITY)
        );
        ((TextView) view.findViewById(R.id.dialog_location_help_manageTitle)).setTextColor(
                ThemeManager.getInstance(requireActivity()).getTextContentColor(getActivity()));
        ((TextView) view.findViewById(R.id.dialog_location_help_manageTitle)).setText(
                getString(R.string.feedback_add_location_manually).replace(
                        "$", getString(R.string.current_location)
                )
        );
    }
}
