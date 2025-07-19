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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getContentDescription
import org.breezyweather.domain.weather.model.getDirection
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

class WindViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_wind, parent, false)
) {
    private val windDirectionView: ImageView = itemView.findViewById(R.id.wind_direction)
    private val windSpeedValueView: TextView = itemView.findViewById(R.id.wind_speed_value)
    private val windDetailView: TextView = itemView.findViewById(R.id.visibility_description)

    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val talkBackBuilder = StringBuilder(context.getString(R.string.wind))

        location.weather?.current?.wind?.let { wind ->
            talkBackBuilder.append(context.getString(R.string.colon_separator))

            val speedUnit = SettingsManager.getInstance(context).getSpeedUnit(context)
            talkBackBuilder.append(wind.getContentDescription(context, speedUnit))

            wind.speed?.let { speed ->
                windSpeedValueView.text = UnitUtils.formatUnitsHalfSize(
                    speedUnit.formatMeasureShort(context, speed, isValueInDefaultUnit = true)
                )
            }

            wind.degree?.let { degree ->
                if (degree != -1.0) {
                    windDirectionView.visibility = View.VISIBLE
                    windDirectionView.setImageDrawable(
                        AppCompatResources.getDrawable(context, R.drawable.wind_arrow)
                    )
                    windDirectionView.rotation = degree.toFloat()
                } else {
                    windDirectionView.visibility = View.VISIBLE
                    windDirectionView.setImageDrawable(
                        AppCompatResources.getDrawable(context, R.drawable.wind_variable)
                    )
                }
            } ?: run {
                windDirectionView.visibility = View.GONE
            }

            windDetailView.text = if (wind.speed != null && wind.gusts != null && wind.gusts!! > wind.speed!!) {
                context.getString(R.string.wind_gusts_short) +
                    context.getString(R.string.colon_separator) +
                    speedUnit.formatMeasureShort(context, wind.gusts!!, isValueInDefaultUnit = true)
            } else {
                wind.getDirection(context, short = true)?.let {
                    if (wind.degree!! in 0.0..360.0) {
                        context.getString(R.string.wind_origin, it)
                    } else {
                        it
                    }
                } ?: ""
            }
        }

        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as BreezyActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_WIND
            )
        }
    }
}
