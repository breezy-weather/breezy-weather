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
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Weather
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import kotlin.time.Duration.Companion.days

class SunViewHolder(parent: ViewGroup) : AstroViewHolder(parent, isSun = true) {

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

        mWeather?.today?.sun?.let { sun ->
            if (sun.isValid) {
                if (sun.setDate!!.time == sun.riseDate!!.time + 1.days.inWholeMilliseconds - 1) {
                    riseTimeView.text = ""
                    setTimeView.text = ""
                    descriptionView.visibility = View.VISIBLE
                    descriptionView.text = context.getString(R.string.ephemeris_polar_day)
                } else {
                    val sunriseTime = sun.riseDate!!.getFormattedTime(location, context, context.is12Hour)
                    val sunsetTime = sun.setDate!!.getFormattedTime(location, context, context.is12Hour)
                    riseTimeView.text = sunriseTime
                    riseTimeView.contentDescription = context.getString(R.string.ephemeris_sunrise_at, sunriseTime)
                    setTimeView.text = sunsetTime
                    setTimeView.contentDescription = context.getString(R.string.ephemeris_sunset_at, sunsetTime)
                    descriptionView.visibility = View.GONE
                }
            } else if (sun.riseDate == null && sun.setDate == null) {
                descriptionView.visibility = View.VISIBLE
                descriptionView.text = context.getString(R.string.ephemeris_polar_night)
            }
        }

        descriptionIconView.visibility = View.GONE
    }

    @SuppressLint("Recycle")
    override fun onEnterScreen() {
        if (itemAnimationEnabled && mWeather != null) {
            val timeDay = ValueAnimator.ofObject(LongEvaluator(), mStartTime, mCurrentTime)
            timeDay.addUpdateListener { animation: ValueAnimator ->
                mAnimCurrentTime = animation.animatedValue as Long
                mSunMoonView.setTime(mStartTime, mEndTime, mAnimCurrentTime)
            }
            val totalRotationDay = 360.0 * 7 * (mCurrentTime - mStartTime) / (mEndTime - mStartTime)
            val rotateDay = ValueAnimator.ofObject(
                FloatEvaluator(),
                0,
                (totalRotationDay - totalRotationDay % 360).toInt()
            )
            rotateDay.addUpdateListener { animation: ValueAnimator ->
                mSunMoonView.setIndicatorRotation((animation.animatedValue as Float))
            }
            mAttachAnimatorSets[0] = AnimatorSet().apply {
                playTogether(timeDay, rotateDay)
                interpolator = OvershootInterpolator(1f)
                duration = getPathAnimatorDuration()
            }.also { it.start() }
        }
    }

    override fun ensurePhaseAngle(weather: Weather) {
        mPhaseAngle = 0
    }
}
