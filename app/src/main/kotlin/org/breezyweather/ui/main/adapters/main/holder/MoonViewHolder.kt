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
import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Weather
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.areBlocksSquished
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import java.util.Date

class MoonViewHolder(parent: ViewGroup) : AstroViewHolder(parent, isSun = false) {

    @SuppressLint("SetTextI18n")
    override fun onBindView(
        activity: BreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)
        mWeather = location.weather!!

        if (itemView.context.areBlocksSquished) bottomGuideline.setGuidelinePercent(0.95f)

        mWeather?.today?.moonPhase?.let { moonPhase ->
            if (moonPhase.isValid) {
                descriptionView.visibility = View.VISIBLE
                descriptionIconView.visibility = View.VISIBLE
                descriptionIconView.setColor(
                    ContextCompat.getColor(context, R.color.colorTextLight2nd),
                    ContextCompat.getColor(context, R.color.colorTextDark2nd),
                    context.getThemeColor(R.attr.colorBodyText)
                )
                descriptionView.text = moonPhase.getDescription(context)
            } else {
                descriptionView.visibility = View.GONE
                descriptionIconView.visibility = View.GONE
            }
        } ?: run {
            descriptionView.visibility = View.GONE
            descriptionIconView.visibility = View.GONE
        }

        mWeather?.today?.moon?.let { moon ->
            if (moon.isValid) {
                val validatedMoon = if (Date().time < moon.riseDate!!.time) {
                    val yesterdayMoon =
                        mWeather!!.dailyForecast.getOrElse(mWeather!!.todayIndex!!.minus(1)) { null }?.moon
                    if (yesterdayMoon?.isValid == true && yesterdayMoon.setDate!!.time > Date().time) {
                        yesterdayMoon
                    } else {
                        moon
                    }
                } else {
                    moon
                }

                val moonriseTime = validatedMoon.riseDate!!.getFormattedTime(location, context, context.is12Hour)
                val moonsetTime = validatedMoon.setDate!!.getFormattedTime(location, context, context.is12Hour)
                riseTimeView.text = moonriseTime
                riseTimeView.contentDescription = context.getString(R.string.ephemeris_moonrise_at, moonriseTime)
                setTimeView.text = moonsetTime
                setTimeView.contentDescription = context.getString(R.string.ephemeris_moonset_at, moonsetTime)
            }
        }
    }

    @SuppressLint("Recycle")
    override fun onEnterScreen() {
        super.onEnterScreen()
        if (itemAnimationEnabled && mWeather != null) {
            val timeNight = ValueAnimator.ofObject(LongEvaluator(), mStartTime, mCurrentTime)
            timeNight.addUpdateListener { animation: ValueAnimator ->
                mAnimCurrentTime = animation.animatedValue as Long
                mSunMoonView.setTime(mStartTime, mEndTime, mAnimCurrentTime)
            }
            val totalRotationNight = 360.0 * 4 * (mCurrentTime - mStartTime) / (mEndTime - mStartTime)
            val rotateNight = ValueAnimator.ofObject(
                FloatEvaluator(),
                0,
                (totalRotationNight - totalRotationNight % 360).toInt()
            )
            rotateNight.addUpdateListener { animation: ValueAnimator ->
                mSunMoonView.setIndicatorRotation(-1 * animation.animatedValue as Float)
            }
            mAttachAnimatorSets[0] = AnimatorSet().apply {
                playTogether(timeNight, rotateNight)
                interpolator = OvershootInterpolator(1f)
                duration = getPathAnimatorDuration()
            }.also { it.start() }
            if (mPhaseAngle > 0) {
                val moonAngle = ValueAnimator.ofObject(FloatEvaluator(), 0, mPhaseAngle)
                moonAngle.addUpdateListener { animation: ValueAnimator ->
                    descriptionIconView.setSurfaceAngle((animation.animatedValue as Float))
                }
                mAttachAnimatorSets[1] = AnimatorSet().apply {
                    playTogether(moonAngle)
                    interpolator = DecelerateInterpolator()
                    duration = phaseAnimatorDuration
                }.also { it.start() }
            }
        }
    }

    override fun ensurePhaseAngle(weather: Weather) {
        mPhaseAngle = weather.today?.moonPhase?.angle ?: 0
    }
}
