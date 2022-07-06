package wangdaye.com.geometricweather.main.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;

public class ApiKeyHelpDialog {

    public static void show(Activity activity) {
        View view = LayoutInflater
                .from(activity)
                .inflate(R.layout.dialog_api_help, null, false);
        initWidget(
                activity,
                view,
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.feedback_api_help_title)
                        .setView(view)
                        .show()
        );
    }

    @SuppressLint("SetTextI18n")
    private static void initWidget(Activity activity, View view, AlertDialog dialog) {
        view.findViewById(R.id.dialog_location_help_providerContainer)
                .setOnClickListener(v -> IntentHelper.startSelectProviderActivity(activity));
    }
}
