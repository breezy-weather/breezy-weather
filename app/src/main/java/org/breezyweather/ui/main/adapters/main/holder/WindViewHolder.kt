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
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getDirection
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

class WindViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_wind, parent, false)
) {
    private val titleView: TextView = itemView.findViewById(R.id.title)
    private val titleIconView: ImageView = itemView.findViewById(R.id.title_icon)
    private val windDirectionView: ImageView = itemView.findViewById(R.id.wind_direction)
    private val windSpeedValueView: TextView = itemView.findViewById(R.id.wind_speed_value)
    private val windDetailView: TextView = itemView.findViewById(R.id.wind_detail)

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
        titleView.setText(R.string.wind)
        titleView.setTextColor(color)
        titleIconView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_wind))
        titleIconView.setColorFilter(color)

        val talkBackBuilder = StringBuilder(titleView.text)
        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as GeoActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_WIND
            )
        }

        location.weather?.current?.wind?.let { wind ->
            wind.degree?.let { degree ->
                windDirectionView.visibility = View.VISIBLE
                windDirectionView.setImageDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.wind_arrow)
                )
                windDirectionView.rotation = degree.toFloat()
            } ?: run {
                windDirectionView.visibility = View.GONE
            }

            val speedUnit = SettingsManager.getInstance(context).speedUnit
            wind.speed?.let { speed ->
                val formattedSpeed = speedUnit.getValueText(context, speed)
                val spannableString = SpannableString(formattedSpeed)
                spannableString.setSpan(
                    RelativeSizeSpan(0.5f),
                    speedUnit.getValueTextWithoutUnit(context, speed).length,
                    formattedSpeed.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                windSpeedValueView.text = spannableString
            }

            windDetailView.text = if (wind.speed != null && wind.gusts != null && wind.gusts!! > wind.speed!!) {
                context.getString(R.string.wind_gusts_short) +
                    context.getString(R.string.colon_separator) +
                    speedUnit.getValueText(context, wind.gusts!!)
            } else {
                wind.getDirection(context, short = true)?.let {
                    context.getString(R.string.wind_origin) + context.getString(R.string.colon_separator) + it
                } ?: ""
            }
        }
    }
}
