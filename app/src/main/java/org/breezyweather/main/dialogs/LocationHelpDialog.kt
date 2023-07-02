package org.breezyweather.main.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.main.MainActivity

object LocationHelpDialog {
    fun show(activity: Activity) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_location_help, null, false)
        initWidget(
            activity,
            view,
            MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.location_dialog_failed_to_locate_title)
                .setView(view)
                .show()
        )
    }

    @SuppressLint("SetTextI18n")
    private fun initWidget(activity: Activity, view: View, dialog: AlertDialog) {
        view.findViewById<View>(R.id.dialog_location_help_permissionContainer)
            .setOnClickListener { IntentHelper.startApplicationDetailsActivity(activity) }
        view.findViewById<View>(R.id.dialog_location_help_locationContainer)
            .setOnClickListener { IntentHelper.startLocationSettingsActivity(activity) }
        view.findViewById<View>(R.id.dialog_location_help_providerContainer)
            .setOnClickListener { IntentHelper.startSelectLocationProviderActivity(activity) }
        view.findViewById<View>(R.id.dialog_location_help_manageContainer).setOnClickListener {
            if (activity is MainActivity) {
                activity.setManagementFragmentVisibility(true)
            } else {
                IntentHelper.startMainActivityForManagement(activity)
            }
            dialog.dismiss()
        }
        view.findViewById<TextView>(R.id.dialog_location_help_manageTitle).text =
            activity.getString(R.string.location_dialog_failed_to_locate_action_add_manually).replace(
                "$", activity.getString(R.string.location_current)
            )
    }
}
