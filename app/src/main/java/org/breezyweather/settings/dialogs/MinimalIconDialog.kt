package org.breezyweather.settings.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.R
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.providers.ResourceProvider

object MinimalIconDialog {
    fun show(
        context: Context,
        code: WeatherCode,
        daytime: Boolean,
        provider: ResourceProvider
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
        provider: ResourceProvider
    ) {
        val lightIconView = view.findViewById<AppCompatImageView>(R.id.dialog_minimal_icon_lightIcon)
        lightIconView.setImageDrawable(
            ResourceHelper.getWidgetNotificationIcon(
                provider, code, daytime, true, "light"
            )
        )
        val greyIconView = view.findViewById<AppCompatImageView>(R.id.dialog_minimal_icon_greyIcon)
        greyIconView.setImageDrawable(
            ResourceHelper.getWidgetNotificationIcon(
                provider, code, daytime, true, "grey"
            )
        )
        val darkIconView = view.findViewById<AppCompatImageView>(R.id.dialog_minimal_icon_darkIcon)
        darkIconView.setImageDrawable(
            ResourceHelper.getWidgetNotificationIcon(
                provider, code, daytime, true, "dark"
            )
        )
    }
}
