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

import android.animation.AnimatorSet
import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
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
import org.breezyweather.unit.pressure.Pressure.Companion.hectopascals

class PressureViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_pressure, parent, false)
) {
    private val pressureValueView: TextView = itemView.findViewById(R.id.pressure_value)
    private val pressureUnitView: TextView = itemView.findViewById(R.id.pressure_unit)
    private val pressureProgress: ArcProgress = itemView.findViewById(R.id.pressure_progress)
    private var mPressure = 963f
    private var mEnable = false
    private var mAttachAnimatorSet: AnimatorSet? = null

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
            if (itemAnimationEnabled) {
                pressureProgress.apply {
                    progress = 0f
                    pressureValueView.text = 0.0.hectopascals.formatValue(context)
                }
            } else {
                pressureProgress.apply {
                    progress = mPressure.minus(963f)
                    pressureValueView.text = it.formatValue(context)
                }
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

    override fun onEnterScreen() {
        if (itemAnimationEnabled && mEnable) {
            mLocation!!.weather!!.current?.pressure?.let {
                val pressureNumber = ValueAnimator.ofObject(FloatEvaluator(), 0, mPressure.minus(963f))
                pressureNumber.addUpdateListener { animation: ValueAnimator ->
                    pressureProgress.apply {
                        progress = (animation.animatedValue as Float)
                    }
                    pressureValueView.text = pressureProgress.progress.plus(963.0).hectopascals
                        .formatValue(context)
                }
                mAttachAnimatorSet = AnimatorSet().apply {
                    playTogether(pressureNumber)
                    interpolator = DecelerateInterpolator()
                    duration = (1500 + (mPressure - 963).coerceAtLeast(1.0f) / 100 * 1500).toLong()
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
