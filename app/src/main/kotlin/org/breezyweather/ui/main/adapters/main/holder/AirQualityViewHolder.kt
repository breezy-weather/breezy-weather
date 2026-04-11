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

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import breezyweather.domain.location.model.Location
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.common.utils.UnitUtils
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.domain.weather.model.getColor
import org.breezyweather.domain.weather.model.getIndex
import org.breezyweather.domain.weather.model.getName
import org.breezyweather.domain.weather.model.validAirQuality
import org.breezyweather.ui.common.widgets.ArcProgress
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import kotlin.math.roundToInt

class AirQualityViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_air_quality, parent, false)
) {
    private val aqiValueView: TextView = itemView.findViewById(R.id.aqi_value)
    private val aqiLevelView: TextView = itemView.findViewById(R.id.aqi_level)
    private val aqiProgress: ArcProgress = itemView.findViewById(R.id.aqi_progress)
    private var mAqiIndex = 0
    private var mEnable = false
    private var mAttachAnimatorSet: AnimatorSet? = null

    @SuppressLint("DefaultLocale")
    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val talkBackBuilder = StringBuilder()
        location.weather!!.validAirQuality?.let { airQuality ->
            mAqiIndex = airQuality.getIndex() ?: 0
            mEnable = true
            if (itemAnimationEnabled) {
                aqiProgress.apply {
                    progress = 0f
                    aqiValueView.text = UnitUtils.formatInt(context, 0)
                    setProgressColor(
                        ContextCompat.getColor(context, R.color.colorLevel_1)
                    )
                    setArcBackgroundColor(context.getThemeColor(com.google.android.material.R.attr.colorOutline))
                }
            } else {
                val aqiColor = airQuality.getColor(aqiProgress.context)
                aqiProgress.apply {
                    progress = mAqiIndex.toFloat()
                    aqiValueView.text = UnitUtils.formatInt(context, mAqiIndex)
                    setProgressColor(aqiColor)
                    setArcBackgroundColor(ColorUtils.setAlphaComponent(aqiColor, (255 * 0.1).toInt()))
                }
            }
            aqiProgress.apply {
                max = PollutantIndex.indexExcessivePollution.toFloat()
            }
            aqiLevelView.text = airQuality.getName(context)
            talkBackBuilder
                .append(context.getString(R.string.air_quality_index))
                .append(context.getString(R.string.colon_separator))
                .append(UnitUtils.formatInt(context, mAqiIndex))
                .append(context.getString(org.breezyweather.unit.R.string.locale_separator))
                .append(airQuality.getName(context))
        }
        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as BreezyActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_AIR_QUALITY
            )
        }
    }

    override fun onEnterScreen() {
        super.onEnterScreen()
        if (itemAnimationEnabled && mEnable) {
            mLocation!!.weather!!.validAirQuality?.let { airQuality ->
                val aqiColor = airQuality.getColor(aqiProgress.context)
                val progressColor = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    ContextCompat.getColor(context, R.color.colorLevel_1),
                    aqiColor
                )
                progressColor.addUpdateListener { animation: ValueAnimator ->
                    aqiProgress.setProgressColor(
                        animation.animatedValue as Int
                    )
                }
                val backgroundColor = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    context.getThemeColor(com.google.android.material.R.attr.colorOutline),
                    ColorUtils.setAlphaComponent(aqiColor, (255 * 0.1).toInt())
                )
                backgroundColor.addUpdateListener { animation: ValueAnimator ->
                    aqiProgress.setArcBackgroundColor((animation.animatedValue as Int))
                }
                val aqiNumber = ValueAnimator.ofObject(FloatEvaluator(), 0, mAqiIndex)
                aqiNumber.addUpdateListener { animation: ValueAnimator ->
                    aqiProgress.apply {
                        progress = (animation.animatedValue as Float)
                    }
                    aqiValueView.text = UnitUtils.formatInt(context, aqiProgress.progress.roundToInt())
                }
                mAttachAnimatorSet = AnimatorSet().apply {
                    playTogether(progressColor, backgroundColor, aqiNumber)
                    interpolator = DecelerateInterpolator()
                    duration = (1500 + mAqiIndex / 400f * 1500).toLong()
                    start()
                }
            }
        }
    }

    override fun onRecycleView() {
        super.onRecycleView()
        mAttachAnimatorSet?.let {
            if (it.isRunning) it.cancel()
        }
        mAttachAnimatorSet = null
    }
}
