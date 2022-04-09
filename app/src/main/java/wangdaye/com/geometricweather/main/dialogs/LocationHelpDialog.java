package wangdaye.com.geometricweather.main.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.main.MainActivity;

public class LocationHelpDialog {

    public static void show(Activity activity) {
        View view = LayoutInflater
                .from(activity)
                .inflate(R.layout.dialog_location_help, null, false);
        initWidget(
                activity,
                view,
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.feedback_location_help_title)
                        .setView(view)
                        .show()
        );
    }

    @SuppressLint("SetTextI18n")
    private static void initWidget(Activity activity, View view, AlertDialog dialog) {
        view.findViewById(R.id.dialog_location_help_permissionContainer)
                .setOnClickListener(v -> IntentHelper.startApplicationDetailsActivity(activity));

        view.findViewById(R.id.dialog_location_help_locationContainer)
                .setOnClickListener(v -> IntentHelper.startLocationSettingsActivity(activity));

        view.findViewById(R.id.dialog_location_help_providerContainer)
                .setOnClickListener(v -> IntentHelper.startSelectProviderActivity(activity));

        view.findViewById(R.id.dialog_location_help_manageContainer).setOnClickListener(v -> {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).setManagementFragmentVisibility(true);
            } else {
                IntentHelper.startMainActivityForManagement(activity);
            }

            dialog.dismiss();
        });
        ((TextView) view.findViewById(R.id.dialog_location_help_manageTitle)).setText(
                activity.getString(R.string.feedback_add_location_manually).replace(
                        "$", activity.getString(R.string.current_location)
                )
        );
    }
}
