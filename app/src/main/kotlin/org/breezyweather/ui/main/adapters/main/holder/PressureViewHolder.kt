/*
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
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.formatMeasure
import org.breezyweather.common.extensions.formatValue
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.common.widgets.ArcProgress
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.unit.formatting.UnitWidth

class PressureViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_pressure, parent, false)
) {
    private val pressureValueView: TextView = itemView.findViewById(R.id.pressure_value)
    private val pressureUnitView: TextView = itemView.findViewById(R.id.pressure_unit)
    private val pressureProgress: ArcProgress = itemView.findViewById(R.id.pressure_progress)
    private var mPressure = 963f
    private var mEnable = false

    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val talkBackBuilder = StringBuilder(context.getString(R.string.pressure))
        location.weather!!.current?.pressure?.let {
            val pressureUnit = SettingsManager.getInstance(context).getPressureUnit(context)
            mPressure = it.inHectopascals.toFloat()
            mEnable = true
            pressureProgress.apply {
                progress = mPressure.minus(963f)
                pressureValueView.text = it.formatValue(context)
            }
            val pressureColor = context.getThemeColor(androidx.appcompat.R.attr.colorPrimary)
            pressureProgress.apply {
                setProgressColor(pressureColor)
                setArcBackgroundColor(ColorUtils.setAlphaComponent(pressureColor, (255 * 0.1).toInt()))
                max = 100f
            }

            pressureUnitView.text = pressureUnit.getNominativeUnit(context)
            talkBackBuilder.append(context.getString(R.string.colon_separator))
            talkBackBuilder.append(it.formatMeasure(context, unitWidth = UnitWidth.LONG))
        }

        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as BreezyActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_PRESSURE
            )
        }
    }
}
