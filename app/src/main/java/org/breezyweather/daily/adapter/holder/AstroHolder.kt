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

package org.breezyweather.daily.adapter.holder

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.DurationUnit
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.common.ui.widgets.astro.MoonPhaseView
import org.breezyweather.daily.adapter.DailyWeatherAdapter
import org.breezyweather.daily.adapter.model.DailyAstro
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.theme.ThemeManager

class AstroHolder(parent: ViewGroup) : DailyWeatherAdapter.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_weather_daily_astro, parent, false)
) {
    private val mSun: LinearLayout = itemView.findViewById(R.id.item_weather_daily_astro_sun)
    private val mSunText: TextView = itemView.findViewById(R.id.item_weather_daily_astro_sunText)
    private val mMoon: LinearLayout = itemView.findViewById(R.id.item_weather_daily_astro_moon)
    private val mMoonText: TextView = itemView.findViewById(R.id.item_weather_daily_astro_moonText)
    private val mMoonPhase: LinearLayout = itemView.findViewById(R.id.item_weather_daily_astro_moonPhase)
    private val mMoonPhaseIcon: MoonPhaseView = itemView.findViewById(R.id.item_weather_daily_astro_moonPhaseIcon)
    private val mMoonPhaseText: TextView = itemView.findViewById(R.id.item_weather_daily_astro_moonPhaseText)

    @SuppressLint("SetTextI18n")
    override fun onBindView(model: DailyWeatherAdapter.ViewModel, position: Int) {
        val context = itemView.context
        val location = (model as DailyAstro).location
        val talkBackBuilder = StringBuilder(context.getString(R.string.ephemeris))
        if (model.sun != null && model.sun.isValid) {
            talkBackBuilder
                .append(context.getString(R.string.comma_separator))
                .append(
                    context.getString(
                        R.string.ephemeris_sunrise_at,
                        model.sun.riseDate?.getFormattedTime(
                            location,
                            context,
                            context.is12Hour
                        ) ?: context.getString(R.string.null_data_text)
                    )
                )
                .append(context.getString(R.string.comma_separator))
                .append(
                    context.getString(
                        R.string.ephemeris_sunset_at,
                        model.sun.setDate?.getFormattedTime(
                            location,
                            context,
                            context.is12Hour
                        ) ?: context.getString(R.string.null_data_text)
                    )
                )
            mSun.visibility = View.VISIBLE
            mSunText.text = if (BreezyWeather.instance.debugMode) {
                (
                    model.sun.riseDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                        ?: context.getString(R.string.null_data_text)
                    ) +
                    "↑ / " +
                    (
                        model.sun.setDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                            ?: context.getString(R.string.null_data_text)
                        ) +
                    "↓" +
                    (model.sun.duration?.let { " / " + DurationUnit.H.getValueText(context, it) } ?: "")
            } else {
                (
                    model.sun.riseDate?.getFormattedTime(location, context, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                    ) +
                    "↑ / " +
                    (
                        model.sun.setDate?.getFormattedTime(location, context, context.is12Hour)
                            ?: context.getString(R.string.null_data_text)
                        ) +
                    "↓" +
                    (model.sun.duration?.let { " / " + DurationUnit.H.getValueText(context, it) } ?: "")
            }
        } else {
            mSun.visibility = View.GONE
        }
        if (model.moon != null && model.moon.isValid) {
            talkBackBuilder
                .append(context.getString(R.string.comma_separator))
                .append(
                    context.getString(
                        R.string.ephemeris_moonrise_at,
                        model.moon.riseDate?.getFormattedTime(
                            location,
                            context,
                            context.is12Hour
                        ) ?: context.getString(R.string.null_data_text)
                    )
                )
                .append(context.getString(R.string.comma_separator))
                .append(
                    context.getString(
                        R.string.ephemeris_moonset_at,
                        model.moon.setDate?.getFormattedTime(
                            location,
                            context,
                            context.is12Hour
                        ) ?: context.getString(R.string.null_data_text)
                    )
                )
            mMoon.visibility = View.VISIBLE
            mMoonText.text = if (BreezyWeather.instance.debugMode) {
                (
                    model.moon.riseDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                        ?: context.getString(R.string.null_data_text)
                    ) +
                    "↑ / " +
                    (
                        model.moon.setDate?.getFormattedDate("yyyy-MM-dd HH:mm", location, context)
                            ?: context.getString(R.string.null_data_text)
                        ) +
                    "↓"
            } else {
                (
                    model.moon.riseDate?.getFormattedTime(location, context, context.is12Hour)
                        ?: context.getString(R.string.null_data_text)
                    ) +
                    "↑ / " +
                    (
                        model.moon.setDate?.getFormattedTime(location, context, context.is12Hour)
                            ?: context.getString(R.string.null_data_text)
                        ) +
                    "↓"
            }
        } else {
            mMoon.visibility = View.GONE
        }
        if (model.moonPhase != null && model.moonPhase.isValid) {
            talkBackBuilder.append(context.getString(R.string.comma_separator))
                .append(model.moonPhase.getDescription(context))
            mMoonPhase.visibility = View.VISIBLE
            mMoonPhaseIcon.setSurfaceAngle(model.moonPhase.angle!!.toFloat())
            mMoonPhaseIcon.setColor(
                ContextCompat.getColor(context, R.color.colorTextLight2nd),
                ContextCompat.getColor(context, R.color.colorTextDark2nd),
                ThemeManager.getInstance(context).getThemeColor(context, R.attr.colorBodyText)
            )
            mMoonPhaseText.text = model.moonPhase.getDescription(context)
        } else {
            mMoonPhase.visibility = View.GONE
        }
        itemView.contentDescription = talkBackBuilder.toString()
    }
}
