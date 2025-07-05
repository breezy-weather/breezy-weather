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
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.WeatherCode
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.basic.models.options.appearance.DetailScreen
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import java.util.Calendar
import java.util.Date

class PrecipitationViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_precipitation, parent, false)
) {
    private val titleView: TextView = itemView.findViewById(R.id.title)
    private val titleIconView: ImageView = itemView.findViewById(R.id.title_icon)
    private val precipitationValueView: TextView = itemView.findViewById(R.id.precipitation_value)
    private val precipitationAmountView: TextView = itemView.findViewById(R.id.precipitation_amount)
    private val precipitationIconView: ImageView = itemView.findViewById(R.id.precipitation_icon)

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
        titleView.setText(R.string.precipitation)
        titleView.setTextColor(color)
        titleIconView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_precipitation))
        titleIconView.setColorFilter(color)

        location.weather?.let { weather ->
            val cal = Date().toCalendarWithTimeZone(location.javaTimeZone)
            val currentHour = cal[Calendar.HOUR_OF_DAY]

            val precipitationUnit = SettingsManager.getInstance(context).precipitationUnit

            // Early morning
            val precipitation = if (currentHour < 5) {
                weather.dailyForecast.getOrElse(weather.todayIndex!!.minus(1)) { null }?.night?.precipitation
            } else if (currentHour < 17) {
                weather.today?.day?.precipitation
            } else {
                weather.today?.night?.precipitation
            }

            val isDay = currentHour in 5..16

            precipitation?.total?.let { total ->
                val formattedSpeed = precipitationUnit.getValueText(context, total)
                val spannableString = SpannableString(formattedSpeed)
                spannableString.setSpan(
                    RelativeSizeSpan(0.5f),
                    precipitationUnit.getValueTextWithoutUnit(context, total).length,
                    formattedSpeed.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                precipitationValueView.text = spannableString
            } ?: run {
                precipitationValueView.text = "-"
            }

            // TODO
            precipitationAmountView.text = if (isDay) {
                "Daytime total"
            } else {
                "Nighttime total"
            }

            precipitationIconView.setImageDrawable(
                ResourceHelper.getWeatherIcon(
                    provider,
                    if ((precipitation?.rain ?: 0.0) > 0 && (precipitation?.snow ?: 0.0) > 0) {
                        WeatherCode.SLEET
                    } else if ((precipitation?.snow ?: 0.0) > 0) {
                        WeatherCode.SNOW
                    } else {
                        WeatherCode.RAIN
                    },
                    isDay
                )
            )
        }

        val talkBackBuilder = StringBuilder(titleView.text)
        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as GeoActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_PRECIPITATION
            )
        }
    }
}
