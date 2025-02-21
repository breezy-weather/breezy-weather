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
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Weather
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.extensions.getFormattedTime
import org.breezyweather.common.extensions.is12Hour
import org.breezyweather.domain.weather.model.getDescription
import org.breezyweather.ui.common.widgets.astro.MoonPhaseView
import org.breezyweather.ui.common.widgets.astro.SunMoonView
import org.breezyweather.ui.main.utils.MainThemeColorProvider
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

class AstroViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_sun_moon, parent, false)
) {
    private val mTitle: TextView = itemView.findViewById(R.id.container_main_sun_moon_title)
    private val mPhaseText: TextView = itemView.findViewById(R.id.container_main_sun_moon_phaseText)
    private val mPhaseView: MoonPhaseView = itemView.findViewById(R.id.container_main_sun_moon_phaseView)
    private val mSunMoonView: SunMoonView = itemView.findViewById(R.id.container_main_sun_moon_controlView)
    private val mSunContainer: RelativeLayout = itemView.findViewById(R.id.container_main_sun_moon_sunContainer)
    private val mSunTxt: TextView = itemView.findViewById(R.id.container_main_sun_moon_sunrise_sunset)
    private val mMoonContainer: RelativeLayout = itemView.findViewById(R.id.container_main_sun_moon_moonContainer)
    private val mMoonTxt: TextView = itemView.findViewById(R.id.container_main_sun_moon_moonrise_moonset)
    private var mWeather: Weather? = null

    @Size(2)
    private var mStartTimes: LongArray = LongArray(2)

    @Size(2)
    private var mEndTimes: LongArray = LongArray(2)

    @Size(2)
    private var mCurrentTimes: LongArray = LongArray(2)

    @Size(2)
    private var mAnimCurrentTimes: LongArray = LongArray(2)
    private var mPhaseAngle = 0

    @Size(3)
    private val mAttachAnimatorSets: Array<AnimatorSet?>

    init {
        mAttachAnimatorSets = arrayOf(null, null, null)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindView(
        activity: GeoActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
        firstCard: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled, firstCard)
        mWeather = location.weather!!
        val themeColors = ThemeManager.getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                WeatherViewController.getWeatherKind(location),
                WeatherViewController.isDaylight(location)
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mTitle.isAccessibilityHeading = true
        }
        mTitle.setTextColor(themeColors[0])
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mTitle.isAccessibilityHeading = true
        }
        val talkBackBuilder = StringBuilder(mTitle.text)
        ensureTime(mWeather!!, location.javaTimeZone)
        ensurePhaseAngle(mWeather!!)
        mWeather?.today?.moonPhase?.let { moonPhase ->
            if (moonPhase.isValid) {
                mPhaseText.visibility = View.VISIBLE
                mPhaseView.visibility = View.VISIBLE
                mPhaseText.setTextColor(MainThemeColorProvider.getColor(location, R.attr.colorBodyText))
                mPhaseView.setColor(
                    ContextCompat.getColor(context, R.color.colorTextLight2nd),
                    ContextCompat.getColor(context, R.color.colorTextDark2nd),
                    MainThemeColorProvider.getColor(location, R.attr.colorBodyText)
                )
                mPhaseText.text = moonPhase.getDescription(context)
                talkBackBuilder.append(context.getString(R.string.comma_separator))
                    .append(mPhaseText.text)
            } else {
                mPhaseText.visibility = View.GONE
                mPhaseView.visibility = View.GONE
            }
        } ?: run {
            mPhaseText.visibility = View.GONE
            mPhaseView.visibility = View.GONE
        }

        mSunMoonView.setSunDrawable(ResourceHelper.getSunDrawable(provider))
        mSunMoonView.setMoonDrawable(ResourceHelper.getMoonDrawable(provider))
        if (MainThemeColorProvider.isLightTheme(context, location)) {
            mSunMoonView.setColors(
                themeColors[0],
                ColorUtils.setAlphaComponent(themeColors[1], (0.66 * 255).toInt()),
                ColorUtils.setAlphaComponent(themeColors[1], (0.33 * 255).toInt()),
                MainThemeColorProvider.getColor(location, R.attr.colorMainCardBackground),
                true
            )
        } else {
            mSunMoonView.setColors(
                themeColors[2],
                ColorUtils.setAlphaComponent(themeColors[2], (0.5 * 255).toInt()),
                ColorUtils.setAlphaComponent(themeColors[2], (0.2 * 255).toInt()),
                MainThemeColorProvider.getColor(location, R.attr.colorMainCardBackground),
                false
            )
        }
        if (itemAnimationEnabled) {
            mSunMoonView.setTime(mStartTimes, mEndTimes, mStartTimes)
            mSunMoonView.setDayIndicatorRotation(0f)
            mSunMoonView.setNightIndicatorRotation(0f)
            mPhaseView.setSurfaceAngle(0f)
        } else {
            mSunMoonView.post { mSunMoonView.setTime(mStartTimes, mEndTimes, mCurrentTimes) }
            mSunMoonView.setDayIndicatorRotation(0f)
            mSunMoonView.setNightIndicatorRotation(0f)
            mPhaseView.setSurfaceAngle(mPhaseAngle.toFloat())
        }

        mWeather?.today?.sun?.let { sun ->
            if (sun.isValid) {
                val sunriseTime = sun.riseDate!!.getFormattedTime(location, context, context.is12Hour)
                val sunsetTime = sun.setDate!!.getFormattedTime(location, context, context.is12Hour)
                mSunContainer.visibility = View.VISIBLE
                mSunTxt.text = sunriseTime + "↑" + "\n" + sunsetTime + "↓"
                talkBackBuilder
                    .append(context.getString(R.string.comma_separator))
                    .append(context.getString(R.string.ephemeris_sunrise_at, sunriseTime))
                    .append(context.getString(R.string.comma_separator))
                    .append(context.getString(R.string.ephemeris_sunset_at, sunsetTime))
            } else {
                mSunContainer.visibility = View.GONE
            }
        } ?: run {
            mSunContainer.visibility = View.GONE
        }

        mWeather?.today?.moon?.let { moon ->
            if (moon.isValid) {
                val moonriseTime = moon.riseDate!!.getFormattedTime(location, context, context.is12Hour)
                val moonsetTime = moon.setDate!!.getFormattedTime(location, context, context.is12Hour)
                mMoonContainer.visibility = View.VISIBLE
                mMoonTxt.text = moonriseTime + "↑" + "\n" + moonsetTime + "↓"
                talkBackBuilder
                    .append(context.getString(R.string.comma_separator))
                    .append(context.getString(R.string.ephemeris_moonrise_at, moonriseTime))
                    .append(context.getString(R.string.comma_separator))
                    .append(context.getString(R.string.ephemeris_moonset_at, moonsetTime))
            } else {
                mMoonContainer.visibility = View.GONE
            }
        } ?: run {
            mMoonContainer.visibility = View.GONE
        }
        itemView.contentDescription = talkBackBuilder.toString()
    }

    private class LongEvaluator : TypeEvaluator<Long> {
        override fun evaluate(fraction: Float, startValue: Long, endValue: Long): Long {
            return startValue + ((endValue - startValue) * fraction).toLong()
        }
    }

    @SuppressLint("Recycle")
    override fun onEnterScreen() {
        if (itemAnimationEnabled && mWeather != null) {
            val timeDay = ValueAnimator.ofObject(LongEvaluator(), mStartTimes[0], mCurrentTimes[0])
            timeDay.addUpdateListener { animation: ValueAnimator ->
                mAnimCurrentTimes[0] = animation.animatedValue as Long
                mSunMoonView.setTime(mStartTimes, mEndTimes, mAnimCurrentTimes)
            }
            val totalRotationDay = 360.0 * 7 * (mCurrentTimes[0] - mStartTimes[0]) / (mEndTimes[0] - mStartTimes[0])
            val rotateDay = ValueAnimator.ofObject(
                FloatEvaluator(),
                0,
                (totalRotationDay - totalRotationDay % 360).toInt()
            )
            rotateDay.addUpdateListener { animation: ValueAnimator ->
                mSunMoonView.setDayIndicatorRotation((animation.animatedValue as Float))
            }
            mAttachAnimatorSets[0] = AnimatorSet().apply {
                playTogether(timeDay, rotateDay)
                interpolator = OvershootInterpolator(1f)
                duration = getPathAnimatorDuration(0)
            }.also { it.start() }
            val timeNight = ValueAnimator.ofObject(LongEvaluator(), mStartTimes[1], mCurrentTimes[1])
            timeNight.addUpdateListener { animation: ValueAnimator ->
                mAnimCurrentTimes[1] = animation.animatedValue as Long
                mSunMoonView.setTime(mStartTimes, mEndTimes, mAnimCurrentTimes)
            }
            val totalRotationNight = 360.0 * 4 * (mCurrentTimes[1] - mStartTimes[1]) / (mEndTimes[1] - mStartTimes[1])
            val rotateNight = ValueAnimator.ofObject(
                FloatEvaluator(),
                0,
                (totalRotationNight - totalRotationNight % 360).toInt()
            )
            rotateNight.addUpdateListener { animation: ValueAnimator ->
                mSunMoonView.setNightIndicatorRotation(-1 * animation.animatedValue as Float)
            }
            mAttachAnimatorSets[1] = AnimatorSet().apply {
                playTogether(timeNight, rotateNight)
                interpolator = OvershootInterpolator(1f)
                duration = getPathAnimatorDuration(1)
            }.also { it.start() }
            if (mPhaseAngle > 0) {
                val moonAngle = ValueAnimator.ofObject(FloatEvaluator(), 0, mPhaseAngle)
                moonAngle.addUpdateListener { animation: ValueAnimator ->
                    mPhaseView.setSurfaceAngle((animation.animatedValue as Float))
                }
                mAttachAnimatorSets[2] = AnimatorSet().apply {
                    playTogether(moonAngle)
                    interpolator = DecelerateInterpolator()
                    duration = phaseAnimatorDuration
                }.also { it.start() }
            }
        }
    }

    override fun onRecycleView() {
        super.onRecycleView()
        for (i in mAttachAnimatorSets.indices) {
            mAttachAnimatorSets[i]?.let {
                if (it.isRunning) {
                    it.cancel()
                }
            }
            mAttachAnimatorSets[i] = null
        }
    }

    private fun ensureTime(weather: Weather, timeZone: TimeZone) {
        val calendar = Calendar.getInstance(timeZone)
        val currentTime = calendar.time.time
        mStartTimes = LongArray(2)
        mEndTimes = LongArray(2)
        mCurrentTimes = longArrayOf(currentTime, currentTime)

        // sun.
        if (weather.today?.sun?.riseDate != null && weather.today!!.sun!!.setDate != null) {
            mStartTimes[0] = weather.today!!.sun!!.riseDate!!.time
            mEndTimes[0] = weather.today!!.sun!!.setDate!!.time
        } else {
            mStartTimes[0] = currentTime + 1
            mEndTimes[0] = currentTime + 1
        }

        // moon.
        if (weather.today?.moon?.riseDate != null && weather.today!!.moon!!.setDate != null) {
            mStartTimes[1] = weather.today!!.moon!!.riseDate!!.time
            mEndTimes[1] = weather.today!!.moon!!.setDate!!.time
        } else {
            mStartTimes[1] = currentTime + 1
            mEndTimes[1] = currentTime + 1
        }
        mAnimCurrentTimes = longArrayOf(mCurrentTimes[0], mCurrentTimes[1])
    }

    private fun ensurePhaseAngle(weather: Weather) {
        mPhaseAngle = weather.today?.moonPhase?.angle ?: 0
    }

    private fun getPathAnimatorDuration(index: Int): Long {
        val duration = max(
            1000 + 3000.0 * (mCurrentTimes[index] - mStartTimes[index]) / (mEndTimes[index] - mStartTimes[index]),
            0.0
        ).toLong()
        return min(duration, 4000)
    }

    private val phaseAnimatorDuration: Long
        get() {
            val duration = max(0.0, mPhaseAngle / 360.0 * 1000 + 1000).toLong()
            return min(duration, 2000)
        }
}
