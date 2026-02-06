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
import android.animation.TypeEvaluator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Size
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.Guideline
import androidx.core.graphics.ColorUtils
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Weather
import org.breezyweather.R
import org.breezyweather.common.activities.BreezyActivity
import org.breezyweather.common.extensions.areBlocksSquished
import org.breezyweather.common.extensions.getThemeColor
import org.breezyweather.common.options.appearance.DetailScreen
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.ui.common.widgets.astro.MoonPhaseView
import org.breezyweather.ui.common.widgets.astro.SunMoonView
import org.breezyweather.ui.theme.ThemeManager
import org.breezyweather.ui.theme.resource.ResourceHelper
import org.breezyweather.ui.theme.resource.providers.ResourceProvider
import org.breezyweather.ui.theme.weatherView.WeatherView
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

abstract class AstroViewHolder(parent: ViewGroup, val isSun: Boolean) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_astro, parent, false)
) {
    private val titleView: TextView = itemView.findViewById(R.id.title)
    private val titleIconView: ImageView = itemView.findViewById(R.id.title_icon)
    protected val mSunMoonView: SunMoonView = itemView.findViewById(R.id.container_main_sun_moon_controlView)
    protected val riseTimeView: TextView = itemView.findViewById(R.id.rise_time)
    protected val setTimeView: TextView = itemView.findViewById(R.id.set_time)
    protected val descriptionIconView: MoonPhaseView = itemView.findViewById(R.id.description_icon)
    protected val descriptionView: TextView = itemView.findViewById(R.id.description)
    protected val topGuideline = itemView.findViewById<Guideline>(R.id.block_top_guideline)
    protected val bottomGuideline = itemView.findViewById<Guideline>(R.id.block_bottom_guideline)
    protected var mWeather: Weather? = null

    protected var mStartTime: Long = 0L
    protected var mEndTime: Long = 0L
    protected var mCurrentTime: Long = 0L
    protected var mAnimCurrentTime: Long = 0L
    protected var mPhaseAngle = 0

    @Size(2)
    protected val mAttachAnimatorSets: Array<AnimatorSet?> = arrayOf(null, null)

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
        val themeColors = ThemeManager.getInstance(context)
            .weatherThemeDelegate
            .getThemeColors(
                context,
                WeatherView.WEATHER_KIND_CLEAR,
                isSun
            )

        titleView.setText(if (isSun) R.string.ephemeris_sun else R.string.ephemeris_moon)
        titleIconView.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                if (isSun) R.drawable.weather_clear_day_mini_xml else R.drawable.weather_clear_night_mini_xml
            )
        )

        if (itemView.context.areBlocksSquished) topGuideline.setGuidelinePercent(0.05f)

        val todayAstro = if (isSun) mWeather!!.today?.sun else mWeather!!.today?.moon
        val validatedAstro = if (!isSun &&
            (todayAstro?.riseDate == null || Date().time < todayAstro.riseDate!!.time)
        ) {
            val yesterdayAstro =
                mWeather!!.dailyForecast.getOrElse(mWeather!!.todayIndex!!.minus(1)) { null }?.moon
            if (yesterdayAstro?.isValid == true && yesterdayAstro.setDate!!.time > Date().time) {
                yesterdayAstro
            } else {
                todayAstro
            }
        } else {
            todayAstro
        }
        ensureTime(
            validatedAstro,
            location.timeZone
        )
        ensurePhaseAngle(mWeather!!)

        mSunMoonView.setDrawable(
            if (isSun) ResourceHelper.getSunDrawable(provider) else ResourceHelper.getMoonDrawable(provider)
        )
        if (ThemeManager.isLightTheme(context, location)) {
            mSunMoonView.setColors(
                themeColors[0],
                ColorUtils.setAlphaComponent(themeColors[1], (0.66 * 255).toInt()),
                context.getThemeColor(R.attr.colorMainCardBackground),
                true
            )
        } else {
            mSunMoonView.setColors(
                themeColors[2],
                ColorUtils.setAlphaComponent(themeColors[2], (0.5 * 255).toInt()),
                context.getThemeColor(R.attr.colorMainCardBackground),
                false
            )
        }
        if (itemAnimationEnabled) {
            mSunMoonView.setTime(mStartTime, mEndTime, mStartTime)
            mSunMoonView.setIndicatorRotation(0f)
            descriptionIconView.setSurfaceAngle(0f)
        } else {
            mSunMoonView.post { mSunMoonView.setTime(mStartTime, mEndTime, mCurrentTime) }
            mSunMoonView.setIndicatorRotation(0f)
            descriptionIconView.setSurfaceAngle(mPhaseAngle.toFloat())
        }

        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as BreezyActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_SUN_MOON
            )
        }
    }

    protected class LongEvaluator : TypeEvaluator<Long> {
        override fun evaluate(fraction: Float, startValue: Long, endValue: Long): Long {
            return startValue + ((endValue - startValue) * fraction).toLong()
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

    private fun ensureTime(astro: Astro?, timeZone: TimeZone) {
        val calendar = Calendar.getInstance(timeZone)
        val currentTime = calendar.time.time
        mCurrentTime = currentTime

        // sun.
        if (astro?.riseDate != null && astro.setDate != null) {
            mStartTime = astro.riseDate!!.time
            mEndTime = astro.setDate!!.time
        } else {
            mStartTime = currentTime + 1
            mEndTime = currentTime + 1
        }

        mAnimCurrentTime = mCurrentTime
    }

    protected abstract fun ensurePhaseAngle(weather: Weather)

    protected fun getPathAnimatorDuration(): Long {
        val duration = max(
            1000 + 3000.0 * (mCurrentTime - mStartTime) / (mEndTime - mStartTime),
            0.0
        ).toLong()
        return min(duration, 4000)
    }

    protected val phaseAnimatorDuration: Long
        get() {
            val duration = max(0.0, mPhaseAngle / 360.0 * 1000 + 1000).toLong()
            return min(duration, 2000)
        }
}
