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

package org.breezyweather.ui.settings.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import breezyweather.domain.weather.reference.WeatherCode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.R
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

object MinimalIconDialog {
    fun show(
        context: Context,
        code: WeatherCode,
        daytime: Boolean,
        provider: ResourceProvider,
    ) {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.dialog_minimal_icon, null, false)
        initWidget(view, code, daytime, provider)
        MaterialAlertDialogBuilder(context)
            .setTitle(code.name + if (daytime) "_DAY" else "_NIGHT")
            .setView(view)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun initWidget(
        view: View,
        code: WeatherCode,
        daytime: Boolean,
        provider: ResourceProvider,
    ) {
        val lightIconView = view.findViewById<AppCompatImageView>(R.id.dialog_minimal_icon_lightIcon)
        lightIconView.setImageDrawable(
            ResourceHelper.getWidgetNotificationIcon(provider, code, daytime, true, "light")
        )
        val greyIconView = view.findViewById<AppCompatImageView>(R.id.dialog_minimal_icon_greyIcon)
        greyIconView.setImageDrawable(
            ResourceHelper.getWidgetNotificationIcon(provider, code, daytime, true, "grey")
        )
        val darkIconView = view.findViewById<AppCompatImageView>(R.id.dialog_minimal_icon_darkIcon)
        darkIconView.setImageDrawable(
            ResourceHelper.getWidgetNotificationIcon(provider, code, daytime, true, "dark")
        )
    }
}
