package org.breezyweather.main.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.IntentHelper

object ApiHelpDialog {
    fun show(
        activity: Activity,
        @StringRes title: Int,
        @StringRes content: Int
    ) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_api_help, null, false)
        view.findViewById<TextView>(R.id.dialog_api_help_content).setText(content)
        initWidget(
            activity,
            view,
            MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setView(view)
                .show()
        )
    }

    @SuppressLint("SetTextI18n")
    private fun initWidget(activity: Activity, view: View, dialog: AlertDialog) {
        view.findViewById<View>(R.id.dialog_location_help_providerContainer)
            .setOnClickListener { IntentHelper.startSelectWeatherProviderActivity(activity) }
    }
}
