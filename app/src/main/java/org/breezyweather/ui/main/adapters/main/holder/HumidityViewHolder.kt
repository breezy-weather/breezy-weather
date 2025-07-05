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

package org.breezyweather.ui.main.adapters.main.holder

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import java.text.NumberFormat

class HumidityViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_humidity, parent, false)
) {
    private val titleView: TextView = itemView.findViewById(R.id.title)
    private val titleIconView: ImageView = itemView.findViewById(R.id.title_icon)
    private val humidityValueView: TextView = itemView.findViewById(R.id.humidity_value)
    private val wavesBackgroundView: ImageView = itemView.findViewById(R.id.waves_background)
    private val dewPointValueView: TextView = itemView.findViewById(R.id.dew_point_value)

    override fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val color = MainThemeColorProvider.getColor(location, R.attr.colorTitleText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            titleView.isAccessibilityHeading = true
        }
        titleView.setText(R.string.humidity)
        titleView.setTextColor(color)
        titleIconView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_humidity_percentage))
        titleIconView.setColorFilter(color)

        location.weather!!.current?.let { current ->
            current.relativeHumidity?.let { relativeHumidity ->
                humidityValueView.text = NumberFormat.getPercentInstance(context.currentLocale).apply {
                    maximumFractionDigits = 0
                }.format(relativeHumidity.div(100.0))

                if (relativeHumidity in 0.0..100.0) {
                    wavesBackgroundView.setImageDrawable(
                        AppCompatResources.getDrawable(
                            context,
                            when (relativeHumidity) {
                                in 0.0..20.0 -> R.drawable.humidity_percent_7
                                in 20.0..40.0 -> R.drawable.humidity_percent_30
                                in 60.0..80.0 -> R.drawable.humidity_percent_75
                                in 80.0..100.0 -> R.drawable.humidity_percent_90
                                else -> R.drawable.humidity_percent_50
                            }
                        )
                    )
                }
            }
            dewPointValueView.text = current.dewPoint?.let {
                SettingsManager.getInstance(context).temperatureUnit.getShortValueText(context, it)
            }
        }

        val talkBackBuilder = StringBuilder(titleView.text)
        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as GeoActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_HUMIDITY
            )
        }
    }
}
