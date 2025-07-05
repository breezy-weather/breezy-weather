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

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.model.getTemperatureRangeSummary
import org.breezyweather.ui.common.widgets.NumberAnimTextView
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class HeaderViewHolder(parent: ViewGroup) : AbstractMainViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_header, parent, false)
) {
    private val mContainer: LinearLayout = itemView.findViewById(R.id.container_main_header)
    private val mTemperatureContainer: RelativeLayout = itemView.findViewById(R.id.container_main_header_temperature)
    private val mTemperature: NumberAnimTextView = itemView.findViewById(R.id.container_main_header_temperature_value)
    private val mTemperatureUnitView: TextView = itemView.findViewById(R.id.container_main_header_temperature_unit)
    private val mWeatherText: TextView = itemView.findViewById(R.id.container_main_header_weather_condition_description)
    private val mFeelsLike: TextView = itemView.findViewById(R.id.container_main_header_feels_like)
    private val mTemperatureRange: TextView = itemView.findViewById(R.id.container_main_header_temperature_range)
    private var mTemperatureCFrom = 0.0
    private var mTemperatureCTo = 0.0
    private var mTemperatureUnit: TemperatureUnit? = null

    @SuppressLint("SetTextI18n")
    override fun onBindView(
        context: Context,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(context, location, provider, listAnimationEnabled, itemAnimationEnabled)
        val textColor = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getOnBackgroundColor(
                context,
                WeatherViewController.getWeatherKind(location),
                WeatherViewController.isDaylight(location)
            )
        mTemperature.setTextColor(textColor)
        mTemperatureUnitView.setTextColor(textColor)
        mWeatherText.setTextColor(textColor)
        mFeelsLike.setTextColor(textColor)
        mTemperatureRange.setTextColor(textColor)
        mTemperatureUnit = SettingsManager.getInstance(context).temperatureUnit
        location.weather?.current?.let { current ->
            if (!current.weatherText.isNullOrEmpty()) {
                mWeatherText.visibility = View.VISIBLE
                mWeatherText.text = current.weatherText
            } else {
                mWeatherText.visibility = View.GONE
            }
            current.temperature?.temperature?.let {
                mTemperatureContainer.visibility = View.VISIBLE
                mTemperatureContainer.contentDescription = mTemperatureUnit!!.getValueVoice(context, it)
                mTemperatureCFrom = mTemperatureCTo
                mTemperatureCTo = it
                mTemperature.isAnimEnabled = itemAnimationEnabled
                // no longer than 2 seconds.
                mTemperature.duration =
                    max(2000.0, abs(mTemperatureCTo - mTemperatureCFrom) / 10f * 1000).toLong()
                mTemperatureUnitView.text = mTemperatureUnit!!.getName(context)
            } ?: run {
                mTemperatureContainer.visibility = View.GONE
            }
            current.temperature?.feelsLikeTemperature?.let { feelsLike ->
                if (current.temperature!!.temperature?.roundToInt() != feelsLike.roundToInt()) {
                    mFeelsLike.visibility = View.VISIBLE
                    mFeelsLike.text = context.getString(R.string.temperature_feels_like) +
                        " " +
                        mTemperatureUnit!!.getShortValueText(context, feelsLike)
                    mFeelsLike.contentDescription = context.getString(R.string.temperature_feels_like) +
                        context.getString(R.string.colon_separator) +
                        mTemperatureUnit!!.getValueVoice(context, feelsLike)
                } else {
                    mFeelsLike.visibility = View.GONE
                }
            } ?: run {
                mFeelsLike.visibility = View.GONE
            }
        }

        location.weather!!.getTemperatureRangeSummary(context, location)?.let {
            mTemperatureRange.visibility = View.VISIBLE
            mTemperatureRange.text = it.first
            mTemperatureRange.contentDescription = it.second
        } ?: run {
            mTemperatureRange.visibility = View.GONE
        }
        val params = mContainer.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = ThemeManager
            .getInstance(context)
            .weatherThemeDelegate
            .getHeaderTopMargin(context)
        mContainer.layoutParams = params
    }

    override fun getEnterAnimator(pendingAnimatorList: List<Animator>): Animator {
        val a: Animator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f)
        a.setDuration(300)
        a.startDelay = 100
        a.interpolator = FastOutSlowInInterpolator()
        return a
    }

    @SuppressLint("DefaultLocale")
    override fun onEnterScreen() {
        super.onEnterScreen()
        mTemperature.setNumberString(
            String.format("%d", mTemperatureUnit!!.getValueWithoutUnit(mTemperatureCFrom).roundToInt()),
            String.format("%d", mTemperatureUnit!!.getValueWithoutUnit(mTemperatureCTo).roundToInt())
        )
    }

    val headerTop: Int
        get() {
            return mContainer.top
        }

    companion object {
        const val NB_CURRENT_ITEMS_PORTRAIT = 4
        const val NB_CURRENT_ITEMS_LANDSCAPE = 5
    }
}
