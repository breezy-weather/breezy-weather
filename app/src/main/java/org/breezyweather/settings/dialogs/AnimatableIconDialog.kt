package org.breezyweather.settings.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.breezyweather.R
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.ui.widgets.AnimatableIconView
import org.breezyweather.theme.resource.ResourceHelper
import org.breezyweather.theme.resource.providers.ResourceProvider

object AnimatableIconDialog {
    fun show(
        context: Context,
        code: WeatherCode,
        daytime: Boolean,
        provider: ResourceProvider
    ) {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.dialog_animatable_icon, null, false)
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
        val iconView = view.findViewById<AnimatableIconView>(R.id.dialog_animatable_icon_icon)
        iconView.setAnimatableIcon(
            ResourceHelper.getWeatherIcons(provider, code, daytime),
            ResourceHelper.getWeatherAnimators(provider, code, daytime)
        )
        val container = view.findViewById<FrameLayout>(R.id.dialog_animatable_icon_container)
        container.setOnClickListener { iconView.startAnimators() }
    }
}
