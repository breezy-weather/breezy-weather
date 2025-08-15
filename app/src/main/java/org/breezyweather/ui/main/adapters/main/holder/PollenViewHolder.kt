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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.weather.index.PollenIndex
import org.breezyweather.domain.weather.index.PollenIndex.Companion.getNegligiblePollenColor
import org.breezyweather.domain.weather.index.PollenIndex.Companion.getNegligiblePollenText
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getIndexName
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.pollensWithConcentration
import org.breezyweather.ui.main.MainActivity
import org.breezyweather.ui.theme.resource.providers.ResourceProvider

class PollenViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_pollen, parent, false)
) {
    private val pollen1Container: RelativeLayout = itemView.findViewById(R.id.pollen_item_1)
    private val pollen1Icon: AppCompatImageView = itemView.findViewById(R.id.pollen_item_1_icon)
    private val pollen1Title: TextView = itemView.findViewById(R.id.pollen_item_1_title)
    private val pollen1Content: TextView = itemView.findViewById(R.id.pollen_item_1_content)
    private val pollen2Container: RelativeLayout = itemView.findViewById(R.id.pollen_item_2)
    private val pollen2Icon: AppCompatImageView = itemView.findViewById(R.id.pollen_item_2_icon)
    private val pollen2Title: TextView = itemView.findViewById(R.id.pollen_item_2_title)
    private val pollen2Content: TextView = itemView.findViewById(R.id.pollen_item_2_content)

    @SuppressLint("SetTextI18n")
    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        pollen1Container.visibility = View.GONE

        val talkBackBuilder = StringBuilder()
        location.weather?.today?.pollen?.let { pollen ->
            val primaryPollens = pollen.pollensWithConcentration
                .filter { it != PollenIndex.MOLD }
                .sortedByDescending { pollen.getIndex(it) }

            val pollenIndexSource = location.pollenSource?.let {
                (activity as MainActivity).sourceManager.getPollenIndexSource(it)
            }

            primaryPollens.getOrElse(0) { null }?.let { p ->
                talkBackBuilder.append(context.getString(R.string.pollen_primary))
                pollen1Container.visibility = View.VISIBLE
                pollen1Icon.setColorFilter(pollen.getColor(context, p, pollenIndexSource))
                pollen1Title.visibility = View.VISIBLE
                pollen1Title.text = pollen.getName(context, p)
                pollen1Title.setTextColor(context.getThemeColor(R.attr.colorTitleText))
                pollen1Content.text = pollen.getIndexName(context, p, pollenIndexSource)
                pollen1Content.setTextColor(context.getThemeColor(R.attr.colorBodyText))

                talkBackBuilder.append(context.getString(R.string.colon_separator))
                talkBackBuilder.append(pollen1Title.text)
                talkBackBuilder.append(context.getString(R.string.colon_separator))
                talkBackBuilder.append(pollen1Content.text)
            } ?: run {
                if (pollen.isValid) {
                    pollen1Container.visibility = View.VISIBLE
                    pollen1Icon.setColorFilter(getNegligiblePollenColor(context, pollenIndexSource))
                    pollen1Title.visibility = View.GONE
                    pollen1Content.text = getNegligiblePollenText(context, pollenIndexSource)
                    pollen1Content.setTextColor(context.getThemeColor(R.attr.colorBodyText))

                    talkBackBuilder.append(context.getString(R.string.pollen))
                    talkBackBuilder.append(context.getString(R.string.colon_separator))
                    talkBackBuilder.append(pollen1Content.text)
                } else {
                    pollen1Container.visibility = View.GONE
                }
            }

            primaryPollens.getOrElse(1) { null }?.let { p ->
                pollen2Container.visibility = View.VISIBLE
                pollen2Icon.setColorFilter(pollen.getColor(context, p, pollenIndexSource))
                pollen2Title.text = pollen.getName(context, p)
                pollen2Title.setTextColor(context.getThemeColor(R.attr.colorTitleText))
                pollen2Content.text = pollen.getIndexName(context, p, pollenIndexSource)
                pollen2Content.setTextColor(context.getThemeColor(R.attr.colorBodyText))

                talkBackBuilder.append(context.getString(org.breezyweather.unit.R.string.locale_separator))
                talkBackBuilder.append(pollen2Title.text)
                talkBackBuilder.append(context.getString(R.string.colon_separator))
                talkBackBuilder.append(pollen2Content.text)
            } ?: run {
                pollen2Container.visibility = View.GONE
            }
        } ?: run {
            talkBackBuilder.append(context.getString(R.string.pollen))
            pollen1Container.visibility = View.GONE
            pollen2Container.visibility = View.GONE
        }

        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as BreezyActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_POLLEN
            )
        }
    }
}
