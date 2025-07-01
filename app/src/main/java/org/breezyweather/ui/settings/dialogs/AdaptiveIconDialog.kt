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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import breezyweather.domain.weather.model.WeatherCode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import james.adaptiveicon.AdaptiveIcon
import james.adaptiveicon.AdaptiveIconView
import org.breezyweather.R
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import java.util.Random
import androidx.core.graphics.drawable.toDrawable

object AdaptiveIconDialog {
    fun show(
        context: Context,
        code: WeatherCode,
        daytime: Boolean,
        provider: ResourceProvider,
    ) {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.dialog_adaptive_icon, null, false)
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
        val iconView = view.findViewById<AdaptiveIconView>(R.id.dialog_adaptive_icon_icon)
        iconView.icon = AdaptiveIcon(
            ResourceHelper.getShortcutsForegroundIcon(provider, code, daytime),
            Color.TRANSPARENT.toDrawable(),
            0.5
        )
        iconView.setPath(Random().nextInt(AdaptiveIconView.PATH_TEARDROP + 1))
    }
}
