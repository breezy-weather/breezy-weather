/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.main.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.utils.helpers.IntentHelper

object ApiHelpDialog {
    fun show(
        activity: Activity,
        @StringRes title: Int,
        @StringRes content: Int,
    ) {
        val view = LayoutInflater
            .from(activity)
            .inflate(R.layout.dialog_api_help, null, false)
        view.findViewById<TextView>(R.id.dialog_api_help_content).text = activity.getString(content)
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
        if (BuildConfig.FLAVOR != "freenet") {
            view.findViewById<View>(R.id.dialog_location_help_providerContainer)
                .setOnClickListener { IntentHelper.startWeatherProviderSettingsActivity(activity) }
        } else {
            view.findViewById<View>(R.id.dialog_location_help_providerContainer)
                .visibility = View.GONE
        }
    }
}
