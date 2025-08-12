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
import android.view.ViewGroup
import android.widget.TextView
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.BreezyActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.basic.models.options.basic.UnitUtils
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

class VisibilityViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_visibility, parent, false)
) {
    private val visibilityValueView: TextView = itemView.findViewById(R.id.visibility_value)
    private val visibilityDescriptionView: TextView = itemView.findViewById(R.id.visibility_description)

    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val talkBackBuilder = StringBuilder(context.getString(R.string.visibility))

        location.weather!!.current?.visibility?.let { visibility ->
            val distanceUnit = SettingsManager.getInstance(context).getDistanceUnit(context)
            visibilityValueView.text = UnitUtils.formatUnitsHalfSize(
                distanceUnit.formatMeasure(context, visibility)
            )
            visibilityDescriptionView.text = DistanceUnit.getVisibilityDescription(context, visibility)

            talkBackBuilder.append(context.getString(R.string.colon_separator))
            talkBackBuilder.append(distanceUnit.formatContentDescription(context, visibility))
            talkBackBuilder.append(context.getString(org.breezyweather.unit.R.string.locale_separator))
            talkBackBuilder.append(visibilityValueView.text)
        }

        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as BreezyActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_VISIBILITY
            )
        }
    }
}
