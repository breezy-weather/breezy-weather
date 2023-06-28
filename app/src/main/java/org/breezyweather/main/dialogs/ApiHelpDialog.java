package org.breezyweather.main.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.breezyweather.R;
import org.breezyweather.common.utils.helpers.IntentHelper;

public class ApiHelpDialog {

    public static void show(Activity activity, @StringRes int title, @StringRes int content) {
        View view = LayoutInflater
                .from(activity)
                .inflate(R.layout.dialog_api_help, null, false);
        ((TextView) view.findViewById(R.id.dialog_api_help_content)).setText(content);
        initWidget(
                activity,
                view,
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(title)
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
